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

import co.bitshifted.appforge.backstage.BackstageConstants
import co.bitshifted.appforge.backstage.exception.BackstageException
import co.bitshifted.appforge.backstage.exception.DeploymentException
import co.bitshifted.appforge.backstage.exception.ErrorInfo
import co.bitshifted.appforge.backstage.util.logger
import co.bitshifted.appforge.backstage.util.safeAppName
import co.bitshifted.appforge.common.model.CpuArch
import co.bitshifted.appforge.common.model.OperatingSystem
import org.apache.commons.io.FileUtils
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory
import kotlin.io.path.isExecutable
import kotlin.io.path.isSymbolicLink

class MacDeploymentBuilder(val builder: DeploymentBuilder){

    private val appBundleMacOSDirPath = "Contents/MacOS"
    private val appBundleResourcesDirPath = "Contents/Resources"
    private val infoPlistTemplate = "mac/info.plist.ftl"
    private val infoPlistFile = "Info.plist"
    private val createDmgTemplate = "mac/create-dmg.sh.ftl"
    private val createDmgScriptNameFormat = "create-dmg-%s.sh"
    private val mbBytes = 1_000_000
    private val dsStoreInput = "/templates/mac/DS_Store"
    private val dsStoreFile = "DS_Store"
    private val backgroundImgInput = "/templates/mac/appforge-background.png"
    private val backgroundImageFile = "appforge-background.png"

    val logger = logger(this)

    fun build() : Boolean {
        val archs = builder.builderConfig.deploymentConfig.applicationInfo.mac.supportedCpuArchitectures
        archs.forEach {
            logger.info("Creating Mac OS X deployment in directory {}", builder.getMacDir(it))
            try {
                createDirectoryStructure(it)
                builder.copyDependencies(getModulesDir(it), getClasspathDir(it), OperatingSystem.MAC)
                builder.copyResources(getMacOsDir(it))
                builder.buildJdkImage(getMacOsDir(it), getModulesDir(it), OperatingSystem.MAC, it)
                val templateData = getTemplateData(it)
                copyLauncher(it)
                copyMacIcons(it)
                copySplashScreen(it)
                createInfoPlist(it, templateData)
                moveNonExecutableFiles(it)
                createInstaller(it, templateData)
                logger.info("Successfully created Mac OS X deployment in directory {}", builder.getMacDir(it))
            } catch(th : Throwable) {
                logger.error("Error building Mac OS X deployment", th)
                throw  th
            }
        }
        return true
    }

    private fun getClasspathDir(arch: CpuArch) : Path {
        return builder.getMacDir(arch).resolve(appBundleMacOSDirPath).resolve(BackstageConstants.OUTPUT_CLASSPATH_DIR)
    }

    private fun getModulesDir(arch: CpuArch) : Path {
        return builder.getMacDir(arch).resolve(appBundleMacOSDirPath).resolve(BackstageConstants.OUTPUT_MODULES_DIR)
    }

    private fun getMacOsDir(arch: CpuArch) : Path {
        return builder.getMacDir(arch).resolve(appBundleMacOSDirPath)
    }

    private fun getResourcesDir(arch: CpuArch) : Path {
        return builder.getMacDir(arch).resolve(appBundleResourcesDirPath)
    }

    private fun createDirectoryStructure(arch : CpuArch) {
        Files.createDirectories(Paths.get(builder.getMacDir(arch).absolutePathString(), appBundleMacOSDirPath))
        Files.createDirectories(builder.getMacDir(arch).resolve(appBundleMacOSDirPath).resolve(BackstageConstants.OUTPUT_CLASSPATH_DIR))
        logger.info("Created classpath directory at {}", getClasspathDir(arch).toFile().absolutePath)
        Files.createDirectories(builder.getMacDir(arch).resolve(appBundleMacOSDirPath).resolve(BackstageConstants.OUTPUT_MODULES_DIR))
        logger.info("Created modules directory at {}", getModulesDir(arch).toFile().absolutePath)
        Files.createDirectories(Paths.get(builder.getMacDir(arch).absolutePathString(), appBundleResourcesDirPath))
        logger.info("Created Resources directory in {}", getResourcesDir(arch).absolutePathString())
    }

    private fun copyLauncher(arch: CpuArch) {
        var launcherName = String.format(BackstageConstants.LAUNCHER_NAME_FORMAT_MAC, arch.display)
        val launcherPath = Path.of(builder.launchCodeDir.absolutePathString(),
            BackstageConstants.OUTPUT_LAUNCHER_DIST_DIR, launcherName)
        logger.debug("Copying Mac OS X launcher from {} to {}", launcherPath.absolutePathString(), getMacOsDir(arch).resolve(launcherName))
        Files.copy(launcherPath, getMacOsDir(arch).resolve(builder.builderConfig.deploymentConfig.applicationInfo.exeName), StandardCopyOption.COPY_ATTRIBUTES)
    }

    private fun copyMacIcons(arch: CpuArch) {
        builder.builderConfig.deploymentConfig.applicationInfo.mac.icons.forEach {
            val name = if (it.target != null) it.target else it.source
            val target = getResourcesDir(arch).resolve(name)
            logger.debug("Icon target: {}", target.toFile().absolutePath)
            Files.createDirectories(target.parent)
            builder.builderConfig.contentService?.get(it.sha256 ?: throw BackstageException(ErrorInfo.EMPTY_CONTENT_CHECKSUM))?.input.use {
                Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    private fun copySplashScreen(arch: CpuArch) {
        val splash = builder.builderConfig.deploymentConfig.applicationInfo.splashScreen
        if (splash!= null) {
            val name = if (splash.target != null) splash.target else splash.source
            val target = getResourcesDir(arch).resolve(name)
            logger.debug("Splash screen target: {}", target.toFile().absolutePath)
            Files.createDirectories(target.parent)
            builder.builderConfig.contentService?.get(splash.sha256 ?: throw BackstageException(ErrorInfo.EMPTY_CONTENT_CHECKSUM))?.input.use {
                Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING)
            }
            // create symlink to splash screen
            Files.createSymbolicLink(getMacOsDir(arch).resolve(name), getMacOsDir(arch).relativize(getResourcesDir(arch).resolve(target.fileName)))
        }
    }

    private fun getTemplateData(arch: CpuArch) : MutableMap<String, Any> {
        val data = mutableMapOf<String, Any>()
        data["appName"] = builder.builderConfig.deploymentConfig.applicationInfo.name
        data["appExecutable"] = builder.builderConfig.deploymentConfig.applicationInfo.exeName
        data["appIcon"] = builder.builderConfig.deploymentConfig.applicationInfo.mac.icons[0].target
        data["appId"] = builder.builderConfig.deploymentConfig.applicationId
        data["bundleFqdn"] = builder.builderConfig.deploymentConfig.applicationId
        data["appVersion"] = builder.builderConfig.deploymentConfig.version
        data["installerDir"] = builder.installerDir.absolutePathString()
        data["appSafeName"] = safeAppName(builder.builderConfig.deploymentConfig.applicationInfo.name)
        data["macOutputDir"] = builder.getMacDir(arch).absolutePathString()
        data["cpuArch"] = arch.display
        val dirSizeBytes = FileUtils.sizeOfDirectory(builder.getMacDir(arch).toFile())
        data["sizeInMb"] = (dirSizeBytes / mbBytes) + 2
        return data
    }

    private fun createInfoPlist(arch: CpuArch, data : Map<String, Any>) {
        val targetPath = getMacOsDir(arch).parent.resolve(infoPlistFile)
        builder.generateFromTemplate(infoPlistTemplate, targetPath, data)
    }

    // Mac OS bundle signing fails if there are non-executable files in Content/MacOS directory.
    // this function moves them
    private fun moveNonExecutableFiles(arch: CpuArch) {
        val macOsDir = getMacOsDir(arch)
        val resourcesDir = getResourcesDir(arch)
        Files.list(macOsDir).forEach {
            if (it.isDirectory()) {
                try {
                    FileUtils.moveDirectory(it.toFile(), resourcesDir.resolve(it.fileName).toFile())
                    createSymlink(macOsDir.resolve(it.fileName), macOsDir.relativize(resourcesDir.resolve(it.fileName)))
                } catch(ex : IOException) {
                    logger.error("Failed to move directory", ex)
                }
            } else if (!it.isExecutable() && !it.isSymbolicLink()) {
                Files.move(it, resourcesDir.resolve(it.fileName))
                Files.createSymbolicLink(macOsDir.resolve(it.fileName), macOsDir.relativize(resourcesDir.resolve(it.fileName)))
            }

        }
    }

    private fun createSymlink(source : Path, target : Path) {
        logger.debug("Creating symlink: {} -> {}", source.absolutePathString(), target.toString())
        Files.createSymbolicLink(source, target)
    }

    private fun createInstaller(arch: CpuArch, data : Map<String, Any>) {
        logger.info("Creating Mac OS X installer")
        val dsStore = this.javaClass.getResourceAsStream(dsStoreInput)
        Files.copy(dsStore, builder.installerDir.resolve(dsStoreFile))
        val bgImage = this.javaClass.getResourceAsStream(backgroundImgInput)
        Files.copy(bgImage, builder.installerDir.resolve(backgroundImageFile))
        val createDmgScriptFileName = String.format(createDmgScriptNameFormat, arch.display)
        val installerFile = builder.installerDir.resolve(createDmgScriptFileName)
        builder.generateFromTemplate(createDmgTemplate, installerFile, data)
        installerFile.toFile().setExecutable(true)
        // invoke installer creation
        builder.runExternalProgram(listOf("./$createDmgScriptFileName"), builder.installerDir.toFile(), mapOf("PWD" to builder.installerDir.absolutePathString()))
    }
}