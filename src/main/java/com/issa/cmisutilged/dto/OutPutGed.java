package com.issa.cmisutilged.dto;

/**********************************
 * 
 *  @Author Issa ben mansour
 * 
 **********************************/
public class OutPutGed {

	private String urlFile;
	private String nameFile;
	private String idDoc;
	private String pathFolder;
	public String getUrlFile() {
		return urlFile;
	}
	public void setUrlFile(String urlFile) {
		this.urlFile = urlFile;
	}
	public String getNameFile() {
		return nameFile;
	}
	public void setNameFile(String nameFile) {
		this.nameFile = nameFile;
	}
	public String getIdDoc() {
		return idDoc;
	}
	public void setIdDoc(String idDoc) {
		this.idDoc = idDoc;
	}

    public String getPathFolder() {
        return pathFolder;
    }

    public void setPathFolder(String pathFolder) {
        this.pathFolder = pathFolder;
    }
}