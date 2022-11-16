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

import co.bitshifted.appforge.backstage.util.GENERATOR_STRATEGY_NAME
import org.hibernate.annotations.GenericGenerator
import javax.persistence.*

@Entity
@Table(name = "installed_jdk_release")
class InstalledJdkRelease(
    @Id
    @GenericGenerator(name = "id_generator", strategy = GENERATOR_STRATEGY_NAME)
    @GeneratedValue(generator = "id_generator")
    var id : String? = null,
    @Column(name = "jdk_release")
    var release : String? = null,
    var latest : Boolean = false,
    @ManyToOne(fetch = FetchType.LAZY)
    var installedJdk : InstalledJdk? = null
) {
}