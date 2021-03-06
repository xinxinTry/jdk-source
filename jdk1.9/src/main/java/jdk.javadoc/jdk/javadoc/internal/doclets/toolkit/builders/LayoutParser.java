/*
 * Copyright (c) 2003, 2016, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package jdk.javadoc.internal.doclets.toolkit.builders;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import jdk.javadoc.internal.doclets.toolkit.Configuration;
import jdk.javadoc.internal.doclets.toolkit.util.DocFileIOException;
import jdk.javadoc.internal.doclets.toolkit.util.SimpleDocletException;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Parse the XML that specified the order of operation for the builders.  This
 * Parser uses SAX parsing.
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 *
 * @author Jamie Ho
 * @see SAXParser
 */
public class LayoutParser extends DefaultHandler {

    /**
     * The map of XML elements that have been parsed.
     */
    private final Map<String,XMLNode> xmlElementsMap;
    private XMLNode currentNode;
    private final Configuration configuration;
    private String currentRoot;
    private boolean isParsing;

    private LayoutParser(Configuration configuration) {
        xmlElementsMap = new HashMap<>();
        this.configuration = configuration;
    }

    /**
     * Return an instance of the BuilderXML.
     *
     * @param configuration the current configuration of the doclet.
     * @return an instance of the BuilderXML.
     */
    public static LayoutParser getInstance(Configuration configuration) {
        return new LayoutParser(configuration);
    }

    /**
     * Parse the XML specifying the layout of the documentation.
     *
     * @param root the name of the desired node
     * @return the list of XML elements parsed.
     * @throws DocFileIOException if there is a problem reading a user-supplied build file
     * @throws SimpleDocletException if there is a problem reading the system build file
     */
    public XMLNode parseXML(String root) throws DocFileIOException, SimpleDocletException {
        if (!xmlElementsMap.containsKey(root)) {
            try {
                currentRoot = root;
                isParsing = false;
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                InputStream in = configuration.getBuilderXML();
                saxParser.parse(in, this);
            } catch (IOException | ParserConfigurationException | SAXException e) {
                String message = (configuration.builderXMLPath == null)
                        ? configuration.getResources().getText("doclet.exception.read.resource",
                                Configuration.DEFAULT_BUILDER_XML, e)
                        : configuration.getResources().getText("doclet.exception.read.file",
                                configuration.builderXMLPath, e);
                throw new SimpleDocletException(message, e);
            }
        }
        return xmlElementsMap.get(root);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(String namespaceURI, String sName, String qName, Attributes attrs)
            throws SAXException {
        if (isParsing || qName.equals(currentRoot)) {
            isParsing = true;
            currentNode = new XMLNode(currentNode, qName);
            for (int i = 0; i < attrs.getLength(); i++)
                currentNode.attrs.put(attrs.getLocalName(i), attrs.getValue(i));
            if (qName.equals(currentRoot))
                xmlElementsMap.put(qName, currentNode);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(String namespaceURI, String sName, String qName)
    throws SAXException {
        if (! isParsing) {
            return;
        }
        currentNode = currentNode.parent;
        isParsing = ! qName.equals(currentRoot);
    }
}
