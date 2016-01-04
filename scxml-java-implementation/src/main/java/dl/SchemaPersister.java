package dl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class SchemaPersister {

    private static final String EXPORT_FILESYSTEM_ROOT = "C:/desarrollo/xsd";
    private static final String XSD_URL = "http://www.w3.org/2011/04/SCXML/scxml.xsd";




    // some caching of the http-responses
    private static Map<String, String> _httpContentCache = new HashMap<String, String>();


    public static void main(String[] args) {
        try {
            new SchemaPersister().doIt();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void doIt() throws Exception {


//      // if you need an inouse-Proxy
//      final String authUser = "xxxxx";
//      final String authPassword = "xxxx"
//
//      System.setProperty("http.proxyHost", "xxxxx");
//      System.setProperty("http.proxyPort", "xxxx");
//      System.setProperty("http.proxyUser", authUser);
//      System.setProperty("http.proxyPassword", authPassword);
//
//      Authenticator.setDefault(
//        new Authenticator() {
//          public PasswordAuthentication getPasswordAuthentication() {
//            return new PasswordAuthentication(authUser, authPassword.toCharArray());
//          }
//        }
//      );
//

        Set<SchemaElement> allElements = new HashSet<SchemaElement>();

//      URL url = new URL("file:/C:/xauslaender-nachrichten-administration.xsd");
        URL url = new URL(XSD_URL);


        allElements.add(new SchemaElement(url));


        for (SchemaElement e : allElements) {

            System.out.println("processing " + e);
            e.doAll();
        }


        System.out.println("done!");

    }


    class SchemaElement {

        private URL _url;
        private String _content;

        public List<SchemaElement> _imports;
        public List<SchemaElement> _includes;

        public SchemaElement(URL url) {
            this._url = url;
        }


        public void checkIncludesAndImportsRecursive() throws Exception {

            InputStream in = new ByteArrayInputStream(downloadContent().getBytes("UTF-8"));

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(in);
            List<Node> includeNodeList = null;
            List<Node> importNodeList = null;


            includeNodeList = getXpathAttribute(doc, "/*[local-name()='schema']/*[local-name()='include']");
            _includes = new ArrayList<SchemaElement>();

            for (Node element : includeNodeList) {

                Node sl = element.getAttributes().getNamedItem("schemaLocation");
                if (sl == null) {
                    System.out.println(_url + " defines one import but no schemaLocation");
                    continue;
                }

                String asStringAttribute = sl.getNodeValue();

                URL url = buildUrl(asStringAttribute, _url);

                SchemaElement tmp = new SchemaElement(url);
                tmp.setSchemaLocation(asStringAttribute);

                tmp.checkIncludesAndImportsRecursive();
                _includes.add(tmp);


            }

            importNodeList = getXpathAttribute(doc, "/*[local-name()='schema']/*[local-name()='import']");
            _imports = new ArrayList<SchemaElement>();

            for (Node element : importNodeList) {

                Node sl = element.getAttributes().getNamedItem("schemaLocation");
                if (sl == null) {
                    System.out.println(_url + " defines one import but no schemaLocation");
                    continue;
                }

                String asStringAttribute = sl.getNodeValue();
                URL url = buildUrl(asStringAttribute, _url);

                SchemaElement tmp = new SchemaElement(url);
                tmp.setSchemaLocation(asStringAttribute);

                tmp.checkIncludesAndImportsRecursive();

                _imports.add(tmp);
            }

            in.close();


        }


        private String schemaLocation;

        private void setSchemaLocation(String schemaLocation) {
            this.schemaLocation = schemaLocation;

        }

        // http://stackoverflow.com/questions/10159186/how-to-get-parent-url-in-java
        private URL buildUrl(String asStringAttribute, URL parent) throws Exception {

            if (asStringAttribute.startsWith("http")) {
                return new URL(asStringAttribute);
            }

            if (asStringAttribute.startsWith("file")) {
                return new URL(asStringAttribute);
            }

            // relative URL
            URI parentUri = parent.toURI().getPath().endsWith("/") ? parent.toURI().resolve("..") : parent.toURI().resolve(".");
            return new URL(parentUri.toURL().toString() + asStringAttribute);

        }


        public void doAll() throws Exception {


            System.out.println("READ ELEMENTS");
            checkIncludesAndImportsRecursive();

            System.out.println("PRINTING DEPENDENCYS");
            printRecursive(0);

            System.out.println("GENERATE OUTPUT");

            patchAndPersistRecursive(0);

        }


        public void patchAndPersistRecursive(int level) throws Exception {


            File f = new File(EXPORT_FILESYSTEM_ROOT + File.separator + this.getXDSName());

            System.out.println("FILENAME: " + f.getAbsolutePath());


            if (_imports.size() > 0) {

                for (int i = 0; i < level; i++) {
                    System.out.print("   ");
                }

                System.out.println("IMPORTS");
                for (SchemaElement kid : _imports) {
                    kid.patchAndPersistRecursive(level + 1);
                }

            }

            if (_includes.size() > 0) {

                for (int i = 0; i < level; i++) {
                    System.out.print("   ");
                }

                System.out.println("INCLUDES");
                for (SchemaElement kid : _includes) {
                    kid.patchAndPersistRecursive(level + 1);
                }

            }


            String contentTemp = downloadContent();

            for (SchemaElement i : _imports) {

                if (i.isHTTP()) {
                    contentTemp = contentTemp.replace(
                            "<xs:import schemaLocation=\"" + i.getSchemaLocation(),
                            "<xs:import schemaLocation=\"" + i.getXDSName());
                }

            }


            for (SchemaElement i : _includes) {

                if (i.isHTTP()) {
                    contentTemp = contentTemp.replace(
                            "<xs:include schemaLocation=\"" + i.getSchemaLocation(),
                            "<xs:include schemaLocation=\"" + i.getXDSName());
                }

            }


            FileOutputStream fos = new FileOutputStream(f);
            fos.write(contentTemp.getBytes("UTF-8"));
            fos.close();

            System.out.println("File written: " + f.getAbsolutePath());


        }


        public void printRecursive(int level) {

            for (int i = 0; i < level; i++) {
                System.out.print("   ");
            }

            System.out.println(_url.toString());

            if (this._imports.size() > 0) {

                for (int i = 0; i < level; i++) {
                    System.out.print("   ");
                }

                System.out.println("IMPORTS");
                for (SchemaElement kid : this._imports) {
                    kid.printRecursive(level + 1);
                }

            }

            if (this._includes.size() > 0) {

                for (int i = 0; i < level; i++) {
                    System.out.print("   ");
                }

                System.out.println("INCLUDES");
                for (SchemaElement kid : this._includes) {
                    kid.printRecursive(level + 1);
                }

            }
        }


        String getSchemaLocation() {
            return schemaLocation;
        }


        /**
         * removes html:// and replaces / with _
         *
         * @return
         */

        private String getXDSName() {


            String tmp = schemaLocation;

            // Root on local File-System -- just grap the last part of it
            if (tmp == null) {
                tmp = _url.toString().replaceFirst(".*/([^/?]+).*", "$1");
            }


            if (isHTTP()) {

                tmp = tmp.replace("http://", "");
                tmp = tmp.replace("/", "_");

            } else {

                tmp = tmp.replace("/", "_");
                tmp = tmp.replace("\\", "_");

            }

            return tmp;

        }


        private boolean isHTTP() {
            return _url.getProtocol().startsWith("http");
        }


        private String downloadContent() throws Exception {


            if (_content == null) {

                System.out.println("reading content from " + _url.toString());

                if (_httpContentCache.containsKey(_url.toString())) {
                    this._content = _httpContentCache.get(_url.toString());
                    System.out.println("Cache hit! " + _url.toString());
                } else {

                    System.out.println("Download " + _url.toString());
                    Scanner scan = new Scanner(_url.openStream(), "UTF-8");

                    if (isHTTP()) {
                        this._content = scan.useDelimiter("\\A").next();
                    } else {
                        this._content = scan.useDelimiter("\\Z").next();
                    }

                    scan.close();

                    if (this._content != null) {
                        _httpContentCache.put(_url.toString(), this._content);
                    }

                }


            }

            if (_content == null) {
                throw new NullPointerException("Content of " + _url.toString() + "is null ");
            }

            return _content;

        }


        private List<Node> getXpathAttribute(Document doc, String path) throws Exception {

            List<Node> returnList = new ArrayList<Node>();

            XPathFactory xPathfactory = XPathFactory.newInstance();

            XPath xpath = xPathfactory.newXPath();

            {
                XPathExpression expr = xpath.compile(path);

                NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

                for (int i = 0; i < nodeList.getLength(); i++) {

                    Node n = nodeList.item(i);

                    returnList.add(n);

                }
            }

            return returnList;

        }


        @Override
        public String toString() {

            if (_url != null) {
                return _url.toString();
            }

            return super.toString();

        }

    }


}