/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.content;

import co.bitshifted.xapps.backstage.model.*;
import co.bitshifted.xapps.backstage.test.TestConfig;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/**
 * @author Vladimir Djurovic
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
public class FileSystemMappingTest {

	private static final String MOCK_JDK_FILE_NAME = "openjdk-hotspot-11-linux-x64";

	@Autowired
	private ContentMapping contentMapping;

	@Before
	public void setup() throws Exception {
		var jdkStorageUri = contentMapping.getJdkStorageUri();
		var testFile = getClass().getResource("/" + MOCK_JDK_FILE_NAME).toURI();
		FileUtils.copyToDirectory(new File(testFile), Path.of(jdkStorageUri).toFile());
	}

	@After
	public void cleanup() throws Exception {
		var testFile = Path.of(contentMapping.getJdkStorageUri()).resolve(MOCK_JDK_FILE_NAME);
		Files.deleteIfExists(testFile);
	}

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

	@Test
	public void testJdkVersionFetch() {
		var uri = contentMapping.getJdkLocation(JdkProvider.OPENJDK, JvmImplementation.HOTSPOT,
				JdkVersion.JDK_11, OS.LINUX, CpuArch.X_64);
		assertTrue(Files.exists(Path.of(uri)));
	}
}
