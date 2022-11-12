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

import co.bitshifted.appforge.common.model.CpuArch
import co.bitshifted.appforge.common.model.JavaVersion
import co.bitshifted.appforge.common.model.JvmVendor
import co.bitshifted.appforge.common.model.OperatingSystem
import java.nio.file.Path

class JdkInstallationSource(config : JdkInstallConfig, val srcFile : Path, val os : OperatingSystem, val arch : CpuArch, val fileName : String) {


    val vendor : JvmVendor
    val majorVersion : JavaVersion
    val latest : Boolean

    init {
        this.vendor = config.platform.vendor
        this.majorVersion = config.majorVersion
        this.latest = config.latest
    }
}