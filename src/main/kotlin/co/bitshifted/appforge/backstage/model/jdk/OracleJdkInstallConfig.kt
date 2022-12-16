/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.backstage.model.jdk

import co.bitshifted.appforge.common.model.JavaVersion
import co.bitshifted.appforge.common.model.OperatingSystem

class OracleJdkInstallConfig (platform : JavaPlatformDetails, majorVersion : JavaVersion, release : String, latest : Boolean, autoUpdate : Boolean ) : JdkInstallConfig(platform, majorVersion, release, latest, autoUpdate) {

    override fun inferOperatingSystem(os: OperatingSystem): String {
        return when(os) {
            OperatingSystem.WINDOWS -> os.display
            OperatingSystem.MAC -> "macos"
            OperatingSystem.LINUX -> os.display
        }
    }

    override fun getVersionBase(version: String): String {
        return majorVersion.display
    }

    override fun createParameters(): Map<String, String> {
        val output = mutableMapOf<String, String>()
        output[urlParameterRelease] = release
        return  output
    }
}