/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.deploy.builders;

import co.bitshfted.xapps.zsync.ZsyncMake;
import co.bitshifted.xapps.backstage.content.ContentMapping;
import co.bitshifted.xapps.backstage.deploy.DeploymentConfig;
import co.bitshifted.xapps.backstage.deploy.TargetDeploymentInfo;
import co.bitshifted.xapps.backstage.deploy.XmlProcessor;
import co.bitshifted.xapps.backstage.dto.UpdateDetail;
import co.bitshifted.xapps.backstage.dto.UpdateInformation;
import co.bitshifted.xapps.backstage.exception.DeploymentException;
import co.bitshifted.xapps.backstage.model.CpuArch;
import co.bitshifted.xapps.backstage.model.OS;
import co.bitshifted.xapps.backstage.service.UpdateService;
import co.bitshifted.xapps.backstage.util.BackstageFunctions;
import co.bitshifted.xapps.backstage.util.PackageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import static co.bitshifted.xapps.backstage.BackstageConstants.*;

/**
 * @author Vladimir Djurovic
 */
@Slf4j
public class WindowsDeploymentBuilder extends DeploymentBuilder {

	@Autowired
	private ContentMapping contentMapping;
	@Value("${update.server.baseurl}")
	private String updateServerBaseUrl;

	public WindowsDeploymentBuilder(TargetDeploymentInfo deploymentInfo) {
		super(deploymentInfo);
	}

	@Override
	protected Path prepareDirectoryStructure() throws DeploymentException {
		try {
			var dirName = targetOs.getBrief() + "-" + targetCpuArch.getDisplay();
			var buildDirectory = deploymentWorkDir.resolve(dirName);
			Files.createDirectory(buildDirectory);
			logger().debug("Created Windows build directory {}", buildDirectory);
			return buildDirectory;
		} catch(IOException ex) {
			log.error("Failed to create build directory");
			throw new DeploymentException(ex);
		}

	}

	@Override
	protected void copyIcons(Path targetDir) {

	}

	@Override
	public void createDeployment() throws DeploymentException {
		try {
			// create windows build directory
			var buildDir = prepareDirectoryStructure();
			// add modules for updates
			copySyncroModules();
//			// create skeleton for app bundle
//			var templateUri = getClass().getResource(APP_BUNDLE_TEMPLATE).toURI();
//			var appBundleArchive = macBuildDir.resolve(APP_BUNDLE_ARCHIVE_NAME);
//			Files.copy(Path.of(templateUri), appBundleArchive, StandardCopyOption.REPLACE_EXISTING);
//			var appBundlePath = PackageUtil.unpackZipArchive(appBundleArchive, config.macAppBundleName());
//			log.debug("Extracted app bundle archive to {}", appBundlePath.toString());
//			// add modules for updates
//			copySyncroModules();
//			// create JRE image
//			var moduleNames = toolsRunner.getApplicationModules(deploymentPackageDir);
//			var targetJdkDir = Path.of(contentMapping.getJdkLocation(config.getJdkProvider(), config.getJvmImplementation(), config.getJdkVersion(), config.getOs(), config.getCpuArch()));
//			var jdkModulesDir = targetJdkDir.resolve(JDK_JMODS_DIR_NAME);
//			var modulesPath = List.of(jdkModulesDir, deploymentPackageDir.resolve(DEPLOY_PKG_MODULES_DIR_NAME));
//			toolsRunner.createRuntimeImage(moduleNames, modulesPath, appBundlePath.resolve(APP_BUNDLE_JRE_DIR));
//			// copy icon to resources
//			config.findMacIcons().stream().map(ic -> deploymentPackageDir.resolve(ic.getPath())).forEach(ic -> {
//				try {
//					FileUtils.copyToDirectory(ic.toFile(), appBundlePath.resolve(APP_BUNDLE_RESOURCES_DIR).toFile());
//				} catch(IOException ex) {
//					log.error("Failed to copy icon {}", ic.toFile().getName());
//				}
//			});
//			copySplashScreen(deploymentPackageDir, appBundlePath.resolve(APP_BUNDLE_MACOS_DIR));
//			// copy launcher
//			var launcherFile = Path.of(contentMapping.getLauncherStorageUri()).resolve(LAUNCHER_FILE_NAME_MAC);
//			var launcherTarget = appBundlePath.resolve(APP_BUNDLE_MACOS_DIR).resolve(config.getExecutableFileName());
//			Files.copy(launcherFile, launcherTarget, StandardCopyOption.REPLACE_EXISTING);
//			var permissions = Set.of(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
//					PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_EXECUTE,
//					PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE);
//			Files.setPosixFilePermissions(launcherTarget, permissions);
//			// create launcher configuration
//			createLauncherConfig(appBundlePath.resolve(APP_BUNDLE_MACOS_DIR));
//			createInfoPlist(appBundlePath.resolve("Contents"), config);
			// create update package
//			var updatePkgPath = createUpdatePackage(appBundlePath.resolve("Contents"));
//			log.debug("Update package path: {}", updatePkgPath.toString());
//			prepareForDownload(updatePkgPath);
		} catch(Exception ex) {
			log.error("Failed to create deployment", ex);
			throw new DeploymentException(ex);
		}

	}



	protected Path createUpdatePackage(Path bundleContentsDir) throws DeploymentException {
		return null;
//		var updateDir = macBuildDir().resolve("update");
//		Files.createDirectories(updateDir);
//		log.debug("Created update directory {}", updateDir.toString());
//		var jreModules = bundleContentsDir.resolve("MacOS/jre/lib/modules");
//		var moved = updateDir.resolve("modules");
//		Files.move(jreModules, moved);
//		var updatePath =  PackageUtil.packZipDeterministic(bundleContentsDir);
//		// update package contents
//		var updateContents = updateDir.resolve("contents.zip");
//		Files.move(updatePath, updateContents);
//		log.debug("Created contents update package at {}", updateContents.toString());
//		// compress modules file
//		var updateModules = PackageUtil.zipSingleFile(moved);
//		log.debug("Created update modules file {}", updateModules.toString());
//		Files.move(moved, jreModules);
//		return updateDir;
	}

	@Override
	protected Logger logger() {
		return log;
	}
}
