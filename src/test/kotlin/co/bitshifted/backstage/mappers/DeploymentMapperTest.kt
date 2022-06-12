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

import co.bitshifted.backstage.dto.ApplicationInfoDTO
import co.bitshifted.backstage.dto.DeploymentDTO
import co.bitshifted.backstage.model.DeploymentStage
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DeploymentMapperTest {

    @Test
    fun dtoToStageOneConfigTest() {
        val appInfo =ApplicationInfoDTO(splashScreen = null, icons = emptyList(), windows = null, linux = null, mac = null)
        val dto = DeploymentDTO(applicationId = "appid", applicationInfo = appInfo, jvmConfig = null)
        val mapper = deploymentMapper()
        val out = mapper.dtoToDeploymentTaskConfig(dto, "deployment-id", DeploymentStage.STAGE_ONE)
        assertEquals("deployment-id", out.id)
        assertEquals(DeploymentStage.STAGE_ONE, out.stage)
    }

}