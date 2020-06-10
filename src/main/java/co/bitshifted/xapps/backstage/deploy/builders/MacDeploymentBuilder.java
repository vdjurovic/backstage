/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.deploy.builders;

import co.bitshifted.xapps.backstage.deploy.DeploymentConfig;
import co.bitshifted.xapps.backstage.deploy.TargetDeploymentInfo;
import co.bitshifted.xapps.backstage.exception.DeploymentException;
import co.bitshifted.xapps.backstage.util.PackageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import static co.bitshifted.xapps.backstage.BackstageConstants.LAUNCHER_FILE_NAME_MAC;

/**
 * @author Vladimir Djurovic
 */
@Slf4j
public class MacDeploymentBuilder extends DeploymentBuilder {

	private static final String APP_BUNDLE_ARCHIVE_NAME = "app-bundle.zip";
	private static final String APP_BUNDLE_TEMPLATE = "/templates/" + APP_BUNDLE_ARCHIVE_NAME;
	private static final String APP_BUNDLE_JRE_DIR = "Contents/MacOS/jre";
	private static final String APP_BUNDLE_MACOS_DIR = "Contents/MacOS";
	private static final String APP_BUNDLE_RESOURCES_DIR = "Contents/Resources";

	public MacDeploymentBuilder(TargetDeploymentInfo targetDeploymentInfo) {
		super(targetDeploymentInfo);
	}

	@Override
	protected Path prepareDirectoryStructure() throws DeploymentException {
		try {
			var macBuildDir = macBuildDir();
			// create mac OS build directory
			Files.createDirectory(macBuildDir);
			// create skeleton for app bundle
			var templateUri = getClass().getResource(APP_BUNDLE_TEMPLATE).toURI();
			var appBundleArchive = macBuildDir.resolve(APP_BUNDLE_ARCHIVE_NAME);
			Files.copy(Path.of(templateUri), appBundleArchive, StandardCopyOption.REPLACE_EXISTING);
			var appBundlePath = PackageUtil.unpackZipArchive(appBundleArchive, config.macAppBundleName());
			log.debug("Extracted app bundle archive to {}", appBundlePath.toString());
			return appBundlePath;
		} catch(IOException| URISyntaxException ex) {
			throw new DeploymentException(ex);
		}

	}

	@Override
	protected void copyIcons(Path targetDir) {
		config.findMacIcons().stream().map(ic -> deploymentPackageDir.resolve(ic.getPath())).forEach(ic -> {
			try {
				FileUtils.copyToDirectory(ic.toFile(), targetDir.toFile());
			} catch(IOException ex) {
				log.error("Failed to copy icon {}", ic.toFile().getName());

			}
		});
	}

	@Override
	public void createDeployment() throws DeploymentException {
		try {
			var appBundlePath = prepareDirectoryStructure();
			// add modules for updates
			copySyncroModules();
			// create JRE image
			createJreImage(appBundlePath.resolve(APP_BUNDLE_JRE_DIR));
			// copy icon to resources
			copyIcons(appBundlePath.resolve(APP_BUNDLE_RESOURCES_DIR));
			copySplashScreen(appBundlePath.resolve(APP_BUNDLE_MACOS_DIR));
			// copy launcher
			copyLauncher(LAUNCHER_FILE_NAME_MAC, appBundlePath.resolve(APP_BUNDLE_MACOS_DIR));
			// create launcher configuration
			writeLauncherConfig(appBundlePath.resolve(APP_BUNDLE_MACOS_DIR));
			createInfoPlist(appBundlePath.resolve("Contents"), config);
			// create update package
			var updatePkgPath = createUpdatePackage(appBundlePath.resolve("Contents"));
			log.debug("Update package path: {}", updatePkgPath.toString());
			prepareForDownload(updatePkgPath);
		} catch(Exception ex) {
			log.error("Failed to create deployment", ex);
			throw new DeploymentException(ex);
		}

	}

	private Path macBuildDir() {
		return deploymentWorkDir.resolve("macos");
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

	@Override
	protected Path createUpdatePackage(Path bundleContentsDir) throws DeploymentException {
		try {
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
		} catch(IOException ex) {
			throw new DeploymentException(ex);
		}

	}

	@Override
	protected Logger logger() {
		return log;
	}
}
