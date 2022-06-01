/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.backstage.service.impl

import co.bitshifted.backstage.ApplicationRepository
import co.bitshifted.backstage.entity.Application
import co.bitshifted.backstage.service.ApplicationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ApplicationServiceImpl(
    @Autowired val applicationRepository: ApplicationRepository
) : ApplicationService {

    override fun createApplication(input: Application): Application {
        return applicationRepository.save(input)
    }

    override fun getApplication(id: String): Application {
        return applicationRepository.findById(id).orElseThrow()
    }

    override fun listApplications(pageable: Pageable): Page<Application> {
        return applicationRepository.findAll(pageable)
    }
}