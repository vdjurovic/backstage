/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.util;

import co.bitshifted.xapps.backstage.BackstageConstants;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

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

	public static Path unpackZipArchive(Path zipFilePath, String targetDirName) throws IOException {
		var parent = zipFilePath.getParent();
		var targetDir = Path.of(parent.toAbsolutePath().toString(), targetDirName).toFile();
		log.debug("Target directory for extraction: {}", targetDir.getName());
		try (var archive = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(zipFilePath.toFile())))) {

			ZipArchiveEntry entry;
			while ((entry = archive.getNextZipEntry()) != null) {
				log.debug("Unpacking entry {}", entry.getName());
				var file = new File(targetDir, entry.getName());
				if (entry.getName().endsWith("/")) {
					Files.createDirectories(file.toPath());
				} else {
					Files.createDirectories(file.getParentFile().toPath());
					IOUtils.copy(archive, new FileOutputStream(file));
				}

			}
		}
		return targetDir.toPath();
	}

	public static Path unpackZipArchive(Path zipFilePath) throws IOException {
		var dirName = zipFilePath.toFile().getName().substring(0, zipFilePath.toFile().getName().lastIndexOf(".zip"));
		return unpackZipArchive(zipFilePath, dirName);
	}

	public static Path packZipArchive(Path sourceFolder) throws IOException {
		var parent = sourceFolder.getParent();
		var archiveFile = Path.of(parent.toAbsolutePath().toString(), sourceFolder.toFile().getName() + ".zip").toFile();
		try (var archive = new ZipArchiveOutputStream(new FileOutputStream(archiveFile))) {
			Files.walk(sourceFolder).forEach(p -> {
				var file = p.toFile();
				if (!file.isDirectory()) {
					log.debug("Zipping file {}", file.getName());
					var entry = new ZipArchiveEntry(file, filePathRelative(file.getAbsolutePath(), sourceFolder.toAbsolutePath().toString()));
					try (var in = new FileInputStream(file)) {
						archive.putArchiveEntry(entry);
						IOUtils.copy(in, archive);
						archive.closeArchiveEntry();
					} catch (IOException ex) {
						log.error("Failed to write archive entry {}", entry.getName());
						throw new RuntimeException(ex);
					}
				}
			});
			archive.finish();
		}
		return archiveFile.toPath();
	}

	public static Path zipSingleFile(Path sourceFile) throws IOException {
		var parent = sourceFile.getParent();
		var archiveFile = Path.of(parent.toAbsolutePath().toString(), sourceFile.toFile().getName() + ".zip").toFile();
		try (var archive = new ZipArchiveOutputStream(new FileOutputStream(archiveFile))) {
			var file = sourceFile.toFile();
			if (file.isDirectory()) {
				throw new IOException("Can not zip a directory");
			}
			var entry = new ZipArchiveEntry(file, file.getName());
			try (var in = new FileInputStream(file)) {
				archive.putArchiveEntry(entry);
				IOUtils.copy(in, archive);
				archive.closeArchiveEntry();
			} catch (IOException ex) {
				log.error("Failed to write archive entry {}", entry.getName());
				throw new RuntimeException(ex);
			}
			archive.finish();
		}
		return archiveFile.toPath();
	}

	public static Path packZipDeterministic(Path sourceFolder) throws IOException {
		var parent = sourceFolder.getParent();
		var archiveFile = Path.of(parent.toAbsolutePath().toString(), sourceFolder.toFile().getName() + ".zip").toFile();
		var filesList = new ArrayList<File>();
		Files.walk(sourceFolder).forEach(p -> {
			if (!p.toFile().isDirectory()) {
				filesList.add(p.toFile());
				System.out.println("Added file " + p.toString());
			}

		});

		try (var archive = new ZipArchiveOutputStream(new FileOutputStream(archiveFile))) {
			filesList.stream().sorted().forEach(file -> {
				log.debug("Zipping file {}", file.getName());
				var entry = new ZipArchiveEntry(file, filePathRelative(file.getAbsolutePath(), sourceFolder.toAbsolutePath().toString()));
				entry.setCreationTime(FileTime.fromMillis(10000000));
				entry.setLastModifiedTime(FileTime.fromMillis(12000000));
				entry.setLastAccessTime(FileTime.fromMillis(12000000));
				if(file.canExecute()) {
					entry.setComment(BackstageConstants.ZIP_ENTRY_EXEC_COMMENT);
				}
				try (var in = new FileInputStream(file)) {
					archive.putArchiveEntry(entry);
					IOUtils.copy(in, archive);
					archive.closeArchiveEntry();
				} catch (IOException ex) {
					log.error("Failed to write archive entry {}", entry.getName());
					throw new RuntimeException(ex);
				}
			});
			archive.finish();
		}
		return archiveFile.toPath();
	}

	private static String filePathRelative(String filePath, String parent) {
		if (filePath.startsWith(parent)) {
			return filePath.substring(parent.length() + 1);
		}
		return filePath;
	}
}
