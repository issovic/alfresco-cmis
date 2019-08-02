package com.issa.cmisutilged.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.client.util.FileUtils;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.issa.cmisutilged.dto.OutPutGed;

/**********************************
 * 
 * @Author Issa ben mansour
 * 
 **********************************/
@Configuration
@Component
public class CmisUtilsGed {

	/**
	 * The Constant LOGGER.
	 */
	private static final Logger log = LoggerFactory.getLogger(CmisUtilsGed.class);

	@Autowired
	private Environment env;

	private static Session session;

	private static String ALFRSCO_SERVER_URL;
	private static String ALFRSCO_LOLCAL_SERVER_URL;
	private static String ALFRSCO_ATOMPUB_URL;
	private static String ALFRSCO_USER;
	private static String ALFRSCO_PASSWORD;
	private static String ALFRESCO_TAG;
	private static String ALFRSCO_PDF_URL;
	private static String ALFRSCO_TITLE_URL;
	private static String AUTH;
	private static String COOKIES;
	private static String FAC_CLASS;

	@PostConstruct
	public void init() {

		ALFRSCO_SERVER_URL = env.getProperty("cmisutil.repository.urlserver");
		ALFRSCO_LOLCAL_SERVER_URL = env.getProperty("cmisutil.repository.urllocalserver");
		ALFRSCO_ATOMPUB_URL = env.getProperty("cmisutil.repository.urlRepo");
		ALFRSCO_USER = env.getProperty("cmisutil.repository.username");
		ALFRESCO_TAG = env.getProperty("cmisutil.repository.tagws");
		ALFRSCO_PDF_URL = env.getProperty("cmisutil.repository.urlpdf");
		ALFRSCO_PASSWORD = env.getProperty("cmisutil.repository.pwd");
		ALFRSCO_TITLE_URL = env.getProperty("cnps.repository.urltitle");
		AUTH = env.getProperty("cmisutil.repository.auth");
		COOKIES = env.getProperty("cmisutil.repository.cookies");
		FAC_CLASS = env.getProperty("cmisutil.repository.fac_class");

	}

	/**
	 * Connect to alfresco repository
	 *
	 * @return root folder object
	 */

	public String getTicket() {

		RestTemplate restTemplate = new RestTemplate();

		String url = ALFRSCO_SERVER_URL + "/alfresco/service/api/login?u=" + ALFRSCO_USER + "&pw=" + ALFRSCO_PASSWORD;

		String consumeJSONString = restTemplate.getForObject(url, String.class);

		int debut = consumeJSONString.indexOf("<ticket>") + 8;
		int fin = consumeJSONString.indexOf("</ticket>");
		String ticket = consumeJSONString.substring(debut, fin);

		return "?alf_ticket=" + ticket;
	}

	public void addTag(String idDoc, List<String> tags) {

		try {
			String ch = ALFRSCO_SERVER_URL + ALFRESCO_TAG + getTicket();

			RestTemplate restTemplate = new RestTemplate();
			for (int i = 0; i < tags.size(); i++) {
				MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
				map.add("a", "add");
				map.add("n", idDoc);
				map.add("t", tags.get(i));
				restTemplate.postForObject(ch, map, String.class);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

	public int getNbFOlders(Folder target) {
		int s = 0;
		try {
			for (Iterator<CmisObject> it = target.getChildren().iterator(); it.hasNext();) {
				CmisObject o = it.next();
				if (BaseTypeId.CMIS_FOLDER.equals(o.getBaseTypeId())) {
					s++;
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return s;
	}

	public void addTitle(String idDoc, String ref_archiv) {
		try {
			String ch = ALFRSCO_SERVER_URL + ALFRSCO_TITLE_URL + idDoc + "/formprocessor" + getTicket();
			RestTemplate restTemplate = new RestTemplate();
			Map<String, String> map = new HashMap<String, String>();
			map.put("prop_cm_title", ref_archiv);
			restTemplate.postForObject(ch, map, String.class);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public Folder connect() {

		try {
			// log.info("--------------connecting to Alfresco Ged--------------");
			SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
			Map<String, String> parameters = new HashMap<String, String>();

			log.debug("--------------setting user Session Params--------------");
			// User credentials.

			parameters.put(SessionParameter.USER, ALFRSCO_USER);
			parameters.put(SessionParameter.PASSWORD, ALFRSCO_PASSWORD);

//			log.info("username :  {} ",ALFRSCO_USER);
//			log.info("password : {} ",ALFRSCO_PASSWORD);
			// Connection settings.
			// log.info("--------------Setting URL Session Params--------------");

			// parameters.put(SessionParameter.BINDING_TYPE,
			// BindingType.ATOMPUB.value());
			// parameters.put(SessionParameter.ATOMPUB_URL,
			// "http://localhost:8083/alfresco/api/-default-/cmis/versions/1.0/atom");
			// parameters.put(SessionParameter.AUTH_HTTP_BASIC, "true");
			// parameters.put(SessionParameter.COOKIES, "true");
			// parameters.put(SessionParameter.OBJECT_FACTORY_CLASS,
			// "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");

			parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
			parameters.put(SessionParameter.ATOMPUB_URL, ALFRSCO_SERVER_URL + ALFRSCO_ATOMPUB_URL);
			parameters.put(SessionParameter.AUTH_HTTP_BASIC, AUTH);
			parameters.put(SessionParameter.COOKIES, COOKIES);
			parameters.put(SessionParameter.OBJECT_FACTORY_CLASS, FAC_CLASS);

			// Create session.
			// Alfresco only provides one repository.

			Repository repository = sessionFactory.getRepositories(parameters).get(0);
			session = repository.createSession();
			Folder root = session.getRootFolder();
			log.debug("--------------connected to Alfresco Ged--------------");
			// log.info(root.getName());

			return root;
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return null;

	}

	public void disconnect() {
		// session.clear();
		// session = null;
	}

	public int getNbDocs(Folder target) {
		int s = 0;
		for (Iterator<CmisObject> it = target.getChildren().iterator(); it.hasNext();) {
			CmisObject o = it.next();
			if (BaseTypeId.CMIS_DOCUMENT.equals(o.getBaseTypeId())) {
				s++;
			}
		}
		return s;
	}

	public Folder createFolder2(Folder target, String newFolderName) {
		Folder rootFolder = connect();
		Folder newFolder = getFolderByName(rootFolder, newFolderName);
		if (newFolder == null) {
			Map<String, String> props = new HashMap<String, String>();
			props.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
			props.put(PropertyIds.NAME, newFolderName);
			newFolder = target.createFolder(props);
		}
		return newFolder;
	}

	public Folder createFolder1(Folder target, String newFolderName, String newPath, String description) {

		Folder rootFolder = connect();
		Folder newFolder = getFolderByName(rootFolder, newPath);
		if (newFolder == null) {
			Map<String, String> props = new HashMap<String, String>();
			props.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
			props.put(PropertyIds.NAME, newFolderName);
			// props.put(PropertyIds.DESCRIPTION, description);
			newFolder = target.createFolder(props);
		}
		return newFolder;
	}

	public String getIdDocs(Folder target, String fileName) {
		String ch = "";

		try {
			for (Iterator<CmisObject> it = target.getChildren().iterator(); it.hasNext();) {
				CmisObject o = it.next();
				if (BaseTypeId.CMIS_DOCUMENT.equals(o.getBaseTypeId())) {
					Document doc = (Document) o;
					if (doc.getName().equals(fileName)) {
						ch = doc.getId();
						ch = ch.substring(0, ch.indexOf(";"));
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return ch;
	}

	public void supprimerAll(Folder target) {
		try {
			for (Iterator<CmisObject> it = target.getChildren().iterator(); it.hasNext();) {
				CmisObject o = it.next();
				if (BaseTypeId.CMIS_DOCUMENT.equals(o.getBaseTypeId())) {
					Document doc = (Document) o;

					doc.delete(true);

				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public Document getDocumentByPath(String path) {
		try {

			Document doc = (Document) session.getObjectByPath(path);

			return doc;
		} catch (final CmisObjectNotFoundException e) {

			return null;
		}
	}

	public void copyDoc(String id, Folder target) {
		try {
			Document doc = (Document) session.getObject(id);
			doc.copy(target);
		} catch (final CmisObjectNotFoundException e) {
			System.out.println("---------> " + e.getMessage());
		}
	}

	public void createDocument(Folder target, String newDocName, byte[] pjFichier, String contentType,
			Map<String, String> props) {
		try {
			ByteArrayInputStream input = new ByteArrayInputStream(pjFichier);
			ContentStream contentStream = session.getObjectFactory().createContentStream(newDocName, pjFichier.length,
					contentType, input);
			target.createDocument(props, contentStream, VersioningState.MAJOR);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

	public void createDocument1(Folder target, String newDocName, byte[] pjFichier, String contentType) {

		try {
			Map<String, String> props = new HashMap<String, String>();

			props.put(PropertyIds.NAME, newDocName);

			// System.out.println("This is a document: " + newDocName);
			ByteArrayInputStream input = new ByteArrayInputStream(pjFichier);

			ContentStream contentStream = session.getObjectFactory().createContentStream(newDocName, pjFichier.length,
					contentType, input);

			target.createDocument(props, contentStream, VersioningState.MAJOR);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

	public Folder createFolder1(Folder target, String newFolderName, String newPath) {

		Folder rootFolder = connect();

		try {
			Folder newFolder = getFolderByName(rootFolder, newPath);
			if (newFolder == null) {
				Map<String, String> props = new HashMap<String, String>();
				props.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
				props.put(PropertyIds.NAME, newFolderName);
				newFolder = target.createFolder(props);
			}
			return newFolder;

		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return null;
	}

	public void supprimer(Folder target, String nom) {
		try {
			for (Iterator<CmisObject> it = target.getChildren().iterator(); it.hasNext();) {
				CmisObject o = it.next();
				if (BaseTypeId.CMIS_DOCUMENT.equals(o.getBaseTypeId())) {
					Document doc = (Document) o;
					if (doc.getName().equals(nom)) {
						doc.delete(true);
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public void supprimerById(Folder target, String idDoc) {
		try {
			for (Iterator<CmisObject> it = target.getChildren().iterator(); it.hasNext();) {
				CmisObject o = it.next();
				if (BaseTypeId.CMIS_DOCUMENT.equals(o.getBaseTypeId())) {
					Document doc = (Document) o;
					String ch = doc.getId().substring(0, doc.getId().indexOf(";"));
					if (ch.equals(idDoc)) {
						doc.delete(true);
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public List<OutPutGed> getListFile(Folder target) {
		List<OutPutGed> listeDoc = new ArrayList<OutPutGed>();
		try {
			for (Iterator<CmisObject> it = target.getChildren().iterator(); it.hasNext();) {
				CmisObject o = it.next();
				if (BaseTypeId.CMIS_DOCUMENT.equals(o.getBaseTypeId())) {
					Document doc = (Document) o;
					OutPutGed outPutGed = new OutPutGed();
					String ch = doc.getId().substring(0, doc.getId().indexOf(";"));
					String[] t = ch.split("/");
					ch = t[t.length - 1];

					outPutGed.setNameFile(doc.getName());
					outPutGed.setIdDoc(ch);
					listeDoc.add(outPutGed);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return listeDoc;
	}

	// public Document createDocumentForInputStream(final Folder parentFolder,
	// final InputStream iStream,
	// Date distributionDate, String tadig, final String fileType, final String
	// fileName, final long lenght) {
	// CmisClient cc = new CmisClient();
	// return cc.createDocumentForInputStream(session, parentFolder, iStream,
	// distributionDate, tadig, fileType,
	// fileName, lenght);
	// }

	/**
	 * Clean up test folder before executing test
	 *
	 * @param target
	 * @param delFolderName
	 */
	private static void cleanup(Folder target, String delFolderName) {
		try {
			CmisObject object = session.getObjectByPath(target.getPath() + delFolderName);
			Folder delFolder = (Folder) object;
			delFolder.deleteTree(true, UnfileObject.DELETE, true);
		} catch (CmisObjectNotFoundException e) {
			System.err.println("No need to clean up.");
		}
	}

	/**
	 * Get Folder By Name
	 *
	 * @param target
	 */
	public Folder getFolderByName(Folder target, String folderName) {
		Folder foundFolder;
		try {
			CmisObject object = session.getObjectByPath(target.getPath() + folderName);
			foundFolder = (Folder) object;
			// return foundFolder;
		} catch (Exception e) {
			foundFolder = null;
			System.err.println("No folder with this name.");
		}
		return foundFolder;
	}

	/**
	 * @param target
	 */
	public static void listFolder(int depth, Folder target) {
		String indent = StringUtils.repeat("\t", depth);
		try {
			for (Iterator<CmisObject> it = target.getChildren().iterator(); it.hasNext();) {
				CmisObject o = it.next();
				if (BaseTypeId.CMIS_DOCUMENT.equals(o.getBaseTypeId())) {
					System.out.println(indent + "[Docment] " + o.getName());
				} else if (BaseTypeId.CMIS_FOLDER.equals(o.getBaseTypeId())) {
					System.out.println(indent + "[Folder] " + o.getName());
					listFolder(++depth, (Folder) o);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

	/**
	 * Delete test document
	 *
	 * @param target
	 * @param delDocName
	 */
	private static void DeleteDocument(Folder target, String delDocName) {
		try {
			CmisObject object = session.getObjectByPath(target.getPath() + delDocName);
			Document delDoc = (Document) object;
			delDoc.delete(true);
		} catch (CmisObjectNotFoundException e) {
			System.err.println("Document is not found: " + delDocName);
		}
	}

	/**
	 * Create test document with content
	 *
	 * @param target
	 * @param newDocName
	 */
	public void createDocument(Folder target, String newDocName, byte[] pjFichier, String contentType) {
		try {
			Map<String, String> props = new HashMap<String, String>();
			props.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
			props.put(PropertyIds.NAME, newDocName);
			// props.put(PropertyIds.DESCRIPTION, "tester");
			System.out.println("This is a document: " + newDocName);
			ByteArrayInputStream input = new ByteArrayInputStream(pjFichier);
			ContentStream contentStream = session.getObjectFactory().createContentStream(newDocName, pjFichier.length,
					contentType, input);
			target.createDocument(props, contentStream, VersioningState.MAJOR);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

	private static void createDocumentFromFile(Folder target, String newDocName, File file, String contentType) {
		Map<String, String> props = new HashMap<String, String>();
		String parentID = file.getParentFile().getPath();
		props.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		props.put(PropertyIds.NAME, newDocName);
		System.out.println("This is a test document: " + newDocName);

		try {
			Document docFromFile = FileUtils.createDocumentFromFile(parentID, file, newDocName, VersioningState.MAJOR,
					session);
			target.addToFolder(docFromFile, true);
		} catch (java.io.FileNotFoundException ex) {
			// Logger.getLogger(CmisUtilsGed.class.getName()).log(Level.SEVERE,
			// null, ex);
			System.out.println(" **!!!!***** FileNotFoundException " + ex.getMessage());
		}

	}

	private static void gettingFolders() {

		try {
			Folder root = session.getRootFolder();
			ItemIterable<CmisObject> children = root.getChildren();
			// ItemIterable<CmisObject> childrens = root.get

			System.out.println("Found the following objects in the root folder:-----");
			for (CmisObject o : children) {
				System.out.println(o.getName() + " -- which is of type  -> " + o.getType().getDisplayName());
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * Create test folder directly under target folder
	 *
	 * @param target
	 * @param
	 * @return newly created folder
	 */
	public Folder createFolder(Folder target, String newFolderName) {
		try {
			Map<String, String> props = new HashMap<String, String>();
			props.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
			props.put(PropertyIds.NAME, newFolderName);
			Folder newFolder = target.createFolder(props);
			return newFolder;
		}
		catch(Exception e)
		{
			log.error(e.getMessage());
		}
		return null;
	}

	public byte[] fileToByteArray(String path) {
		FileInputStream fileInputStream = null;

		File file = new File(path);

		byte[] bFile = new byte[(int) file.length()];

		try {
			// convert file into array of bytes
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(bFile);
			fileInputStream.close();

			// for (int i = 0; i < bFile.length; i++) {
			// System.out.print((char)bFile[i]);
			// }
			//
			// System.out.println("Done");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bFile;
	}

}
