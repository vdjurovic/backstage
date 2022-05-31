/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.backstage.util

import co.bitshifted.backstage.BackstageConstants
import org.hashids.Hashids
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.IdentifierGenerator
import java.io.Serializable
import java.time.ZonedDateTime
import java.util.*

/**
 * Generates unique alphanumeric IDs based on current timestamp in UTC timezone, and on random generated number.
 * It should guarantee that all IDs are unique, regardless of installation (with low collision probability).
 *
 * @author Vladimir Djurovic
 */

const val GENERATOR_STRATEGY_NAME = "co.bitshifted.backstage.util.IdGenerator"

class IdGenerator : IdentifierGenerator {

    /**
     * Salt for generated IDs. Not intended to be secure, just a common base for all installations.
     */
    private val HASH_SALT = "Backstage"

    /**
     * Minimum length for hashes.
     */
    private val MIN_HASH_LENGTH = 10

    private val RANDOM_BOUND = 100000L

    private val GENERATOR  = Hashids(HASH_SALT, MIN_HASH_LENGTH)
    private val RANDOM = Random()

    override fun generate(session: SharedSessionContractImplementor?, obj: Any?): Serializable {
        val now: Long = ZonedDateTime.now(BackstageConstants.UTC_TIME_ZONE).toEpochSecond()
        return GENERATOR.encode(now, RANDOM.nextLong(RANDOM_BOUND))
    }
}