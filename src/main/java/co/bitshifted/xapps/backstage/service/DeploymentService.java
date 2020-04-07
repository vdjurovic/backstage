/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.service;

import co.bitshifted.xapps.backstage.entity.AppDeploymentStatus;
import co.bitshifted.xapps.backstage.exception.DeploymentException;

import java.nio.file.Path;

public interface DeploymentService {


	/**
	 * Process deployment package by creating installation and update packages for all
	 * required target platforms.
	 *
	 * @param deploymentPath path to deployment package
	 * @param appId application ID
	 * @throws DeploymentException if an error occurs
	 */
	void processContent(Path deploymentPath, String appId) throws DeploymentException;

	/**
	 * Validate that deployment package is actually a .zip archive
	 *
	 * @param deploymentPath path to deployment package
	 * @return {@code true} if archive is .zip file, {@code false} otherwise
	 * @throws DeploymentException if an error occurs
	 */
	boolean validateDeploymentContent(Path deploymentPath) throws DeploymentException;

	AppDeploymentStatus getDeploymentStatus(String id);
}
