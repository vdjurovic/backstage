/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.backstage.service.impl

import co.bitshifted.appforge.backstage.model.jdk.JavaPlatformDetails
import co.bitshifted.appforge.common.model.JavaVersion
import co.bitshifted.appforge.common.model.JvmVendor
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DefaultJdkInstallationServiceTest {

    private val yamlLocation = "/jdk-config.yaml"

    @Test
    fun testLoadConfigFromYaml() {
        val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
        val content = javaClass.getResourceAsStream(yamlLocation)
        val out = mapper.readValue(content,  object : TypeReference<List<JavaPlatformDetails>>(){})

        assertEquals(4, out.size)
        val adoptium = out.get(0)
        assertEquals(JvmVendor.ADOPTIUM, adoptium.vendor)
        assertEquals(3, adoptium.supportedVersions.size)
        val java19Version = adoptium.supportedVersions.find { it.majorVersion == JavaVersion.JAVA_19 }
        assertNotNull(java19Version)
        assertTrue(java19Version?.releases?.contains("19.0.1+10") ?: false)
        val params = adoptium.parameters
        val releaseMap = params["19"]
        assertEquals("temurin19-binaries", releaseMap?.get("repo"))
    }
}