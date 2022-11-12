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

class JdkInstallConfigFactoryTest {

    private val yamlLocation = "/jdk-config.yaml"
    private val platformConfig : List<JavaPlatformDetails>

   init {
       val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
       val content = javaClass.getResourceAsStream(yamlLocation)
       platformConfig = mapper.readValue(content,  object : TypeReference<List<JavaPlatformDetails>>(){})
   }

    @Test
    fun testAdoptiumInstallConfig() {
        val expectedDownloadUrl = "https://github.com/adoptium/temurin19-binaries/releases/download/jdk-19.0.1%2B10/OpenJDK19U-jre_x64_linux_hotspot_19.0.1_10.tar.gz"
        val adoptium = platformConfig.find { it.vendor == JvmVendor.ADOPTIUM }
        assertNotNull(adoptium)
        val installConfig = JdkInstallConfigFactory.createInstallConfig(adoptium ?: throw Exception("java platform is null"), JavaVersion.JAVA_19, "19.0.1+10", false)
        val downloadUrl = installConfig.createDownloadLink(OperatingSystem.LINUX, CpuArch.X64)
        assertNotNull(downloadUrl)
        println("URL: $downloadUrl")
        assertEquals(expectedDownloadUrl, downloadUrl)
    }

    @Test
    fun testAzulInstallConfig() {
        val expectedDownloadUrl = "https://cdn.azul.com/zulu/bin/zulu19.30.11-ca-jdk19.0.1-linux_x64.tar.gz"
        val azul = platformConfig.find { it.vendor == JvmVendor.AZUL }
        assertNotNull(azul)
        val installConfig = JdkInstallConfigFactory.createInstallConfig(azul ?: throw Exception("java platform is null"), JavaVersion.JAVA_19, "19.0.1+10", false)
        val downloadUrl = installConfig.createDownloadLink(OperatingSystem.LINUX, CpuArch.X64)
        assertNotNull(downloadUrl)
        println("URL: $downloadUrl")
        assertEquals(expectedDownloadUrl, downloadUrl)
        // test link with short version
        val expectedShortUrl = "https://cdn.azul.com/zulu/bin/zulu19.28.81-ca-jdk19.0.0-linux_x64.tar.gz"
        val shortInstallConfig = JdkInstallConfigFactory.createInstallConfig(azul, JavaVersion.JAVA_19, "19+36", false)
        val shortDownloadUrl = shortInstallConfig.createDownloadLink(OperatingSystem.LINUX, CpuArch.X64)
        println("URL: $shortDownloadUrl")
        assertEquals(expectedShortUrl, shortDownloadUrl)
    }

    @Test
    fun testCorrettoInstallConfig() {
        val expectedDownloadUrl = "https://corretto.aws/downloads/resources/19.0.1.10.1/amazon-corretto-19.0.1.10.1-linux-x64.tar.gz"
        val corretto = platformConfig.find { it.vendor == JvmVendor.CORRETTO }
        assertNotNull(corretto)
        val installConfig = JdkInstallConfigFactory.createInstallConfig(corretto ?: throw Exception("java platform is null"), JavaVersion.JAVA_19, "19.0.1.10.1", false)
        val downloadUrl = installConfig.createDownloadLink(OperatingSystem.LINUX, CpuArch.X64)
        assertNotNull(downloadUrl)
        println("URL: $downloadUrl")
        assertEquals(expectedDownloadUrl, downloadUrl)
    }

    @Test
    fun testOpenJdkInstallConfig() {
        val expectedDownloadUrl = "https://download.java.net/java/GA/jdk18.0.2/f6ad4b4450fd4d298113270ec84f30ee/9/GPL/openjdk-18.0.2_linux-x64_bin.tar.gz"
        val openjdk = platformConfig.find { it.vendor == JvmVendor.OPENJDK }
        assertNotNull(openjdk)
        val installConfig = JdkInstallConfigFactory.createInstallConfig(openjdk ?: throw Exception("java platform is null"), JavaVersion.JAVA_18, "18.0.2+9", false)
        val downloadUrl = installConfig.createDownloadLink(OperatingSystem.LINUX, CpuArch.X64)
        assertNotNull(downloadUrl)
        println("URL: $downloadUrl")
        assertEquals(expectedDownloadUrl, downloadUrl)
    }
}