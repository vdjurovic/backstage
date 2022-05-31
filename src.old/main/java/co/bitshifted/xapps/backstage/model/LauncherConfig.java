/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.model;

import lombok.Data;

import javax.xml.bind.annotation.*;

/**
 * XML mapping for launcher configuration.
 *
 * @author Vladimir Djurovic
 */
@Data
@XmlRootElement(name = "application")
@XmlAccessorType(XmlAccessType.FIELD)
public class LauncherConfig {
	@XmlAttribute
	private String version;
	@XmlAttribute(name = "release-number")
	private String releaseNumber;
	@XmlElement
	private Server server;
	@XmlElement
	private JvmConfig jvm;
}
