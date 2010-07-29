/*
 Milyn - Copyright (C) 2006

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License (version 2.1) as published by the Free Software
 Foundation.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

 See the GNU Lesser General Public License for more details:
 http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.xml;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.milyn.io.StreamUtils;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * XMl utility methods.
 *
 * @author Tom Fennelly
 */

public class XmlUtil {

    /**
     * Document validation types.
     */
    public static enum VALIDATION_TYPE {
        /**
         * No validation.
         */
        NONE,
        /**
         * DTD based validation.
         */
        DTD,
        /**
         * XSD based validation.
         */
        XSD,
    }

    private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    public static final char[] LT = new char[] {'&', 'l', 't', ';'};
    public static final char[] GT = new char[] {'&', 'g', 't', ';'};
    public static final char[] AMP = new char[] {'&', 'a', 'm', 'p', ';'};
    public static final char[] QUOT = new char[] {'&', 'q', 'u', 'o', 't', ';'};
    public static final char[] APOS = new char[] {'&', 'a', 'p', 'o', 's', ';'};

    /**
     * Remove all entities from the supplied <code>Reader</code> stream
     * replacing them with their actual character values. <p/> Both the read and
     * write streams are returned unclosed.
     *
     * @param reader The read stream.
     * @param writer The write stream.
     */
    public static void removeEntities(Reader reader, Writer writer)
            throws IOException {
        int curChar = -1;
        StringBuffer ent = null;

        if (reader == null) {
            throw new IllegalArgumentException("null reader arg");
        } else if (writer == null) {
            throw new IllegalArgumentException("null writer arg");
        }

        ent = new StringBuffer(50);
        while ((curChar = reader.read()) != -1) {
            if (curChar == '&') {
                if (ent.length() > 0) {
                    writer.write(ent.toString());
                    ent.setLength(0);
                }
                ent.append((char) curChar);
            } else if (curChar == ';' && ent.length() > 0) {
                int entLen = ent.length();

                if (entLen > 1) {
                    if (ent.charAt(1) == '#') {
                        if (entLen > 2) {
                            char char2 = ent.charAt(2);

                            try {
                                if (char2 == 'x' || char2 == 'X') {
                                    if (entLen > 3) {
                                        writer.write(Integer.parseInt(ent
                                                .substring(3), 16));
                                    } else {
                                        writer.write(ent.toString());
                                        writer.write(curChar);
                                    }
                                } else {
                                    writer.write(Integer.parseInt(ent
                                            .substring(2)));
                                }
                            } catch (NumberFormatException nfe) {
                                // bogus character ref - leave as is.
                                writer.write(ent.toString());
                                writer.write(curChar);
                            }
                        } else {
                            writer.write("&#;");
                        }
                    } else {
                        Character character = HTMLEntityLookup
                                .getCharacterCode(ent.substring(1));

                        if (character != null) {
                            writer.write(character.charValue());
                        } else {
                            // bogus entity ref - leave as is.
                            writer.write(ent.toString());
                            writer.write(curChar);
                        }
                    }
                } else {
                    writer.write("&;");
                }

                ent.setLength(0);
            } else if (ent.length() > 0) {
                ent.append((char) curChar);
            } else {
                writer.write(curChar);
            }
        }

        if (ent.length() > 0) {
            writer.write(ent.toString());
        }
    }

    /**
     * Remove all entities from the supplied <code>String</code> stream
     * replacing them with there actual character values.
     *
     * @param string The string on which the operation is to be carried out.
     * @return The string with its entities rewriten.
     */
    public static String removeEntities(String string) {
        if (string == null) {
            throw new IllegalArgumentException("null string arg");
        }

        try {
            StringReader reader = new StringReader(string);
            StringWriter writer = new StringWriter();

            XmlUtil.removeEntities(reader, writer);

            return writer.toString();
        } catch (Exception excep) {
            excep.printStackTrace();
            return string;
        }
    }

    /**
     * Rewrite all entities from the supplied <code>Reader</code> stream
     * replacing them with their character reference equivalents. <p/> Example:
     * <b>&ampnbsp;</b> is rewriten as <b>&amp#160;</b> <p/> Both the read and
     * write streams are returned unclosed.
     *
     * @param reader The read stream.
     * @param writer The write stream.
     */
    public static void rewriteEntities(Reader reader, Writer writer)
            throws IOException {
        int curChar = -1;
        StringBuffer ent;
        char[] entBuf;

        if (reader == null) {
            throw new IllegalArgumentException("null reader arg");
        } else if (writer == null) {
            throw new IllegalArgumentException("null writer arg");
        }

        ent = new StringBuffer(50);
        entBuf = new char[50];
        while ((curChar = reader.read()) != -1) {
            if (curChar == '&') {
                if (ent.length() > 0) {
                    writer.write(ent.toString());
                    ent.setLength(0);
                }
                ent.append((char) curChar);
            } else if (curChar == ';' && ent.length() > 0) {
                int entLen = ent.length();

                if (entLen > 1) {
                    if (ent.charAt(1) == '#') {
                        // Already a character ref.
                        ent.getChars(0, ent.length(), entBuf, 0);
                        writer.write(entBuf, 0, ent.length());
                        writer.write(';');
                    } else {
                        Character character = HTMLEntityLookup
                                .getCharacterCode(ent.substring(1));

                        if (character != null) {
                            writer.write("&#");
                            writer.write(String.valueOf((int) character
                                    .charValue()));
                            writer.write(";");
                        } else {
                            // bogus entity ref - leave as is.
                            writer.write(ent.toString());
                            writer.write(curChar);
                        }
                    }
                } else {
                    writer.write("&;");
                }

                ent.setLength(0);
            } else if (ent.length() > 0) {
                ent.append((char) curChar);
            } else {
                writer.write(curChar);
            }
        }

        if (ent.length() > 0) {
            writer.write(ent.toString());
        }
    }

    /**
     * Parse the XML stream and return the associated W3C Document object.
     *
     * @param stream           The stream to be parsed.
     * @param validation       Validation type to be carried out on the document.
     * @param expandEntityRefs Expand entity References as per
     *                         {@link DocumentBuilderFactory#setExpandEntityReferences(boolean)}.
     * @return The W3C Document object associated with the input stream.
     */
    public static Document parseStream(InputStream stream, VALIDATION_TYPE validation,
                                       boolean expandEntityRefs) throws SAXException, IOException {
        return parseStream(stream, new LocalDTDEntityResolver(), validation,
                expandEntityRefs);
    }

    /**
     * Parse the XML stream and return the associated W3C Document object.
     *
     * @param stream           The stream to be parsed.
     * @param entityResolver   Entity resolver to be used during the parse.
     * @param validation       Validation type to be carried out on the document.
     * @param expandEntityRefs Expand entity References as per
     *                         {@link javax.xml.parsers.DocumentBuilderFactory#setExpandEntityReferences(boolean)}.
     * @return The W3C Document object associated with the input stream.
     */
    public static Document parseStream(InputStream stream,
                                       EntityResolver entityResolver, VALIDATION_TYPE validation,
                                       boolean expandEntityRefs) throws SAXException, IOException {

        return parseStream(new InputStreamReader(stream), entityResolver, validation, expandEntityRefs);
    }

    /**
     * Parse the XML stream and return the associated W3C Document object.
     *
     * @param stream           The stream to be parsed.
     * @param entityResolver   Entity resolver to be used during the parse.
     * @param validation       Validation type to be carried out on the document.
     * @param expandEntityRefs Expand entity References as per
     *                         {@link javax.xml.parsers.DocumentBuilderFactory#setExpandEntityReferences(boolean)}.
     * @return The W3C Document object associated with the input stream.
     */
    public static Document parseStream(Reader stream,
                                       EntityResolver entityResolver, VALIDATION_TYPE validation,
                                       boolean expandEntityRefs) throws SAXException, IOException {
        if (stream == null) {
            throw new IllegalArgumentException("null 'stream' arg in method call.");
        }

        try {
            String streamData = StreamUtils.readStream(stream);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder;

            // Setup validation...
            if (validation == VALIDATION_TYPE.DTD) {
                factory.setValidating(true);
            } else if (validation == VALIDATION_TYPE.XSD) {
                try {
                    Schema schema = getSchema(entityResolver);

                    schema.newValidator().validate(new StreamSource(new StringReader(streamData)));
                } catch (IllegalArgumentException e) {
                    throw new SAXException("Unable to validate document.  Installed parser '" + factory.getClass().getName() + "' doesn't support JAXP 1.2", e);
                }
            }

            factory.setExpandEntityReferences(expandEntityRefs);
            docBuilder = factory.newDocumentBuilder();
            if (validation == VALIDATION_TYPE.DTD) {
                docBuilder.setEntityResolver(entityResolver);
            }
            docBuilder.setErrorHandler(XMLParseErrorHandler.getInstance());

            return docBuilder.parse(new InputSource(new StringReader(streamData)));
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Unable to parse XML stream - XML Parser not configured correctly.", e);
        } catch (FactoryConfigurationError e) {
            throw new IllegalStateException("Unable to parse XML stream - DocumentBuilderFactory not configured correctly.", e);
        }
    }

    /**
     * Basic DOM namespace aware parse.
     * @param stream Document stream.
     * @return Document instance.
     */
    public static Document parseStream(InputStream stream) throws ParserConfigurationException, IOException, SAXException {
        return parseStream(new InputStreamReader(stream));
    }

    /**
     * Basic DOM namespace aware parse.
     * @param stream Document stream.
     * @return Document instance.
     */
    public static Document parseStream(Reader stream) throws ParserConfigurationException, IOException, SAXException {
        return XmlUtil.parseStream(stream, null);
    }

    /**
     * Basic DOM namespace aware parse.
     * @param stream Document stream.
     * @param errorHandler {@link ErrorHandler} to be set on the DocumentBuilder.
     *                      This can be used to controll error reporting. If null
     *                      the default error handler will be used.
     * @return Document instance.
     */
    public static Document parseStream(Reader stream, final ErrorHandler errorHandler) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;

        factory.setNamespaceAware(true);
        docBuilder = factory.newDocumentBuilder();
        if (errorHandler != null) {
            docBuilder.setErrorHandler(errorHandler);
        }
        return docBuilder.parse(new InputSource(stream));
    }

    private static Schema getSchema(EntityResolver entityResolver) throws SAXException, IOException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        if(entityResolver instanceof LocalXSDEntityResolver) {
            return schemaFactory.newSchema(((LocalXSDEntityResolver)entityResolver).getSchemaSources());
        }

        return schemaFactory.newSchema(new StreamSource(entityResolver.resolveEntity("default", "default").getByteStream()));
    }

    private static String ELEMENT_NAME_FUNC = "/name()";

    private static XPathFactory xPathFactory = XPathFactory.newInstance();

    /**
     * Get the W3C NodeList instance associated with the XPath selection
     * supplied.
     *
     * @param node  The document node to be searched.
     * @param xpath The XPath String to be used in the selection.
     * @return The W3C NodeList instance at the specified location in the
     *         document, or null.
     */
    public static NodeList getNodeList(Node node, String xpath) {
        if (node == null) {
            throw new IllegalArgumentException(
                    "null 'document' arg in method call.");
        } else if (xpath == null) {
            throw new IllegalArgumentException(
                    "null 'xpath' arg in method call.");
        }
        try {
            XPath xpathEvaluater = xPathFactory.newXPath();

            if (xpath.endsWith(ELEMENT_NAME_FUNC)) {
                return (NodeList) xpathEvaluater.evaluate(xpath.substring(0,
                        xpath.length() - ELEMENT_NAME_FUNC.length()), node,
                        XPathConstants.NODESET);
            } else {
                return (NodeList) xpathEvaluater.evaluate(xpath, node,
                        XPathConstants.NODESET);
            }
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException("bad 'xpath' expression ["
                    + xpath + "].");
        }
    }

    /**
     * Get the W3C Node instance associated with the XPath selection supplied.
     *
     * @param node  The document node to be searched.
     * @param xpath The XPath String to be used in the selection.
     * @return The W3C Node instance at the specified location in the document,
     *         or null.
     */
    public static Node getNode(Node node, String xpath) {
        NodeList nodeList = getNodeList(node, xpath);

        if (nodeList == null || nodeList.getLength() == 0) {
            return null;
        } else {
            return nodeList.item(0);
        }
    }

    /**
     * Get the String data associated with the XPath selection supplied.
     *
     * @param node  The node to be searched.
     * @param xpath The XPath String to be used in the selection.
     * @return The string data located at the specified location in the
     *         document, or an empty string for an empty resultset query.
     */
    public static String getString(Node node, String xpath) {
        NodeList nodeList = getNodeList(node, xpath);

        if (nodeList == null || nodeList.getLength() == 0) {
            return "";
        }

        if (xpath.endsWith(ELEMENT_NAME_FUNC)) {
            if (nodeList.getLength() > 0) {
                return nodeList.item(0).getNodeName();
            } else {
                return "";
            }
        } else {
            return serialize(nodeList);
        }
    }

    /**
     * Serialise the supplied W3C DOM subtree.
     * <p/>
     * The output is unformatted.
     *
     * @param nodeList The DOM subtree as a NodeList.
     * @return The subtree in serailised form.
     * @throws DOMException Unable to serialise the DOM.
     */
    public static String serialize(NodeList nodeList) throws DOMException {
        return serialize(nodeList, false);
    }

    /**
     * Serialise the supplied W3C DOM subtree.
     *
     * @param node The DOM node to be serialized.
     * @param format Format the output.
     * @return The subtree in serailised form.
     * @throws DOMException Unable to serialise the DOM.
     */
    public static String serialize(final Node node, boolean format) throws DOMException {
        StringWriter writer = new StringWriter();
        serialize(node, format, writer);
        return writer.toString();
    }

    /**
     * Serialise the supplied W3C DOM subtree.
     *
     * @param node The DOM node to be serialized.
     * @param format Format the output.
     * @param writer The target writer for serialization.
     * @throws DOMException Unable to serialise the DOM.
     */
    public static void serialize(final Node node, boolean format, Writer writer) throws DOMException {
        if(node.getNodeType() == Node.DOCUMENT_NODE) {
            serialize(node.getChildNodes(), format, writer);
        } else {
            serialize(new NodeList() {
                public Node item(int index) {
                    return node;
                }

                public int getLength() {
                    return 1;
                }
            }, format, writer);
        }
    }

    /**
     * Serialise the supplied W3C DOM subtree.
     *
     * @param nodeList The DOM subtree as a NodeList.
     * @param format Format the output.
     * @return The subtree in serailised form.
     * @throws DOMException Unable to serialise the DOM.
     */
    public static String serialize(NodeList nodeList, boolean format) throws DOMException {
        StringWriter writer = new StringWriter();
        serialize(nodeList, format, writer);
        return writer.toString();
    }

    /**
     * Serialise the supplied W3C DOM subtree.
     *
     * @param nodeList The DOM subtree as a NodeList.
     * @param format Format the output.
     * @param writer The target writer for serialization.
     * @throws DOMException Unable to serialise the DOM.
     */
    public static void serialize(NodeList nodeList, boolean format, Writer writer) throws DOMException {

        if (nodeList == null) {
            throw new IllegalArgumentException(
                    "null 'subtree' NodeIterator arg in method call.");
        }

        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer;

            if(format) {
                try {
                    factory.setAttribute("indent-number", new Integer(4));
                } catch(Exception e) {
                    // Ignore... Xalan may throw on this!!
                    // We handle Xalan indentation below (yeuckkk) ...
                }
            }
            transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            if(format) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "4");
            }

            int listLength = nodeList.getLength();

            // Iterate through the Node List.
            for (int i = 0; i < listLength; i++) {
                Node node = nodeList.item(i);

                if (XmlUtil.isTextNode(node)) {
                    writer.write(node.getNodeValue());
                } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                    writer.write(((Attr) node).getValue());
                } else if (node.getNodeType() == Node.ELEMENT_NODE) {
                    transformer.transform(new DOMSource(node), new StreamResult(writer));
                }
            }
        } catch (Exception e) {
            DOMException domExcep = new DOMException(
                    DOMException.INVALID_ACCESS_ERR,
                    "Unable to serailise DOM subtree.");
            domExcep.initCause(e);
            throw domExcep;
        }
    }

    /**
     * Is the supplied W3C DOM Node a text node.
     *
     * @param node The node to be tested.
     * @return True if the node is a text node, otherwise false.
     */
    public static boolean isTextNode(Node node) {
        short nodeType;

        if (node == null) {
            return false;
        }
        nodeType = node.getNodeType();

        return nodeType == Node.CDATA_SECTION_NODE
                || nodeType == Node.TEXT_NODE;
    }

    public static void encodeTextValue(char[] characters, int offset, int length, Writer writer) throws IOException {
        for(int i = offset; i < offset + length; i++) {
            char c = characters[i];
            switch(c) {
                case '<' :
                    writer.write(LT, 0, LT.length);
                    break;
                case '>' :
                    writer.write(GT, 0, GT.length);
                    break;
                case '&' :
                    writer.write(AMP, 0, AMP.length);
                    break;
                default:
                    writer.write(c);
            }
        }
    }

    public static void encodeAttributeValue(char[] characters, int offset, int length, Writer writer) throws IOException {
        for(int i = offset; i < offset + length; i++) {
            char c = characters[i];
            switch(c) {
                case '<' :
                    writer.write(LT, 0, LT.length);
                    break;
                case '>' :
                    writer.write(GT, 0, GT.length);
                    break;
                case '&' :
                    writer.write(AMP, 0, AMP.length);
                    break;
                case '\'' :
                    writer.write(APOS, 0, APOS.length);
                    break;
                case '\"' :
                    writer.write(QUOT, 0, QUOT.length);
                    break;
                default:
                    writer.write(c);
            }
        }
    }

    /**
     * XML Parse error handler.
     *
     * @author tfennelly
     */
    static class XMLParseErrorHandler implements ErrorHandler {

        /**
         * Singleton instance reference of this class.
         */
        private static XMLParseErrorHandler singleton = new XMLParseErrorHandler();

        /**
         * Private constructor.
         */
        private XMLParseErrorHandler() {
        }

        /**
         * Get this classes singleton reference.
         *
         * @return This classes singleton reference.
         */
        private static XMLParseErrorHandler getInstance() {
            return singleton;
        }

        /*
           * (non-Javadoc)
           *
           * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
           */
        public void warning(SAXParseException arg0) throws SAXException {
            throw arg0;
        }

        /*
           * (non-Javadoc)
           *
           * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
           */
        public void error(SAXParseException arg0) throws SAXException {
			throw arg0;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
		 */
		public void fatalError(SAXParseException arg0) throws SAXException {
			throw arg0;
		}
	}
}
