/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.deploy;

import co.bitshifted.xapps.backstage.model.CpuArch;
import co.bitshifted.xapps.backstage.model.OS;
import lombok.Builder;
import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessorType;
import java.nio.file.Path;

/**
 * @author Vladimir Djurovic
 */
@Getter
@Builder
public class TargetDeploymentInfo {
	private final DeploymentConfig deploymentConfig;
	private final Path deploymentPackageDir;
	private final OS targetOs;
	private final CpuArch targetCpuArch;
	private final XmlProcessor xmlProcessor;

}
