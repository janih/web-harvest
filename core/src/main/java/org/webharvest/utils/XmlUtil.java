package org.webharvest.utils;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import org.w3c.dom.Document;
import org.webharvest.runtime.RuntimeConfig;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * XML utils - contains common logic for XML handling
 */
public class XmlUtil {

    public static void prettyPrintXml(Document doc, Writer writer) throws IOException {
        try {
            final Transformer serializer = TransformerFactory.newInstance().newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            serializer.setOutputProperty(OutputKeys.METHOD, "xml");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.transform(new DOMSource(doc), new StreamResult(writer));
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public static String prettyPrintXml(String xmlAsString) throws IOException, ParserConfigurationException, SAXException {
        final StringWriter writer = new StringWriter();
        prettyPrintXml(DocumentBuilderFactory.newInstance().newDocumentBuilder().
                parse(new InputSource(new StringReader(xmlAsString))),
                writer);
        return writer.toString();
    }

    /**
     * Evaluates specified XPath expression against given XML text and using given runtime configuration.
     *
     * @param xpath
     * @param xml
     * @param runtimeConfig
     * @return Instance of ListVariable that contains results.
     * @throws XPathException
     */
    public static ListVariable evaluateXPath(String xpath, String xml, RuntimeConfig runtimeConfig) throws XPathException {
        StaticQueryContext sqc = runtimeConfig.getStaticQueryContext();
        Configuration config = sqc.getConfiguration();

        XQueryExpression exp = runtimeConfig.getXQueryExpressionPool().getCompiledExpression(xpath);
        DynamicQueryContext dynamicContext = new DynamicQueryContext(config);
        StringReader reader = new StringReader(xml);

        dynamicContext.setContextItem(sqc.buildDocument(new StreamSource(reader)));

        return createListOfXmlNodes(exp, dynamicContext);
    }

    /**
     * Creates list variable of resulting XML nodes.
     *
     * @param exp
     * @param dynamicContext
     * @return
     * @throws XPathException
     */
    public static ListVariable createListOfXmlNodes(XQueryExpression exp, DynamicQueryContext dynamicContext) throws XPathException {
        final SequenceIterator iter = exp.iterator(dynamicContext);
        final ListVariable listVariable = new ListVariable();

        for (Item item = iter.next(); item != null; item = iter.next()) {
            listVariable.addVariable(new NodeVariable(
                    new XmlNodeWrapper(item, exp.getExecutable().getDefaultOutputProperties())));
        }

        return listVariable;
    }

}
