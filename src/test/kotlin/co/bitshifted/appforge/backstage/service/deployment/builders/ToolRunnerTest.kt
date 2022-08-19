/*
 *
 *  * Copyright (c) 2022-2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.backstage.service.deployment.builders

import co.bitshifted.appforge.backstage.BackstageConstants
import co.bitshifted.appforge.backstage.deleteDirectory
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

class ToolRunnerTest {

    private lateinit var baseDir : Path

    @BeforeEach
    fun setup() {
        val baseDirPath = this.javaClass.getResource("/deployment-package")
        baseDir = Path.of(baseDirPath.toURI())
    }

    @Test
    fun getJdkModulesTest() {
        val runner = ToolsRunner(baseDir)
        val modules = runner.getJdkModules()
        assertTrue(modules.contains("java.base"))
        assertTrue(modules.contains("java.desktop"))
        assertFalse(modules.contains("foo"))
    }

    @Test
    fun createRuntimeImageTest() {
        val javaHome = System.getProperty("java.home")
        println("java home dir: $javaHome")
        val jmodsDir = Path.of(javaHome, BackstageConstants.JDK_JMODS_DIR_NAME)
        val appModsDir = baseDir.resolve(BackstageConstants.OUTPUT_MODULES_DIR)
        val runner = ToolsRunner(baseDir)
        val modules = runner.getJdkModules()
        val modulesPath = listOf(jmodsDir, appModsDir)
        val outputDir = Files.createTempDirectory("backstage-test")
        runner.createRuntimeImage(modules, modulesPath, outputDir.resolve("jre"))
        // verify that image was created
        assertTrue(outputDir.resolve("jre/bin/java").exists())
        assertTrue(outputDir.resolve("jre/lib/modules").exists())
        deleteDirectory(outputDir.toFile())
    }

}