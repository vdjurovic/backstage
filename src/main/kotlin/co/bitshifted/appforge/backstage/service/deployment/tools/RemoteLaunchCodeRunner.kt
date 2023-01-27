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
import co.bitshifted.appforge.backstage.util.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
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
        logger.debug("Running remote copy-src command")
        BuildContext.executeRemoteCommand(sshConfig, "copy-src ${targetDir.absolutePathString()}")
    }

    override fun buildLaunchers(targetDir: Path) {
        val sshConfig = createSSHConfig()
        BuildContext.executeRemoteCommand(sshConfig, "PATH=\$PATH:/usr/local/go/bin:\$HOME/go/bin build-launchers ${targetDir.absolutePathString()}")
    }

    private fun createSSHConfig() : SSHConfig {
        return SSHConfig(host, port, sshDefaultUsername, sshDefaultPass, sshDefaultTimeout)
    }

}