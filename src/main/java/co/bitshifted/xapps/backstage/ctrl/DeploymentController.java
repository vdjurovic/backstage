/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.ctrl;

import co.bitshifted.xapps.backstage.content.ContentStorage;
import co.bitshifted.xapps.backstage.dto.DeploymentStatusDTO;
import co.bitshifted.xapps.backstage.exception.ContentException;
import co.bitshifted.xapps.backstage.exception.DeploymentException;
import co.bitshifted.xapps.backstage.service.DeploymentService;
import co.bitshifted.xapps.backstage.util.BackstageFunctions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static co.bitshifted.xapps.backstage.BackstageConstants.DEPLOYMENT_CONTENT_TYPE;
import static co.bitshifted.xapps.backstage.BackstageConstants.DEPLOYMENT_STATUS_HEADER;

/**
 * @author Vladimir Djurovic
 */
@RestController
@RequestMapping("/deployment")
@Slf4j
public class DeploymentController {

	@Autowired
	private DeploymentService deploymentService;
	@Autowired
	private ContentStorage contentStorage;

	@RequestMapping(method = RequestMethod.POST, value = "/{appId}/content", consumes = DEPLOYMENT_CONTENT_TYPE)
	public ResponseEntity<Object> handleDeploymentUpload(HttpServletRequest request, @PathVariable String appId) throws DeploymentException, ContentException {
		String contentType = request.getContentType();
		if (!DEPLOYMENT_CONTENT_TYPE.equals(contentType)) {
			throw new DeploymentException("Invalid content type");
		}
		try {
			var deploymentPackagePath = Path.of(contentStorage.uploadDeploymentPackage(request.getInputStream(), appId));
			boolean valid = deploymentService.validateDeploymentContent(deploymentPackagePath);
			if (!valid) {
				throw new DeploymentException("Invalid or corrupt deployment archive");
			}
			deploymentService.processContent(deploymentPackagePath, appId);
			String statusUrl = BackstageFunctions.generateServerUrl(request, "/deployment/status/" + deploymentPackagePath.getParent().getFileName().toString());
			log.debug("Deployment status URL: {}", statusUrl);
			return ResponseEntity.accepted().header(DEPLOYMENT_STATUS_HEADER, statusUrl).build();
		} catch (IOException ex) {
			throw new DeploymentException(ex);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value="/status/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	DeploymentStatusDTO getDeploymentStatus(@PathVariable("id")  String id) {
		return deploymentService.getDeploymentStatus(id).convertToDto();
	}

}
