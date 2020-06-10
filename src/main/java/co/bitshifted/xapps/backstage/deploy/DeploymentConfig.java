/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.deploy;

import co.bitshifted.xapps.backstage.BackstageConstants;
import co.bitshifted.xapps.backstage.model.*;
import lombok.*;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Vladimir Djurovic
 */
@Getter
@Builder
public class DeploymentConfig {
	private final String appId;
	private final String appName;
	private final String appVersion;
	private final List<FileInfo> icons;
	private final FileInfo splashScreen;
	private final JdkProvider jdkProvider;
	private final JvmImplementation jvmImplementation;
	private final JdkVersion jdkVersion;
	private final LauncherConfig launcherConfig;

	public String macAppBundleName() {
		return appName + ".app";
	}

	public List<FileInfo> findMacIcons() {
		return icons.stream().filter(ic -> ic.getFileName().endsWith(".icns")).collect(Collectors.toList());
	}

	public String getExecutableFileName() {
		return appName.toLowerCase().replaceAll("\\s", "-");
	}
}
