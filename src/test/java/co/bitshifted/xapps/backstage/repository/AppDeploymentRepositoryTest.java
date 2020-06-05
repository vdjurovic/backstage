/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.repository;

import co.bitshifted.xapps.backstage.BackstageConstants;
import co.bitshifted.xapps.backstage.entity.AppDeployment;
import co.bitshifted.xapps.backstage.entity.Application;
import co.bitshifted.xapps.backstage.test.TestConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;

import static org.junit.Assert.*;

/**
 * @author Vladimir Djurovic
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@Transactional
public class AppDeploymentRepositoryTest {

	@Autowired
	private AppDeploymentRepository appDeploymentRepository;
	@Autowired
	private ApplicationRepository applicationRepository;
	AppDeployment deployment1;
	AppDeployment deployment2;
	private String appId;

	@Before
	public void setup(){
		var app = new Application();
		app.setName("app name");
		var out = applicationRepository.save(app);
		appId = out.getId();

		deployment1 = new AppDeployment();
		deployment1.setApplication(out);
		deployment1.setReleaseNumber("20200301-131415-120");
		deployment1.setReleaseTime(ZonedDateTime.now(BackstageConstants.UTC_ZONE_ID));
		appDeploymentRepository.save(deployment1);

		deployment2 = new AppDeployment();
		deployment2.setApplication(out);
		deployment2.setReleaseNumber("20200301-161415-080");
		deployment2.setReleaseTime(ZonedDateTime.now(BackstageConstants.UTC_ZONE_ID));
		appDeploymentRepository.save(deployment2);
	}

	@Test
	public void testFindLatestDeployment() {
		var outDeps = appDeploymentRepository.findFirstByApplication_IdOrderByReleaseNumberDesc(appId);
		assertEquals(deployment2.getReleaseNumber(), outDeps.getReleaseNumber());
	}
}
