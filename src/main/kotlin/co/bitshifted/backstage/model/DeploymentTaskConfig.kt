/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.backstage.model

import co.bitshifted.backstage.dto.BasicResourceDTO
import co.bitshifted.backstage.dto.JvmConfigurationDTO

class DeploymentTaskConfig(val id : String, val stage : DeploymentStage, val jvmConfig: JvmConfigurationDTO, val resources : List<BasicResourceDTO>) {
}