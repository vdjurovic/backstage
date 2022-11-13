/*
 *
 *  * Copyright (c) 2022-2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.backstage.util

import co.bitshifted.appforge.backstage.model.DeploymentConfig
import co.bitshifted.appforge.common.model.BasicResource
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import javax.servlet.http.HttpServletRequest


inline fun <reified T : Any> logger(from: T): Logger {
    return LoggerFactory.getLogger(from.javaClass)
}

fun threadPoolCoreSize() = Runtime.getRuntime().availableProcessors()

fun maxThreadPoolSize() = 2 * threadPoolCoreSize()

fun generateServerUrl(request: HttpServletRequest, path: String): String? {
    val sb = StringBuilder()
    sb.append(request.scheme).append("://")
    sb.append(request.serverName)
    if (request.serverPort != 0) {
        sb.append(":").append(request.serverPort)
    }
    sb.append(request.contextPath)
    if (!path.startsWith("/")) {
        sb.append("/")
    }
    sb.append(path)
    return sb.toString()
}

fun collectAllDeploymentResources(deployment: DeploymentConfig): List<BasicResource> {
    val allResources = mutableListOf<BasicResource>()
    allResources.addAll(deployment.resources)
    if (deployment.applicationInfo.splashScreen != null) {
        allResources.add(deployment.applicationInfo.splashScreen)
    }
    if (deployment.applicationInfo.license != null) {
        allResources.add(deployment.applicationInfo.license)
    }

    allResources.addAll(deployment.applicationInfo.icons ?: emptyList())
    allResources.addAll(deployment.applicationInfo.linux.icons ?: emptyList())
    allResources.addAll(deployment.applicationInfo.mac.icons ?: emptyList())
    allResources.addAll(deployment.applicationInfo.windows.icons ?: emptyList())
    return allResources
}

fun safeAppName(name: String): String {
    return name.replace(Regex("\\s"), "-").lowercase()
}

fun directoryToTarGz(sourceDir: Path, target: Path) {
    if (!Files.isDirectory(sourceDir)) {
        throw IOException("Please provide a directory.");
    }
    val tos = Files.newOutputStream(target)
    val buffOut = BufferedOutputStream(tos)
    val gzOut = GzipCompressorOutputStream(buffOut)
    val tarOs = TarArchiveOutputStream(gzOut)
    tarOs.use {
        tarOs.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR)
        tarOs.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU)

        Files.walkFileTree(sourceDir, SimpleFileVisitor(sourceDir, tarOs))
        tarOs.finish()
    }
}

/**
 * Extract source .tar.gz archive to specified path.
 * @param source path to archive to extract
 * @param targetDir directory to which archive is extracted
 * @return path of the top-level entry (usually directory inside archive)
 */
fun extractTarGzArchive(source: Path, targetDir: Path) : Path? {
    var topLevelPath : Path? = null
    val gzipIn = GzipCompressorInputStream(Files.newInputStream(source))
    TarArchiveInputStream(gzipIn).use { tarIn ->
        var entry: TarArchiveEntry?
        while (tarIn.nextTarEntry.also { entry = it } != null) {
            if (entry?.isDirectory == true) {
                val dirPath = Files.createDirectories(targetDir.resolve(entry?.name))
                if(topLevelPath == null) {
                    topLevelPath = dirPath
                }
            } else {
                Files.copy(tarIn.readAllBytes().inputStream(), targetDir.resolve(entry?.name))
                Files.setPosixFilePermissions(
                    targetDir.resolve(entry?.name),
                    PosixFilePermissions.fromString(posixModeToString(entry?.mode ?: 420))
                )
            }
        }
    }
    return topLevelPath
}

fun extractZipArchive(source : Path, targetDir : Path) : Path? {
    var topLevelPath : Path? = null
    ZipArchiveInputStream(Files.newInputStream(source)).use { zipArchIn ->
        var entry : ZipArchiveEntry?
        while(zipArchIn.nextZipEntry.also { entry = it } != null) {
            if(entry?.isDirectory == true) {
                val dirPath = Files.createDirectories(targetDir.resolve(entry?.name))
                if(topLevelPath == null) {
                    topLevelPath = dirPath
                }
            } else {
                Files.copy(zipArchIn.readAllBytes().inputStream(), targetDir.resolve(entry?.name))
                Files.setPosixFilePermissions(
                    targetDir.resolve(entry?.name),
                    PosixFilePermissions.fromString("rwxrwxr-x")
                )
            }
        }
    }
    return topLevelPath
}

fun posixModeToString(mode: Int): String {
    val ds = Integer.toOctalString(mode).toCharArray()
    val ss = charArrayOf('-', '-', '-', '-', '-', '-', '-', '-', '-')
    for (i in ds.indices.reversed()) {
        val n = ds[i] - '0'
        if (i == ds.size - 1) {
            if (n and 1 != 0) ss[8] = 'x'
            if (n and 2 != 0) ss[7] = 'w'
            if (n and 4 != 0) ss[6] = 'r'
        } else if (i == ds.size - 2) {
            if (n and 1 != 0) ss[5] = 'x'
            if (n and 2 != 0) ss[4] = 'w'
            if (n and 4 != 0) ss[3] = 'r'
        } else if (i == ds.size - 3) {
            if (n and 1 != 0) ss[2] = 'x'
            if (n and 2 != 0) ss[1] = 'w'
            if (n and 4 != 0) ss[0] = 'r'
        }
    }
    return String(ss)
}

fun currentTimeUtc() = ZonedDateTime.now(ZoneId.of("UTC"))