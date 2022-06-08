/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.backstage.entity

import co.bitshifted.backstage.model.DeplyomentStatus
import co.bitshifted.backstage.util.GENERATOR_STRATEGY_NAME
import org.hibernate.annotations.GenericGenerator
import java.time.ZonedDateTime
import javax.persistence.*

@Entity(name = "deployment")
class Deployment {
    @Id
    @GenericGenerator(name = "id_generator", strategy = GENERATOR_STRATEGY_NAME)
    @GeneratedValue(generator = "id_generator")
    @Column(name = "deployment_id")
    lateinit var id : String

    @Column(name = "submitted_at")
    lateinit var submittedAd : ZonedDateTime

    @Enumerated(EnumType.STRING)
    lateinit var status : DeplyomentStatus

    @Column(name = "details")
    var details : String? = null

    @ManyToOne
    lateinit var application: Application
}