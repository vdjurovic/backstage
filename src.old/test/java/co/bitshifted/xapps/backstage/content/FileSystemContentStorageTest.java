/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.content;

import co.bitshifted.xapps.backstage.test.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.util.Random;

/**
 * @author Vladimir Djurovic
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
public class FileSystemContentStorageTest {

	@Autowired
	private ContentStorage contentStorage;

	@Test
	public void deploymentPackageUploadTest() throws Exception {
		var random = new Random();
		var data = new byte[100];
		random.nextBytes(data);
		ByteArrayInputStream bin = new ByteArrayInputStream(data);
		var out = contentStorage.uploadDeploymentPackage(bin, "abcd");
	}
}
