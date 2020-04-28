/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.deploy;

import co.bitshifted.xapps.backstage.BackstageConstants;
import co.bitshifted.xapps.backstage.entity.AppDeploymentStatus;
import co.bitshifted.xapps.backstage.entity.Application;
import co.bitshifted.xapps.backstage.model.DeploymentStatus;
import co.bitshifted.xapps.backstage.exception.DeploymentException;
import co.bitshifted.xapps.backstage.repository.AppDeploymentStatusRepository;
import co.bitshifted.xapps.backstage.util.PackageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * @author Vladimir Djurovic
 */
@Slf4j
public class DeploymentProcessTask implements Runnable {

	private final Path deploymentArchive;
	private final Path deploymentWorkDir;
	private AppDeploymentStatus status;

	@Autowired
	private AppDeploymentStatusRepository deploymentStatusRepo;
	@Autowired
	private Function<DeploymentConfig, MacDeploymentBuilder> macDeploymentBuilderFactory;

	public DeploymentProcessTask(File file) {
		this.deploymentArchive = file.toPath();
		this.deploymentWorkDir = deploymentArchive.getParent();
	}

	@Override
	public void run() {
		log.info("Start processing deployment archive {}", deploymentArchive.toFile().getName());
		status.setCurrentStatus(DeploymentStatus.IN_PROGRESS);
		deploymentStatusRepo.save(status);
		log.debug("Setting deployment task status to {}", DeploymentStatus.IN_PROGRESS);

		try{
			var deploymentPackageDir = PackageUtil.unpackZipArchive(deploymentArchive);
			// create deployment configuration
			var igniteConfigPath = deploymentPackageDir.resolve(BackstageConstants.IGNITE_CONFIG_FILE_NAME);
			var xmlProcessor = new XmlProcessor(igniteConfigPath);
			var deploymentConfig = xmlProcessor.getDeploymentConfig();
			deploymentConfig.setDeploymentPackageDir(deploymentPackageDir);
			var deploymentBuilder = macDeploymentBuilderFactory.apply(deploymentConfig);
			deploymentBuilder.createDeployment();
			status.setCurrentStatus(DeploymentStatus.SUCCESS);
			status.setDetails("Deployment completed successfully");
		} catch(IOException | DeploymentException | ParserConfigurationException | SAXException | XPathExpressionException ex) {
			log.error("Failed to create deployment package", ex);
			status.setCurrentStatus(DeploymentStatus.FAILED);
		}
		deploymentStatusRepo.save(status);
	}

	public void init(Application app) {
		log.debug("Initializing deployment task with app id {}", app.getId());
		status = deploymentStatusRepo.save(new AppDeploymentStatus(deploymentArchive.getParent().toFile().getName(), app));
		log.info("Initialized deployment task {}", deploymentArchive.getParent().toFile().getName());
	}

}
