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

import co.bitshifted.appforge.backstage.BackstageConstants
import co.bitshifted.appforge.backstage.BackstageConstants.sshDefaultPass
import co.bitshifted.appforge.backstage.BackstageConstants.sshDefaultTimeout
import co.bitshifted.appforge.backstage.BackstageConstants.sshDefaultUsername
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.nio.file.Path
import kotlin.io.path.absolutePathString

@Component
@ConditionalOnProperty(value = ["external.tools.mode"], havingValue = "remote", matchIfMissing = false)
class RemotePackageToolsRunner(
    @Value("\${package.runner.host}")val host : String,
    @Value("\${package.runner.port}")val port : Int
) : PackageToolsRunner {

    override fun runNsis(baseDir: Path, installerFile: String, options: Map<String, String>) {
        val config = createSshConfig()
        BuildContext.executeRemoteCommand(config, "build-nsis ${baseDir.absolutePathString()} $installerFile")
    }

    override fun runDpkg(baseDir: Path, packageName: String, debWorkDirName: String) {
        val config = createSshConfig()
        BuildContext.executeRemoteCommand(config, "build-deb ${baseDir.absolutePathString()} $debWorkDirName $packageName")
    }

    override fun runRpm(baseDir: Path, specFileName: String) {
        val config = createSshConfig()
        BuildContext.executeRemoteCommand(config, "build-rpm ${baseDir.absolutePathString()} $specFileName")
    }

    override fun createDmg(baseDir: Path, scriptName: String, options: Map<String, String>) {
        val config = createSshConfig()
        BuildContext.executeRemoteCommand(config, "build-dmg ${baseDir.absolutePathString()} $scriptName")
    }

    private fun createSshConfig() : SSHConfig {
        return SSHConfig(host, port, sshDefaultUsername, sshDefaultPass, sshDefaultTimeout)
    }
}