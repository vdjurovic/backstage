/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.deploy;

import co.bitshifted.xapps.backstage.model.*;
import lombok.Data;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Vladimir Djurovic
 */
@Data
public class DeploymentConfig {
	private String appId;
	private String appName;
	private List<String> icons;
	private Path deploymentPackageDir;
	private JdkProvider jdkProvider;
	private JvmImplementation jvmImplementation;
	private JdkVersion jdkVersion;
	private OS os;
	private CpuArch cpuArch;
	private LauncherConfig launcherConfig;

	public String macAppBundleName() {
		return appName + ".app";
	}

	public List<String> findMacIcons() {
		return icons.stream().filter(ic -> ic.endsWith(".icns")).collect(Collectors.toList());
	}
}
