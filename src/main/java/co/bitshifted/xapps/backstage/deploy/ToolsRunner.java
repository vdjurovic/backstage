/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.deploy;

import co.bitshifted.xapps.backstage.exception.DeploymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
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

	public Set<String> getApplicationModules(Path deploymentPackageDir) throws Exception {
		var xmlProcessor = new XmlProcessor(deploymentPackageDir.resolve("ignite-config.xml"));
		var mainArtifact = xmlProcessor.findMainArtifact();
		var mainArtifactPath = deploymentPackageDir.resolve(mainArtifact.get("path"));
		var mainArtifactModuleName = mainArtifactModuleName(mainArtifactPath);
		log.debug("Found main artifact: {}", mainArtifact);
		var outString = new StringWriter();
		var out = new PrintWriter(outString);
		var errString = new StringWriter();
		var err = new PrintWriter(errString);

		var argsList = new ArrayList<String>();
		argsList.add("--module-path");
		argsList.add(deploymentPackageDir.resolve("modules").toAbsolutePath().toString());
		argsList.add("--list-deps");
		argsList.add(mainArtifactPath.toAbsolutePath().toString());
		// if classpath dir exists, add it to arguments
		var classpath = deploymentPackageDir.resolve("classpath");
		if(Files.exists(classpath)) {
			log.debug("Classpath directory found, adding it to jdeps command");
			argsList.add("classpath/*");
		}
		log.debug("jdeps arguments: {}", argsList);
		var result = jdeps.run(out, err, argsList.toArray(new String[argsList.size()]));
		var modules = new HashSet<String>();
		if(result == 0){
			log.debug("jdeps output: {}", outString.toString());
			try(var reader = new BufferedReader(new StringReader(outString.toString()))){
				String line;
				while((line = reader.readLine()) != null) {
					modules.add(line.trim());
				}
			}

		} else {
			log.error("Error running jdeps: {}", errString.toString());
			throw new DeploymentException(errString.toString());
		}
		if(mainArtifactModuleName != null) {
			modules.add(mainArtifactModuleName);
		}
		log.debug("Found modules: {}", modules);
		return modules;
	}

	private String mainArtifactModuleName(Path artifactPath) {
		var finder = ModuleFinder.of(artifactPath);
		var modules = finder.findAll();
		if(modules.isEmpty()) {
			return null;
		}
		return modules.iterator().next().descriptor().name();
	}

	public Path createRuntimeImage(Set<String> modules, Path deploymentPackageDir) throws Exception {
		var jrePath = deploymentPackageDir.getParent().resolve("jre");
		var argList = new ArrayList<String>();
		argList.add("--module-path");
		argList.add(deploymentPackageDir.resolve("modules").toAbsolutePath().toString());
		argList.add("--add-modules");
		argList.add(createModulesArgs(modules));
		argList.add("--output");
		argList.add(jrePath.toAbsolutePath().toString());
		log.debug("jlink args: {}", argList);

		var outString = new StringWriter();
		var out = new PrintWriter(outString);
		var errString = new StringWriter();
		var err = new PrintWriter(errString);
		var result = jlink.run(out, err, argList.toArray(new String[argList.size()]));
		if(result == 0){
			log.debug("jlink output: {}", outString.toString());
		} else {
			log.error("Error running jlink: {}", errString.toString());
			throw new DeploymentException(errString.toString());
		}

		return jrePath;
	}

	private String createModulesArgs(Set<String> modules) {
		StringBuilder sb = new StringBuilder();
		modules.forEach(m -> sb.append(",").append(m));
		return sb.toString().substring(1);
	}
}
