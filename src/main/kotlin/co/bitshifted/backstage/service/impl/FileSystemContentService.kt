/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.backstage.service.impl

import co.bitshifted.backstage.service.ContentService
import co.bitshifted.backstage.util.logger
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.digest.MessageDigestAlgorithms
import org.slf4j.Logger
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
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream


@Service("fileSystemContentService")
class FileSystemContentService(
    @Value("\${content.storage.location}") val contentStorageLocation : String
) : ContentService {

    val logger = logger(this)
    val digester = DigestUtils(MessageDigestAlgorithms.SHA3_256)

    override fun save(input: InputStream): URI {
        logger.debug("Saving content")
        val bytes = ByteArrayOutputStream()
        input.copyTo(bytes)
        val hash = digester.digestAsHex(bytes.toByteArray())
        // construct storage path
        val level1 = hash.substring(0, 2)
        val level2 = hash.substring(2, 4)
        val level3 = hash.substring(4, 6)
        val targetDir = Path.of(contentStorageLocation, level1, level2, level3).createDirectories()
        val targetFile = Path.of(targetDir.toFile().absolutePath, hash)

        Files.copy(ByteArrayInputStream(bytes.toByteArray()), targetFile, StandardCopyOption.REPLACE_EXISTING)
        input.close()
        return targetFile.toUri()
    }

    override fun get(sha256: String): InputStream {
        val level1 = sha256.substring(0, 2)
        val level2 = sha256.substring(2, 4)
        val level3 = sha256.substring(4, 6)

        val target = Path.of(contentStorageLocation, level1, level2, level3, sha256)
        return target.inputStream()
    }

    override fun exists(sha256: String, size: Long): Boolean {
        val level1 = sha256.substring(0, 2)
        val level2 = sha256.substring(2, 4)
        val level3 = sha256.substring(4, 6)
        val target = Path.of(contentStorageLocation, level1, level2, level3, sha256)
        return (target.exists(LinkOption.NOFOLLOW_LINKS) && target.fileSize() == size)
    }
}