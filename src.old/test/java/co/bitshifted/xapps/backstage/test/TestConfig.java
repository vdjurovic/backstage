/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.test;

import co.bitshifted.xapps.backstage.content.ContentMapping;
import org.mockito.Mockito;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;
import ru.yandex.qatools.embed.postgresql.distribution.Version;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * @author Vladimir Djurovic
 */
@Configuration
@ComponentScan(basePackages = {"co.bitshifted.xapps.backstage"})
@PropertySource(value = "classpath:test.properties")
public class TestConfig {

	@Bean
	public PostgresConfig postgresConfig() throws IOException {
		final PostgresConfig postgresConfig = new PostgresConfig(Version.V9_6_11,
				new AbstractPostgresConfig.Net("localhost", 60000),
				new AbstractPostgresConfig.Storage("backstage"),
				new AbstractPostgresConfig.Timeout(),
				new AbstractPostgresConfig.Credentials("user", "pass")
		);
		return postgresConfig;
	}

	@Bean
	@DependsOn("postgresProcess")
	public DataSource dataSource(PostgresConfig config) {
		DriverManagerDataSource ds = new DriverManagerDataSource();
		ds.setDriverClassName("org.postgresql.Driver");
		ds.setUrl("jdbc:postgresql://localhost:60000/backstage");
		ds.setUsername(config.credentials().username());
		ds.setPassword(config.credentials().password());
		return ds;
	}

	@Bean(destroyMethod = "stop")
	public PostgresProcess postgresProcess(PostgresConfig config) throws IOException {
		PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getDefaultInstance();
		PostgresExecutable exec = runtime.prepare(config);
		PostgresProcess process = exec.start();
		return process;
	}
}
