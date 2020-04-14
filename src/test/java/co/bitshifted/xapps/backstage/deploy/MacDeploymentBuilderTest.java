/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.deploy;

import co.bitshifted.xapps.backstage.content.ContentMapping;
import co.bitshifted.xapps.backstage.test.TestConfig;
import co.bitshifted.xapps.backstage.util.PackageUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * @author Vladimir Djurovic
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
public class MacDeploymentBuilderTest {

	private static final String TEST_ARCHIVE_NAME = "7PjPKl4iLX7.zip";

	@Autowired
	private ContentMapping contentMapping;
	private Path deploymentWorkDir;
	private Path deploymentPackageDir;
	private MacDeploymentBuilder deploymentBuilder;
	private DeploymentConfig deploymentConfig;

	@Before
	public void setup() throws Exception {
		var workspace = contentMapping.getWorkspaceUri();
		deploymentWorkDir = Path.of(workspace).resolve("test");
		var archivePath = Path.of(ToolsRunnerTest.class.getResource("/deployment/7PjPKl4iLX7.zip").toURI());
		Files.createDirectory(deploymentWorkDir);
		var copyTarget = deploymentWorkDir.resolve(TEST_ARCHIVE_NAME);
		Files.copy(archivePath, copyTarget, StandardCopyOption.REPLACE_EXISTING);
		deploymentPackageDir = PackageUtil.unpackZipArchive(copyTarget);

		deploymentConfig = new DeploymentConfig();
		deploymentConfig.setAppName("TestApp");
		deploymentConfig.setIcons(List.of("data/icons/icon1.png", "data/icons/winicon.ico", "data/icons/maicon.icns"));
		deploymentBuilder = new MacDeploymentBuilder(deploymentPackageDir, deploymentConfig);
	}


	@Test
	public void testMacDeploymentBuild() throws Exception {
		deploymentBuilder.createDeployment();
	}
}
