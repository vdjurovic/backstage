/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.deploy;

import lombok.val;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Vladimir Djurovic
 */
public class XmlProcessor {

	private Document xmlDocument;
	private XPathFactory xpathFactory;


	public XmlProcessor(Path documentPath) throws ParserConfigurationException, SAXException, IOException {
		var docBuilderFactory = DocumentBuilderFactory.newInstance();
		var builder = docBuilderFactory.newDocumentBuilder();
		xmlDocument = builder.parse(documentPath.toString());
		xpathFactory = XPathFactory.newInstance();
	}

	public Map<String, String> findMainArtifact() throws XPathExpressionException {
		var xpath = xpathFactory.newXPath();
		var expression = xpath.compile("//dependency[@main-artifact='true']	");
		var node = (Element)expression.evaluate(xmlDocument, XPathConstants.NODE);
		var map = new HashMap<String, String>();
		map.put("scope", node.getAttribute("scope"));
		map.put("path", node.getAttribute("path"));
		return map;
	}
}
