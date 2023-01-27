/*
 *
 *  * Copyright (c) 2023  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.backstage.service.deployment.tools

import co.bitshifted.appforge.backstage.exception.DeploymentException
import co.bitshifted.appforge.backstage.util.logger
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.io.File
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.absolutePathString

@Component
@ConditionalOnProperty(value = ["external.tools.mode"], havingValue = "local", matchIfMissing = false)
class LocalLaunchCodeRunner : LaunchCodeRunner {

    private val logger = logger(this)

    @Value("\${launchcode.source.root}")
    private lateinit var launchCodeSourceLocation : String

    override fun copySourceCode(targetDir: Path) {
        val sourceDir = File(launchCodeSourceLocation)
        logger.debug("Launchcode source location: {}", sourceDir)
        logger.debug("Copying Launchcode source to {}", targetDir.absolutePathString())
        FileUtils.copyDirectory(sourceDir, targetDir.toFile())
    }

    override fun buildLaunchers(targetDir: Path) {
        val pb = ProcessBuilder("make", "all")
        pb.directory(File(targetDir.absolutePathString()))
        val path = System.getenv("PATH")
        val userHome = System.getProperty("user.home")
        pb.environment().put("PATH", "/usr/bin:/usr/local/bin:/usr/local/go/bin:$userHome/go/bin:/bin:/sbin")
        pb.environment().put("PWD", targetDir.absolutePathString())

        println("PATH: $path")
        val process = pb.start()
        if (process.waitFor() == 0) {
            logger.info(process.inputReader().use { it.readText() })
            logger.info("Launchers created successfully")
        } else {
            logger.error("Error encountered while building launchers. Details:")
            logger.error(process.inputReader().use { it.readText() })
            logger.error(process.errorReader().use { it.readText() })
            throw DeploymentException("Failed to build launchers")
        }
    }

}