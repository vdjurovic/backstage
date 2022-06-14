/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.backstage.config

import co.bitshifted.backstage.dto.DeploymentDTO
import co.bitshifted.backstage.model.DeploymenTaskConfig
import co.bitshifted.backstage.service.deployment.DeploymentProcessTask
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import java.util.function.Function

@Configuration
class DeploymentProcessConfig {

    @Bean
    @Scope("prototype")
    fun deploymentProcessTask(deploymentConfig : DeploymenTaskConfig) : DeploymentProcessTask =  DeploymentProcessTask(deploymentConfig)

    @Bean
    fun deploymentTaskFactory(): Function<DeploymenTaskConfig, DeploymentProcessTask>? {
        return Function { source: DeploymenTaskConfig ->
            deploymentProcessTask(source)
        }
    }
}