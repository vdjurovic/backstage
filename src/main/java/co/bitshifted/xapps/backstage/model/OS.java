/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.model;

public enum OS {
	WINDOWS ("Windows", "win"),
	MAC_OS_X ("Mac OS X", "mac"),
	LINUX ("Linux", "linux");

	private String display;
	private String brief;

	OS(String display, String brief) {
		this.display = display;
		this.brief = brief;
	}

	public String getDisplay() {
		return display;
	}

	public String getBrief() {
		return brief;
	}

	public static OS fromShortString(String source) {
		switch (source){
			case "win":
				return OS.WINDOWS;
			case "mac":
				return OS.MAC_OS_X;
			case "linux":
				return OS.LINUX;
			default:
				throw new IllegalArgumentException("Invalid argument for OS type");
		}
	}
}
