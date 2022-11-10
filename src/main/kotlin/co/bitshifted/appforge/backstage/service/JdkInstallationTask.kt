/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.backstage.service

import co.bitshifted.appforge.backstage.model.jdk.JdkInstallConfig
import co.bitshifted.appforge.backstage.model.jdk.JdkInstallationSource
import co.bitshifted.appforge.backstage.util.logger
import co.bitshifted.appforge.common.model.CpuArch
import co.bitshifted.appforge.common.model.OperatingSystem
import org.springframework.beans.factory.annotation.Value
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.util.concurrent.CompletableFuture

class JdkInstallationTask(val installConfigList : List<JdkInstallConfig>) : Runnable {

    private val logger = logger(this)
    private val platforms = listOf(
        Pair(OperatingSystem.LINUX, CpuArch.X64),
        Pair(OperatingSystem.LINUX, CpuArch.AARCH64),
        Pair(OperatingSystem.MAC, CpuArch.X64),
        Pair(OperatingSystem.MAC, CpuArch.AARCH64),
        Pair(OperatingSystem.WINDOWS, CpuArch.X64)
    )
    private val client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build()

    @Value("\${jdk.root.location}") lateinit var jdkRootLocation : String

    override fun run() {
        logger.info("JDK root directory: $jdkRootLocation")
        val futuresList = mutableListOf<CompletableFuture<JdkInstallationSource>>()
       installConfigList.forEach { config ->
           logger.info("Started JDk download: vendor=${config.platform.vendor}, release=${config.release}")
           platforms.forEach { pair ->
               val downloadLink = config.createDownloadLink(pair.first, pair.second)
               logger.info("Starting JDK download for ${pair.first} and ${pair.second}")
//               val request = HttpRequest.newBuilder(URI(downloadLink)).GET().build()
               val request = HttpRequest.newBuilder(URI("https://bitshifted.co")).GET().build()
               val future = client.sendAsync(request, BodyHandlers.ofByteArray()).thenApply { response ->
                   logger.info("Completed JDK download for ${pair.first} and ${pair.second}")
                   JdkInstallationSource(config, response.body(), pair.first, pair.second, extractFileName(downloadLink) )  }
               futuresList.add(future)
           }
       }
        CompletableFuture.allOf(*futuresList.toTypedArray()).join()
       logger.info("All JDK downloads completed")
        futuresList.forEach{
            logger.debug("file name: ${it.get().fileName}, latest: ${it.get().latest}")
        }
    }

    private fun extractFileName(url : String) : String {
        return url.substring(url.lastIndexOf("/") + 1)
    }
}