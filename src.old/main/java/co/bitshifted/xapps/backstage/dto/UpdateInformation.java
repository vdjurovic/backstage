/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.dto;

import co.bitshifted.xapps.backstage.BackstageConstants;
import co.bitshifted.xapps.backstage.util.BackstageFunctions;
import com.sun.xml.txw2.annotation.XmlAttribute;
import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vladimir Djurovic
 */
@XmlRootElement(name = "update-info")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class UpdateInformation {
	@XmlElement(name = "release-number")
	private String releaseNumber;
	@XmlElementWrapper(name = "details")
	@XmlElement(name = "detail")
	private List<UpdateDetail> details = new ArrayList<>();

	public void addDetail(UpdateDetail detail) {
		details.add(detail);
	}
}
