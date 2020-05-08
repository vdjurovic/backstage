/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.ctrl.partial;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Vladimir Djurovic
 */
public class Range {

	long start;
	long end;
	long length;
	long total;

	/**
	 * Construct a byte range.
	 * @param start Start of the byte range.
	 * @param end End of the byte range.
	 * @param total Total length of the byte source.
	 */
	public Range(long start, long end, long total) {
		this.start = start;
		this.end = end;
		this.length = end - start + 1;
		this.total = total;
	}

	public static long sublong(String value, int beginIndex, int endIndex) {
		String substring = value.substring(beginIndex, endIndex);
		return (substring.length() > 0) ? Long.parseLong(substring) : -1;
	}

	public static void copy(InputStream input, OutputStream output, long inputSize, long start, long length) throws IOException {
		byte[] buffer = new byte[MultipartFileSender.DEFAULT_BUFFER_SIZE];
		int read;

		if (inputSize == length) {
			// Write full range.
			while ((read = input.read(buffer)) > 0) {
				output.write(buffer, 0, read);
				output.flush();
			}
		} else {
			input.skip(start);
			long toRead = length;

			while ((read = input.read(buffer)) > 0) {
				if ((toRead -= read) > 0) {
					output.write(buffer, 0, read);
					output.flush();
				} else {
					output.write(buffer, 0, (int) toRead + read);
					output.flush();
					break;
				}
			}
		}
	}
}
