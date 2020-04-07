/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.content.impl;

import co.bitshifted.xapps.backstage.content.ContentMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.file.Path;

/**
 * @author Vladimir Djurovic
 */
@Component
public class FileSystemContentMapping implements ContentMapping {

	private final Path workspace;

	public FileSystemContentMapping(@Value("${content.workspace.location}") String workspaceLocation) {
		this.workspace = Path.of(workspaceLocation);
	}

	@Override
	public URI getWorkspaceUri() {
		return workspace.toUri();
	}
}
