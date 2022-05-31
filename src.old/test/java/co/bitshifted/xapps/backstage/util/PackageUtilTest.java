/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.util;


import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.Assert.assertTrue;

/**
 * @author Vladimir Djurovic
 */
public class PackageUtilTest {

	@Before
	public void setup() throws Exception {
		String workDir = System.getProperty("user.dir");
		var source = Path.of(workDir, "src", "test", "resources", "archive");
		var target = Path.of(workDir, "target", "archive");
		Files.walk(source).forEach(p -> {
			try {
				Path d = target.resolve(source.relativize(p));
				if( Files.isDirectory(p) ) {
					if( !Files.exists(d) )
						Files.createDirectory(d);
					return;
				}
				Files.copy(p, d, StandardCopyOption.REPLACE_EXISTING);
			} catch(IOException ex) {
				throw new RuntimeException(ex);
			}

		});
	}

	@After
	public void cleanup() throws Exception {
		String workDir = System.getProperty("user.dir");
		var archiveDir = Path.of(workDir, "target", "archive");
		if(Files.exists(archiveDir)) {
			FileUtils.deleteDirectory(archiveDir.toFile());
		}

		var zip = Path.of(workDir, "target", "archive.zip");
		Files.deleteIfExists(zip);
	}

	@Test
	public void testArchivePackaging() throws Exception {
		String workDir = System.getProperty("user.dir");
		var source = Path.of(workDir, "target", "archive");
		var result = PackageUtil.packZipArchive(source);
		assertTrue(Files.exists(result));
	}

	@Test
	public void testArchiveUnpack() throws Exception {
		String workDir = System.getProperty("user.dir");
		var source = Path.of(workDir, "target", "archive");
		var result = PackageUtil.packZipArchive(source);
		assertTrue(Files.exists(result));
		FileUtils.deleteDirectory(source.toFile());

		var out = PackageUtil.unpackZipArchive(result);
		assertTrue(Files.exists(out));
	}

	@Test
	public void unpackArchiveFolderName() throws Exception {
		String workDir = System.getProperty("user.dir");
		var source = Path.of(workDir, "target", "archive");
		var result = PackageUtil.packZipArchive(source);
		assertTrue(Files.exists(result));
		FileUtils.deleteDirectory(source.toFile());

		var out = PackageUtil.unpackZipArchive(result, "unpack-target");
		assertTrue(Files.exists(out));
	}


}
