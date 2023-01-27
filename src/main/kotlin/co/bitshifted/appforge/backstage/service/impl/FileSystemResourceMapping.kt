/*
 *
 *  * Copyright (c) 2022-2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.backstage.service.impl

import co.bitshifted.appforge.backstage.BackstageConstants
import co.bitshifted.appforge.backstage.exception.DeploymentException
import co.bitshifted.appforge.common.model.JavaVersion
import co.bitshifted.appforge.common.model.JvmVendor
import co.bitshifted.appforge.backstage.service.ResourceMapping
import co.bitshifted.appforge.backstage.util.logger
import co.bitshifted.appforge.common.model.CpuArch
import co.bitshifted.appforge.common.model.OperatingSystem
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.notExists

@Service
class FileSystemResourceMapping(
    @Value("\${jdk.root.location}") val jdkRoot : String,
    @Value("\${syncro.jar.location}") val syncroJarLocation : String) : ResourceMapping {

    val logger = logger(this)

    override fun getJdkLocation(vendor: JvmVendor, version: JavaVersion, os : OperatingSystem, arch : CpuArch, release: String): URI {
        val base = Path.of(jdkRoot, vendor.code, version.display, os.display, arch.display)
        if (base.notExists()) {
            throw DeploymentException("Directory ${base.toFile().absolutePath} does not exist")
        }
        var target : Path
        target = if (release != "") {
            base.resolve(release)
        } else {
            base.resolve(BackstageConstants.LATEST_JAVA_DIR_LINK)
        }
        if(os == OperatingSystem.MAC) {
            target = target.resolve("Contents/Home")
        }
        if (target.notExists()) {
            throw DeploymentException("Directory ${target.toFile().absolutePath} does not exist")
        }
        return target.toUri()
    }

    override fun getSyncroJarLocation(): URI {
        return Path.of(syncroJarLocation).toUri()
    }
}