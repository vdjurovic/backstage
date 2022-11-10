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

class AzulJdkInstallConfig(platform : JavaPlatformDetails, majorVersion : JavaVersion, release : String, latest : Boolean ) : JdkInstallConfig(platform, majorVersion, release, latest) {

    override fun inferOperatingSystem(os: OperatingSystem): String {
        return when(os) {
                OperatingSystem.WINDOWS -> "win"
                OperatingSystem.MAC -> "macosx"
                OperatingSystem.LINUX -> os.display
        }
    }

    override fun createParameters(): Map<String, String> {
        return  platform.parameters[release] ?: mapOf()
    }

    override fun getVersionBase(version: String): String {
        val parts = version.split("+")
        var version = parts[0]
        if (!version.contains(".")) {
            version = "$version.0.0"
        }
        return version
    }
}