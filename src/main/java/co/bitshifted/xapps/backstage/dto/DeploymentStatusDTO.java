/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.dto;

import co.bitshifted.xapps.backstage.enums.DeploymentStatus;
import lombok.Data;

/**
 * @author Vladimir Djurovic
 */
@Data
public class DeploymentStatusDTO {
	private DeploymentStatus currentStatus;
	private String details;
}
