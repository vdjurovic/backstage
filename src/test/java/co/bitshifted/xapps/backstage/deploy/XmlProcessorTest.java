/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.deploy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Path;
import static org.junit.Assert.assertEquals;

/**
 * @author Vladimir Djurovic
 */
public class XmlProcessorTest {

	private XmlProcessor xmlProcessor;

	@Before
	public void setup() throws Exception {
		var testFilePath = Path.of(getClass().getResource("/xml/ignite-config.xml").toURI());
		xmlProcessor = new XmlProcessor(testFilePath);
	}

	@Test
	public void mainArtifactFetchTest() throws Exception {
		var out = xmlProcessor.findMainArtifact();
		assertEquals(out.get("scope"), "MODULEPATH");
		assertEquals(out.get("path"), "launchtest-1.0-SNAPSHOT.jar");
	}
}
