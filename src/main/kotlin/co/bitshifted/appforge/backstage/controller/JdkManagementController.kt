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
import co.bitshifted.appforge.common.dto.JavaPlatformInfoDTO
import co.bitshifted.appforge.common.dto.JdkInstallRequestDTO
import co.bitshifted.appforge.common.dto.JdkInstallStatusDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/jdks")
class JdkManagementController(@Autowired val jdkInstallationService: JdkInstallationService) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun installJdk(@RequestBody request : List<JdkInstallRequestDTO>) : ResponseEntity<JdkInstallStatusDTO> {
        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(jdkInstallationService.installJdk(request))
    }

    @GetMapping
    fun listInstalledJdks() : ResponseEntity<List<JavaPlatformInfoDTO>> {
        return ResponseEntity.ok(jdkInstallationService.listInstalledJdks())
    }
}