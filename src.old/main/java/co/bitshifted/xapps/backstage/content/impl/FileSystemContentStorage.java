/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.content.impl;

import co.bitshifted.xapps.backstage.BackstageConstants;
import co.bitshifted.xapps.backstage.content.ContentMapping;
import co.bitshifted.xapps.backstage.content.ContentStorage;
import co.bitshifted.xapps.backstage.exception.ContentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author Vladimir Djurovic
 */
@Component
@Slf4j
public class FileSystemContentStorage implements ContentStorage {

	@Autowired
	private ContentMapping contentMapping;
	private final SimpleDateFormat RELEASE_NUMBER_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");

	@Override
	public URI uploadDeploymentPackage(InputStream in, String applicationId) throws ContentException {
		var workspacePath = Path.of(contentMapping.getWorkspaceUri());
		var buildTimestamp = ZonedDateTime.now(BackstageConstants.UTC_ZONE_ID);
		var workDirectoryPath = workspacePath.resolve(applicationId + "_" +
				RELEASE_NUMBER_FORMAT.format(Date.from(buildTimestamp.toInstant())));
		log.debug("Working directory: {}", workDirectoryPath.toAbsolutePath().toString());

		var deploymentArchivePath = workDirectoryPath.resolve("package.zip");
		try {
			Files.createDirectories(workDirectoryPath);
			Files.copy(in, deploymentArchivePath, StandardCopyOption.REPLACE_EXISTING);
		} catch(IOException ex) {
			log.error("Failed to copy deployment package", ex);
			throw new ContentException("Failed to copy deployment package", ex);
		}

		return deploymentArchivePath.toUri();
	}
}
