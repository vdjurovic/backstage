/*
 *
 *  * Copyright (c) 2022-2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.backstage.model.jdk

import co.bitshifted.appforge.common.model.JvmVendor
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class JavaPlatformDetails(
    val vendor : JvmVendor = JvmVendor.ADOPTIUM,
    @JsonProperty("download-url-format") val downloadUrlFormat : String = "",
    @JsonProperty("checksum-url-format") val checksumUrlFormat : String = "",
    @JsonProperty("public-key-url") val publicKeyUrl : String = "",
    @JsonProperty(" public-key-url-format") val publicKeyUrlFormat: String = "",
    @JsonProperty("supported-versions") val supportedVersions : List<JavaReleaseDetails> = listOf(),
    val parameters : Map<String, Map<String, String>> = mapOf()
    ) {
}
