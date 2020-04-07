/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage;

import java.time.ZoneId;

/**
 * Constants used across application.
 *
 * @author Vladimir Djurovic
 */
public final class BackstageConstants {

	private BackstageConstants() {
		// private constructor to prevent instantiation
	}

	public static final String DEPLOYMENT_CONTENT_TYPE = "application/zip";

	/**
	 * HTTP header containing URL of deployment status.
	 */
	public static final String DEPLOYMENT_STATUS_HEADER = "X-Deployment-Status";

	/**
	 * UTC timezone ID.
	 */
	public static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");

	/**
	 * UTC time zone ID as a string.
	 */
	public static final String UTC_ZONE_ID_STRING = "UTC";
}
