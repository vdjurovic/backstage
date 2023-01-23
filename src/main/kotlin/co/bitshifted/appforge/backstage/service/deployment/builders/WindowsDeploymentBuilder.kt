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
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.absolutePathString

class WindowsDeploymentBuilder(val builder: DeploymentBuilder) {

    private val installerTemplate = "windows/installer.nsi.ftl"
    private val installerConfigFileName = "installer.nsi"
//    private val nsisCompilerCmd = "makensis"
    val logger = logger(this)
    lateinit var classpathDir: Path
    lateinit var modulesDir: Path

    fun build(): Boolean {
        val archs = builder.builderConfig.deploymentConfig.applicationInfo.windows.supportedCpuArchitectures
        archs.forEach {
            logger.info("Creating Windows deployment in directory {}", builder.getWindowsDir(it))
            try {
                createDirectoryStructure(it)
                builder.copyDependencies(modulesDir, classpathDir, OperatingSystem.WINDOWS, it)
                builder.copyResources(builder.getWindowsDir(it))
                builder.buildJdkImage(builder.getWindowsDir(it), modulesDir, OperatingSystem.WINDOWS, it)
                val templateData = getTemplateData(it)
                copyLauncher(it)
                copyWindowsIcons(it)
                copySplashScreen(it)
                createInstaller(templateData)
                logger.info("Successfully created Windows deployment in directory {}", builder.getWindowsDir(it))
                return true
            } catch (th: Throwable) {
                logger.error("Error building Windows deployment", th)
                throw th
            }
        }
        return true
    }

    private fun createDirectoryStructure(arch: CpuArch) {
        classpathDir = Files.createDirectories(
            Paths.get(
                builder.getWindowsDir(arch).absolutePathString(), BackstageConstants.OUTPUT_CLASSPATH_DIR
            )
        )
        logger.info("Created classpath directory at {}", classpathDir.toFile().absolutePath)
        modulesDir = Files.createDirectories(
            Paths.get(
                builder.getWindowsDir(arch).absolutePathString(),
                BackstageConstants.OUTPUT_MODULES_DIR
            )
        )
        logger.info("Created modules directory at {}", modulesDir.toFile().absolutePath)
    }

    private fun copyLauncher(arch: CpuArch) {
        var launcherName = String.format(BackstageConstants.LAUNCHER_NAME_FORMAT_WINDOWS, arch.display)
        val launcherPath = Path.of(
            builder.launchCodeDir.absolutePathString(),
            BackstageConstants.OUTPUT_LAUNCHER_DIST_DIR, launcherName
        )

        var exeName = builder.builderConfig.deploymentConfig.applicationInfo.exeName
        if (!exeName.endsWith(".exe")) {
            exeName = "$exeName.exe"
        }
        logger.debug(
            "Copying Windows launcher from {} to {}", launcherPath.absolutePathString(), builder.getWindowsDir(arch).resolve(
                exeName
            )
        )
        Files.copy(
            launcherPath,
            builder.getWindowsDir(arch).resolve(exeName),
            StandardCopyOption.COPY_ATTRIBUTES
        )
    }

    private fun copyWindowsIcons(arch: CpuArch) {
        builder.builderConfig.deploymentConfig.applicationInfo.windows.icons.forEach {
            val name = if (it.target != null) it.target else it.source
            val target = builder.getWindowsDir(arch).resolve(name)
            logger.debug("Icon target: {}", target.toFile().absolutePath)
            Files.createDirectories(target.parent)
            builder.builderConfig.contentService?.get(
                it.sha256 ?: throw BackstageException(ErrorInfo.EMPTY_CONTENT_CHECKSUM)
            )?.input
                .use {
                    Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING)
                }
        }
    }

    private fun copySplashScreen(arch: CpuArch) {
        val splash = builder.builderConfig.deploymentConfig.applicationInfo.splashScreen
        if (splash != null) {
            val name = if (splash.target != null) splash.target else splash.source
            val target = builder.getWindowsDir(arch).resolve(name)
            logger.debug("Splash screen target: {}", target.toFile().absolutePath)
            Files.createDirectories(target.parent)
            builder.builderConfig.contentService?.get(
                splash.sha256 ?: throw BackstageException(ErrorInfo.EMPTY_CONTENT_CHECKSUM)
            )?.input.use {
                Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    private fun getTemplateData(arch: CpuArch): MutableMap<String, Any> {
        val data = mutableMapOf<String, Any>()
        data["exe"] = builder.builderConfig.deploymentConfig.applicationInfo.exeName
        data["appName"] = builder.builderConfig.deploymentConfig.applicationInfo.name
        data["appSafeName"] = safeAppName(builder.builderConfig.deploymentConfig.applicationInfo.name)
        data["version"] = builder.builderConfig.deploymentConfig.version
        data["licenseFile"] =
            builder.getWindowsDir(arch).resolve(builder.builderConfig.deploymentConfig.applicationInfo.license.target)
                .absolutePathString()
        data["contentDir"] = builder.getWindowsDir(arch).absolutePathString()
        val installerExeName = String.format(
            "%s-%s-windows-%s.exe",
            safeAppName(builder.builderConfig.deploymentConfig.applicationInfo.name),
            builder.builderConfig.deploymentConfig.version, arch.display
        )
        data["installerExe"] = builder.installerDir.resolve(installerExeName).absolutePathString()
        return data
    }

    private fun createInstaller(data : Map<String, Any>) {
        logger.info("Creating installer in directory {}", builder.builderConfig.baseDir.absolutePathString())
        val installerFile = builder.builderConfig.baseDir.resolve(installerConfigFileName)
        builder.generateFromTemplate(installerTemplate, installerFile, data)
        // run NSIS compiler
        builder.buildContext.packageToolsRunner.runNsis(builder.builderConfig.baseDir, installerConfigFileName, mapOf("PWD" to builder.builderConfig.baseDir.absolutePathString()))
        // make installer executable
        val installerExe = Path.of(data["installerExe"].toString()).toFile()
        installerExe.setExecutable(true)
    }
}