/*
 *
 *  * Copyright (c) 2022-2022  Bitshift D.O.O (http://bitshifted.co)
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
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.Version
import java.io.StringReader
import java.io.StringWriter

open class JdkInstallConfig(val platform : JavaPlatformDetails, val majorVersion : JavaVersion, val release : String, val latest : Boolean, val autoUpdate : Boolean ) {

    val freeMarkerConfig = Configuration(Version(2,3,20))
    private val urlParamPackaging = "PACKAGING"
    private val urlParamOs = "OS"
    private val urlParamArch = "ARCH"
    private val urlParamVersionBase = "VERSION_BASE"
    private val urlParameterBump = "BUMP"
    private val urlParameterMajorVersion = "MAJOR_VERSION"
    protected val urlParamCorrettoVersion = "CORRETTO_VERSION"
    protected val urlParameterRelease = "RELEASE"

    fun createDownloadLink(os : OperatingSystem, arch : CpuArch) : String {
        val inputString = platform.downloadUrlFormat
        val template = Template("download-url", StringReader(inputString), freeMarkerConfig)
        val model = createModel(os, arch)
        val writer = StringWriter()
        template.process(model, writer)
        return writer.toString()
    }

    protected open fun createModel(os : OperatingSystem, arch: CpuArch) : Map<String, Any> {
        val output = mutableMapOf<String, Any>()
        output[urlParamPackaging] = when (os) {
            OperatingSystem.LINUX, OperatingSystem.MAC -> "tar.gz"
            OperatingSystem.WINDOWS  -> "zip"
        }
        output[urlParamOs] = inferOperatingSystem(os)
        output[urlParamArch] = arch.display
        output[urlParamVersionBase] = getVersionBase(release)
        output[urlParameterBump] = getBump(release)
        output[urlParameterMajorVersion] = majorVersion.display
        val params = createParameters()
        params.entries.forEach {
            output[it.key.uppercase().replace("-", "_")] = it.value
        }

        return output
    }

    protected open fun inferOperatingSystem(os : OperatingSystem) : String {
        return os.display
    }

    protected open fun getVersionBase(version : String) : String {
        val parts = version.split("+")
        return parts[0]
    }

    private fun getBump(version: String) : String {
        if(version.contains("+")) {
            return version.split("+")[1]
        }
        return ""
    }

    protected open fun createParameters() : Map<String, String> {
       return mapOf()
    }
}