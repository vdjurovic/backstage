/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.backstage.deploy;

import co.bitshifted.xapps.backstage.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vladimir Djurovic
 */
public class XmlProcessor {

	private Document xmlDocument;
	private XPathFactory xpathFactory;
	private final String releaseNumber;


	public XmlProcessor(Path documentPath, String releaseNumber) throws ParserConfigurationException, SAXException, IOException {
		var docBuilderFactory = DocumentBuilderFactory.newInstance();
		var builder = docBuilderFactory.newDocumentBuilder();
		xmlDocument = builder.parse(documentPath.toString());
		xpathFactory = XPathFactory.newInstance();
		this.releaseNumber = releaseNumber;
	}

	public XmlProcessor(Path documentPath) throws ParserConfigurationException, SAXException, IOException {
		this(documentPath, null);
	}

	public String createLauncherConfigXml(LauncherConfig launcherConfig) throws JAXBException {
		var context = JAXBContext.newInstance(LauncherConfig.class);
		var marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		var writer = new StringWriter();
		marshaller.marshal(launcherConfig, writer);
		return writer.toString();
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

	public DeploymentConfig getDeploymentConfig() throws XPathExpressionException {
		var deploymentConfigBuilder = DeploymentConfig.builder();
		var xpath = xpathFactory.newXPath();
		deploymentConfigBuilder.appId(applicationId(xpath))
				.appName(applicationName(xpath))
				.appVersion(attributeValueAsString(xpath, "//application/@version"))
				.icons(icons(xpath))
				.splashScreen(new FileInfo(
						attributeValueAsString(xpath, "//jvm/splash-screen/@file-name"),
						attributeValueAsString(xpath, "//jvm/splash-screen/@path")))
				.jdkProvider(jdkProvider(xpath))
				.jvmImplementation(jvmImplementation(xpath))
				.jdkVersion(jdkVersion(xpath))
				.launcherConfig(createLauncherConfig(xpath));
		return deploymentConfigBuilder.build();
	}

	private String applicationId(XPath xpath) throws XPathExpressionException {
		var expression = xpath.compile("//application/@id");
		return (String)expression.evaluate(xmlDocument, XPathConstants.STRING);
	}

	private String applicationName(XPath xpath) throws XPathExpressionException {
		var expression = xpath.compile("//application-name");
		var appNameNode = (Element)expression.evaluate(xmlDocument, XPathConstants.NODE);
		return appNameNode.getTextContent();
	}

	private List<FileInfo> icons(XPath xpath) throws XPathExpressionException {
		var expression = xpath.compile("//icons/icon");
		var elements = (NodeList)expression.evaluate(xmlDocument, XPathConstants.NODESET);
		var icons = new ArrayList<FileInfo>();
		for(int i =0;i < elements.getLength();i++) {
			var element = (Element)elements.item(i);
			icons.add(new FileInfo(element.getAttribute("file-name").trim(),element.getAttribute("path").trim()));
		}
		return icons;
	}


	private JdkProvider jdkProvider(XPath xpath) throws XPathExpressionException {
		var expression = xpath.compile("//jvm/@provider");
		return JdkProvider.valueOf((String)expression.evaluate(xmlDocument, XPathConstants.STRING));
	}

	private JvmImplementation jvmImplementation(XPath xpath) throws XPathExpressionException {
		var expression = xpath.compile("//jvm/@implementation");
		return JvmImplementation.valueOf((String)expression.evaluate(xmlDocument, XPathConstants.STRING));
	}

	private JdkVersion jdkVersion(XPath xpath) throws XPathExpressionException {
		var expression = xpath.compile("//jvm/@version");
		return JdkVersion.valueOf((String)expression.evaluate(xmlDocument, XPathConstants.STRING));
	}

	private LauncherConfig createLauncherConfig(XPath xpath) throws XPathExpressionException {
		var launcherConfig = new LauncherConfig();
		launcherConfig.setVersion(attributeValueAsString(xpath, "//application/@version"));
		launcherConfig.setReleaseNumber(releaseNumber);
		var jvmConfig = new JvmConfig();
		jvmConfig.setMainClass(nodeValueAsString(xpath, "//jvm/main-class"));
		jvmConfig.setModule(nodeValueAsString(xpath, "//jvm/module-name"));
		jvmConfig.setJvmProperties(nodeValueAsString(xpath, "//jvm-properties"));
		jvmConfig.setJvmOptions(nodeValueAsString(xpath, "//jvm/jvm-options"));
		jvmConfig.setJvmDir(nodeValueAsString(xpath, "//jvm/jvm-dir"));
		jvmConfig.setAddModules(nodeValueAsString(xpath, "//jvm/add-mmodules"));
		jvmConfig.setArguments(nodeValueAsString(xpath, "//jvm/arguments"));
		jvmConfig.setClasspath(nodeValueAsString(xpath, "//jvm/classpath"));
		jvmConfig.setJar(nodeValueAsString(xpath, "//jvm/jar"));
		jvmConfig.setSplashScreen(attributeValueAsString(xpath, "//jvm/splash-screen/@file-name"));

		var server = new Server();
		server.setBaseUrl(attributeValueAsString(xpath, "//server/@base-url"));
		launcherConfig.setServer(server);

		launcherConfig.setJvm(jvmConfig);

		return launcherConfig;
	}

	private String nodeValueAsString(XPath xpath, String expressionString) throws XPathExpressionException {
		var expression = xpath.compile(expressionString);
		var node = (Element)expression.evaluate(xmlDocument, XPathConstants.NODE);
		return node != null ? node.getTextContent() : null;
	}

	private String attributeValueAsString(XPath xpath, String expressionString) throws XPathExpressionException {
		var expression = xpath.compile(expressionString);
		return (String)expression.evaluate(xmlDocument, XPathConstants.STRING);
	}
}
