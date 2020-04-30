/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.repository;

import co.bitshifted.xapps.backstage.entity.AppDeployment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppDeploymentRepository extends CrudRepository<AppDeployment, Long> {

	AppDeployment findFirstByApplication_IdOrderByReleaseNumberDesc(String appId);
}
