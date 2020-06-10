/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage;


import co.bitshifted.xapps.backstage.deploy.DeploymentConfig;
import co.bitshifted.xapps.backstage.deploy.DeploymentProcessTask;
import co.bitshifted.xapps.backstage.deploy.TargetDeploymentInfo;
import co.bitshifted.xapps.backstage.deploy.builders.MacDeploymentBuilder;
import co.bitshifted.xapps.backstage.deploy.builders.DeploymentBuilder;
import co.bitshifted.xapps.backstage.deploy.builders.WindowsDeploymentBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.util.function.Function;

@SpringBootApplication
@EnableScheduling
public class BackstageApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackstageApplication.class, args);
	}

	@Bean
	public Function<File, DeploymentProcessTask> deploymentTaskFactory() {
		return source ->  processTask(source);
	}

	@Bean
	@Scope("prototype")
	public DeploymentProcessTask processTask(File source) {
		return new DeploymentProcessTask(source);
	}

	@Bean
	public Function<TargetDeploymentInfo, DeploymentBuilder> deploymentBuilderFactory() {
		return source -> deploymentBuilder(source);
	}

	@Bean
	@Scope("prototype")
	public DeploymentBuilder deploymentBuilder(TargetDeploymentInfo deploymentInfo){
		switch (deploymentInfo.getTargetOs()){
			case MAC_OS_X:
				return new MacDeploymentBuilder(deploymentInfo);
			case WINDOWS:
			case LINUX:
				return new WindowsDeploymentBuilder(deploymentInfo);
		}
		throw new IllegalArgumentException("Unsupported target operating system");
	}

}
