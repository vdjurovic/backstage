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
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.Charset

@Component
class BuildContext(
    @Autowired val launchCodeRunner: LaunchCodeRunner,
    @Autowired val packageToolsRunner: PackageToolsRunner) {


    companion object Util {
        val logger = LoggerFactory.getLogger(Util::class.java)

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

        fun executeRemoteCommand(config : SSHConfig, cmd : String) {
            logger.debug("Command to execute: $cmd")
            val jsch = JSch()

            val session = jsch.getSession(config.username, config.host, config.port)
            session.setConfig("StrictHostKeyChecking", "no")
            session.setPassword(config.password)
            try {
                session.connect(config.connectTimeout)
                logger.info("SSH connection established")
                val channel = session.openChannel("exec")
                val errStream = ByteArrayOutputStream()
                channel.inputStream = null
                if (channel is ChannelExec) {
                    channel.setCommand(cmd)
                    channel.setErrStream(errStream)
                }
                channel.connect()
                logger.debug("Channel connected")
                val inputStream = channel.inputStream
                channel.connect()
                val byteObject = ByteArray(10240)
                val output = StringBuilder()
                while (true) {
                    while (inputStream.available() > 0) {
                        val readByte = inputStream.read(byteObject, 0, 1024)
                        if (readByte < 0) break
                        output.append(String(byteObject, 0, readByte))
                    }
                    if (channel.isClosed) break
                }
                output.append(errStream.toString(Charset.defaultCharset()))
                output.append("STDERR:").append("\n")
                logger.debug(output.toString())
                channel.disconnect()
            } catch (ex : Throwable) {
                logger.error("Failed to connect to server", ex)
                throw DeploymentException("Failed to run remote command: $cmd")
            }

        }
    }
}