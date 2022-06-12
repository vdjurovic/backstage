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

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.web.context.request.WebRequest

class BackstageExceptionHandlerTest {

    @Test
    fun handleBackstageExceptionSuccess() {
        val handler = BackstageExceptionHandler()
        handler.init()

        val ex = BackstageException(ErrorInfo.NON_EXISTENT_APPLICATION_ID, "app-id")
        val req = Mockito.mock(WebRequest::class.java)
        val out = handler.handleBackstageException(ex, req)
        val dto = out.body
        assertNotNull(dto)
        assertEquals(ErrorInfo.NON_EXISTENT_APPLICATION_ID.errorCode, dto?.errorCode)
        assertEquals("Application with ID app-id not found", dto?.message)
    }
}