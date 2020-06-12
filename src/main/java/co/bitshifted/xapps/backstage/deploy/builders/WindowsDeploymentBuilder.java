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
			var appDirPath = buildDirectory.resolve(config.getAppName());
			Files.createDirectory(appDirPath);
			return appDirPath;
		} catch (IOException ex) {
			log.error("Failed to create build directory");
			throw new DeploymentException(ex);
		}

	}

	@Override
	public void createDeployment() throws DeploymentException {
		try {
			// create windows build directory
			var appDirPath = prepareDirectoryStructure();
			// add modules for updates
			copySyncroModules();
			createJreImage(appDirPath.resolve("jre"));
			copyIcons(appDirPath);
			copySplashScreen(appDirPath);
			copyLauncher(LAUNCHER_FILE_NAME_WIN_64, appDirPath);
			// create launcher configuration
			writeLauncherConfig(appDirPath);
			var updatePkgPath = createUpdatePackage(appDirPath);
			log.debug("Update package path: {}", updatePkgPath.toString());
			prepareForDownload(updatePkgPath);
		} catch (Exception ex) {
			log.error("Failed to create deployment", ex);
			throw new DeploymentException(ex);
		}

	}

	@Override
	protected Path createUpdatePackage(Path appDir) throws DeploymentException {
		try {
			var updateDir = appDir.getParent().resolve("update");
			Files.createDirectories(updateDir);
			log.debug("Created update directory {}", updateDir.toString());
			var jreModules = appDir.resolve("jre/lib/modules");
			var moved = updateDir.resolve("modules");
			Files.move(jreModules, moved);
			var updatePath = PackageUtil.packZipDeterministic(appDir);
			// update package contents
			var updateContents = updateDir.resolve("contents.zip");
			Files.move(updatePath, updateContents);
			log.debug("Created contents update package at {}", updateContents.toString());
			// compress modules file
			var updateModules = PackageUtil.zipSingleFile(moved);
			log.debug("Created update modules file {}", updateModules.toString());
			Files.move(moved, jreModules);
			return updateDir;
		} catch (IOException ex) {
			throw new DeploymentException(ex);
		}
	}

	@Override
	protected Logger logger() {
		return log;
	}
}
