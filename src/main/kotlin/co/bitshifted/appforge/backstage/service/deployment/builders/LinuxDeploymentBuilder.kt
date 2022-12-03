/*
 *
 *  * Copyright (c) 2022-2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.backstage.service.deployment.builders

import co.bitshifted.appforge.backstage.BackstageConstants.LAUNCHER_NAME_FORMAT_LINUX
import co.bitshifted.appforge.backstage.BackstageConstants.OUTPUT_CLASSPATH_DIR
import co.bitshifted.appforge.backstage.BackstageConstants.OUTPUT_LAUNCHER_DIST_DIR
import co.bitshifted.appforge.backstage.BackstageConstants.OUTPUT_MODULES_DIR
import co.bitshifted.appforge.backstage.exception.BackstageException
import co.bitshifted.appforge.backstage.exception.ErrorInfo
import co.bitshifted.appforge.backstage.util.directoryToTarGz
import co.bitshifted.appforge.backstage.util.logger
import co.bitshifted.appforge.backstage.util.safeAppName
import co.bitshifted.appforge.common.model.CpuArch
import co.bitshifted.appforge.common.model.OperatingSystem
import org.apache.commons.io.FileUtils
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors
import kotlin.io.path.absolutePathString

class LinuxDeploymentBuilder(val builder : DeploymentBuilder) {

    private val desktopEntryTemplate = "linux/desktop-entry.desktop.ftl"
    private val installerTemplate = "linux/install-script.sh.ftl"
    private val installerFileName = "installer.sh"
    val logger = logger(this)

    fun build(): Boolean {
        val archs = builder.builderConfig.deploymentConfig.applicationInfo.linux.supportedCpuArchitectures
        archs.forEach {
            logger.info("Creating Linux deployment in directory {}", builder.getLinuxDir(it))
            try {
                createDirectoryStructure(it)
                builder.copyDependencies(getModulesDir(it), getClasspathDir(it), OperatingSystem.LINUX)
                builder.copyResources(builder.getLinuxDir(it))
                builder.buildJdkImage(builder.getLinuxDir(it), getModulesDir(it), OperatingSystem.LINUX, it)
                copyLauncher(it)
                copyLinuxIcons(it)
                copySplashScreen(it)
                createDesktopEntry(it)
                createTarGzPackage(it)
                createDebPackage(it)
                logger.info("Successfully created Linux deployment in directory {}", builder.getLinuxDir(it))
            } catch (th: Throwable) {
                logger.error("Error building Linux deployment", th)
                throw th
            }
        }
        return true
    }

    private fun getClasspathDir(arch: CpuArch) : Path {
        return builder.getLinuxDir(arch).resolve(OUTPUT_CLASSPATH_DIR)
    }

    private fun getModulesDir(arch: CpuArch) : Path {
        return builder.getLinuxDir(arch).resolve(OUTPUT_MODULES_DIR)
    }

    private fun createDirectoryStructure(arch : CpuArch) {
        Files.createDirectories(Paths.get(builder.getLinuxDir(arch).absolutePathString(), OUTPUT_CLASSPATH_DIR))
        logger.info("Created classpath directory at {}", getClasspathDir(arch).toFile().absolutePath)
        Files.createDirectories(Paths.get(builder.getLinuxDir(arch).absolutePathString(), OUTPUT_MODULES_DIR))
        logger.info("Created modules directory at {}", getModulesDir(arch).toFile().absolutePath)
    }

    private fun copyLauncher(arch : CpuArch) {
        var launcherName = String.format(LAUNCHER_NAME_FORMAT_LINUX, arch.display)
        val launcherPath = Path.of(builder.launchCodeDir.absolutePathString(), OUTPUT_LAUNCHER_DIST_DIR, launcherName)
        logger.debug("Copying Linux launcher from {} to {}", launcherPath.absolutePathString(), builder.getLinuxDir(arch).resolve(launcherName))
        Files.copy(launcherPath, builder.getLinuxDir(arch).resolve(builder.builderConfig.deploymentConfig.applicationInfo.exeName), StandardCopyOption.COPY_ATTRIBUTES)
    }

    private fun copyLinuxIcons(arch : CpuArch) {
        builder.builderConfig.deploymentConfig.applicationInfo.linux.icons.forEach {
            val name = if (it.target != null) it.target else it.source
            val target = builder.getLinuxDir(arch).resolve(name)
            logger.debug("Icon target: {}", target.toFile().absolutePath)
            Files.createDirectories(target.parent)
            builder.builderConfig.contentService?.get(it.sha256 ?: throw BackstageException(ErrorInfo.EMPTY_CONTENT_CHECKSUM))?.input.use {
                Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    private fun copySplashScreen(arch : CpuArch) {
        val splash = builder.builderConfig.deploymentConfig.applicationInfo.splashScreen
        if (splash!= null) {
            val name = if (splash.target != null) splash.target else splash.source
            val target = builder.getLinuxDir(arch).resolve(name)
            logger.debug("Splash screen target: {}", target.toFile().absolutePath)
            Files.createDirectories(target.parent)
            builder.builderConfig.contentService?.get(splash.sha256 ?: throw BackstageException(ErrorInfo.EMPTY_CONTENT_CHECKSUM))?.input.use {
                Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    private fun getTemplateData(arch: CpuArch) : MutableMap<String, Any> {
        logger.debug("Linux desktop categories: {}", builder.builderConfig.deploymentConfig.applicationInfo.linux.categories)
        val data = mutableMapOf<String, Any>()
        data["icon"] = builder.builderConfig.deploymentConfig.applicationInfo.linux.icons[0].target
        data["exe"] = builder.builderConfig.deploymentConfig.applicationInfo.exeName
        data["appName"] = builder.builderConfig.deploymentConfig.applicationInfo.name
        data["comment"] = builder.builderConfig.deploymentConfig.applicationInfo.headline
        data["appSafeName"] = safeAppName(builder.builderConfig.deploymentConfig.applicationInfo.name)
        data["categories"] = builder.builderConfig.deploymentConfig.applicationInfo.linux.categories.stream().collect(Collectors.joining(";"))
        logger.debug("template categories: {}", data["categories"])
        data["version"] = builder.builderConfig.deploymentConfig.version
        data["appUrl"] = builder.builderConfig.deploymentConfig.applicationInfo.homePageUrl ?: ""
        data["publisher"] = builder.builderConfig.deploymentConfig.applicationInfo.publisher
        data["deb_arch"] = when(arch){
            CpuArch.X64 ->  "amd64"
            CpuArch.AARCH64 -> "arm64"
        }
        data["description"] = builder.builderConfig.deploymentConfig.applicationInfo.description
        // find all executable files
        val fileList = FileUtils.listFiles(builder.getLinuxDir(arch).toFile(), null, true)
        val exeFiles = fileList.filter { it.canExecute() }.map { builder.getLinuxDir(arch).relativize(it.toPath()).toString() }
        data["exeFiles"] = exeFiles
        return data
    }

    private fun createDesktopEntry(arch: CpuArch) {
        val data = getTemplateData(arch)
        val template = builder.freemarkerConfig.getTemplate(desktopEntryTemplate)
        val safeName = data["appSafeName"]
        val targetPath = builder.getLinuxDir(arch).resolve("${safeName}.desktop")

        val writer = FileWriter(targetPath.toFile())
        writer.use {
            template.process(data, writer)
        }
    }

    private fun createTarGzPackage(arch: CpuArch) {
        logger.info("Creating tar.gz package in directory {}", builder.installerDir.absolutePathString())
        val data = getTemplateData(arch)
        val installerWorkDirName = String.format("linux/%s-%s-linux-%s", data["appSafeName"], data["version"], arch.display)
        val workDir = builder.installerDir.resolve(installerWorkDirName)
        Files.createDirectories(workDir)
        logger.debug("Linux .tar.gz package working directory: {}", workDir.absolutePathString())
        val template = builder.freemarkerConfig.getTemplate(installerTemplate)
        val installerFile = workDir.resolve(installerFileName)
        val writer = FileWriter(installerFile.toFile())
        writer.use {
            template.process(data, writer)
        }
        installerFile.toFile().setExecutable(true)
        // copy content
        val contentDir = workDir.resolve("content")
        Files.createDirectories(contentDir)
        FileUtils.copyDirectory(builder.getLinuxDir(arch).toFile(), contentDir.toFile())
        val installerName = String.format("%s-%s-linux-%s.tar.gz",  data["appSafeName"], data["version"], arch.display)
        val installerPath = builder.installerDir.resolve(installerName)
        logger.debug("Linux installer name: {}", installerName)
        directoryToTarGz(workDir.parent, installerPath)
        // cleanup
        FileUtils.deleteDirectory(workDir.toFile())
    }

    private fun createDebPackage(arch: CpuArch) {
        logger.info("Creating .deb package in directory {}", builder.installerDir.absolutePathString())
        val data = getTemplateData(arch)
        val debWorkDirName = String.format("deb-%s", arch.display)
        val debWorkDir = builder.installerDir.resolve(debWorkDirName)
        Files.createDirectories(debWorkDir)
        logger.debug("Linux .deb package working directory: {}", debWorkDir.absolutePathString())
        // create .deb package directories
        val debianDir = Files.createDirectories(debWorkDir.resolve("DEBIAN"))
        val contentDir = Files.createDirectories(Path.of(debWorkDir.absolutePathString(), "/opt", safeAppName(builder.builderConfig.deploymentConfig.applicationInfo.name)))
        val controlFileTemplate = builder.freemarkerConfig.getTemplate("linux/deb-control.ftl")
        val controlFile = debianDir.resolve("control")
        val writer = FileWriter(controlFile.toFile())
        writer.use {
            controlFileTemplate.process(data, writer)
        }
        // copy content
        FileUtils.copyDirectory(builder.getLinuxDir(arch).toFile(), contentDir.toFile())
    }
}