/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaResources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletContext;
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
 * @author miguel
 */
public class CoCCleanUp {
    
    private final Logger logger = LogManager.getLogger(CoCCleanUp.class);

    //New file name
    private final String NEWFILENAME;

    //New clean file storage location
    private final String FILELOCATION;

    private String errorMessage;
    private int notValid;
    private int valid;

    private Document newDoc;
    private Element cocs;

    public CoCCleanUp(String newFileName, String newFileLocation) {
        notValid = 0;
        valid = 0;
        cocs = null;
        newDoc = null;
        errorMessage=null;
        
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

            NodeList nodeLst = doc.getElementsByTagName("coc");

            int size = nodeLst.getLength();

            for (int i = 0; i < size; i++) {
                handleCoC(nodeLst.item(i));
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            newDoc.setXmlStandalone(true);
            DOMSource source = new DOMSource(newDoc);
            StreamResult output = new StreamResult(new File(FILELOCATION));

            transformer.transform(source, output);
            
            logger.info("File " + NEWFILENAME + " was cleaned");

            return 0;
            
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

    public void handleCoC(Node cocNode) {
        NodeList nodeList = cocNode.getChildNodes();
        Node cocDocument = null;

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node n = nodeList.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("cocDocument")) {
                cocDocument = n;
            }
        }

        if (cocDocument != null) {
            Element cocDocument_e = (Element) cocDocument;
            
            if (handleCoCDocument(cocDocument_e)) {
                Element e = (Element) newDoc.importNode(cocNode, true);
                cocs.appendChild(e);
            }
        }
    }

    public boolean handleCoCDocument(Element cocDocumElement) {
        if (cocDocumElement.getElementsByTagName("status").item(0).getTextContent().equals("not valid")) {
            notValid++;
            return false;
        }
        if (cocDocumElement.getElementsByTagName("status").item(0).getTextContent().equals("valid")) {
            valid++;
            return true;
        }
        return false;
    }

    public String printHTMLStatistics() {
        String statistics = "Total number of nodes: " + (valid + notValid) + "</br>" +
        "Number of valid nodes: " + valid +
        "</br>Number of NOT valid nodes: " + notValid + "</br>";
        
        return statistics;
    }
    
    public String getErrorMessage () {
        return errorMessage;
    }
}
