/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.exception;

import co.bitshifted.xapps.backstage.deploy.DeploymentConfig;

/**
 * @author Vladimir Djurovic
 */
public class DeploymentException extends Exception {

	public DeploymentException(String message) {
		super(message);
	}

	public DeploymentException(Throwable cause) {
		super(cause);
	}

	public DeploymentException(String message, Throwable cause) {
		super(message, cause);
	}
}
