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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File

@Component
class BuildContext(
    @Autowired val launchCodeRunner: LaunchCodeRunner,
    @Autowired val packageToolsRunner: PackageToolsRunner) {


    companion object Util {
        val logger = logger(BuildContext.Util::class)
        fun runExternalProgram(cmdLine : List<String>, workingDirectory : File, environment : Map<String, String> = emptyMap()) {
            val pb = ProcessBuilder(*cmdLine.toTypedArray())
            logger.debug("Running command: {} in working directory {}", pb.command(), workingDirectory.absolutePath)
            pb.directory(workingDirectory)
            pb.environment().putAll(environment)
            val process = pb.start()
            if (process.waitFor() == 0) {
                logger.info(process.inputReader().use { it.readText() })
                logger.info("Command executed successfully")
            } else {
                logger.error("Error encountered while running command. Details:")
                logger.error(process.inputReader().use { it.readText() })
                logger.error(process.errorReader().use { it.readText() })
                throw DeploymentException("Failed to run command: $cmdLine")
            }
        }
    }
}