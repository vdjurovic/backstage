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

import co.bitshifted.backstage.dto.DeploymentDTO
import co.bitshifted.backstage.dto.RequiredResourcesDTO
import co.bitshifted.backstage.entity.Application
import co.bitshifted.backstage.entity.Deployment
import co.bitshifted.backstage.exception.BackstageException
import co.bitshifted.backstage.exception.ErrorInfo
import co.bitshifted.backstage.model.DeploymentStatus
import co.bitshifted.backstage.repository.ApplicationRepository
import co.bitshifted.backstage.repository.DeploymentRepository
import co.bitshifted.backstage.service.DeploymentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service("deploymentServiceImpl")
class DeploymentServiceImpl(
    @Autowired val deploymentRepository: DeploymentRepository,
    @Autowired val applicationRepository: ApplicationRepository) : DeploymentService {

    override fun validateDeployment(deploymentDto: DeploymentDTO): String? {
        val app = applicationRepository.findById(deploymentDto.applicationId).orElseThrow { BackstageException(ErrorInfo.NON_EXISTENT_APPLICATION_ID, deploymentDto.applicationId) }
        val deployment = Deployment(application = app, status = DeploymentStatus.ACCEPTED)
        val out = deploymentRepository.save(deployment)
        return out.id
    }

    override fun processDeploymentStageOne(deploymentDto: DeploymentDTO): RequiredResourcesDTO {
        TODO("Not yet implemented")
    }
}