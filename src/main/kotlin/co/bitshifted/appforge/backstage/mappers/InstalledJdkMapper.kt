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

import co.bitshifted.appforge.backstage.entity.InstalledJdk
import co.bitshifted.appforge.backstage.entity.InstalledJdkRelease
import co.bitshifted.appforge.common.dto.InstalledJdkDTO
import co.bitshifted.appforge.common.dto.JavaPlatformInfoDTO
import co.bitshifted.appforge.common.dto.JavaReleaseDTO
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import java.util.stream.Collectors

@Mapper(uses = [InstalledJdkReleaseMapper::class])
interface InstalledJdkMapper {

    fun toDto(input : InstalledJdk) : InstalledJdkDTO

//    companion object{
//        @Named(value = "releaseToDto")
//        @JvmStatic
//        fun releaseToDto(input : MutableSet<InstalledJdkRelease>) : List<JavaReleaseDTO> {
//            val releaseDto = JavaReleaseDTO()
//            releaseDto.majorVersion = input.first().installedJdk?.majorVersion
//            releaseDto.releases = input.stream().map { it.release }.collect(Collectors.toList())
//            return listOf(releaseDto)
//        }
//    }
}