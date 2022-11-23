/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.backstage.mappers

import co.bitshifted.appforge.backstage.entity.InstalledJdkRelease
import co.bitshifted.appforge.common.dto.jdk.InstalledJdkReleaseDTO
import org.mapstruct.Mapper

@Mapper
interface InstalledJdkReleaseMapper {

    fun toDto(installedJdkRelease: InstalledJdkRelease) : InstalledJdkReleaseDTO
}