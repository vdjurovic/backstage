/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.model;

public enum JdkProvider {
	OPENJDK ("openjdk");

	private String display;

	JdkProvider(String display) {
		this.display = display;
	}

	public String getDisplay() {
		return display;
	}
}
