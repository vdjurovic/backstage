/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.backstage.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

data class DeploymentDTO(
    @JsonProperty("id") var applicationId: String,
    @JsonProperty("application-info") var applicationInfo : ApplicationInfoDTO,
    @JsonProperty("jvm") var jvmConfig : JvmConfigurationDTO?,
    @JsonProperty("resources") var resources : List<BasicResourceDTO> = mutableListOf()
    )
