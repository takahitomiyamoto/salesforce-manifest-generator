package com.smg.service;

import com.sforce.soap.metadata.DescribeMetadataObject;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ListMetadataQuery;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectionException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.smg.util.CommonUtils;

public class MetadataService {

    public enum MetadataType {
        Dashboard,
        Document,
        EmailTemplate,
        Report;
    }

    public enum MetadataTypeFolder {
        DashboardFolder,
        DocumentFolder,
        EmailFolder,
        ReportFolder;
    }

    private static final String CREDENTIALS_FILE = CommonUtils.CREDENTIALS_FILE;
    private static final String FILE_ENCODING_UTF8 = CommonUtils.FILE_ENCODING_UTF8;
    private static final String ELEMENT_PACKAGE = "Package";
    private static final String ELEMENT_XMLNS = "xmlns";
    private static final String ELEMENT_TYPES = "types";
    private static final String ELEMENT_MEMBERS = "members";
    private static final String ELEMENT_NAME = "name";
    private static final String ELEMENT_VERSION = "version";
    private static final String MESSAGE_SUCCESS = "successfully gererated: ";
    private static final String PROPERTY_INDENT_AMOUNT = "{http://xml.apache.org/xslt}indent-amount";
    private static final String VALUE_XMLNS = "http://soap.sforce.com/2006/04/metadata";
    private static final String VALUE_YES = "Yes";
    private static final String VALUE_XML = "xml";
    private static final String VALUE_INDENT_AMOUNT = "4";
    private static final String MANIFEST_FILE_TEMP = "package_temp.xml";
    private static final String MANIFEST_FILE = "package.xml";
    private static final String MANIFEST_FILE_MANAGED = "package_managed.xml";
    private static final String MANIFEST_FILE_UNLOCKED = "package_unlocked.xml";
    private static final String MANIFEST_FILE_UNMANAGED = "package_unmanaged.xml";
    private static final String[] manageableStateManaged = {"beta", "deleted", "deprecated", "installed", "released"};
    private static final String[] manageableStateUnmanaged = {"unmanaged"};
    private static SimpleDateFormat simpleDate = CommonUtils.simpleDate;

    private final MetadataConnection metadataConnection;
    private final Map<String, String> metadataTypeFolderMap = new HashMap<String, String>();

    public MetadataService(MetadataConnection metadataConnection) {
        this.metadataConnection = metadataConnection;
    }

    private void setMetadataTypeFolderMap() {
        this.metadataTypeFolderMap.put(MetadataType.Dashboard.toString(), MetadataTypeFolder.DashboardFolder.toString());
        this.metadataTypeFolderMap.put(MetadataType.Document.toString(), MetadataTypeFolder.DocumentFolder.toString());
        this.metadataTypeFolderMap.put(MetadataType.EmailTemplate.toString(), MetadataTypeFolder.EmailFolder.toString());
        this.metadataTypeFolderMap.put(MetadataType.Report.toString(), MetadataTypeFolder.ReportFolder.toString());
    }

    private Document setInitialDocument()
      throws ParserConfigurationException {
        final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        final Document doc = docBuilder.newDocument();

        doc.setXmlStandalone(true);
        return doc;
    }

    private Element setRootElement(Document initialDocument) {
        final Element rootElement = initialDocument.createElement(ELEMENT_PACKAGE);
        final Attr attr = initialDocument.createAttribute(ELEMENT_XMLNS);

        attr.setValue(VALUE_XMLNS);
        rootElement.setAttributeNode(attr);
        return rootElement;
    }

    private List<String> getMetadataTypeList(final Double apiVersion)
      throws ConnectionException {
        final List<String> dmoStringList = new ArrayList<String>();
        final DescribeMetadataObject[] dmo = this.metadataConnection.describeMetadata(apiVersion).getMetadataObjects();

        for (DescribeMetadataObject obj : dmo) {
            dmoStringList.add(obj.getXmlName());
        }

        Collections.sort(dmoStringList);
        return dmoStringList;
    }

    private Element setTypes(Document initialDocument, List<String> fullNameList, String metadataTypeLv2) {
        final Element types = initialDocument.createElement(ELEMENT_TYPES);
        final Set<String> fullNameSet = new HashSet<>(fullNameList);
        final List<String> fullNameListNoDuplication = new ArrayList<>(fullNameSet);
        Collections.sort(fullNameListNoDuplication);

        for (String fullName : fullNameListNoDuplication) {
            final Element members = initialDocument.createElement(ELEMENT_MEMBERS);
            members.appendChild(initialDocument.createTextNode(fullName));
            types.appendChild(members);
        }

        final Element name = initialDocument.createElement(ELEMENT_NAME);
        name.appendChild(initialDocument.createTextNode(metadataTypeLv2));
        types.appendChild(name);
        return types;
    }

    private Element setVersion(Document initialDocument, Double apiVersion) {
        final Element version = initialDocument.createElement(ELEMENT_VERSION);
        version.appendChild(initialDocument.createTextNode(String.valueOf(apiVersion)));
        return version;
    }

    private void exportManifestFile(Document initialDocument, String fileNameTemp, String fileName, String os)
      throws TransformerException, IOException {
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, VALUE_YES);
        transformer.setOutputProperty(OutputKeys.METHOD, VALUE_XML);
        transformer.setOutputProperty(PROPERTY_INDENT_AMOUNT, VALUE_INDENT_AMOUNT);

        final DOMSource source = new DOMSource(initialDocument);
        final File manifestFileTemp = new File(fileNameTemp);
        final StreamResult result = new StreamResult(manifestFileTemp);
        transformer.transform(source, result);

        final File manifestFile = new File(fileName);
        final FileWriter filewriter = new FileWriter(manifestFile);
        final String allLine = CommonUtils.readAllLine(fileNameTemp, os);
        filewriter.write(allLine.replace("><", ">\n<"));
        filewriter.close();
        manifestFileTemp.delete();

        System.out.println(MESSAGE_SUCCESS + fileName);
    }

    public void generateManifest()
      throws JSONException, IOException, ConnectionException, ParserConfigurationException, TransformerException {
        final JSONObject jsonObj = new JSONObject(CommonUtils.readAllLine(CREDENTIALS_FILE, FILE_ENCODING_UTF8));
        final Double apiVersion = CommonUtils.getApiVersion(jsonObj);
        final Boolean exceptManagedPackage = CommonUtils.getExceptManagedPackage(jsonObj);
        final Boolean exceptUnmanagedPackage = CommonUtils.getExceptUnmanagedPackage(jsonObj);
        final String os = CommonUtils.getOs(jsonObj);

        this.setMetadataTypeFolderMap();
        final List<String> metadataTypeList = this.getMetadataTypeList(apiVersion);

        final Document initialDocument = this.setInitialDocument();
        final Document initialDocumentManaged = this.setInitialDocument();
        final Document initialDocumentUnlocked = this.setInitialDocument();
        final Document initialDocumentUnmanaged = this.setInitialDocument();
        final Element rootElement = this.setRootElement(initialDocument);
        final Element rootElementManaged = this.setRootElement(initialDocumentManaged);
        final Element rootElementUnlocked = this.setRootElement(initialDocumentUnlocked);
        final Element rootElementUnmanaged = this.setRootElement(initialDocumentUnmanaged);

        Boolean hasTypes = false;
        Boolean hasTypesManaged = false;
        Boolean hasTypesUnlocked = false;
        Boolean hasTypesUnmaged = false;

        for (String mt : metadataTypeList) {
            String metadataTypeLv1;
            String metadataTypeLv2;
            final Boolean hasKey = this.metadataTypeFolderMap.containsKey(mt.toString());

            if (hasKey) {
                metadataTypeLv1 = this.metadataTypeFolderMap.get(mt.toString());
            } else {
                metadataTypeLv1 = mt.toString();
            }
            metadataTypeLv2 = mt.toString();

            final ListMetadataQuery queryLv1 = new ListMetadataQuery();
            queryLv1.setType(metadataTypeLv1);
            final ListMetadataQuery[] queriesLv1 = new ListMetadataQuery[]{queryLv1};
            final FileProperties[] filePropertiesLv1 = this.metadataConnection.listMetadata(queriesLv1, apiVersion);
            Boolean hasFilePropertiesLv1 = (filePropertiesLv1.length > 0);

            if (!hasFilePropertiesLv1) {
                continue;
            }

            final List<String> folderList = new ArrayList<String>();
            final List<String> fullNameList = new ArrayList<String>();
            final List<String> fullNameListManaged = new ArrayList<String>();
            final List<String> fullNameListUnlocked = new ArrayList<String>();
            final List<String> fullNameListUnmanaged = new ArrayList<String>();

            System.out.println("");
            System.out.print("[" + simpleDate.format(new Date()) + "] ");
            System.out.println(metadataTypeLv1);

            for (FileProperties fpLv1 : filePropertiesLv1) {
                System.out.print("[" + simpleDate.format(new Date()) + "] ");
                System.out.print("<NamespacePrefix> ");
                System.out.print(fpLv1.getNamespacePrefix());
                System.out.print(" ");
                System.out.print("<ManageableState> ");
                System.out.print(fpLv1.getManageableState());
                System.out.print(" ");
                System.out.print("<FullName> ");
                System.out.println(fpLv1.getFullName());

                Boolean hasFilePropertiesManaged = (
                    exceptManagedPackage && (
                        Arrays.asList(manageableStateManaged).contains(String.valueOf(fpLv1.getManageableState())) &&
                        (null != fpLv1.getNamespacePrefix())
                    )
                );

                Boolean hasFilePropertiesUnlocked = (
                    exceptManagedPackage && (
                        Arrays.asList(manageableStateManaged).contains(String.valueOf(fpLv1.getManageableState())) &&
                        (null == fpLv1.getNamespacePrefix())
                    )
                );

                Boolean hasFilePropertiesUnmanaged = (
                    exceptUnmanagedPackage && (
                        Arrays.asList(manageableStateUnmanaged).contains(String.valueOf(fpLv1.getManageableState()))
                    )
                );

                if (hasFilePropertiesManaged) {
                    fullNameListManaged.add(fpLv1.getFullName());
                } else if (hasFilePropertiesUnlocked) {
                    fullNameListUnlocked.add(fpLv1.getFullName());
                } else if (hasFilePropertiesUnmanaged) {
                    fullNameListUnmanaged.add(fpLv1.getFullName());
                } else if (hasKey) {
                    folderList.add(fpLv1.getFullName());
                } else {
                    fullNameList.add(fpLv1.getFullName());
                }
            }

            for (String folder : folderList) {
                // FIXME: this logic is the same as Lv1, so that can be written simply.
                final ListMetadataQuery queryLv2 = new ListMetadataQuery();
                queryLv2.setType(metadataTypeLv2);
                queryLv2.setFolder(folder);
                final ListMetadataQuery[] queriesLv2 = new ListMetadataQuery[]{queryLv2};
                final FileProperties[] filePropertiesLv2 = this.metadataConnection.listMetadata(queriesLv2, apiVersion);
                Boolean hasFilePropertiesLv2 = (filePropertiesLv2.length > 0);

                if (!hasFilePropertiesLv2) {
                    continue;
                }
    
                System.out.println("");
                System.out.print("[" + simpleDate.format(new Date()) + "] ");
                System.out.println(metadataTypeLv2);

                for (FileProperties fpLv2 : filePropertiesLv2) {
                    System.out.print("[" + simpleDate.format(new Date()) + "] ");
                    System.out.print("<NamespacePrefix> ");
                    System.out.print(fpLv2.getNamespacePrefix());
                    System.out.print(" ");
                    System.out.print("<ManageableState> ");
                    System.out.print(fpLv2.getManageableState());
                    System.out.print(" ");
                    System.out.print("<FullName> ");
                    System.out.println(fpLv2.getFullName());
    
                    Boolean hasFilePropertiesManaged = (
                        exceptManagedPackage && (
                            Arrays.asList(manageableStateManaged).contains(String.valueOf(fpLv2.getManageableState())) &&
                            (null != fpLv2.getNamespacePrefix())
                        )
                    );
    
                    Boolean hasFilePropertiesUnlocked = (
                        exceptManagedPackage && (
                            Arrays.asList(manageableStateManaged).contains(String.valueOf(fpLv2.getManageableState())) &&
                            (null == fpLv2.getNamespacePrefix())
                        )
                    );
    
                    Boolean hasFilePropertiesUnmanaged = (
                        exceptUnmanagedPackage && (
                            Arrays.asList(manageableStateUnmanaged).contains(String.valueOf(fpLv2.getManageableState()))
                        )
                    );
    
                    if (hasFilePropertiesManaged) {
                        fullNameListManaged.add(fpLv2.getFullName());
                    } else if (hasFilePropertiesUnlocked) {
                        fullNameListUnlocked.add(fpLv2.getFullName());
                    } else if (hasFilePropertiesUnmanaged) {
                        fullNameListUnmanaged.add(fpLv2.getFullName());
                    } else if (hasKey) {
                        folderList.add(fpLv2.getFullName());
                    } else {
                        fullNameList.add(fpLv2.getFullName());
                    }
                }
            }

            Boolean hasFullNameList = (fullNameList.size() > 0);
            if (hasFullNameList) {
                final Element types = this.setTypes(initialDocument, fullNameList, metadataTypeLv2);
                rootElement.appendChild(types);
                hasTypes = true;
            }

            Boolean hasFullNameListManaged = (fullNameListManaged.size() > 0);
            if (hasFullNameListManaged) {
                final Element typesManaged = this.setTypes(initialDocumentManaged, fullNameListManaged, metadataTypeLv2);
                rootElementManaged.appendChild(typesManaged);
                hasTypesManaged = true;
            }

            Boolean hasFullNameListUnlocked = (fullNameListUnlocked.size() > 0);
            if (hasFullNameListUnlocked) {
                final Element typesUnlocked = this.setTypes(initialDocumentUnlocked, fullNameListUnlocked, metadataTypeLv2);
                rootElementUnlocked.appendChild(typesUnlocked);
                hasTypesUnlocked = true;
            }

            Boolean hasFullNameListUnmanaged = (fullNameListUnmanaged.size() > 0);
            if (hasFullNameListUnmanaged) {
                final Element typesUnmanaged = this.setTypes(initialDocumentUnmanaged, fullNameListUnmanaged, metadataTypeLv2);
                rootElementUnmanaged.appendChild(typesUnmanaged);
                hasTypesUnmaged = true;
            }
        }

        if (hasTypes) {
            final Element version = this.setVersion(initialDocument, apiVersion);
            rootElement.appendChild(version);
            initialDocument.appendChild(rootElement);
        }

        if (hasTypesManaged) {
            final Element versionManaged = this.setVersion(initialDocumentManaged, apiVersion);
            rootElementManaged.appendChild(versionManaged);
            initialDocumentManaged.appendChild(rootElementManaged);
        }

        if (hasTypesUnlocked) {
            final Element versionUnlocked = this.setVersion(initialDocumentUnlocked, apiVersion);
            rootElementUnlocked.appendChild(versionUnlocked);
            initialDocumentUnlocked.appendChild(rootElementUnlocked);
        }

        if (hasTypesUnmaged) {
            final Element versionUnmanaged = this.setVersion(initialDocumentUnmanaged, apiVersion);
            rootElementUnmanaged.appendChild(versionUnmanaged);
            initialDocumentUnmanaged.appendChild(rootElementUnmanaged);
        }

        System.out.println("");
        this.exportManifestFile(initialDocument, MANIFEST_FILE_TEMP, MANIFEST_FILE, os);
        this.exportManifestFile(initialDocumentManaged, MANIFEST_FILE_TEMP, MANIFEST_FILE_MANAGED, os);
        this.exportManifestFile(initialDocumentUnlocked, MANIFEST_FILE_TEMP, MANIFEST_FILE_UNLOCKED, os);
        this.exportManifestFile(initialDocumentUnmanaged, MANIFEST_FILE_TEMP, MANIFEST_FILE_UNMANAGED, os);
    }

}