/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.util;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class for working with archives.
 *
 * @author Vladimir Djurovic
 */
@Slf4j
public final class PackageUtil {

	private PackageUtil() {
		// private constructor to prevent instantiation
	}

	public static Path unpackZipArchive(Path zipFilePath) throws IOException {
		var parent = zipFilePath.getParent();
		var targetDir = Path.of(parent.toAbsolutePath().toString(), zipFilePath.toFile().getName().replaceAll("\\.", "_")).toFile();
		log.debug("Target directory for extraction: {}", targetDir.getName());
		try(var archive = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(zipFilePath.toFile())))) {

			ZipArchiveEntry entry;
			while((entry = archive.getNextZipEntry()) != null) {
				log.debug("Unpacking entry {}", entry.getName());
				var file = new File(targetDir, entry.getName());
				Files.createDirectories(file.getParentFile().toPath());
				IOUtils.copy(archive, new FileOutputStream(file));
			}
		}
		return targetDir.toPath();
	}

	public  static Path packZipArchive(Path sourceFolder) throws IOException {
		var parent = sourceFolder.getParent();
		var archiveFile = Path.of(parent.toAbsolutePath().toString(), sourceFolder.toFile().getName() + ".zip").toFile();
		try(var archive = new ZipArchiveOutputStream(new FileOutputStream(archiveFile))) {

			Files.walk(sourceFolder).forEach(p -> {
				var file = p.toFile();
				if(!file.isDirectory()) {
					log.debug("Zipping file {}", file.getName());
					var entry = new ZipArchiveEntry(file, filePathRelative(file.getAbsolutePath(), sourceFolder.toAbsolutePath().toString()));
					try(var in = new FileInputStream(file)) {
						archive.putArchiveEntry(entry);
						IOUtils.copy(in, archive);
						archive.closeArchiveEntry();
					} catch(IOException ex) {
						log.error("Failed to write archive entry {}", entry.getName());
						throw new RuntimeException(ex);
					}
				}
			});
			archive.finish();
		}
		return archiveFile.toPath();
	}

	private static String filePathRelative(String filePath, String parent) {
		if(filePath.startsWith(parent)) {
			return filePath.substring(parent.length() + 1);
		}
		return filePath;
	}
}
