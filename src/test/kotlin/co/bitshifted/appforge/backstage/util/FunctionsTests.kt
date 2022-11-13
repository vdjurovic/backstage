/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.backstage.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class FunctionsTests {

    @Test
    fun testExtractGzip() {
        val archivePathUrl = this.javaClass.getResource("/testarchive.tar.gz")
        val archivePath = Path.of(archivePathUrl.toURI())
        val tmpDir = Files.createTempDirectory("tar_gz_unpack_test_")
        val out = extractTarGzArchive(archivePath, tmpDir)
        val expected = tmpDir.resolve("archive")
        Assertions.assertEquals(expected.absolutePathString(), out?.absolutePathString())
    }

    @Test
    fun testExtractZip() {
        val tmpDir = Files.createTempDirectory("zip_unpack_test_")
        val path = "/home/vlada/local/work/bitshift/AppForge/jdk-root/adoptium/17/windows/OpenJDK17U-jdk_x64_windows_hotspot_17.0.3_7.zip"
        extractZipArchive(Path.of(path), tmpDir)
    }
}