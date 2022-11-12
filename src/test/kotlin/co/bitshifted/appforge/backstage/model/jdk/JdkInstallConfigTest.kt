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
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JdkInstallConfigTest {

    private val yamlLocation = "/jdk-config.yaml"

    @Test
    fun testValidDownloadUrl() {
        val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
        val content = javaClass.getResourceAsStream(yamlLocation)
        val out = mapper.readValue(content,  object : TypeReference<List<JavaPlatformDetails>>(){})
        val adoptium = out.find { it.vendor == JvmVendor.ADOPTIUM }
        assertNotNull(adoptium)

        val installConfig = JdkInstallConfig(adoptium ?: throw Exception("java platform is null"), JavaVersion.JAVA_19, "19.0.1+10", false)
        val downloadUrl = installConfig.createDownloadLink(OperatingSystem.LINUX, CpuArch.X64)
        assertNotNull(downloadUrl)
        println("URL: $downloadUrl")
    }
}