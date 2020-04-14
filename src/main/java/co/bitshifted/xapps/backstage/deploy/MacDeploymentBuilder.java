/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.deploy;

import co.bitshifted.xapps.backstage.exception.DeploymentException;
import co.bitshifted.xapps.backstage.util.PackageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * @author Vladimir Djurovic
 */
@Slf4j
public class MacDeploymentBuilder {

	private static final String APP_BUNDLE_ARCHIVE_NAME = "app-bundle.zip";
	private static final String APP_BUNDLE_TEMPLATE = "/templates/" + APP_BUNDLE_ARCHIVE_NAME;
	private static final String APP_BUNDLE_JRE_DIR = "Contents/MacOS/jre";
	private static final String APP_BUNDLE_RESOURCES_DIR = "Contents/Resources";

	private final Path deploymentWorkDir;
	private final Path deploymentPackageDir;
	private final DeploymentConfig config;
	private final ToolsRunner toolsRunner;

	public MacDeploymentBuilder(Path deploymentPackageDir, DeploymentConfig config) {
		this.deploymentPackageDir = deploymentPackageDir;
		this.deploymentWorkDir = deploymentPackageDir.getParent();
		this.config = config;
		toolsRunner = new ToolsRunner();
		toolsRunner.init();
	}

	public void createDeployment() throws DeploymentException {
		var macBuildDir = deploymentWorkDir.resolve("macos");
		try {
			// create mac OS build directory
			Files.createDirectory(macBuildDir);
			// create skeleton for app bundle
			var templateUri = getClass().getResource(APP_BUNDLE_TEMPLATE).toURI();
			var appBundleArchive = macBuildDir.resolve(APP_BUNDLE_ARCHIVE_NAME);
			Files.copy(Path.of(templateUri), appBundleArchive, StandardCopyOption.REPLACE_EXISTING);
			var appBundlePath = PackageUtil.unpackZipArchive(appBundleArchive, config.macAppBundleName());
			// create JRE image
			var moduleNames = toolsRunner.getApplicationModules(deploymentPackageDir);
			toolsRunner.createRuntimeImage(moduleNames, deploymentPackageDir.resolve("modules"), appBundlePath.resolve(APP_BUNDLE_JRE_DIR));
			// copy icon to resources
			config.findMacIcons().stream().map(ic -> deploymentPackageDir.resolve(ic)).forEach(ic -> {
				try {
					FileUtils.copyToDirectory(ic.toFile(), appBundlePath.resolve(APP_BUNDLE_RESOURCES_DIR).toFile());
				} catch(IOException ex) {
					log.error("Failed to copy icon {}", ic.toFile().getName());
				}

			});

		} catch(IOException | URISyntaxException ex) {
			log.error("Failed to create deployment", ex);
			throw new DeploymentException(ex);
		}

	}
}
