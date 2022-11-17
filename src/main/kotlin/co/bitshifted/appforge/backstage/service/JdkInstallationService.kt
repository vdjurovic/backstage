/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.backstage.service

import co.bitshifted.appforge.common.dto.InstalledJdkDTO
import co.bitshifted.appforge.common.dto.JavaPlatformInfoDTO
import co.bitshifted.appforge.common.dto.JdkInstallRequestDTO
import co.bitshifted.appforge.common.dto.JdkInstallStatusDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface JdkInstallationService {

    fun installJdk(input : List<JdkInstallRequestDTO>) : JdkInstallStatusDTO

    fun listInstalledJdks() : List<InstalledJdkDTO>
}