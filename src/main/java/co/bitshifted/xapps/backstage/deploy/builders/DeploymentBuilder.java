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
import co.bitshifted.xapps.backstage.deploy.ToolsRunner;
import co.bitshifted.xapps.backstage.deploy.XmlProcessor;
import co.bitshifted.xapps.backstage.dto.UpdateDetail;
import co.bitshifted.xapps.backstage.dto.UpdateInformation;
import co.bitshifted.xapps.backstage.exception.DeploymentException;
import co.bitshifted.xapps.backstage.model.CpuArch;
import co.bitshifted.xapps.backstage.model.OS;
import co.bitshifted.xapps.backstage.service.UpdateService;
import co.bitshifted.xapps.backstage.util.BackstageFunctions;
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
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Set;

import static co.bitshifted.xapps.backstage.BackstageConstants.*;

/**
 * @author Vladimir Djurovic
 */
public abstract class DeploymentBuilder {

	@Autowired
	protected ContentMapping contentMapping;
	@Value("${update.server.baseurl}")
	protected String updateServerBaseUrl;

	protected final Path deploymentWorkDir;
	protected final Path deploymentPackageDir;
	protected final DeploymentConfig config;
	protected final ToolsRunner toolsRunner;
	protected final OS targetOs;
	protected final CpuArch targetCpuArch;
	protected final XmlProcessor xmlProcessor;

	protected DeploymentBuilder(TargetDeploymentInfo targetDeploymentInfo) {
		deploymentPackageDir = targetDeploymentInfo.getDeploymentPackageDir();
		deploymentWorkDir = deploymentPackageDir.getParent();
		config = targetDeploymentInfo.getDeploymentConfig();
		targetOs = targetDeploymentInfo.getTargetOs();
		targetCpuArch = targetDeploymentInfo.getTargetCpuArch();
		xmlProcessor = targetDeploymentInfo.getXmlProcessor();
		toolsRunner = new ToolsRunner();
		toolsRunner.init();
	}

	/**
	 * Copy additional modules needed for updates.
	 */
	protected void copySyncroModules() throws DeploymentException {
		try {
			var syncroDir = Path.of(contentMapping.getSyncroStorageUri());
			var targetDir = deploymentPackageDir.resolve("modules");
			if(Files.exists(targetDir)) {
				Files.copy(syncroDir.resolve("syncro.jar"), targetDir.resolve("syncro.jar"), StandardCopyOption.REPLACE_EXISTING);
				Files.copy(syncroDir.resolve("zsyncer.jar"), targetDir.resolve("zsyncer.jar"), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch(IOException ex) {
			throw new DeploymentException(ex);
		}

	}

	protected void createJreImage(Path outputJreDir) throws DeploymentException {
		var moduleNames = toolsRunner.getApplicationModules(deploymentPackageDir);
		var targetJdkDir = Path.of(contentMapping.getJdkLocation(config.getJdkProvider(), config.getJvmImplementation(), config.getJdkVersion(), targetOs, targetCpuArch));
		var jdkModulesDir = targetJdkDir.resolve(JDK_JMODS_DIR_NAME);
		var modulesPath = List.of(jdkModulesDir, deploymentPackageDir.resolve(DEPLOY_PKG_MODULES_DIR_NAME));
		toolsRunner.createRuntimeImage(moduleNames, modulesPath, outputJreDir);
	}

	protected void copySplashScreen(Path targetDir) throws IOException {
		var splashScreen = config.getSplashScreen();
		if(splashScreen != null) {
			var realPath = deploymentPackageDir.resolve(splashScreen.getPath());
			FileUtils.copyToDirectory(realPath.toFile(), targetDir.toFile());
		}
	}

	protected void copyLauncher(String launcherName, Path targetDir) throws DeploymentException {
		try {
			var launcherFile = Path.of(contentMapping.getLauncherStorageUri()).resolve(launcherName);
			var sb = new StringBuilder(config.getExecutableFileName());
			if(targetOs == OS.WINDOWS) {
				sb.append(".exe");
			}
			var launcherTarget = targetDir.resolve(sb.toString());
			Files.copy(launcherFile, launcherTarget, StandardCopyOption.REPLACE_EXISTING);
			var permissions = Set.of(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
					PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_EXECUTE,
					PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE);
			Files.setPosixFilePermissions(launcherTarget, permissions);
		} catch(IOException ex) {
			throw new DeploymentException(ex);
		}
	}

	protected void writeLauncherConfig(Path targetDir) throws DeploymentException {
		try {
			var fileContent = xmlProcessor.createLauncherConfigXml(config.getLauncherConfig());
			var launcherConfigPath = targetDir.resolve(LAUNCHER_CONFIG_FILE_NAME);
			try(var writer = new PrintWriter(new FileWriter(launcherConfigPath.toFile()))) {
				writer.write(fileContent);
			}
		} catch (Exception ex) {
			throw new DeploymentException(ex);
		}
	}

	protected String makeControlFileUrl(String fileName) {
		var path = String.format(UpdateService.UPDATE_DOWNLOAD_ENDPOINT_FORMAT, config.getAppId(),
				BackstageFunctions.getReleaseNumberFromDeploymentDir(deploymentWorkDir.toFile()),
				targetOs.getBrief(), targetCpuArch.getDisplay(),fileName);
		var url = BackstageFunctions.generateServerUrl(updateServerBaseUrl, path);
		logger().info("Generated ZSync file URL: {}", url);
		return url;
	}

	protected void writeUpdateInfoFile(Path baseDir) throws IOException, JAXBException {
		var updateInfo = new UpdateInformation();
		updateInfo.setReleaseNumber(config.getLauncherConfig().getReleaseNumber());
		Files.walk(baseDir)
				.filter(p -> p.toFile().getName().endsWith(".zsync"))
				.forEach(f -> {
					var name = f.toFile().getName();
					var detail = new UpdateDetail();
					detail.setFileName(name);
					detail.setUrl(makeControlFileUrl(name));
					detail.setSize(f.toFile().length());
					updateInfo.addDetail(detail);
				});
		var ctx = JAXBContext.newInstance(UpdateInformation.class);
		var marshaller = ctx.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(updateInfo, baseDir.resolve(UPDATE_INFO_FILE_NAME).toFile());

	}

	protected void prepareForDownload(Path updatesSourcePath) throws IOException, JAXBException {
		var targetUpdatePath = Path.of(contentMapping.getUpdatesParentLocation(
				config.getAppId(), config.getLauncherConfig().getReleaseNumber(), targetOs, targetCpuArch));
		Files.createDirectories(targetUpdatePath);
		logger().debug("Created directory structure for updates at {}", targetUpdatePath.toString());
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

		writeUpdateInfoFile(targetUpdatePath);
	}

	protected void copyIcons(Path targetDir) {
		config.findIcons(getIconExtensionForOs()).stream().map(ic -> deploymentPackageDir.resolve(ic.getPath())).forEach(ic -> {
			try {
				FileUtils.copyToDirectory(ic.toFile(), targetDir.toFile());
			} catch(IOException ex) {
				logger().error("Failed to copy icon {}", ic.toFile().getName());

			}
		});
	}

	protected abstract Path prepareDirectoryStructure() throws DeploymentException;

	protected abstract Path createUpdatePackage(Path appDir) throws DeploymentException;

	protected abstract Logger logger();

	public abstract void createDeployment() throws DeploymentException;

	private String getIconExtensionForOs() {
		switch (targetOs) {
			case MAC_OS_X:
				return "icns";
			case WINDOWS:
				return ".ico";
			case LINUX:
				return ".png";
		}
		throw new IllegalArgumentException("Unknown operating system");
	}
}
