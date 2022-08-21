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

import co.bitshifted.appforge.backstage.service.ContentService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class FileSystemContentServiceTest {

    var contentStorageLocation : String = ""
    private lateinit var contentService : ContentService

    @BeforeEach
    fun setup() {
       contentStorageLocation = Files.createTempDirectory("backstage").toFile().absolutePath
        contentService = FileSystemContentService(contentStorageLocation)
    }

    @AfterEach
    fun cleanup() {
        val file = File(contentStorageLocation)
        file.delete()
    }

    @Test
    fun saveContentSuccess() {
        val fileUrl = javaClass.getResource("/content/content.txt")
        val file = File(fileUrl.toURI())
        val out = contentService.save(file.inputStream())
        assertEquals("290f493c44f5d63d06b374d0a5abd292fae38b92cab2fae5efefe1b0e9347f56", out)
        val content = String(contentService.get(out).input.readAllBytes())
        assertEquals("some content", content)
    }

    @Test
    fun saveExecutableFileSuccess() {
        val fileUrl = javaClass.getResource("/content/executable.sh")
        val file = File(fileUrl.toURI())
        val hash = contentService.save(file.inputStream(), true)
        assertEquals("4ceed03c439de7d288a1542737a152cafd8f8be1d6fdc9e75fd7ddba77986c20", hash)
        // retrieve file
        val out = contentService.get(hash)
        assertEquals(out.hash, hash)
        assertEquals(out.executable, true)
        assertEquals(out.size, 328)
    }

    @Test
    fun getContentSuccess() {
        val fileStream = javaClass.getResourceAsStream("/content/content.txt")

        val out = contentService.save(fileStream)
        //val hash = out.path.substring(out.path.lastIndexOf("/") + 1)
        val content = contentService.get(out)
        val text = String(content.input.readAllBytes(), StandardCharsets.UTF_8)
        assertEquals("some content", text)
    }

    @Test
    fun confirmContentExists() {
//        val service = FileSystemContentService(contentStorageLocation)
        val fileStream = javaClass.getResourceAsStream("/content/content.txt")
        val out = contentService.save(fileStream)

        val result = contentService.exists(out, 12)
        assertTrue(result)
    }
}