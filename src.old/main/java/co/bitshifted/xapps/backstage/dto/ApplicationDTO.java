/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.dto;

import co.bitshifted.xapps.backstage.entity.Application;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.modelmapper.ModelMapper;

/**
 * @author Vladimir Djurovic
 */
@Data
public class ApplicationDTO {

	@JsonIgnore
	private final ModelMapper mapper = new ModelMapper();

	private String id;
	private String name;
	private String headline;
	private String description;
	private String appImageUrl;

	public Application convertToEntity() {
		return mapper.map(this, Application.class);
	}
}
