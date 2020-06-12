/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.deploy;

import co.bitshifted.xapps.backstage.util.PackageUtil;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Vladimir Djurovic
 */
public class ToolsRunnerTest {

	private static final String TEST_ARCHIVE_NAME = "7PjPKl4iLX7.zip";

	private static Path deploymentPackageDir;

	@BeforeClass
	public static void setupClass() throws Exception {
		var archivePath = Path.of(ToolsRunnerTest.class.getResource("/deployment/7PjPKl4iLX7.zip").toURI());
		var workDir = System.getProperty("user.dir");
		var unpackDir = Path.of(workDir, "target", "deploy_test");
		Files.createDirectory(unpackDir);
		var copyTarget = unpackDir.resolve(TEST_ARCHIVE_NAME);
		Files.copy(archivePath, copyTarget, StandardCopyOption.REPLACE_EXISTING);
		deploymentPackageDir = PackageUtil.unpackZipArchive(copyTarget);
	}

	@AfterClass
	public static void cleanupClass() throws Exception{
		FileUtils.deleteDirectory(deploymentPackageDir.getParent().toFile());
	}

	@Test
	@Ignore
	public void getListOfAppModules() throws Exception {
		var runner = new ToolsRunner();
		runner.init();
		var modules = runner.getApplicationModules(deploymentPackageDir);

		assertEquals(5, modules.size());
	}

	@Test
	@Ignore
	public void createRuntimeImageTest() throws Exception {
		var runner = new ToolsRunner();
		runner.init();
		var modules = runner.getApplicationModules(deploymentPackageDir);
		var jrePath = deploymentPackageDir.getParent().resolve("jre");
		runner.createRuntimeImage(modules, List.of(deploymentPackageDir.resolve("modules")), jrePath);

		assertTrue(Files.exists(jrePath));
	}
}
