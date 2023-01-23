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

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
@ConditionalOnProperty(value = ["external.tools.mode"], havingValue = "remote", matchIfMissing = false)
class RemotePackageToolsRunner(
    @Value("\${package.runner.host}")val host : String,
    @Value("\${package.runner.port}")val port : Int
) : PackageToolsRunner {

    override fun runNsis(baseDir: Path, installerFile: String, options: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override fun runDpkg(baseDir: Path, packageName: String, debWorkDirName: String) {
        TODO("Not yet implemented")
    }

    override fun runRpm(baseDir: Path, specFileName: String) {
        TODO("Not yet implemented")
    }

    override fun createDmg(baseDir: Path, scriptName: String, options: Map<String, String>) {
        TODO("Not yet implemented")
    }
}