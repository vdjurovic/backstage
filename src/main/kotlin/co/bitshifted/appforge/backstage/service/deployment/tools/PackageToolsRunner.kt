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

import java.nio.file.Path

interface PackageToolsRunner {

    fun runNsis(baseDir: Path, installerFile : String, options : Map<String, String> = emptyMap())

    fun runDpkg(baseDir : Path, packageName : String, debWorkDirName : String)

    fun runRpm(baseDir: Path, specFileName : String)

    fun createDmg(baseDir: Path, scriptName : String, options : Map<String, String> = emptyMap())
}