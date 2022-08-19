/*
 *
 *  * Copyright (c) 2022-2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.backstage.service

import co.bitshifted.appforge.backstage.dto.AppInstallerDTO
import co.bitshifted.appforge.backstage.model.DeploymentConfig
import co.bitshifted.ignite.common.model.OperatingSystem
import java.nio.file.Path
import java.util.Optional

interface ReleaseService {

    fun initRelease(deploymentConfig: DeploymentConfig) : String

    fun completeRelease(baseDir : Path, deploymentConfig : DeploymentConfig, releaseId : String)

    fun checkForNewRelease(applicationId : String, currentRelease : String, os : OperatingSystem) : Optional<String>

    fun getInstallersList(applicationId: String) : List<AppInstallerDTO>

    fun getInstallerData(applicationId: String, hash : String) : AppInstallerDTO
}