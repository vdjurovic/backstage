/*
 *
 *  * Copyright (c) 2022-2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.backstage.util

import co.bitshifted.appforge.backstage.BackstageConstants
import com.github.f4b6a3.uuid.UuidCreator
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.IdentifierGenerator
import java.io.Serializable
import java.time.ZonedDateTime

/**
 * Generates time-ordered UUID in UUID 7 format.
 *
 * @author Vladimir Djurovic
 */

const val GENERATOR_STRATEGY_NAME = "co.bitshifted.appforge.backstage.util.IdGenerator"

class IdGenerator : IdentifierGenerator {



    override fun generate(session: SharedSessionContractImplementor?, obj: Any?): Serializable {
        val instant = ZonedDateTime.now(BackstageConstants.UTC_TIME_ZONE).toInstant()
        return UuidCreator.getTimeOrdered(instant, null, null).toString()
    }
}