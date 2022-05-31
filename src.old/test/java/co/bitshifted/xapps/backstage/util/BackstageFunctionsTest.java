/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.util;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Vladimir Djurovic
 */
public class BackstageFunctionsTest {

	@Test
	public void testGetReleaseNumber() {
		var file = mock(File.class);
		when(file.getName()).thenReturn("12345_20200102-130010-123");
		var out = BackstageFunctions.getReleaseNumberFromDeploymentDir(file);
		assertEquals("20200102-130010-123", out);
	}

	@Test (expected = IllegalStateException.class)
	public void testInvalidReleaseNumber() {
		var file = mock(File.class);
		when(file.getName()).thenReturn("12345_20200102-130010");
		BackstageFunctions.getReleaseNumberFromDeploymentDir(file);
	}

	@Test
	public void testGenerateServerUrl() {
		var request = mock(HttpServletRequest.class);
		when(request.getScheme()).thenReturn("http");
		when(request.getServerName()).thenReturn("my.server.host");
		when(request.getServerPort()).thenReturn(8000);
		when(request.getContextPath()).thenReturn("");

		var out = BackstageFunctions.generateServerUrl(request, "sample/path?param=1");
		assertEquals("http://my.server.host:8000/sample/path?param=1", out);
	}
}
