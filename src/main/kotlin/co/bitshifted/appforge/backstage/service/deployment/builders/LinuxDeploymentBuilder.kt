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
import co.bitshifted.appforge.backstage.exception.DeploymentException
import co.bitshifted.appforge.backstage.exception.ErrorInfo
import co.bitshifted.appforge.backstage.util.directoryToTarGz
import co.bitshifted.appforge.backstage.util.logger
import co.bitshifted.appforge.backstage.util.safeAppName
import co.bitshifted.appforge.common.model.CpuArch
import co.bitshifted.appforge.common.model.LinuxPackageType
import co.bitshifted.appforge.common.model.OperatingSystem
import org.apache.commons.io.FileUtils
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermissions
import java.util.stream.Collectors
import kotlin.io.path.absolutePathString
import kotlin.io.path.setPosixFilePermissions

class LinuxDeploymentBuilder(val builder : DeploymentBuilder) {

    private val desktopEntryLocalTemplate = "linux/desktop-entry-local.ftl"
    private val desktopEntryGlobalTemplate = "linux/desktop-entry-global.ftl"
    private val installerTemplate = "linux/install-script.sh.ftl"
    private val debianControlFileTemplate = "linux/deb-control.ftl"
    private val debianPostInstFileTemplate = "linux/deb-postinst.ftl"
    private val debianPostRmFileTemplate = "linux/deb-postrm.ftl"
    private val rpmSpecFileTemplate = "linux/rpm-build-spec.ftl"
    private val debianControlDir = "DEBIAN"
    private val debianControlFileName = "control"
    private val debianPostInstFileName = "postinst"
    private val debianPostRmFileName = "postrm"
    private val installerFileName = "installer.sh"
    private val appSafeNameDataKey = "appSafeName"
    val logger = logger(this)
    private lateinit var rpmSpecsDir : Path
    private lateinit var rpmBuildrootDir : Path
    private lateinit var rpmWorkDir : Path
    private lateinit var rpmsDir : Path

    fun build(): Boolean {
        val packageTypes = builder.builderConfig.deploymentConfig.applicationInfo.linux.packageTypes
        if(packageTypes.contains(LinuxPackageType.RPM)) {
            createRpmDirs()
        }
        val archs = builder.builderConfig.deploymentConfig.applicationInfo.linux.supportedCpuArchitectures
        archs.forEach {
            logger.info("Creating Linux deployment in directory {}", builder.getLinuxDir(it))
            val data = getTemplateData(it)
            try {
                createDirectoryStructure(it)
                builder.copyDependencies(getModulesDir(it), getClasspathDir(it), OperatingSystem.LINUX, it)
                builder.copyResources(builder.getLinuxDir(it))
                builder.buildJdkImage(builder.getLinuxDir(it), getModulesDir(it), OperatingSystem.LINUX, it)
                copyLauncher(it)
                copyLinuxIcons(it)
                copySplashScreen(it)
                createDesktopEntry(desktopEntryGlobalTemplate, builder.getLinuxDir(it), data)
                if(packageTypes.contains(LinuxPackageType.TAR_GZ)) {
                    createTarGzPackage(it)
                }
                if(packageTypes.contains(LinuxPackageType.DEB)) {
                    createDebPackage(it)
                }
                if(packageTypes.contains(LinuxPackageType.RPM)) {
                    createRpmPackage(it)
                }
                logger.info("Successfully created Linux deployment in directory {}", builder.getLinuxDir(it))
            } catch (th: Throwable) {
                logger.error("Error building Linux deployment", th)
                throw th
            }
        }
        if(packageTypes.contains(LinuxPackageType.RPM)) {
            // clean up RPM work dir
            FileUtils.deleteDirectory(rpmWorkDir.toFile())
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
        data[appSafeNameDataKey] = safeAppName(builder.builderConfig.deploymentConfig.applicationInfo.name)
        data["categories"] = builder.builderConfig.deploymentConfig.applicationInfo.linux.categories.stream().collect(Collectors.joining(";"))
        logger.debug("template categories: {}", data["categories"])
        data["version"] = builder.builderConfig.deploymentConfig.version
        data["rpmVersion"] = builder.builderConfig.deploymentConfig.version.replace("-", "_")
        data["appUrl"] = builder.builderConfig.deploymentConfig.applicationInfo.homePageUrl ?: ""
        data["publisher"] = builder.builderConfig.deploymentConfig.applicationInfo.publisher ?: ""
        data["publisher_email"] = builder.builderConfig.deploymentConfig.applicationInfo.publisherEmail ?: ""
        data["description"] = builder.builderConfig.deploymentConfig.applicationInfo.description
        // find all executable files
        data["rpmRelease"] = "1"
        data["debArch"] = generateTargetArchitectureString(arch, LinuxPackageType.DEB)
        return data
    }

    private fun createDesktopEntry(templateName : String, targetDirectory : Path, data : Map<String, Any>) {
        val safeName = data[appSafeNameDataKey]
        val targetPath = targetDirectory.resolve("${safeName}.desktop")
        builder.generateFromTemplate(templateName, targetPath, data)
    }

    private fun createTarGzPackage(arch: CpuArch) {
        logger.info("Creating tar.gz package in directory {}", builder.installerDir.absolutePathString())
        val data = getTemplateData(arch)
        val installerWorkDirName = String.format("linux/%s-%s-linux-%s", data["appSafeName"], data["version"], arch.display)
        val workDir = builder.installerDir.resolve(installerWorkDirName)
        Files.createDirectories(workDir)
        logger.debug("Linux .tar.gz package working directory: {}", workDir.absolutePathString())
        val installerFile = workDir.resolve(installerFileName)
        builder.generateFromTemplate(installerTemplate, installerFile, data)
        installerFile.toFile().setExecutable(true)
        // copy content
        val contentDir = workDir.resolve("content")
        Files.createDirectories(contentDir)
        FileUtils.copyDirectory(builder.getLinuxDir(arch).toFile(), contentDir.toFile())
        // generate desktop entry file
        // TODO needs to be fixed, see https://github.com/bitshifted/appforge/issues/63
//        createDesktopEntry(desktopEntryLocalTemplate, contentDir, data)
        val installerName = linuxPackageFinalName(data[appSafeNameDataKey].toString(), data["version"].toString(), arch, LinuxPackageType.TAR_GZ)
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
        val debianDir = Files.createDirectories(debWorkDir.resolve(debianControlDir))
        val contentDir = Files.createDirectories(Path.of(debWorkDir.absolutePathString(), "/opt", data[appSafeNameDataKey].toString()))
        val controlFile = debianDir.resolve(debianControlFileName)
        builder.generateFromTemplate(debianControlFileTemplate, controlFile, data)
        // create post installation script
        val postInstFile = debianDir.resolve(debianPostInstFileName)
        builder.generateFromTemplate(debianPostInstFileTemplate, postInstFile, data)
        postInstFile.setPosixFilePermissions(PosixFilePermissions.fromString("rwxr-xr-x"))
        // create postrm file
        val postRmFile = debianDir.resolve(debianPostRmFileName)
        builder.generateFromTemplate(debianPostRmFileTemplate, postRmFile, data)
        postRmFile.setPosixFilePermissions(PosixFilePermissions.fromString("rwxr-xr-x"))
        // copy content
        FileUtils.copyDirectory(builder.getLinuxDir(arch).toFile(), contentDir.toFile())
        // generate desktop entry file
        // TODO needs to be fixed, see https://github.com/bitshifted/appforge/issues/63
//        createDesktopEntry(desktopEntryGlobalTemplate, contentDir, data)
        // create .deb package
        val packageFinalName = linuxPackageFinalName(data[appSafeNameDataKey].toString(), data["version"].toString(), arch, LinuxPackageType.DEB)
        builder.buildContext.packageToolsRunner.runDpkg(builder.installerDir, packageFinalName, debWorkDirName)
        // cleanup
        FileUtils.deleteDirectory(debWorkDir.toFile())
    }

    private fun linuxPackageFinalName(appName : String, version : String, arch : CpuArch, type : LinuxPackageType) : String {
        return String.format("%s-%s-linux-%s%s", appName, version, generateTargetArchitectureString(arch, type), type.display)
    }

    private fun generateTargetArchitectureString(arch: CpuArch, packageType: LinuxPackageType) : String {
        when(packageType) {
            LinuxPackageType.DEB -> return if(arch == CpuArch.X64) return "amd64" else "arm64"
            LinuxPackageType.RPM -> return if(arch == CpuArch.X64) return "x86_64" else "arm64"
            LinuxPackageType.TAR_GZ -> return arch.display
        }
    }

    private fun createRpmDirs() {
        logger.info("Creating .rpm directories in directory {}", builder.installerDir.absolutePathString())
        val rpmWorkDirName = "rpmbuild"
        rpmWorkDir = builder.installerDir.resolve(rpmWorkDirName)
        Files.createDirectories(rpmWorkDir)
        logger.debug("Linux .rpm package working directory: {}", rpmWorkDir.absolutePathString())
        // create RPM dir hierarchy
        val rpmBuildDir = rpmWorkDir.resolve("BUILD")
        Files.createDirectories(rpmBuildDir)
        rpmBuildrootDir = rpmWorkDir.resolve("BUILDROOT")
        Files.createDirectories(rpmBuildrootDir)
        rpmsDir = rpmWorkDir.resolve("RPMS")
        Files.createDirectories(rpmsDir)
        rpmSpecsDir = rpmWorkDir.resolve("SPECS")
        Files.createDirectories(rpmSpecsDir)
    }

    private fun createRpmPackage(arch: CpuArch) {
        if(arch == CpuArch.AARCH64) {
            logger.warn("Building RPM packages for AARCH64 is not supported at the moment.")
        }
        logger.info("Creating .rpm package in directory {}", builder.installerDir.absolutePathString())
        val data = getTemplateData(arch)
        data["cpuArch"] = generateTargetArchitectureString(arch, LinuxPackageType.RPM)
        // copy content
        val rpmBuildDirName = String.format("%s-%s-%s.%s", data[appSafeNameDataKey], data["rpmVersion"],data["rpmRelease"], generateTargetArchitectureString(arch, LinuxPackageType.RPM))
        val contentDir = Path.of(rpmBuildrootDir.absolutePathString(), rpmBuildDirName, "opt", data[appSafeNameDataKey].toString())
        Files.createDirectories(contentDir)
        FileUtils.copyDirectory(builder.getLinuxDir(arch).toFile(), contentDir.toFile())
        // create global desktop entry file
        // TODO needs to be fixed, see https://github.com/bitshifted/appforge/issues/63
//        createDesktopEntry(desktopEntryGlobalTemplate, contentDir, data)
        // generate spec file
        val specFileName = "build-spec-${arch.display}.spec"
        val specFile = rpmSpecsDir.resolve(specFileName)
        builder.generateFromTemplate(rpmSpecFileTemplate, specFile, data)
        // build package
        if(arch == CpuArch.AARCH64) {
            logger.error("RPM packages currently not supported for ARM architecture")
            return
        }
        // build command in form: rpmbuild --define "_topdir `pwd`" -bb ./SPECS/rpm-build.spec
        builder.buildContext.packageToolsRunner.runRpm(rpmWorkDir, specFileName)

        // copy RPM to installers directory
        val rpmPackageName = "$rpmBuildDirName.rpm"
        FileUtils.copyFile(Path.of(rpmsDir.absolutePathString(), data["cpuArch"].toString(), rpmPackageName).toFile(), Path.of(builder.installerDir.absolutePathString(), rpmPackageName).toFile())
    }
}