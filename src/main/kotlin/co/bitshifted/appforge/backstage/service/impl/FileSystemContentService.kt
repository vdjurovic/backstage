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

import co.bitshifted.appforge.backstage.model.ContentItem
import co.bitshifted.appforge.backstage.service.ContentService
import co.bitshifted.appforge.backstage.util.logger
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.digest.MessageDigestAlgorithms
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.*


@Service("fileSystemContentService")
class FileSystemContentService(
    @Value("\${content.storage.location}") val contentStorageLocation : String
) : ContentService {

    val logger = logger(this)
    val digester = DigestUtils(MessageDigestAlgorithms.SHA_256)

    override fun save(input: InputStream): String {
        logger.debug("Saving content")
        val bytes = ByteArrayOutputStream()
        val count = input.copyTo(bytes)
        logger.debug("Copied {} bytes", count)
        val hash = digester.digestAsHex(bytes.toByteArray())
        logger.debug("File hash: {}", hash)
        val targetFile = getPathFromHash(hash)

        Files.copy(ByteArrayInputStream(bytes.toByteArray()), targetFile, StandardCopyOption.REPLACE_EXISTING)
        input.close()
        return hash
    }

    override fun save(input: InputStream, executable : Boolean) : String {
        val hash = save(input)

        if(executable) {
            val path = getPathFromHash(hash)
            logger.debug("Setting executable flag for {}", path.absolutePathString())
            val result = path.toFile().setExecutable(true)
            if(!result) {
                logger.error("Failed to set executable flag for {}", path.absolutePathString())
            }
        }
        return hash
    }

    override fun get(sha256: String): ContentItem {
        val level1 = sha256.substring(0, 2)
        val level2 = sha256.substring(2, 4)
        val level3 = sha256.substring(4, 6)

        var target = Path.of(contentStorageLocation, level1, level2, level3, sha256)
        var executable = Files.isExecutable(target)
        return ContentItem(target.inputStream(), sha256, target.fileSize(), executable)
    }

    override fun exists(sha256: String, size: Long): Boolean {
        val level1 = sha256.substring(0, 2)
        val level2 = sha256.substring(2, 4)
        val level3 = sha256.substring(4, 6)
        val target = Path.of(contentStorageLocation, level1, level2, level3, sha256)
        logger.debug("Checking content storage location: {}", contentStorageLocation)
        return (target.exists(LinkOption.NOFOLLOW_LINKS) && target.fileSize() == size)
    }

    private fun getPathFromHash(hash : String) : Path {
        val level1 = hash.substring(0, 2)
        val level2 = hash.substring(2, 4)
        val level3 = hash.substring(4, 6)
        val targetDir = Path.of(contentStorageLocation, level1, level2, level3).createDirectories()
        return Path.of(targetDir.toFile().absolutePath, hash)
    }
}