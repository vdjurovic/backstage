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

import co.bitshifted.appforge.backstage.BackstageConstants.sshDefaultPass
import co.bitshifted.appforge.backstage.BackstageConstants.sshDefaultTimeout
import co.bitshifted.appforge.backstage.BackstageConstants.sshDefaultUsername
import co.bitshifted.appforge.backstage.exception.DeploymentException
import co.bitshifted.appforge.backstage.util.logger
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.file.Path
import kotlin.io.path.absolutePathString


@Component
@ConditionalOnProperty(value = ["external.tools.mode"], havingValue = "remote", matchIfMissing = false)
class RemoteLaunchCodeRunner(
    @Value("\${launchcode.runner.host}")val host : String,
    @Value("\${launchcode.runner.port}")val port : Int
    ) : LaunchCodeRunner {

    private val logger = logger(this)

    override fun copySourceCode(targetDir: Path) {
        val sshConfig = createSSHConfig()
        BuildContext.executeCommand(sshConfig, "copy-src ${targetDir.absolutePathString()}")
    }

    override fun buildLaunchers(targetDir: Path) {
        val sshConfig = createSSHConfig()
        BuildContext.executeCommand(sshConfig, "PATH=\$PATH:/usr/local/go/bin:\$HOME/go/bin build-launchers ${targetDir.absolutePathString()}")
    }

    private fun createSSHConfig() : SSHConfig {
        return SSHConfig(host, port, sshDefaultUsername, sshDefaultPass, sshDefaultTimeout)
    }

    private fun executeCommand(cmd : String) {
        logger.debug("Command to execute: $cmd")
        val jsch = JSch()

        val session = jsch.getSession(sshDefaultUsername, host, port)
        session.setConfig("StrictHostKeyChecking", "no")
        session.setPassword(sshDefaultPass)
        try {
            session.connect(sshDefaultTimeout)
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