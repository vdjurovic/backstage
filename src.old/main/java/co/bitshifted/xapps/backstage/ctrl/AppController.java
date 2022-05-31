/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */


package co.bitshifted.xapps.backstage.ctrl;

import co.bitshifted.xapps.backstage.dto.ApplicationDTO;
import co.bitshifted.xapps.backstage.entity.Application;
import co.bitshifted.xapps.backstage.service.ApplicationService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Vladimir Djurovic <vdjurovic@vektorsoft.com>
 */
@RestController
@RequestMapping(value = "/applications")
public class AppController {

	@Autowired
	private ApplicationService applicationService;
//	@Autowired
//	private ContentStorage contentStorage;

	@RequestMapping(method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<ApplicationDTO> createApplication(@RequestBody ApplicationDTO appDto) {
		var app = appDto.convertToEntity();
		var created = applicationService.createApplication(app);
		return ResponseEntity.status(HttpStatus.CREATED).body(created.convertToDto());
	}

    @RequestMapping(method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody List<ApplicationDTO> listApplications(@RequestParam(name = "page", required = false, defaultValue = "0")  Integer page,
															   @RequestParam(name = "size", required = false, defaultValue = "20") Integer size,
															   HttpServletRequest request) {
		Page<Application> apps = applicationService.getApplications(PageRequest.of(page, size));
		String baseUrl = baseUrl(request);
		return apps.stream().map(app -> app.convertToDto(baseUrl)).collect(Collectors.toList());
    }

//    @RequestMapping(method = RequestMethod.POST, value = "/{appId}/img", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ApplicationDTO addApplicationImage(@RequestParam("file") MultipartFile file, @PathVariable("appId") String appId, HttpServletRequest request) throws IOException, ContentException {
//		applicationService.setApplicationImage(file.getInputStream(), appId);
//		return applicationService.getApplication(appId).convertToDto(baseUrl(request));
//	}
//
//	@RequestMapping(method = RequestMethod.GET, value = "/{appId}/img", produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE})
//	public ResponseEntity<byte[]> getAppImage(@PathVariable("appId") String appId) throws ContentException, IOException {
//		Application app = applicationService.getApplication(appId);
//		InputStream in = contentStorage.getBinaryData(app.getAppImageHash());
//		byte[] data = IOUtils.toByteArray(in);
//		return new ResponseEntity<>(data, HttpStatus.OK);
//	}

	private String baseUrl(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		sb.append(request.getScheme()).append("://");
		sb.append(request.getServerName());
		int port = request.getServerPort();
		if(port != 0) {
			sb.append(":").append(port);
		}
		sb.append(request.getContextPath());
		return sb.toString();
	}
}
