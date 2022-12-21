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

class AzulJdkInstallConfigTest {

    private val yamlLocation = "/jdk-config.yaml"
    private val platformConfig : List<JavaPlatformDetails>

    init {
        val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
        val content = javaClass.getResourceAsStream(yamlLocation)
        platformConfig = mapper.readValue(content,  object : TypeReference<List<JavaPlatformDetails>>(){})
    }

    @Test
    fun testCustomizeDownloadLinkForJava8() {
        val expectedUrl = "https://cdn.azul.com/zulu-embedded/bin/zulu8.66.0.15-ca-jdk8.0.352-linux_aarch64.tar.gz"
        val azul = platformConfig.find { it.vendor == JvmVendor.AZUL }
        Assertions.assertNotNull(azul)
        val installConfig = JdkInstallConfigFactory.createInstallConfig(azul ?: throw Exception("java platform is null") , JavaVersion.JAVA_8, "8u352b08", true, autoUpdate = false)
        val downloadUrl = installConfig.createDownloadLink(OperatingSystem.LINUX, CpuArch.AARCH64)
        Assertions.assertEquals(expectedUrl, downloadUrl)
    }

    @Test
    fun testCustomizeDownloadLinkForJava11() {
        val expectedUrl = "https://cdn.azul.com/zulu-embedded/bin/zulu11.60.19-ca-jdk11.0.17-linux_aarch64.tar.gz"
        val azul = platformConfig.find { it.vendor == JvmVendor.AZUL }
        Assertions.assertNotNull(azul)
        val installConfig = JdkInstallConfigFactory.createInstallConfig(azul ?: throw Exception("java platform is null") , JavaVersion.JAVA_11, "11.0.17+8", true, autoUpdate = false)
        val downloadUrl = installConfig.createDownloadLink(OperatingSystem.LINUX, CpuArch.AARCH64)
        Assertions.assertEquals(expectedUrl, downloadUrl)
    }
}