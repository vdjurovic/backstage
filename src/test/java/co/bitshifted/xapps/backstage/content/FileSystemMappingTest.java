/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.content;

import co.bitshifted.xapps.backstage.test.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/**
 * @author Vladimir Djurovic
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
public class FileSystemMappingTest {

	@Autowired
	private ContentMapping contentMapping;

	@Test
	public void workspaceLocationTest() {
		var out = contentMapping.getWorkspaceUri();
		var workDir = System.getProperty("user.dir");

		assertTrue(Files.exists(Path.of(out)));
		assertTrue(out.getPath().startsWith(workDir));
	}

	@Test
	public void jdkLocationTest() {
		var out = contentMapping.getJdkStorageUri();
		var workDir = System.getProperty("user.dir");

		assertTrue(Files.exists(Path.of(out)));
		assertTrue(out.getPath().startsWith(workDir));
	}
}
