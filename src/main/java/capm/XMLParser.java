package capm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;

public class XMLParser {

    private static final Logger log = LogManager.getLogger("XMLParser");

    public String getVCMFVersionFromXMLResponse (String xml) {
        String id;
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            Document document = documentBuilder.parse(input);
            //Document document = documentBuilder.parse(SITE_URL);
            Node root = document.getDocumentElement();
            NodeList ids = ((Element)root).getElementsByTagName("Version");
//            for (int i=0; i<ids.getLength(); i++) {
//                metricFamilyIDs.add(ids.item(i).getTextContent());
//            }
            id = ids.item(0).getTextContent();
        } catch (Exception e) {
            return null;
        }
        return id;
    }

}
