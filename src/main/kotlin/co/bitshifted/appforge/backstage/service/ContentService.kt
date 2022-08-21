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

import co.bitshifted.appforge.backstage.model.ContentItem
import java.io.InputStream
import java.net.URI
import java.net.URL

interface ContentService {

    fun save(input : InputStream) : String

    fun save(input: InputStream, executable : Boolean) : String

    fun get(sha256: String) : ContentItem

    fun exists(sha256 : String, size : Long) : Boolean
}