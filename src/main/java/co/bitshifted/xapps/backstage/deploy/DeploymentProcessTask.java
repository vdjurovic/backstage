/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.deploy;

import co.bitshifted.xapps.backstage.BackstageConstants;
import co.bitshifted.xapps.backstage.deploy.builders.DeploymentBuilder;
import co.bitshifted.xapps.backstage.entity.AppDeployment;
import co.bitshifted.xapps.backstage.entity.AppDeploymentStatus;
import co.bitshifted.xapps.backstage.entity.Application;
import co.bitshifted.xapps.backstage.model.CpuArch;
import co.bitshifted.xapps.backstage.model.DeploymentStatus;
import co.bitshifted.xapps.backstage.exception.DeploymentException;
import co.bitshifted.xapps.backstage.model.OS;
import co.bitshifted.xapps.backstage.repository.AppDeploymentRepository;
import co.bitshifted.xapps.backstage.repository.AppDeploymentStatusRepository;
import co.bitshifted.xapps.backstage.repository.ApplicationRepository;
import co.bitshifted.xapps.backstage.util.BackstageFunctions;
import co.bitshifted.xapps.backstage.util.PackageUtil;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.function.Function;

/**
 * @author Vladimir Djurovic
 */
@Slf4j
@EqualsAndHashCode
public class DeploymentProcessTask implements Runnable {

	@EqualsAndHashCode.Include
	private final Path deploymentArchive;
	@EqualsAndHashCode.Include
	private final Path deploymentWorkDir;
	@EqualsAndHashCode.Include
	private String deploymentId;

	@EqualsAndHashCode.Exclude
	@Autowired
	private Function<TargetDeploymentInfo, DeploymentBuilder> deploymentBuilderFactory;

	@EqualsAndHashCode.Exclude
	@Autowired
	private ApplicationRepository applicationRepository;

	@EqualsAndHashCode.Exclude
	@Autowired
	private AppDeploymentRepository appDeploymentRepository;

	public DeploymentProcessTask(File file) {
		this.deploymentArchive = file.toPath();
		this.deploymentWorkDir = deploymentArchive.getParent();
	}

	@Override
	public void run() {
		log.info("Start processing deployment archive {}", deploymentArchive.toFile().getName());
		log.debug("Setting deployment task status to {}", DeploymentStatus.IN_PROGRESS);

		try{
			var deploymentPackageDir = PackageUtil.unpackZipArchive(deploymentArchive);
			// create deployment configuration
			var igniteConfigPath = deploymentPackageDir.resolve(BackstageConstants.IGNITE_CONFIG_FILE_NAME);
			var releaseNumber = BackstageFunctions.getReleaseNumberFromDeploymentDir(deploymentWorkDir.toFile());
			var xmlProcessor = new XmlProcessor(igniteConfigPath, releaseNumber);
			var deploymentConfig = xmlProcessor.getDeploymentConfig();
			// run Mac deployment
			var macDeploymentInfo = TargetDeploymentInfo.builder().deploymentConfig(deploymentConfig)
					.deploymentPackageDir(deploymentPackageDir)
					.targetOs(OS.MAC_OS_X)
					.targetCpuArch(CpuArch.X_64)
					.xmlProcessor(xmlProcessor)
					.build();
			var macDeploymentBuilder = deploymentBuilderFactory.apply(macDeploymentInfo);
			macDeploymentBuilder.createDeployment();
			// run Windows deployment
			var win64DeploymentInfo = TargetDeploymentInfo.builder()
					.deploymentConfig(deploymentConfig)
					.deploymentPackageDir(deploymentPackageDir)
					.targetOs(OS.WINDOWS)
					.targetCpuArch(CpuArch.X_64)
					.xmlProcessor(xmlProcessor)
					.build();
			var win64deploymentBuilder = deploymentBuilderFactory.apply(win64DeploymentInfo);
			win64deploymentBuilder.createDeployment();
			saveDeployment(deploymentConfig.getAppId());
		} catch(Exception ex) {
			log.error("Failed to create deployment package", ex);
			throw new RuntimeException(ex);
		}
	}

	public void init(String deploymentId) {
		this.deploymentId = deploymentId;
		log.info("Initialized deployment task {}", deploymentArchive.getParent().toFile().getName());
	}

	public String getDeploymentId() {
		return deploymentId;
	}

	private void saveDeployment(String applicationId) {
		var app = applicationRepository.findById(applicationId).get();
		var deployment = new AppDeployment();
		deployment.setApplication(app);
		deployment.setReleaseTime(ZonedDateTime.now(BackstageConstants.UTC_ZONE_ID));
		deployment.setReleaseNumber(BackstageFunctions.getReleaseNumberFromDeploymentDir(deploymentWorkDir.toFile()));
		appDeploymentRepository.save(deployment);
	}

}
