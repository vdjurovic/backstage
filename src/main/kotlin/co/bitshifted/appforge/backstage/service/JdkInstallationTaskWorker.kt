/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.backstage.service

import co.bitshifted.appforge.backstage.BackstageConstants
import co.bitshifted.appforge.backstage.entity.InstalledJdk
import co.bitshifted.appforge.backstage.entity.InstalledJdkRelease
import co.bitshifted.appforge.backstage.model.jdk.JdkInstallConfig
import co.bitshifted.appforge.backstage.model.jdk.JdkInstallationSource
import co.bitshifted.appforge.backstage.repository.InstalledJdkRepository
import co.bitshifted.appforge.backstage.repository.JdkInstallationTaskRepository
import co.bitshifted.appforge.backstage.util.currentTimeUtc
import co.bitshifted.appforge.backstage.util.extractTarGzArchive
import co.bitshifted.appforge.backstage.util.extractZipArchive
import co.bitshifted.appforge.backstage.util.logger
import co.bitshifted.appforge.common.model.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.nio.file.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

@Transactional(readOnly = false)
class JdkInstallationTaskWorker(val installConfigList: List<JdkInstallConfig>, val taskId : String) : Runnable {

    private val logger = logger(this)
    private val platforms = listOf(
        Pair(OperatingSystem.LINUX, CpuArch.X64),
        Pair(OperatingSystem.LINUX, CpuArch.AARCH64),
        Pair(OperatingSystem.MAC, CpuArch.X64),
        Pair(OperatingSystem.MAC, CpuArch.AARCH64),
        Pair(OperatingSystem.WINDOWS, CpuArch.X64)
    )
    private val client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build()

    @Value("\${jdk.root.location}")
    lateinit var jdkRootLocation: String
    @Autowired
    lateinit var jdkInstallationTaskRepository: JdkInstallationTaskRepository
    @Autowired
    lateinit var installedJdkRepository: InstalledJdkRepository

    override fun run() {
        logger.info("JDK root directory: $jdkRootLocation")
        val currentTask = jdkInstallationTaskRepository.findById(taskId).get()
        //val downloadsList = initDownloads()
        currentTask.status = JdkInstallationStatus.DOWNLOAD_IN_PROGRESS
        jdkInstallationTaskRepository.save(currentTask)
//        CompletableFuture.allOf(*downloadsList.toTypedArray()).join()
        logger.info("All JDK downloads completed")
        // unpack downloads
        currentTask.status = JdkInstallationStatus.INSTALL_IN_PROGRESS
        jdkInstallationTaskRepository.save(currentTask)
//        val unpackTaskList = unpackDownloads(downloadsList)
//        CompletableFuture.allOf(*unpackTaskList.toTypedArray()).join()
        currentTask.status = JdkInstallationStatus.COMPLETED
        currentTask.completedOn = currentTimeUtc()
        // cleanup temporary files
//        downloadsList.forEach { Files.delete(it.get().srcFile) }
        // save JDK installation to DB
        val installedJdkList = mutableListOf<InstalledJdk>()
        installConfigList.forEach {
            val current = installedJdkRepository.findOneByVendorAndMajorVersion(it.platform.vendor, it.majorVersion).orElse(InstalledJdk(vendor = it.platform.vendor, majorVersion = it.majorVersion, autoUpdate = it.autoUpdate))
            // reset latest flag
            if(it.latest) {
                current.releases.forEach { it.latest = false }
            }
            current.releases.add(InstalledJdkRelease(release = it.release, latest = it.latest, installedJdk = current))
            installedJdkList.add(current)
        }

        installedJdkRepository.saveAll(installedJdkList)
        jdkInstallationTaskRepository.save(currentTask)
    }

    private fun extractFileName(url: String): String {
        return url.substring(url.lastIndexOf("/") + 1)
    }

    private fun createDirectoryStructure(vendor: JvmVendor, version: JavaVersion) {
        val vendorVersionPath = Paths.get(jdkRootLocation, vendor.code, version.display)
        // create directories per os-arch
        platforms.forEach {
            val target = vendorVersionPath.resolve(Paths.get(it.first.display, it.second.display))
            if (!Files.exists(target)) {
                logger.info("Creating directory ${target.absolutePathString()}")
                Files.createDirectories(target)
            }
        }
    }

    private fun jdkInstallDirectory(source : JdkInstallationSource) : Path {
        return Paths.get(jdkRootLocation, source.vendor.code, source.majorVersion.display, source.os.display, source.arch.display)
    }

    private fun initDownloads() : List<CompletableFuture<JdkInstallationSource>> {
        val downloadsList = mutableListOf<CompletableFuture<JdkInstallationSource>>()
        installConfigList.forEach { config ->
            createDirectoryStructure(config.platform.vendor, config.majorVersion)
            logger.info("Started JDk download: vendor=${config.platform.vendor}, release=${config.release}")
            platforms.forEach { pair ->
                val downloadLink = config.createDownloadLink(pair.first, pair.second)
                logger.debug("Download link: $downloadLink")
                val tmpFile = Files.createTempFile("jdk_install", "${pair.first}_${pair.second}")
                logger.info("Starting JDK download for ${pair.first} and ${pair.second}")
                val request = HttpRequest.newBuilder(URI(downloadLink)).GET().build()
                val future = client.sendAsync(request, BodyHandlers.ofInputStream())
                    .thenApply { ins ->
                        Files.copy(ins.body(), tmpFile, StandardCopyOption.REPLACE_EXISTING)
                        logger.debug("Copying data to file ${tmpFile.absolutePathString()}")
                    }
                    .thenApply { response ->
                        logger.info("Completed JDK download for ${pair.first} and ${pair.second}")
                        JdkInstallationSource(config, tmpFile, pair.first, pair.second, extractFileName(downloadLink))
                    }
                downloadsList.add(future)
            }
        }
        return downloadsList
    }

    private fun unpackDownloads(downloadsList : List<CompletableFuture<JdkInstallationSource>>) : List<CompletableFuture<Unit>> {
        val unpackTaskList = mutableListOf<CompletableFuture<Unit>>()
        downloadsList.forEach { installSrc ->
            logger.debug("file name: ${installSrc.get().fileName}, latest: ${installSrc.get().latest}")
            if(installSrc.get().os == OperatingSystem.WINDOWS) {
                unpackTaskList.add(CompletableFuture.supplyAsync { extractZipArchive(installSrc.get().srcFile, jdkInstallDirectory(installSrc.get())) }
                    .thenApply {
                        if(installSrc.get().latest) {
                            applyLatestLink(it)
                        }
                    })
            } else {
                unpackTaskList.add(CompletableFuture.supplyAsync { extractTarGzArchive(installSrc.get().srcFile, jdkInstallDirectory(installSrc.get())) }
                    .thenApply {
                        if(installSrc.get().latest) {
                            applyLatestLink(it)
                        }
                    })
            }
        }
        return unpackTaskList
    }

    private fun applyLatestLink(target : Path?) {
        if(target == null) {
            return
        }
        val parent = target.parent
        val existing = parent.resolve(BackstageConstants.LATEST_JAVA_DIR_LINK)
        logger.debug("latest link path: ${existing.absolutePathString()}")
       Files.deleteIfExists(existing)
        // create latest link
        logger.info("Creating link to target directory ${target.fileName}")
        Files.createSymbolicLink(parent.resolve(BackstageConstants.LATEST_JAVA_DIR_LINK), target)
    }
}