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

	/**
	 * File name of update archive for JRE modules
	 */
	public static final String MODULES_UPDATE_FILE_NAME = "modules.zip";

	public static final String MODULES_UPDATE_ZSYNC_FILE_NAME = MODULES_UPDATE_FILE_NAME + ".zsync";

	/**
	 * File name of update archive for application content.
	 */
	public static final String CONTENT_UPDATE_FILE_NAME = "contents.zip";

	public static final String CONTENT_UPDATE_ZSYNC_FILE_NAME = CONTENT_UPDATE_FILE_NAME + ".zsync";

	/**
	 * Name of update information file.
	 */
	public static final String UPDATE_INFO_FILE_NAME = "update-info.xml";

	/**
	 * MIME type of Zsync control file
	 */
	public static final String ZSYNC_MIME_TYPE = "application/x-zsync ";

	public static final String IGNITE_CONFIG_FILE_NAME = "ignite-config.xml";

	public static final String LAUNCHER_CONFIG_FILE_NAME = "application.xml";

	public static final String LAUNCHER_FILE_NAME_MAC = "launchcode-mac-x64";
	public static final String LAUNCHER_FILE_NAME_WIN_64 = "launchcode-windows-x64";

	/**
	 * Comment to set on executable files when they are added to .zip archives.
	 */
	public static final String ZIP_ENTRY_EXEC_COMMENT = "exec:true";

	public static final String DEPLOY_PKG_MODULES_DIR_NAME = "modules";
	public static final String DEPLOY_PKG_CLASSPATH_DIR_NAME = "classpath";
	public static final String JDK_JMODS_DIR_NAME = "jmods";
}
