/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */
package co.bitshifted.appforge.backstage.controller.ui

import co.bitshifted.appforge.backstage.mappers.applicationMapper
import co.bitshifted.appforge.backstage.service.ApplicationService
import co.bitshifted.appforge.backstage.service.ReleaseService
import co.bitshifted.appforge.common.dto.ApplicationDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import java.util.Map
import java.util.stream.Collectors

@Controller
@RequestMapping("/ui")
class ApplicationsListController(@Autowired val applicationService : ApplicationService,
                                 @Autowired val releaseService: ReleaseService
) {

    @GetMapping("/applications")
    fun listApplications(@PageableDefault(size = 20) pageable : Pageable): ModelAndView {
        val apps = applicationService.listApplications(pageable)
        val model = mutableMapOf<String, List<ApplicationDTO>>()
        model["appList"] = apps.toList().map { applicationMapper().toDto(it) }

        return ModelAndView("ui/applications", model)
    }

    @GetMapping("/application/{id}")
    fun getApplication(@PathVariable("id") id : String) : ModelAndView {
        val application = applicationService.getApplication(id)
        val model = mutableMapOf<String, Any>()
        if (application == null) {
            return ModelAndView("ui/application-details", model)
        }
        model["application"] = applicationMapper().toDto(application)
        val installers = releaseService.getInstallersList(id)
        model["installers"] = installers
        return ModelAndView("ui/application-details", model)
    }
}