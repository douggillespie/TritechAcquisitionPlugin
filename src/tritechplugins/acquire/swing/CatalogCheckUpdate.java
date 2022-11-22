package tritechplugins.acquire.swing;

public class CatalogCheckUpdate {

	protected int totalFiles;
	
	protected int processedFiles;
	
	protected int addedFast;
	
	protected int addedMain;
	
	protected String fileName;

	public CatalogCheckUpdate(int totalFiles, int processedFiles, int addedFast, int addedMain, String fileName) {
		super();
		this.totalFiles = totalFiles;
		this.processedFiles = processedFiles;
		this.addedFast = addedFast;
		this.addedMain = addedMain;
		this.fileName = fileName;
	}

}
