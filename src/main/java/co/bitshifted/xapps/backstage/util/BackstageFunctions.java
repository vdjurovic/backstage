/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.util;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.regex.Pattern;

/**
 * @author Vladimir Djurovic
 */
public final class BackstageFunctions {

	private static final Pattern RELEASE_NUMBER_PATTERN = Pattern.compile("\\d{8}-\\d{6}-\\d{3}");

	private BackstageFunctions() {

	}

	/**
	 * Finds out release number from deployment work directory name.
	 *
	 * @param directory deplyoment work directory
	 * @return release number
	 */
	public static String getReleaseNumberFromDeploymentDir(File directory) {
		var name = directory.getName();
		var releaseNum = name.substring(name.lastIndexOf("_") + 1);
		var matcher = RELEASE_NUMBER_PATTERN.matcher(releaseNum);
		if(matcher.matches()) {
			return releaseNum;
		}
		throw new IllegalStateException("release number format mismatch: " + releaseNum);
	}

	public static String generateServerUrl(HttpServletRequest request, String path) {
		StringBuilder sb = new StringBuilder();
		sb.append(request.getScheme()).append("://");
		sb.append(request.getServerName());
		if (request.getServerPort() != 0) {
			sb.append(":").append(request.getServerPort());
		}
		sb.append(request.getContextPath());
		if(!path.startsWith("/")) {
			sb.append("/");
		}
		sb.append(path);
		return sb.toString();
	}

	public static String generateServerUrl(String base, String path) {
		var sb = new StringBuilder(base);
		if(!path.startsWith("/")) {
			sb.append("/");
		}
		sb.append(path);
		return sb.toString();
	}

}
