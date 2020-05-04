/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.ctrl;

import co.bitshifted.xapps.backstage.model.CpuArch;
import co.bitshifted.xapps.backstage.model.OS;
import co.bitshifted.xapps.backstage.service.UpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


/**
 * @author Vladimir Djurovic
 */
@RestController
@RequestMapping(value = "/update")
public class UpdateController {

	@Autowired
	private UpdateService updateService;

	@GetMapping(value = "/app/{appId}/release/{releaseNumber}")
	public ResponseEntity<String> checkUpdates(HttpServletRequest request, @PathVariable  String appId, @PathVariable String releaseNumber,
											   @RequestParam(name = "os") OS os, @RequestParam(name = "cpu") CpuArch cpuArch) {
		var status = updateService.hasUpdateAvailable(appId, releaseNumber);
		if(status) {
			var updateInfo = updateService.getUpdateInformation(appId, os, cpuArch);
			return ResponseEntity.ok(updateInfo.asString(request));
		} else {
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
		}
	}
}
