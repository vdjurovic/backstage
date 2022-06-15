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

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class JvmDependencyDTO(
    var groupId : String? = null,
    var artifactId : String? = null,
    var version : String? = null,
    var type : String? = null,
    var classifier : String? = null,
    var sha256 : String? = null,
    var size : Long? = null,
    var modular : Boolean = false
)
