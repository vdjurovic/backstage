/*
 *
 *  * Copyright (c) 2022-2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.backstage.service.impl

import co.bitshifted.appforge.common.model.CpuArch
import co.bitshifted.appforge.common.model.JavaVersion
import co.bitshifted.appforge.common.model.JvmVendor
import co.bitshifted.appforge.common.model.OperatingSystem
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

class FileSystemResourceMappingTest {

    @Test
    fun testGetJdkLocation() {
        val jdkRoot = Files.createTempDirectory("backstage-test")
        val jdkDir = Paths.get(jdkRoot.toAbsolutePath().toString(), JvmVendor.OPENJDK.code, JavaVersion.JAVA_11.display, OperatingSystem.LINUX.display, CpuArch.X64.display, "11.0.2")
        Files.createDirectories(jdkDir)
        val latestLink = jdkDir.parent.resolve("latest")
        val latest = Files.createSymbolicLink(latestLink, jdkDir)

        val launchcodeRoot = Files.createTempDirectory("backstage-test-launchcode")
        val syncroJarLocation = Files.createTempFile("backstage-syncro", ".jar")

        val mapping = FileSystemResourceMapping(jdkRoot.toAbsolutePath().toString(), launchcodeRoot.absolutePathString(), syncroJarLocation.absolutePathString())
        val result = mapping.getJdkLocation(JvmVendor.OPENJDK, JavaVersion.JAVA_11, OperatingSystem.LINUX, CpuArch.X64, "latest")
        Assertions.assertEquals(Path.of(result).toFile().absolutePath, latest.toFile().absolutePath)
        FileUtils.deleteDirectory(jdkRoot.toFile())
    }
}