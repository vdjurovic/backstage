/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.service;

import co.bitshifted.xapps.backstage.entity.AppDeployment;
import co.bitshifted.xapps.backstage.model.CpuArch;
import co.bitshifted.xapps.backstage.model.OS;
import co.bitshifted.xapps.backstage.repository.AppDeploymentRepository;
import co.bitshifted.xapps.backstage.service.impl.BasicUpdateService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Vladimir Djurovic
 */
public class BasicUpdateServiceTest {

	private static final String APP_ID = "1234";
	private static final String RELEASE = "20200301-141516-230";

	@Mock
	private AppDeploymentRepository appDeploymentRepository;
	@InjectMocks
	private BasicUpdateService updateService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testHasUpdateAvailable() {
		var currentRelease = "20200301-141516-200";
		AppDeployment mockDeployment = mock(AppDeployment.class);
		when(mockDeployment.getReleaseNumber()).thenReturn(RELEASE);
		when(appDeploymentRepository.findFirstByApplication_IdOrderByReleaseNumberDesc(APP_ID)).thenReturn(mockDeployment);


		var result = updateService.hasUpdateAvailable(APP_ID, currentRelease);
		assertTrue(result);
	}

	@Test
	public void testUpdateInformation() {
		var currentRelease = "20200301-141516-200";
		AppDeployment mockDeployment = mock(AppDeployment.class);
		when(mockDeployment.getReleaseNumber()).thenReturn(RELEASE);
		when(appDeploymentRepository.findFirstByApplication_IdOrderByReleaseNumberDesc(APP_ID)).thenReturn(mockDeployment);

		var out = updateService.getUpdateInformation(APP_ID, OS.MAC_OS_X, CpuArch.X_64);
		assertEquals("/update/app/1234/download?release=20200301-141516-230&os=mac&cpu=x64&file-name=contents.zip.zsync", out.getContentsUrl());
		assertEquals("/update/app/1234/download?release=20200301-141516-230&os=mac&cpu=x64&file-name=modules.zip.zsync", out.getModulesUrl());
	}
}
