/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.backstage.service

import co.bitshifted.backstage.entity.Application
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ApplicationService {

    fun createApplication(input : Application) : Application

    fun getApplication(id : String) : Application

    fun listApplications(pageable: Pageable) : Page<Application>
}