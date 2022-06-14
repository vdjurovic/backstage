/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.backstage.exception

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST

enum class ErrorInfo (val errorCode : Int, val httpStatus : Int) {

    INVALID_INPUT(1110, BAD_REQUEST.value()),
    NON_EXISTENT_APPLICATION_ID(1200, BAD_REQUEST.value()),

    DEPLOYMENT_NOT_FOND(1300, HttpStatus.NOT_FOUND.value())
}