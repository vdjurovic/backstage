/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.deploy;

import co.bitshifted.xapps.backstage.model.*;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;

import static org.junit.Assert.*;

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
		assertEquals(out.get("path"), "modules/launchtest-1.0-SNAPSHOT.jar");
	}

	@Test
	public void testLauncherConfigXml() throws Exception {
		var launcherConfig = new LauncherConfig();
		var server = new Server();
		server.setBaseUrl("http://localhost:8080");
		launcherConfig.setServer(server);

		var jvm = new JvmConfig();
		jvm.setJvmDir("some/dir");
		jvm.setJvmOptions("-Xms=20m");
		jvm.setJvmProperties("-Dsome.option=value");
		jvm.setModule("my.module");
		jvm.setMainClass("com.my.MainClass");
		launcherConfig.setJvm(jvm);

		var out = xmlProcessor.createLauncherConfigXml(launcherConfig);
		assertNotNull(out);
		assertTrue(out.contains("<jvm-dir>some/dir</jvm-dir>"));
		assertTrue(out.contains("<module>my.module</module>"));
		assertTrue(out.contains("<server base-url=\"http://localhost:8080\"/>"));
	}

	@Test
	public void testDeploymentConfig() throws Exception {
		var config = xmlProcessor.getDeploymentConfig();

		var icon1 = new FileInfo("icon1.png", "data/icons/icon1.png");
		var icon2 = new FileInfo("winicon.ico", "data/icons/winicon.ico");

		assertNotNull(config);
		assertEquals("7PjPKl4iLX7", config.getAppId());
		assertEquals("LaunchTest", config.getAppName());
		assertEquals(3, config.getIcons().size());
		assertTrue(config.getIcons().contains(icon1));
		assertTrue(config.getIcons().contains(icon2));
		assertEquals(JdkProvider.OPENJDK, config.getJdkProvider());
		assertEquals(JvmImplementation.HOTSPOT, config.getJvmImplementation());
		assertEquals(JdkVersion.JDK_11, config.getJdkVersion());

		var launcherConfig = config.getLauncherConfig();
		assertEquals("co.bitshifted.xapps.ignite.Ignite", launcherConfig.getJvm().getMainClass());
		assertEquals("launchtest", launcherConfig.getJvm().getModule());
		assertEquals("-Xms=32m -Xmx=128m", launcherConfig.getJvm().getJvmOptions());
		assertEquals("arg1 arg2", launcherConfig.getJvm().getArguments());
		assertEquals("splash.png", launcherConfig.getJvm().getSplashScreen());

		var serverConfig = config.getLauncherConfig().getServer();
		assertNotNull(serverConfig);
		assertEquals("http://localhost:8080", serverConfig.getBaseUrl());
	}
}
