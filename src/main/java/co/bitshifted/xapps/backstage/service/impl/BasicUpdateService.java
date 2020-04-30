/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.service.impl;

import co.bitshifted.xapps.backstage.repository.AppDeploymentRepository;
import co.bitshifted.xapps.backstage.service.UpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Vladimir Djurovic
 */
@Service
public class BasicUpdateService implements UpdateService {

	@Autowired
	private AppDeploymentRepository appDeploymentRepository;

	@Override
	public boolean hasUpdateAvailable(String applicationId, String currentRelease) {
		var latest = appDeploymentRepository.findFirstByApplication_IdOrderByReleaseNumberDesc(applicationId);
		return !latest.getReleaseNumber().equals(currentRelease);
	}
}
