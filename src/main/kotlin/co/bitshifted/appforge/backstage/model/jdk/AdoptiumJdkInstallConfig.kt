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
import co.bitshifted.appforge.common.model.OperatingSystem

class AdoptiumJdkInstallConfig(platform : JavaPlatformDetails, majorVersion : JavaVersion, release : String, latest : Boolean, autoUpdate : Boolean ) : JdkInstallConfig(platform, majorVersion, release, latest, autoUpdate) {

    override fun createParameters(): Map<String, String> {
        return platform.parameters[majorVersion.display] ?: mapOf()
    }

    override fun createDownloadLink(os: OperatingSystem, arch: CpuArch): String {
        var link =  super.createDownloadLink(os, arch)
        // customize link for Java 8
        if(majorVersion == JavaVersion.JAVA_8) {
            val cleanRelease = release.replace("-", "")
           link = link.replace("jdk-$release", "jdk$release")
               .replace("%2B", "")
               .replace("${release}_.tar.gz", "$cleanRelease.tar.gz")
               .replace("${release}_.zip", "$cleanRelease.zip")
        }
        return link
    }
}