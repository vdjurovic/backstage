/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.service.impl;

import co.bitshifted.xapps.backstage.BackstageConstants;
import co.bitshifted.xapps.backstage.content.ContentMapping;
import co.bitshifted.xapps.backstage.dto.UpdateInformation;
import co.bitshifted.xapps.backstage.exception.ContentException;
import co.bitshifted.xapps.backstage.model.CpuArch;
import co.bitshifted.xapps.backstage.model.DownloadInfo;
import co.bitshifted.xapps.backstage.model.OS;
import co.bitshifted.xapps.backstage.repository.AppDeploymentRepository;
import co.bitshifted.xapps.backstage.service.UpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Vladimir Djurovic
 */
@Service
@Slf4j
public class BasicUpdateService implements UpdateService {

	@Autowired
	private AppDeploymentRepository appDeploymentRepository;
	@Autowired
	private ContentMapping contentMapping;

	@Override
	public boolean hasUpdateAvailable(String applicationId, String currentRelease) {
		var latest = appDeploymentRepository.findFirstByApplication_IdOrderByReleaseNumberDesc(applicationId);
		return !latest.getReleaseNumber().equals(currentRelease);
	}

	@Override
	public UpdateInformation getUpdateInformation(String applicationId, OS os, CpuArch cpuArch) throws ContentException {
		var latest = appDeploymentRepository.findFirstByApplication_IdOrderByReleaseNumberDesc(applicationId);
		var updateInfoFile = Path.of(contentMapping.getUpdatesParentLocation(applicationId, latest.getReleaseNumber(), os, cpuArch)).resolve(BackstageConstants.UPDATE_INFO_FILE_NAME);

		try {
			var unmarshaller = JAXBContext.newInstance(UpdateInformation.class).createUnmarshaller();
			return (UpdateInformation) unmarshaller.unmarshal(updateInfoFile.toFile());
		} catch(JAXBException ex) {
			throw new ContentException(ex);
		}

	}

	@Override
	public DownloadInfo getUpdateFile(String fileName, String applicationId, String release, OS os, CpuArch cpuArch) throws ContentException {
		var fileUri = contentMapping.getUpdateFile(fileName, applicationId, release, os, cpuArch);
		try {
			var size = Files.size(Path.of(fileUri));
			return new DownloadInfo(fileUri, size);
		} catch(IOException ex) {
			log.error("Failed to get download info for file {}, applicationId={}, relese={}", fileName, applicationId, release);
			throw new ContentException(ex);
		}
	}
}
