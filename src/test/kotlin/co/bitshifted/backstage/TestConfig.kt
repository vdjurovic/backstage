/*
 *
 *  * Copyright (c) 2022-2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.backstage

import co.bitshifted.backstage.controller.ApplicationController
import co.bitshifted.backstage.repository.ApplicationRepository
import co.bitshifted.backstage.service.ApplicationService
import co.bitshifted.backstage.service.impl.ApplicationServiceImpl
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.mockito.Mockito
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@Configuration
@EnableWebMvc
class TestConfig {

    @Bean
    fun objectMapper() : ObjectMapper {
        return jacksonObjectMapper()
    }

    @Bean
    fun applicationServiceImpl(repository : ApplicationRepository) : ApplicationService {
        return ApplicationServiceImpl(repository)
    }

    @Bean
    fun mockApplicationRepository() : ApplicationRepository {
        return Mockito.mock(ApplicationRepository::class.java)
    }

    @Bean
    fun applicationController(service : ApplicationService) : ApplicationController {
        return ApplicationController(service)
    }
}