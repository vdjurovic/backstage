/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.service.impl;

import co.bitshifted.xapps.backstage.BackstageConstants;
import co.bitshifted.xapps.backstage.dto.UpdateInformation;
import co.bitshifted.xapps.backstage.model.CpuArch;
import co.bitshifted.xapps.backstage.model.OS;
import co.bitshifted.xapps.backstage.repository.AppDeploymentRepository;
import co.bitshifted.xapps.backstage.service.UpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Vladimir Djurovic
 */
@Service
public class BasicUpdateService implements UpdateService {

	private static final String UPDATE_DOWNLOAD_ENDPOINT = "/update/download?appId=%s&release=%s&os=%s&cpu=%s&file=%s";

	@Autowired
	private AppDeploymentRepository appDeploymentRepository;

	@Override
	public boolean hasUpdateAvailable(String applicationId, String currentRelease) {
		var latest = appDeploymentRepository.findFirstByApplication_IdOrderByReleaseNumberDesc(applicationId);
		return !latest.getReleaseNumber().equals(currentRelease);
	}

	@Override
	public UpdateInformation getUpdateInformation(String applicationId, OS os, CpuArch cpuArch) {
		var latest = appDeploymentRepository.findFirstByApplication_IdOrderByReleaseNumberDesc(applicationId);
		var contents = String.format(UPDATE_DOWNLOAD_ENDPOINT, applicationId, latest.getReleaseNumber(), os.getBrief(), cpuArch.getDisplay(), BackstageConstants.CONTENT_UPDATE_ZSYNC_FILE_NAME);
		var modules = String.format(UPDATE_DOWNLOAD_ENDPOINT, applicationId, latest.getReleaseNumber(), os.getBrief(), cpuArch.getDisplay(), BackstageConstants.MODULES_UPDATE_ZSYNC_FILE_NAME);

		return new UpdateInformation(contents, modules);
	}
}
