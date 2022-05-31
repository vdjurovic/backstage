/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.util;

import co.bitshifted.xapps.backstage.deploy.DeploymentProcessTask;
import co.bitshifted.xapps.backstage.model.DeploymentStatus;
import co.bitshifted.xapps.backstage.repository.AppDeploymentStatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Vladimir Djurovic
 */
@Component
@Slf4j
public class DeploymentExecutorService extends ThreadPoolExecutor {

	private static final int CORE_POOL_SIZE;
	private static final int MAX_POOL_SIZE;
	private static final int KEEP_ALIVE_MS = 5000;
	private static final Map<Future, String> TASK_MAP;

	static {
		CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
		MAX_POOL_SIZE = 2 * CORE_POOL_SIZE;
		TASK_MAP = new HashMap<>();
	}

	@Autowired
	private AppDeploymentStatusRepository deploymentStatusRepo;

	public DeploymentExecutorService() {
		super(CORE_POOL_SIZE,MAX_POOL_SIZE, KEEP_ALIVE_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(MAX_POOL_SIZE));
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		if(TASK_MAP.containsKey(r)) {
			var deploymentId = TASK_MAP.get(r);
			var statusOpt = deploymentStatusRepo.findById(deploymentId);
			if(statusOpt.isEmpty()) {
				log.warn("Could not find deployment status with ID {}", deploymentId);
			} else {
				var currentStatus = statusOpt.get();
				var newStatus = (t != null) ? DeploymentStatus.FAILED : DeploymentStatus.SUCCESS;
				currentStatus.setCurrentStatus(newStatus);
				if(newStatus == DeploymentStatus.FAILED) {
					currentStatus.setDetails(t.getCause().getMessage());
				}
				deploymentStatusRepo.save(currentStatus);
				log.debug("Updated status for deployment ID {} to {}", deploymentId, newStatus.name());
			}
		}
	}


	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		if(TASK_MAP.containsKey(r)) {
			var deploymentId = TASK_MAP.get(r);
			var statusOpt = deploymentStatusRepo.findById(deploymentId);
			if(statusOpt.isEmpty()) {
				log.warn("Could not find deployment status with ID {}", deploymentId);
			} else {
				var status = statusOpt.get();
				status.setCurrentStatus(DeploymentStatus.IN_PROGRESS);
				deploymentStatusRepo.save(status);
				log.debug("Updated status for deployment ID {} to IN_PROGRESS", deploymentId);
			}
		}
	}

	@Override
	public Future<?> submit(Runnable task) {
		var ftask = super.submit(task);
		if(task instanceof DeploymentProcessTask) {
			var t = (DeploymentProcessTask)task;
			TASK_MAP.put(ftask, t.getDeploymentId());
		}
		return ftask;
	}
}
