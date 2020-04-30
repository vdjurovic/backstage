/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.ctrl;

import co.bitshifted.xapps.backstage.service.UpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Vladimir Djurovic
 */
@RestController
@RequestMapping(value = "/update")
public class UpdateController {

	@Autowired
	private UpdateService updateService;

	@GetMapping(value = "/app/{appId}/release/{releaseNumber}")
	public ResponseEntity<String> checkUpdates(@PathVariable  String appid, @PathVariable String releaseNumber) {
		var status = updateService.hasUpdateAvailable(appid, releaseNumber);
		if(status) {
			return ResponseEntity.ok("links");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
		}
	}
}
