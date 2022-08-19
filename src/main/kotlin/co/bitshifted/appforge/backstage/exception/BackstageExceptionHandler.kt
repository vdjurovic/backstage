/*
 *
 *  * Copyright (c) 2022-2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.backstage.exception

import co.bitshifted.appforge.backstage.util.logger
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.util.Properties
import javax.annotation.PostConstruct


@RestControllerAdvice
class BackstageExceptionHandler : ResponseEntityExceptionHandler() {

    private  val errorMessagesLocation = "/errors.properties"
    private val logger = logger(this)
    private lateinit var errorMessages : Properties

    @PostConstruct
    fun init() {
        errorMessages = Properties()
        errorMessages.load(javaClass.getResourceAsStream(errorMessagesLocation))
        logger.info("Successfully loaded error messages")
    }

    @ExceptionHandler(BackstageException::class)
    fun handleBackstageException(exception : BackstageException, request : WebRequest) : ResponseEntity<ErrorDTO> {
        var errMsg = errorMessages.getProperty(exception.errorInfo.name, "")
        if (exception.format.isNotEmpty()) {
            errMsg = String.format(errMsg, *exception.format)
        }
        val error = ErrorDTO(exception.errorInfo.errorCode, errMsg)
        return ResponseEntity.status(exception.errorInfo.httpStatus).body(error)
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        logger.error("Validation error", ex)
        val errorMsg = errorMessages.getProperty(ErrorInfo.INVALID_INPUT.name, "")
        val errList = ex.bindingResult.allErrors.stream().map { err -> err.defaultMessage }.toList()
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorDTO(ErrorInfo.INVALID_INPUT.errorCode, String.format(errorMsg, errList.toString())))
    }
}