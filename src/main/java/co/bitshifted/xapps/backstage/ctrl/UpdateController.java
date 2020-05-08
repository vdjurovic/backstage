/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.ctrl;

import co.bitshifted.xapps.backstage.BackstageConstants;
import co.bitshifted.xapps.backstage.ctrl.partial.MultipartFileSender;
import co.bitshifted.xapps.backstage.exception.ContentException;
import co.bitshifted.xapps.backstage.model.CpuArch;
import co.bitshifted.xapps.backstage.model.OS;
import co.bitshifted.xapps.backstage.service.UpdateService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;


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

	@GetMapping(value = "/app/{appId}/download")
	public void downloadUpdateFile(
			@PathVariable("appId") String applicationId,
			@RequestParam("file-name") String fileName,
			@RequestParam("release") String release,
			@RequestParam("os") OS os,
			@RequestParam("cpu") CpuArch cpu,
			HttpServletResponse response) throws ContentException, IOException {
		var downloadInfo = updateService.getUpdateFile(fileName, applicationId, release, os, cpu);
		var in = downloadInfo.getSourceUri().toURL().openStream();
		var out = response.getOutputStream();
		response.setContentLengthLong(downloadInfo.getSize());
		response.setContentType(BackstageConstants.ZSYNC_MIME_TYPE);
		IOUtils.copy(in, out);
		response.flushBuffer();
	}

	@GetMapping(value = "/app/{appId}/download", headers = {HttpHeaders.RANGE})
	public void downloadPartialFile(
			@PathVariable("appId") String applicationId,
			@RequestParam("file-name") String fileName,
			@RequestParam("release") String release,
			@RequestParam("os") OS os,
			@RequestParam("cpu") CpuArch cpu,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		var downloadInfo = updateService.getUpdateFile(fileName, applicationId, release, os, cpu);
		var filePath = Path.of(downloadInfo.getSourceUri());
		MultipartFileSender.fromPath(filePath).with(request).with(response).serveResource();
	}

}
