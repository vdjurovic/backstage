/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.service.impl;

import co.bitshifted.xapps.backstage.entity.Application;
import co.bitshifted.xapps.backstage.repository.ApplicationRepository;
import co.bitshifted.xapps.backstage.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * @author Vladimir Djurovic
 */
@Service
public class BasicApplicationService implements ApplicationService {

	@Autowired
	private ApplicationRepository applicationRepository;

	@Override
	public Application createApplication(Application app) {
		return applicationRepository.save(app);
	}

	@Override
	public Application getApplication(String id) {
		return applicationRepository.findById(id).get();
	}

	@Override
	public Page<Application> getApplications(Pageable pageable) {
		return applicationRepository.findAll(pageable);
	}
}
