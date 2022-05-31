/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.service.impl;

import co.bitshifted.xapps.backstage.entity.AppDeploymentStatus;
import co.bitshifted.xapps.backstage.entity.Application;
import co.bitshifted.xapps.backstage.exception.DeploymentException;
import co.bitshifted.xapps.backstage.repository.AppDeploymentStatusRepository;
import co.bitshifted.xapps.backstage.repository.ApplicationRepository;
import co.bitshifted.xapps.backstage.deploy.DeploymentProcessTask;
import co.bitshifted.xapps.backstage.service.DeploymentService;
import co.bitshifted.xapps.backstage.util.DeploymentExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * @author Vladimir Djurovic
 */
@Service
@Slf4j
public class BasicDeploymentService implements DeploymentService {

	@Autowired
	private DeploymentExecutorService executorService;
	@Autowired
	private Function<File, DeploymentProcessTask> deploymentTaskFactory;
	@Autowired
	private ApplicationRepository appRepository;
	@Autowired
	private AppDeploymentStatusRepository deploymentStatusRepository;


	@Override
	public void processContent(Path deploymentPath, String appId) throws DeploymentException {
		Application app = appRepository.findById(appId).get();
		var deploymentId = deploymentPath.getParent().toFile().getName();
		var status = new AppDeploymentStatus(deploymentId, app);
		deploymentStatusRepository.save(status);
		log.debug("Created deployment ID {}", deploymentId);
		DeploymentProcessTask task = deploymentTaskFactory.apply(deploymentPath.toFile());
		task.init(deploymentId);
		executorService.submit(task);
		log.info("Deployment task submitted. Deployment archive: {}", deploymentPath.getFileName().toString());
	}

	@Override
	public boolean validateDeploymentContent(Path deploymentPath) throws DeploymentException {
		try {
			RandomAccessFile raf = new RandomAccessFile(deploymentPath.toFile(), "r");
			long n = raf.readInt();
			raf.close();
			if (n != 0x504B0304 && n != 0x504B0506 && n != 0x504B0708) {
				return false;
			}
		} catch (IOException ex) {
			throw new DeploymentException(ex.getMessage());
		}
		return true;
	}

	@Override
	public AppDeploymentStatus getDeploymentStatus(String id) {
		return deploymentStatusRepository.findById(id).get();
	}
}
