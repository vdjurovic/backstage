/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.dto;

import co.bitshifted.xapps.backstage.BackstageConstants;
import co.bitshifted.xapps.backstage.util.BackstageFunctions;
import lombok.Data;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Vladimir Djurovic
 */
@Data
public class UpdateInformation {
	private final String contentsUrl;
	private final String modulesUrl;

	public String asString(HttpServletRequest request) {
		var sb = new StringBuilder();
		sb.append(BackstageConstants.CONTENT_UPDATE_FILE_NAME)
				.append("->")
				.append(BackstageFunctions.generateServerUrl(request, contentsUrl))
				.append("\n");
		sb.append(BackstageConstants.MODULES_UPDATE_FILE_NAME)
				.append("->")
				.append(BackstageFunctions.generateServerUrl(request, modulesUrl));
		return sb.toString();
	}
}
