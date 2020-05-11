/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.deploy;

import co.bitshfted.xapps.zsync.ZsyncMake;
import co.bitshifted.xapps.backstage.BackstageConstants;
import co.bitshifted.xapps.backstage.content.ContentMapping;
import co.bitshifted.xapps.backstage.exception.DeploymentException;
import co.bitshifted.xapps.backstage.model.CpuArch;
import co.bitshifted.xapps.backstage.model.OS;
import co.bitshifted.xapps.backstage.service.UpdateService;
import co.bitshifted.xapps.backstage.util.BackstageFunctions;
import co.bitshifted.xapps.backstage.util.PackageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Set;


import static co.bitshifted.xapps.backstage.BackstageConstants.*;
import static co.bitshifted.xapps.backstage.BackstageConstants.DEPLOY_PKG_MODULES_DIR_NAME;
import static co.bitshifted.xapps.backstage.BackstageConstants.JDK_JMODS_DIR_NAME;

/**
 * @author Vladimir Djurovic
 */
@Slf4j
public class MacDeploymentBuilder {

	private static final String APP_BUNDLE_ARCHIVE_NAME = "app-bundle.zip";
	private static final String APP_BUNDLE_TEMPLATE = "/templates/" + APP_BUNDLE_ARCHIVE_NAME;
	private static final String APP_BUNDLE_JRE_DIR = "Contents/MacOS/jre";
	private static final String APP_BUNDLE_MACOS_DIR = "Contents/MacOS";
	private static final String APP_BUNDLE_RESOURCES_DIR = "Contents/Resources";

	@Autowired
	private ContentMapping contentMapping;
	@Value("${update.server.baseurl}")
	private String updateServerBaseUrl;

	private final Path deploymentWorkDir;
	private final Path deploymentPackageDir;
	private final DeploymentConfig config;
	private final ToolsRunner toolsRunner;

	public MacDeploymentBuilder(DeploymentConfig config) {
		this.deploymentPackageDir = config.getDeploymentPackageDir();
		this.deploymentWorkDir = deploymentPackageDir.getParent();
		this.config = config;
		// add platform specific info
		config.setOs(OS.MAC_OS_X);
		config.setCpuArch(CpuArch.X_64);
		toolsRunner = new ToolsRunner();
		toolsRunner.init();
	}

	public void createDeployment() throws DeploymentException {
		var macBuildDir = macBuildDir();
		try {
			// create mac OS build directory
			Files.createDirectory(macBuildDir);
			// create skeleton for app bundle
			var templateUri = getClass().getResource(APP_BUNDLE_TEMPLATE).toURI();
			var appBundleArchive = macBuildDir.resolve(APP_BUNDLE_ARCHIVE_NAME);
			Files.copy(Path.of(templateUri), appBundleArchive, StandardCopyOption.REPLACE_EXISTING);
			var appBundlePath = PackageUtil.unpackZipArchive(appBundleArchive, config.macAppBundleName());
			log.debug("Extracted app bundle archive to {}", appBundlePath.toString());
			// create JRE image
			var moduleNames = toolsRunner.getApplicationModules(deploymentPackageDir);
			var targetJdkDir = Path.of(contentMapping.getJdkLocation(config.getJdkProvider(), config.getJvmImplementation(), config.getJdkVersion(), config.getOs(), config.getCpuArch()));
			var jdkModulesDir = targetJdkDir.resolve(JDK_JMODS_DIR_NAME);
			var modulesPath = List.of(jdkModulesDir, deploymentPackageDir.resolve(DEPLOY_PKG_MODULES_DIR_NAME));
			toolsRunner.createRuntimeImage(moduleNames, modulesPath, appBundlePath.resolve(APP_BUNDLE_JRE_DIR));
			// copy icon to resources
			config.findMacIcons().stream().map(ic -> deploymentPackageDir.resolve(ic.getPath())).forEach(ic -> {
				try {
					FileUtils.copyToDirectory(ic.toFile(), appBundlePath.resolve(APP_BUNDLE_RESOURCES_DIR).toFile());
				} catch(IOException ex) {
					log.error("Failed to copy icon {}", ic.toFile().getName());
				}
			});
			copySplashScreen(deploymentPackageDir, appBundlePath.resolve(APP_BUNDLE_MACOS_DIR));
			// copy launcher
			var launcherFile = Path.of(contentMapping.getLauncherStorageUri()).resolve(LAUNCHER_FILE_NAME_MAC);
			var launcherTarget = appBundlePath.resolve(APP_BUNDLE_MACOS_DIR).resolve(config.getExecutableFileName());
			Files.copy(launcherFile, launcherTarget, StandardCopyOption.REPLACE_EXISTING);
			var permissions = Set.of(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
					PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_EXECUTE,
					PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE);
			Files.setPosixFilePermissions(launcherTarget, permissions);
			// create launcher configuration
			createLauncherConfig(appBundlePath.resolve(APP_BUNDLE_MACOS_DIR));
			createInfoPlist(appBundlePath.resolve("Contents"), config);
			// create update package
			var updatePkgPath = createUpdatePackage(appBundlePath.resolve("Contents"));
			log.debug("Update package path: {}", updatePkgPath.toString());
			prepareForDownload(updatePkgPath);
		} catch(IOException | URISyntaxException ex) {
			log.error("Failed to create deployment", ex);
			throw new DeploymentException(ex);
		}

	}

	private Path macBuildDir() {
		return deploymentWorkDir.resolve("macos");
	}

	private void createLauncherConfig(Path appBundleMacOsPath) throws DeploymentException {
		try {
			var xmlProcessor = new XmlProcessor(config.getIgniteConfigFile());
			config.getLauncherConfig().setReleaseNumber(BackstageFunctions.getReleaseNumberFromDeploymentDir(deploymentWorkDir.toFile()));
			var fileContent = xmlProcessor.createLauncherConfigXml(config.getLauncherConfig());
			var launcherConfigPath = appBundleMacOsPath.resolve(LAUNCHER_CONFIG_FILE_NAME);
			try(var writer = new PrintWriter(new FileWriter(launcherConfigPath.toFile()))) {
				writer.write(fileContent);
			}
		} catch (Exception ex) {
			throw new DeploymentException(ex);
		}
	}

	private void copySplashScreen(Path deploymentPackageDir, Path appBundleMacOsPath) throws IOException {
		var splashScreen = config.getSplashScreen();
		if(splashScreen != null) {
			var realPath = deploymentPackageDir.resolve(splashScreen.getPath());
			FileUtils.copyToDirectory(realPath.toFile(), appBundleMacOsPath.toFile());
		}
	}

	private void createInfoPlist(Path appBundleContentsPath, DeploymentConfig config) throws IOException {
		var infoPlist = appBundleContentsPath.resolve("Info.plist");
		var contents = Files.readString(infoPlist);
		var replaced = contents.replace("${app.name}", config.getAppName())
				.replace("${app.executable}", config.getExecutableFileName())
				.replace("${app.icon}", config.findMacIcons().get(0).getFileName())
				.replace("${app.id}", config.getAppId())
				.replace("${bundle.fqdn}", config.getAppId())
				.replace("${app.version}", config.getAppVersion());
		Files.writeString(infoPlist, replaced, StandardOpenOption.TRUNCATE_EXISTING);
	}

	private Path createUpdatePackage(Path bundleContentsDir) throws IOException {
		var updateDir = macBuildDir().resolve("update");
		Files.createDirectories(updateDir);
		log.debug("Created update directory {}", updateDir.toString());
		var jreModules = bundleContentsDir.resolve("MacOS/jre/lib/modules");
		var moved = updateDir.resolve("modules");
		Files.move(jreModules, moved);
		var updatePath =  PackageUtil.packZipDeterministic(bundleContentsDir);
		// update package contents
		var updateContents = updateDir.resolve("contents.zip");
		Files.move(updatePath, updateContents);
		log.debug("Created contents update package at {}", updateContents.toString());
		// compress modules file
		var updateModules = PackageUtil.zipSingleFile(moved);
		log.debug("Created update modules file {}", updateModules.toString());
		Files.move(moved, jreModules);
		return updateDir;
	}

	private void prepareForDownload(Path updatesSourcePath) throws IOException {
		var targetUpdatePath = Path.of(contentMapping.getUpdatesParentLocation(
				config.getAppId(), config.getLauncherConfig().getReleaseNumber(), OS.MAC_OS_X, CpuArch.X_64));
		Files.createDirectories(targetUpdatePath);
		log.debug("Created directory structure for updates at {}", targetUpdatePath.toString());
		var contentsTarget = targetUpdatePath.resolve(CONTENT_UPDATE_FILE_NAME);
		var contentSource = updatesSourcePath.resolve(CONTENT_UPDATE_FILE_NAME);
		Files.move(contentSource, contentsTarget, StandardCopyOption.REPLACE_EXISTING);

		var modulesTarget = targetUpdatePath.resolve(MODULES_UPDATE_FILE_NAME);
		var moduleSource = updatesSourcePath.resolve(MODULES_UPDATE_FILE_NAME);
		Files.move(moduleSource, modulesTarget, StandardCopyOption.REPLACE_EXISTING);
		// make zsync files for synchronization
		var zsyncmake = new ZsyncMake();
		var contentOptions = new ZsyncMake.Options();
		contentOptions.setUrl(makeControlFileUrl(CONTENT_UPDATE_FILE_NAME));
		zsyncmake.writeToFile(contentsTarget, contentOptions);

		var moduleOptions = new ZsyncMake.Options();
		moduleOptions.setUrl(makeControlFileUrl(MODULES_UPDATE_FILE_NAME));
		zsyncmake.writeToFile(modulesTarget, moduleOptions);
	}

	private String makeControlFileUrl(String fileName) {
		var path = String.format(UpdateService.UPDATE_DOWNLOAD_ENDPOINT_FORMAT, config.getAppId(),
				BackstageFunctions.getReleaseNumberFromDeploymentDir(deploymentWorkDir.toFile()),
				OS.MAC_OS_X.getBrief(), CpuArch.X_64.getDisplay(),fileName);
		var url = BackstageFunctions.generateServerUrl(updateServerBaseUrl, path);
		log.info("Generated ZSync filr URL: {}", url);
		return url;
	}
}
