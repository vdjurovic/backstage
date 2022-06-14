/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.backstage.mappers

import co.bitshifted.backstage.dto.DeploymentDTO
import co.bitshifted.backstage.model.DeploymenTaskConfig
import co.bitshifted.backstage.model.DeploymentStage
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper
interface DeploymentMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "stage", target = "stage")
    fun dtoToDeploymentTaskConfig(deploymentDTO: DeploymentDTO, id : String, stage : DeploymentStage) : DeploymenTaskConfig
}