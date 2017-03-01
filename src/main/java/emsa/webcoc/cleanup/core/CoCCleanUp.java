/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emsa.webcoc.cleanup.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author aanciaes
 */
public class CoCCleanUp {

    private final Logger logger = LogManager.getLogger(CoCCleanUp.class);

    private static final String CoC = "coc";
    private static final String EaR = "ear";
    private static final String CoP = "cop";

    //New file name
    private final String NEWFILENAME;

    //New clean file storage location
    private final String FILELOCATION;

    private String errorMessage;

    //Statistics
    private int total;
    //coc
    private int cocNotValid;
    private int cocValid;

    //ear
    private int earNotValid;
    private int earValid;

    //cop
    private int copNotValid;
    private int copValid;
    //

    private Document newDoc;
    private Element cocs;
    private Element ears;
    private Element cops;

    public CoCCleanUp(String newFileName, String newFileLocation) {
        total = 0;
        cocNotValid = 0;
        cocValid = 0;
        earNotValid = 0;
        earValid = 0;
        copNotValid = 0;
        copValid = 0;
        cocs = null;
        ears = null;
        cops = null;
        newDoc = null;
        errorMessage = null;

        NEWFILENAME = newFileName;
        FILELOCATION = newFileLocation + NEWFILENAME;
    }

    public int cleanDocument(InputStream stream) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(stream);
            doc.getDocumentElement().normalize();

            newDoc = dBuilder.newDocument();
            Element rootElement = newDoc.createElement("documents");
            newDoc.appendChild(rootElement);
            cocs = newDoc.createElement("cocs");
            rootElement.appendChild(cocs);
            ears = newDoc.createElement("ears");
            rootElement.appendChild(ears);
            cops = newDoc.createElement("cops");
            rootElement.appendChild(cops);

            checkBasicSintax(doc);

            NodeList nodeLst = doc.getElementsByTagName("documents").item(0).getChildNodes();

            int size = nodeLst.getLength();

            for (int i = 0; i < size; i++) {
                handleGroup(nodeLst.item(i));
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            newDoc.setXmlStandalone(true);
            DOMSource source = new DOMSource(newDoc);
            StreamResult output = new StreamResult(new File(FILELOCATION));

            transformer.transform(source, output);

            logger.info("File " + NEWFILENAME + " was cleaned");

            return 0;
        } catch (NullPointerException ex) {
            errorMessage = "Wrong XML syntax: " + ex.getMessage();
            return -1;
        } catch (SAXParseException ex) {
            errorMessage = "An error occured while parsing the file</br>Xml file Line Number: " + ex.getLineNumber() + "</br>" + ex.getMessage();
            logger.error(errorMessage);
            return -1;
        } catch (SAXException | ParserConfigurationException | TransformerException | IOException ex) {
            errorMessage = "An error occured";
            logger.error(errorMessage);
            return -1;
        }
    }

    public void handleGroup(Node group) {
        if (group.getNodeType() == Node.ELEMENT_NODE) {
            NodeList nodeList = group.getChildNodes();
            int size = group.getChildNodes().getLength();

            for (int i = 0; i < size; i++) {
                handleElement(nodeList.item(i));
            }
        }
    }

    public void handleElement(Node xNode) {

        if (xNode.getNodeType() == Node.ELEMENT_NODE) {
            total++;
            NodeList nodeList = xNode.getChildNodes();
            Node xDocument = null;

            String nodeType = xNode.getNodeName();
            String x = nodeType.concat("Document");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node n = nodeList.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(x)) {
                    xDocument = n;
                }
            }

            if (xDocument != null) {
                Element xDocuElement_e = (Element) xDocument;

                switch (nodeType) {
                    case CoC:
                        if (verifyValidity(xDocuElement_e)) {
                            Element e = (Element) newDoc.importNode(xNode, true);
                            cocs.appendChild(e);
                            cocValid++;
                        } else {
                            cocNotValid++;
                        }
                        break;

                    case EaR:
                        if (verifyValidity(xDocuElement_e)) {
                            Element e = (Element) newDoc.importNode(xNode, true);
                            ears.appendChild(e);
                            earValid++;
                        } else {
                            earNotValid++;
                        }
                        break;

                    case CoP:
                        if (verifyValidity(xDocuElement_e)) {
                            Element e = (Element) newDoc.importNode(xNode, true);
                            cops.appendChild(e);
                            copValid++;
                        } else {
                            copNotValid++;
                        }
                        break;

                    default:
                        break;
                }
            }

        }
    }

    public boolean verifyValidity(Element xDocumElement) {
        if (xDocumElement.getElementsByTagName("status").getLength() == 0) {
            throw new NullPointerException("'status' tag missing or mal-formed");
        } else {
            if (xDocumElement.getElementsByTagName("status").item(0).getTextContent().equals("not valid")) {
                return false;
            }
            if (xDocumElement.getElementsByTagName("status").item(0).getTextContent().equals("valid")) {
                return true;
            }
            return false;
        }
    }

    public void checkBasicSintax(Document doc) throws NullPointerException {
        if (doc.getElementsByTagName("documents").getLength() == 0) {
            throw new NullPointerException("'documents' tag missing or mal-formed");
        }
    }

    public String printHTMLStatistics() {
        String statistics = "Total number of nodes: " + total + "</br>"
                + "Total number of valid nodes: " + (cocValid + earValid + copValid)
                + "</br>Number of NOT valid nodes: " + (cocNotValid + earNotValid + copNotValid) + "</br>"
                + "Total number of CoC nodes: " + (cocValid + cocNotValid) + "</br>"
                + "Number of CoC valid nodes: " + cocValid
                + "</br>Number of CoC NOT valid nodes: " + cocNotValid + "</br>"
                + "Total number of EaR nodes: " + (earNotValid + earValid) + "</br>"
                + "Number of EaR valid nodes: " + earValid
                + "</br>Number of EaR NOT valid nodes: " + earNotValid + "</br>"
                + "Total number of CoP nodes: " + (copValid + copNotValid) + "</br>"
                + "Number of CoP valid nodes: " + copValid
                + "</br>Number of CoP NOT valid nodes: " + copNotValid + "</br>";

        return statistics;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
