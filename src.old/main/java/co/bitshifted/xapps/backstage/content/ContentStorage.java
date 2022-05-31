/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.content;

import co.bitshifted.xapps.backstage.exception.ContentException;

import java.io.InputStream;
import java.net.URI;

public interface ContentStorage {

	/**
	 * Receives deployment package content from {@code in} and saves it to appropriate
	 * location for processing
	 *
	 * @param in deploymant package content
	 * @param applicationId application ID
	 * @return URI of the saved package
	 */
	URI uploadDeploymentPackage(InputStream in, String applicationId) throws ContentException;

}
