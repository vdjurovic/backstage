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

import co.bitshifted.appforge.backstage.entity.InstalledJdk
import co.bitshifted.appforge.backstage.entity.JdkInstallationTask
import co.bitshifted.appforge.backstage.exception.BackstageException
import co.bitshifted.appforge.backstage.exception.ErrorInfo
import co.bitshifted.appforge.backstage.mappers.installedJdkMapper
import co.bitshifted.appforge.backstage.model.jdk.JdkInstallConfig
import co.bitshifted.appforge.backstage.model.jdk.JavaPlatformDetails
import co.bitshifted.appforge.backstage.model.jdk.JdkInstallConfigFactory
import co.bitshifted.appforge.backstage.repository.InstalledJdkRepository
import co.bitshifted.appforge.backstage.repository.JdkInstallationTaskRepository
import co.bitshifted.appforge.backstage.service.JdkInstallationService
import co.bitshifted.appforge.backstage.service.JdkInstallationTaskWorker
import co.bitshifted.appforge.backstage.util.logger
import co.bitshifted.appforge.common.dto.InstalledJdkDTO
import co.bitshifted.appforge.common.dto.JavaPlatformInfoDTO
import co.bitshifted.appforge.common.dto.JdkInstallRequestDTO
import co.bitshifted.appforge.common.dto.JdkInstallStatusDTO
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.stream.Collectors

@Service
@Transactional
class DefaultJdkInstallationService(@Autowired @Qualifier("yamlObjectMapper") val yamlObjectMapper : ObjectMapper,
                                    @Autowired val jdkInstallTaskFactory : java.util.function.BiFunction<List<JdkInstallConfig>, String, JdkInstallationTaskWorker>,
                                    @Autowired val jdkInstallTaskRepository : JdkInstallationTaskRepository,
                                    @Autowired val installedJdkRepository: InstalledJdkRepository,
                                    @Value("\${jdk.install.config.url}") val jdkConfigUrl : String) : JdkInstallationService {

    private val logger = logger(this)
    private val mapper =  installedJdkMapper()

    override fun installJdk(input: List<JdkInstallRequestDTO>) : JdkInstallStatusDTO {
        val availableJdks = getAvailableJdks()
        logger.debug("Available JDKS: $availableJdks")
        val installConfigList = verifyReleaseMatches(input, availableJdks)
        // start installation in separate thread
        val task = JdkInstallationTask()
        val out = jdkInstallTaskRepository.save(task)
        Thread(jdkInstallTaskFactory.apply(installConfigList, out.taskId ?: throw BackstageException(ErrorInfo.JDK_INSTALL_TASK_NOT_EXIST))).start()
        return JdkInstallStatusDTO(out.taskId, out.status)
    }

    override fun listInstalledJdks(): List<InstalledJdkDTO> {
        return installedJdkRepository.findAll().map { mapper.toDto(it)  }
    }

    private fun getAvailableJdks() : List<JavaPlatformDetails> {
        logger.debug("Reading configuration from $jdkConfigUrl")
        val request = HttpRequest.newBuilder(URI(jdkConfigUrl)).GET().build()
        val bodyHandler = HttpResponse.BodyHandlers.ofString()
        val client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build()
        val response = client.send(request, bodyHandler)
        if(response.statusCode() != HttpStatus.OK.value()) {
            logger.error("Failed to get JDK configuration file: ${response.statusCode()}")
            throw BackstageException(ErrorInfo.JDK_CONFIG_FETCH_ERROR, response.body())
        }
        return yamlObjectMapper.readValue(response.body(), object : TypeReference<List<JavaPlatformDetails>>(){})
    }

    private fun verifyReleaseMatches(input : List<JdkInstallRequestDTO>, available : List<JavaPlatformDetails>) : List<JdkInstallConfig> {
        val installConfigList = mutableListOf<JdkInstallConfig>()
        input.forEach { outer ->
           val platformDetails = available.find { outer.vendor == it.vendor }  ?: throw BackstageException(ErrorInfo.JDK_VENDOR_UNKNOWN, outer.vendor.name)
            var isLatest : Boolean
            var releaseString : String
            if("latest" == outer.release) {
                isLatest = true
                releaseString = platformDetails.supportedVersions.find { rd -> rd.majorVersion == outer.majorVersion }?.releases?.get(0) ?: throw BackstageException(ErrorInfo.JDK_RELEASE_UNKNOWN, outer.release)
            } else {
                val releaseDetails = platformDetails.supportedVersions.find { detail -> detail.majorVersion == outer.majorVersion && detail.releases.contains(outer.release) } ?: throw BackstageException(ErrorInfo.JDK_RELEASE_UNKNOWN, outer.release)
                isLatest = outer.release.equals(releaseDetails.releases[0])
                releaseString = outer.release
            }

            installConfigList.add(JdkInstallConfigFactory.createInstallConfig(platformDetails, outer.majorVersion, releaseString, isLatest, outer.isAutoUpdate))
        }
        return installConfigList
    }
}