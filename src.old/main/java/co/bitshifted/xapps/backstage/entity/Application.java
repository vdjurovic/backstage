/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.entity;

import co.bitshifted.xapps.backstage.dto.ApplicationDTO;
import co.bitshifted.xapps.backstage.util.IdGenerator;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.modelmapper.ModelMapper;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Vladimir Djurovic
 */
@Data
@Entity(name = "application")
public class Application {

	@Transient
	private final ModelMapper mapper = new ModelMapper();

	@Id
	@GenericGenerator(name = "id_generator", strategy = IdGenerator.GENERATOR_STRATEGY_NAME)
	@GeneratedValue(generator = "id_generator")
	@Column(name = "app_id")
	private String id;

	@Column(name = "app_name")
	private String name;
	private String headline;
	private String description;

	@Column(name = "app_img_hash")
	private String appImageHash;

	@OneToMany(mappedBy = "application")
	private Set<AppDeployment> deployments = new HashSet<>();

	public ApplicationDTO convertToDto() {
		return mapper.map(this, ApplicationDTO.class);
	}

	public ApplicationDTO convertToDto(String serverUrl) {
		var dto = mapper.map(this, ApplicationDTO.class);
		dto.setAppImageUrl(serverUrl + "/applications/" + id + "/img");
		return dto;
	}
}
