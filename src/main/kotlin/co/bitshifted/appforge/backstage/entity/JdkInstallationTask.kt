/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.backstage.entity

import co.bitshifted.appforge.common.model.JdkInstallationStatus
import co.bitshifted.appforge.backstage.util.GENERATOR_STRATEGY_NAME
import org.hibernate.annotations.GenericGenerator
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.persistence.*

@Entity
@Table(name = "jdk_installation_task")
class JdkInstallationTask(
    @Id
    @GenericGenerator(name = "id_generator", strategy = GENERATOR_STRATEGY_NAME)
    @GeneratedValue(generator = "id_generator")
    @Column(name = "task_id")
    var taskId : String? = null,
    @Column(name = "started_on") var startedOn : ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
    @Enumerated(EnumType.STRING) var status : JdkInstallationStatus = JdkInstallationStatus.PENDING) {

}