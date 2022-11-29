/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.backstage.controller

import co.bitshifted.appforge.backstage.service.JdkInstallationService
import co.bitshifted.appforge.common.dto.jdk.InstalledJdkDTO
import co.bitshifted.appforge.common.dto.jdk.JavaPlatformInfoDTO
import co.bitshifted.appforge.common.dto.jdk.JdkInstallRequestDTO
import co.bitshifted.appforge.common.dto.jdk.JdkInstallStatusDTO
import co.bitshifted.appforge.common.model.JdkInstallationStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/jdks")
class JdkManagementController(@Autowired val jdkInstallationService: JdkInstallationService) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun installJdk(@RequestBody request : List<JdkInstallRequestDTO>) : ResponseEntity<JdkInstallStatusDTO> {
        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(jdkInstallationService.installJdk(request))
    }

    @GetMapping("/installations/{taskId}")
    fun getJdkInstallationStatus(@PathVariable("taskId") taskId : String) : ResponseEntity<JdkInstallStatusDTO> {
        return ResponseEntity.ok(jdkInstallationService.getInstallationStatus(taskId))
    }

    @GetMapping
    fun listInstalledJdks() : ResponseEntity<List<InstalledJdkDTO>> {
        return ResponseEntity.ok(jdkInstallationService.listInstalledJdks())
    }

    @GetMapping("/available")
    fun listAvailableJdks() : ResponseEntity<List<JavaPlatformInfoDTO>> {
        return ResponseEntity.ok(jdkInstallationService.listAvailableJdks())
    }

    @DeleteMapping("{jdkId}/releases/{releaseId}")
    fun removeJdkRelease(@PathVariable("jdkId") jdkId : String, @PathVariable("releaseId") releaseId : String) : ResponseEntity<String> {
        jdkInstallationService.removeJdkRelease(jdkId, releaseId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("")
    }
}