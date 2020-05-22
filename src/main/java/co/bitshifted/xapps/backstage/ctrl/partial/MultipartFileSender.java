/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.ctrl.partial;

import co.bitshifted.xapps.backstage.BackstageConstants;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles downloads of partial content (HTTP Range requests).
 * Adapted from https://gist.github.com/davinkevin/b97e39d7ce89198774b4#file-multipartfilesender
 *
 * @author Vladimir Djurovic
 */
@Slf4j
public class MultipartFileSender {

	public static final int DEFAULT_BUFFER_SIZE = 20480; // ..bytes = 20KB.
	private static final long DEFAULT_EXPIRE_TIME = 604800000L; // ..ms = 1 week.

	Path filepath;
	HttpServletRequest request;
	HttpServletResponse response;

	public MultipartFileSender() {
	}

	public static MultipartFileSender fromPath(Path path) {
		return new MultipartFileSender().setFilepath(path);
	}

	public static MultipartFileSender fromFile(File file) {
		return new MultipartFileSender().setFilepath(file.toPath());
	}

	public static MultipartFileSender fromURIString(String uri) {
		return new MultipartFileSender().setFilepath(Paths.get(uri));
	}

	//** internal setter **//
	private MultipartFileSender setFilepath(Path filepath) {
		this.filepath = filepath;
		return this;
	}

	public MultipartFileSender with(HttpServletRequest httpRequest) {
		request = httpRequest;
		return this;
	}

	public MultipartFileSender with(HttpServletResponse httpResponse) {
		response = httpResponse;
		return this;
	}

	public void serveResource() throws Exception {
		if (response == null || request == null) {
			return;
		}

		if (!Files.exists(filepath)) {
			log.error("File doesn't exist at URI : {}", filepath.toAbsolutePath().toString());
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		Long length = Files.size(filepath);
		String fileName = filepath.getFileName().toString();
		FileTime lastModifiedObj = Files.getLastModifiedTime(filepath);
		String contentType = BackstageConstants.DEPLOYMENT_CONTENT_TYPE;

		long lastModified = LocalDateTime.ofInstant(lastModifiedObj.toInstant(), ZoneId.of(ZoneOffset.systemDefault().getId())).toEpochSecond(ZoneOffset.UTC);

		// Validate and process range -------------------------------------------------------------

		// Prepare some variables. The full Range represents the complete file.
		Range full = new Range(0, length - 1, length);
		List<Range> ranges = new ArrayList<>();

		// Validate and process Range and If-Range headers.
		String range = request.getHeader("Range");
		if (range != null) {

			// Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
			if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
				response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
				response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
				return;
			}


			// If any valid If-Range header, then process each part of byte range.
			if (ranges.isEmpty()) {
				for (String part : range.substring(6).split(",")) {
					// Assuming a file with length of 100, the following examples returns bytes at:
					// 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).
					long start = Range.sublong(part, 0, part.indexOf("-"));
					long end = Range.sublong(part, part.indexOf("-") + 1, part.length());

					if (start == -1) {
						start = length - end;
						end = length - 1;
					} else if (end == -1 || end > length - 1) {
						end = length - 1;
					}

					// Check if Range is syntactically valid. If not, then return 416.
					if (start > end) {
						response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
						response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
						return;
					}

					// Add range.
					ranges.add(new Range(start, end, length));
				}
			}
		}

		// Prepare and initialize response --------------------------------------------------------

		// Get content type by file name and set content disposition.
		String disposition = "inline";

		log.debug("Content-Type : {}", contentType);
		// Initialize response.
		response.reset();
		response.setBufferSize(DEFAULT_BUFFER_SIZE);
		response.setHeader("Content-Type", contentType);
		response.setHeader("Content-Disposition", disposition + ";filename=\"" + fileName + "\"");
		log.debug("Content-Disposition : {}", disposition);
		response.setHeader("Accept-Ranges", "bytes");
		response.setHeader("ETag", fileName);
		response.setDateHeader("Last-Modified", lastModified);
		response.setDateHeader("Expires", System.currentTimeMillis() + DEFAULT_EXPIRE_TIME);

		// Send requested file (part(s)) to client ------------------------------------------------
		var multipartBoundary = UUID.randomUUID().toString();

		// Prepare streams.
		try (InputStream input = new BufferedInputStream(Files.newInputStream(filepath));
			 OutputStream output = response.getOutputStream()) {

			if (ranges.isEmpty() || ranges.get(0) == full) {

				// Return full file.
				log.info("Return full file");
				response.setContentType(contentType);
				response.setHeader("Content-Range", "bytes " + full.start + "-" + full.end + "/" + full.total);
				response.setHeader("Content-Length", String.valueOf(full.length));
				Range.copy(input, output, length, full.start, full.length);

			} else if (ranges.size() == 1) {

				// Return single part of file.
				Range r = ranges.get(0);
				log.info("Return 1 part of file : from ({}) to ({})", r.start, r.end);
				response.setContentType(contentType);
				response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);
				response.setHeader("Content-Length", String.valueOf(r.length));
				response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

				// Copy single part range.
				Range.copy(input, output, length, r.start, r.length);

			} else {

				// Return multiple parts of file.
				response.setContentType("multipart/byteranges; boundary=" + multipartBoundary);
				response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

				// Cast back to ServletOutputStream to get the easy println methods.
				ServletOutputStream sos = (ServletOutputStream) output;

				// Copy multi part range.
				for (Range r : ranges) {
					log.info("Return multi part of file : from ({}) to ({})", r.start, r.end);
					// Add multipart boundary and header fields for every range.
					sos.println();
					sos.println("--" + multipartBoundary);
					sos.println("Content-Type: " + contentType);
					sos.println("Content-Range: bytes " + r.start + "-" + r.end + "/" + r.total);

					// Copy single part range of multi part range.
					Range.copy(input, output, length, r.start, r.length);
				}

				// End with multipart boundary.
				sos.println();
				sos.println("--" + multipartBoundary + "--");
			}
		}

	}
}
