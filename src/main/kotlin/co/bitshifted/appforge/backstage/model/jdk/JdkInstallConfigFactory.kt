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
import co.bitshifted.appforge.common.model.JvmVendor

object JdkInstallConfigFactory {

    fun createInstallConfig(platform : JavaPlatformDetails, majorVersion : JavaVersion, release : String, latest : Boolean, autoUpdate : Boolean) : JdkInstallConfig {
        return when(platform.vendor) {
            JvmVendor.ADOPTIUM -> AdoptiumJdkInstallConfig(platform, majorVersion, release, latest, autoUpdate)
            JvmVendor.AZUL -> AzulJdkInstallConfig(platform, majorVersion, release, latest, autoUpdate)
            JvmVendor.CORRETTO -> CorrettoJdkInstallConfig(platform, majorVersion, release, latest,autoUpdate)
            JvmVendor.OPENJDK -> OpenJdkInstallConfig(platform, majorVersion, release, latest, autoUpdate)
            JvmVendor.ORACLE -> OracleJdkInstallConfig(platform, majorVersion, release, latest, autoUpdate)
        }
    }
}