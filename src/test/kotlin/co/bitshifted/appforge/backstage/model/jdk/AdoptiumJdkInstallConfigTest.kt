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
import org.junit.jupiter.api.Test

class AdoptiumJdkInstallConfigTest {

    private val yamlLocation = "/jdk-config.yaml"
    private val platformConfig : List<JavaPlatformDetails>

    init {
        val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
        val content = javaClass.getResourceAsStream(yamlLocation)
        platformConfig = mapper.readValue(content,  object : TypeReference<List<JavaPlatformDetails>>(){})
    }

    @Test
    fun testCustomizeDownloadLinkForJava8() {
        val expectedUrl = "https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u352-b08/OpenJDK8U-jdk_x64_linux_hotspot_8u352b08.tar.gz"
        val adoptium = platformConfig.find { it.vendor == JvmVendor.ADOPTIUM }
        Assertions.assertNotNull(adoptium)
        val installConfig = JdkInstallConfigFactory.createInstallConfig(adoptium ?: throw Exception("java platform is null") , JavaVersion.JAVA_8, "8u352-b08", true, autoUpdate = false)
        val downloadUrl = installConfig.createDownloadLink(OperatingSystem.LINUX, CpuArch.X64)
        Assertions.assertEquals(expectedUrl, downloadUrl)
    }
}