/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.model;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Vladimir Djurovic
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class JvmConfig {
	@XmlElement(name = "jvm-dir")
	private String jvmDir;
	@XmlElement(name = "module-path")
	private String modulePath;
	@XmlElement
	private String module;
	@XmlElement(name = "add-modules")
	private String addModules;
	@XmlElement
	private String classpath;
	@XmlElement(name = "jvm-options")
	private String jvmOptions;
	@XmlElement(name = "jvm-properties")
	private String jvmProperties;
	@XmlElement(name = "main-class")
	private String mainClass;
	@XmlElement(name = "args")
	private String arguments;
	private String jar;
	@XmlElement(name = "splash-screen")
	private String splashScreen;
}
