/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.service;

import co.bitshifted.xapps.backstage.dto.UpdateInformation;
import co.bitshifted.xapps.backstage.model.CpuArch;
import co.bitshifted.xapps.backstage.model.OS;

public interface UpdateService {

	/**
	 * Check if there is an updates available for application relative to specified release. If
	 * there is new release, thei method will return {@code true}, otherwise {@code false}.
	 *
	 * @param applicationId application ID
	 * @param currentRelease current application release on client
	 * @return {@code true} if there is an update available, {@code false} otherwise
	 */
	boolean hasUpdateAvailable(String applicationId, String currentRelease);

	/**
	 * Returns data needed to perform application update.
	 *
	 * @param applicationId
	 * @param os
	 * @param cpuArch
	 * @return
	 */
	UpdateInformation getUpdateInformation(String applicationId, OS os, CpuArch cpuArch);

}
