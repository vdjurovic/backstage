/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.deploy;

import co.bitshifted.xapps.backstage.entity.AppDeploymentStatus;
import co.bitshifted.xapps.backstage.entity.Application;
import co.bitshifted.xapps.backstage.enums.DeploymentStatus;
import co.bitshifted.xapps.backstage.repository.AppDeploymentStatusRepository;
import co.bitshifted.xapps.backstage.util.PackageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.Buffer;

/**
 * @author Vladimir Djurovic
 */
@Slf4j
public class DeploymentProcessTask implements Runnable {

	private final File deploymentArchive;
	private AppDeploymentStatus status;

	@Autowired
	private AppDeploymentStatusRepository deploymentStatusRepo;

	public DeploymentProcessTask(File file) {
		this.deploymentArchive = file;
	}

	@Override
	public void run() {
		log.info("Start processing deployment archive {}", deploymentArchive.getName());
		status.setCurrentStatus(DeploymentStatus.IN_PROGRESS);
		deploymentStatusRepo.save(status);
		log.debug("Setting deployment task status to {}", DeploymentStatus.IN_PROGRESS);

		try{
			var deploymentPackageDir = PackageUtil.unpackZipArchive(deploymentArchive.toPath());
		} catch(IOException ex) {
			log.error("Failed to unpack distribution archive", ex);
		}

	}

	public void init(Application app) {
		log.debug("Initializing deployment task with app id {}", app.getId());
		status = deploymentStatusRepo.save(new AppDeploymentStatus(deploymentArchive.getParentFile().getName(), app));
		log.info("Initialized deployment task {}", deploymentArchive.getParentFile().getName());
	}

}
