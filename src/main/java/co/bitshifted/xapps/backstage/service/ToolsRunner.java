/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.spi.ToolProvider;

/**
 * @author Vladimir Djurovic
 */
@Component
@Slf4j
public class ToolsRunner {

	private ToolProvider jdeps;
	private ToolProvider jlink;

	@PostConstruct
	public void init() {
		jdeps = ToolProvider.findFirst("jdeps").orElseThrow();
		jlink = ToolProvider.findFirst("jlink").orElseThrow();
	}

	public Set<String> getApplicationModulesList(Path deploymentPackageDir) {
		var out = new PrintStream(new String());
		var err = new PrintStream(new String());
		var result = jdeps.run(out, err, "--module-path", deploymentPackageDir.resolve("modules").toAbsolutePath().toString(), "--list-deps");
	}
}
