/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.content.impl;

import co.bitshifted.xapps.backstage.content.ContentMapping;
import co.bitshifted.xapps.backstage.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Vladimir Djurovic
 */
@Component("fileSystemContentMapping")
@Slf4j
public class FileSystemContentMapping implements ContentMapping {

	private static final String JDK_LOCATION_FORMAT = "{provider}-{impl}-{version}-{os}-{cpu}";

	private final Path contentRoot;
	private final Path workspace;
	private final Path jdkStorageDirectory;
	private final Path launcherDirectory;
	private final Path updatesDirectory;

	public FileSystemContentMapping(@Value("${content.root.location}") String rootLocation) {
		this.contentRoot = Path.of(rootLocation);
		workspace = contentRoot.resolve("workspace");
		jdkStorageDirectory = contentRoot.resolve("jdk");
		launcherDirectory = contentRoot.resolve("launchers");
		updatesDirectory = contentRoot.resolve("download").resolve("updates");
	}

	@PostConstruct
	public void initialize() {
		try {
			Files.createDirectories(workspace);
			Files.createDirectories(jdkStorageDirectory);
			Files.createDirectories(launcherDirectory);
			Files.createDirectories(updatesDirectory);
		} catch (IOException ex) {
			log.error("Failed to create content directories", ex);
		}

	}

	@Override
	public URI getWorkspaceUri() {
		return workspace.toUri();
	}

	@Override
	public URI getJdkStorageUri() {
		return jdkStorageDirectory.toUri();
	}

	@Override
	public URI getLauncherStorageUri() {
		return launcherDirectory.toUri();
	}

	@Override
	public URI getUpdatesDownloadLocation() {
		return updatesDirectory.toUri();
	}

	@Override
	public URI getJdkLocation(JdkProvider provider, JvmImplementation jvmImplementation, JdkVersion version, OS os, CpuArch cpuArch) {
		String name = JDK_LOCATION_FORMAT.replace("{provider}", provider.getDisplay())
				.replace("{impl}", jvmImplementation.getDisplay())
				.replace("{version}", version.getText())
				.replace("{os}", os.getBrief())
				.replace("{cpu}", cpuArch.getDisplay());
		log.debug("JDK file name: {}", name);
		return jdkStorageDirectory.resolve(name).toUri();
	}

	@Override
	public URI getUpdatesParentLocation(String applicationId, String releaseNumber, OS os, CpuArch cpuArch) {
		var updateBaseDir = Path.of(updatesDirectory.toString(), applicationId, releaseNumber, os.getBrief(), cpuArch.getDisplay());
		return updateBaseDir.toUri();
	}
}
