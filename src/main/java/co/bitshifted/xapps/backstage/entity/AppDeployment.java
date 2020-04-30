/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.entity;

import jdk.jfr.Enabled;
import lombok.Data;

import javax.persistence.*;
import java.time.ZonedDateTime;

/**
 * @author Vladimir Djurovic
 */
@Entity
@Table(name = "app_deployment")
@Data
public class AppDeployment {
	@Id
	@SequenceGenerator(name = "app_deployment_id_seq", sequenceName = "app_deployment_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_deployment_id_seq")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "app_id")
	private Application application;
	@Column(name = "release_number")
	private String releaseNumber;
	@Column(name = "release_time")
	private ZonedDateTime releaseTime;
}
