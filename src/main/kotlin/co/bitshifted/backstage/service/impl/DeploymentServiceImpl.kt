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
import co.bitshifted.backstage.service.DeploymentService
import org.springframework.stereotype.Service

@Service("deploymentServiceImpl")
class DeploymentServiceImpl : DeploymentService {

    override fun processDeploymentStageOne(deploymentDto: DeploymentDTO): RequiredResourcesDTO {
        TODO("Not yet implemented")
    }
}