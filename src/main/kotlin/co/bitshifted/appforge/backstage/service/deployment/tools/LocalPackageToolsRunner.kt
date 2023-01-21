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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.nio.file.Path
import kotlin.io.path.absolutePathString

@Component
@ConditionalOnProperty(value = ["external.tools.mode"], havingValue = "local", matchIfMissing = false)
class LocalPackageToolsRunner : PackageToolsRunner {

    val nsisCompilerCmd = "makensis"

    override fun runNsis(baseDir: Path, installerFile : String, options : Map<String, String>) {
        BuildContext.runExternalProgram(listOf(nsisCompilerCmd, installerFile), baseDir.toFile(), options)
    }
}