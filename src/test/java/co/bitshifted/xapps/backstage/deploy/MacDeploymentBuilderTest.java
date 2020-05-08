/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.deploy;

import co.bitshifted.xapps.backstage.content.ContentMapping;
import co.bitshifted.xapps.backstage.model.*;
import co.bitshifted.xapps.backstage.test.TestConfig;
import co.bitshifted.xapps.backstage.util.PackageUtil;
import org.apache.commons.io.FileUtils;
import org.junit.After;
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
import java.util.function.Function;

/**
 * @author Vladimir Djurovic
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
public class MacDeploymentBuilderTest {

	private static final String TEST_ARCHIVE_NAME = "7PjPKl4iLX7.zip";

	@Autowired
	private ContentMapping contentMapping;
	@Autowired
	private Function<DeploymentConfig, MacDeploymentBuilder> macDeploymentBuilderFactory;

	private Path deploymentWorkDir;
	private Path deploymentPackageDir;
	private MacDeploymentBuilder deploymentBuilder;
	private DeploymentConfig deploymentConfig;
	private Path jdkLinkPath;

	@Before
	public void setup() throws Exception {
		var workspace = contentMapping.getWorkspaceUri();
		deploymentWorkDir = Path.of(workspace).resolve("test");
		var archivePath = Path.of(ToolsRunnerTest.class.getResource("/deployment/7PjPKl4iLX7.zip").toURI());
		Files.createDirectory(deploymentWorkDir);
		var copyTarget = deploymentWorkDir.resolve(TEST_ARCHIVE_NAME);
		Files.copy(archivePath, copyTarget, StandardCopyOption.REPLACE_EXISTING);
		deploymentPackageDir = PackageUtil.unpackZipArchive(copyTarget);
		// copy dummy launcher
		var dummyLauncherPath = Path.of(getClass().getResource("/launchcode-mac-x64").toURI());
		var launcherTarget = Path.of(contentMapping.getLauncherStorageUri()).resolve("launchcode-mac-x64");
		Files.copy(dummyLauncherPath, launcherTarget, StandardCopyOption.REPLACE_EXISTING);

		deploymentConfig = new DeploymentConfig();
		deploymentConfig.setAppName("TestApp");
		deploymentConfig.setAppId("appid");
		deploymentConfig.setAppVersion("1.0.0");
		deploymentConfig.setIcons(List.of(
				new FileInfo("icon1.png","data/icons/icon1.png"),
				new FileInfo("winicon.ico","data/icons/winicon.ico"),
				new FileInfo("maicon.icns", "data/icons/maicon.icns")));
		deploymentConfig.setSplashScreen(new FileInfo("splash.png", "data/splash.png"));
		deploymentConfig.setDeploymentPackageDir(deploymentPackageDir);
		deploymentConfig.setJdkProvider(JdkProvider.OPENJDK);
		deploymentConfig.setJvmImplementation(JvmImplementation.HOTSPOT);
		deploymentConfig.setJdkVersion(JdkVersion.JDK_11);
		deploymentConfig.setOs(OS.LINUX);
		deploymentConfig.setCpuArch(CpuArch.X_64);

		var launcherConfig = new LauncherConfig();
		launcherConfig.setVersion("1.0.0");
		var jvm = new JvmConfig();
		jvm.setMainClass("my.MainClass");
		jvm.setJvmDir("/some/jvm/dir");
		launcherConfig.setJvm(jvm);
		deploymentConfig.setLauncherConfig(launcherConfig);

		deploymentBuilder = macDeploymentBuilderFactory.apply(deploymentConfig);
		// create link to system JDK
		var javaHome = System.getProperty("java.home");
		var jdkStorageDirPath = Path.of(contentMapping.getJdkStorageUri());
		jdkLinkPath = jdkStorageDirPath.resolve("openjdk-hotspot-11-linux-x64");
		Files.createSymbolicLink(jdkLinkPath, Path.of(javaHome));
	}

	@After
	public void cleanup() throws Exception {
		Files.deleteIfExists(jdkLinkPath);
		FileUtils.deleteDirectory(deploymentWorkDir.toFile());
	}


	@Test
	public void testMacDeploymentBuild() throws Exception {
		deploymentBuilder.createDeployment();
	}
}
