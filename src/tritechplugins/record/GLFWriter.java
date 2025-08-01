package tritechplugins.record;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import PamUtils.FileParts;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import binaryFileStorage.CountingOutputStream;
import tritechgemini.fileio.CatalogException;
import tritechgemini.fileio.CountingInputStream;
import tritechgemini.fileio.GLFFileCatalog;
import tritechgemini.fileio.LittleEndianDataOutputStream;
import tritechgemini.imagedata.GLFImageRecord;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.ImageDataBlock;
import tritechplugins.acquire.ImageDataUnit;

/**
 * Writes a GLF file in the same format as Tritech software. 
 * First, data are written to a .dat file, which is then zipped into
 * the glf file, with no additional compression. 
 * GLF file names are in the form log_2024-12-17-000450.glf
 * while the dat files within are in the form data_2024-12-17-000450.dat
 * Times are local - which is maddening, but will stick with it for now rather than 
 * convert to UTC, which is VERY tempting. 
 * The glf also contains a .cfg file containing xml, which seems to have very little information of 
 * any use apart from start and end times. not having this file does not seem to stop Genesis from 
 * opening thse files, though it would be relatively simple to recreate this xml if needed. 
 * 
 * @author dg50
 *
 */
public class GLFWriter extends PamObserverAdapter {

	private String rootFolder;
	
	private long maxFileSize;
	
	private File currentGLFFile;
	
	private File currentDATFile;
	
	private LittleEndianDataOutputStream outputStream;
	
	private CountingOutputStream countingOutput;

	private static String dateformat = "yyyy-MM-dd-HHmmss";
	
	private ImageDataBlock databuffer;
	
	private GLFFileCatalog glfFileCatalog;
	
	private ImageDataUnit lastWrittenRecord;
		
	public GLFWriter(ImageDataBlock databuffer) {
		super();
		this.databuffer = databuffer;
		this.glfFileCatalog = new GLFFileCatalog(null);
	}

	@Override
	public String getObserverName() {
		return "GLF Writer";
	}

	@Override
	public void addData(PamObservable observable, PamDataUnit pamDataUnit) {
		// anything sent here, we want to write to the GLF. Start by doing file and folder checks.
		ImageDataUnit imageData = (ImageDataUnit) pamDataUnit;
		GeminiImageRecordI imageRecord = imageData.getGeminiImage();
		if (imageRecord instanceof GLFImageRecord == false) {
			System.out.println("no GLF data. Can't write to GLF file");
			return; // can't do anything with this at the moment. 
		}
		checkFiles(imageData.getTimeMilliseconds());
		writeAllData();
	}

	private synchronized int writeAllData() {
		/*
		 *  the buffer only contains data that we WANT to write, so write
		 *  everything in it, but being careful to not lock the synch for 
		 *  longer than necessary to remove each record.  
		 *  Easiest way to do this is simply to remove everything. 
		 */
		ArrayList<ImageDataUnit> dataCopy;
		synchronized (databuffer.getSynchLock()) {
			dataCopy = databuffer.getDataCopy();
			databuffer.clearAll();
		}
		if (dataCopy == null) {
			return 0;
		}
		for (ImageDataUnit idu : dataCopy) {
			writeData(idu);
		}
		return dataCopy.size();
	}

	private void writeData(ImageDataUnit idu) {
		checkFiles(idu.getTimeMilliseconds());

		GLFImageRecord glfRecord = (GLFImageRecord) idu.getGeminiImage();
		try {
			// need to write a short header before writing each record. 
			glfFileCatalog.writeGLFHeader(glfRecord, outputStream);
			glfFileCatalog.writeGLFReecord(glfRecord, outputStream);
			lastWrittenRecord = idu;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private synchronized void checkFiles(long timeMilliseconds) {
		if (needNewFile(timeMilliseconds)) {
			closeCurrentFile();
			openNewFile(timeMilliseconds);
		}
	}

	private synchronized boolean openNewFile(long timeMilliseconds) {
		File folder = getFolderPath(timeMilliseconds);
		currentDATFile = new File(folder, getDATName(timeMilliseconds));
		currentGLFFile = new File(folder, getGLFName(timeMilliseconds));
		try {
			countingOutput = new CountingOutputStream(new BufferedOutputStream(new FileOutputStream(currentDATFile)));
			outputStream = new LittleEndianDataOutputStream(countingOutput);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			closeCurrentFile();
			return false;
		}
		return true;
	}

	public void pamStop() {
		flushAndClose();
	}
	/**
	 * Close the dat file, then launch a thread to zip it 
	 * and catalogue the zip for future speed. 
	 */
	private synchronized void closeCurrentFile() {
		if (countingOutput == null) {
			return;
		}
		// do everything
		try {
			countingOutput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// finalise the file in a separate thread to let this one get on with 
		// whatever...
		if (countingOutput.getByteCount() > 0) {
			GLFZipper zipper = new GLFZipper(currentDATFile, currentGLFFile);
			new Thread(zipper).start();
		}
		
		outputStream = null;
		countingOutput = null;
		currentDATFile = null;
		currentGLFFile = null;
	}
	
	private class GLFZipper implements Runnable {

		private File datFile;
		private File glfFile;
		
		public GLFZipper(File datFile, File glfFile) {
			super();
			this.datFile = datFile;
			this.glfFile = glfFile;
		}

		@Override
		public void run() {
//			boolean ok = zipOld();
			boolean ok = zipApache();
			if (ok) {
				// finally delete the .dat file
//				datFile.delete();

				// finally, while we're in a separate thread, catalogue it. 
				// would really be better to do this on the fly, but this will do for now
				try {
					GLFFileCatalog.getFileCatalog(glfFile.getAbsolutePath(), true);
				} catch (CatalogException e) {
					e.printStackTrace();
				}
			}
		}
		private boolean zipApache() {
			try {
				ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(glfFile);
				zipOut.setLevel(0);
				zipOut.setMethod(0);
				ZipArchiveEntry zipEnt = zipOut.createArchiveEntry(datFile, datFile.getName());
				zipEnt.setMethod(0);
				zipOut.putArchiveEntry(zipEnt);
				Path datPath = datFile.toPath();
				InputStream input = new FileInputStream(datFile);
				IOUtils.copy(input, zipOut);
				zipOut.closeArchiveEntry();
				zipOut.finish();
				zipOut.close();
				input.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
			return true;
			
		}
		private boolean zipOld() {
			try {
				/*
				 * Having a bit of trouble getting correct size data in here. 
				 * Some info at https://stackoverflow.com/questions/1206970/how-to-create-uncompressed-zip-archive-in-java
				 */
				
				FileOutputStream fos = new FileOutputStream(glfFile);
				ZipOutputStream zos = new ZipOutputStream(fos);
				zos.setMethod(ZipOutputStream.DEFLATED);
				zos.setLevel(0);
//				ZipOutputStream.
				ZipEntry zEnt = new ZipEntry(datFile.getName());
//				zEnt.setCompressedSize(datFile.length());
				zEnt.setSize(datFile.length());
				zEnt.setCrc(maxFileSize);
//				zEnt.setMethod(0);
				zos.putNextEntry(zEnt);

		        CRC32 crc = new CRC32();
		        
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(datFile));
				int n = 1048576;
				byte[] data = new byte[n];
				int read;
				while ((read = bis.read(data)) > 0) {
					crc.update(data, 0, read);
					zos.write(data, 0, read);
				}
//				zEnt.setCrc(crc.getValue());
				zos.closeEntry();
				zos.flush();
				zos.finish();
				zos.close();
				fos.close();
				bis.close();
				
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			return true;			
		}
		
	}

	private boolean needNewFile(long timeMilliseconds) {
		if (countingOutput == null) {
			return true;
		}
		long currLen = countingOutput.getByteCount();
		if (currLen >= maxFileSize << 20) { // compare to size in megabytes.
			return true;
		}
		return false;
	}

	public String getRootFolder() {
		return rootFolder;
	}

	public void setRootFolder(String rootFolder) {
		this.rootFolder = rootFolder;
	}

	public long getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}
	
	public String getNameDatePart(long time) {
		// this is in the form "2024-12-17-000450" local. 

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		c.setTimeZone(TimeZone.getDefault());

		DateFormat df = new SimpleDateFormat(dateformat );
		Date d = c.getTime();

		return df.format(d);
	}
	
	/**
	 * Get the folder a new file should be written to, check 
	 * the folder exists, create if necessary, etc.  
	 * @param time
	 * @return
	 */
	private File getFolderPath(long time) {
		String subfolder = PamCalendar.formatFileDate(time);
		File path = new File(rootFolder + FileParts.getFileSeparator() + subfolder);
		if (path.exists() == false) {
			path.mkdirs();
		}
		if (path.exists() == false) {
			return null;
		}
		return path;
	}
	
	/**
	 * Get a standard name for the GLF file
	 * @param time
	 * @return file name (no path)
	 */
	public String getGLFName(long time) {
		return "log_" + getNameDatePart(time) + ".glf";
	}
	
	/**
	 * Get a standard name for the dat file. 
	 * @param time
	 * @return file name (no path)
	 */
	public String getDATName(long time) {
		return "data_" + getNameDatePart(time) + ".dat";
	}

	/**
	 * Write any remaining data in the buffer and close. 
	 */
	public void flushAndClose() {
		
		writeAllData();
		closeCurrentFile();
	}

	/**
	 * @return the currentGLFFile
	 */
	public File getCurrentGLFFile() {
		return currentGLFFile;
	}

	/**
	 * @return the currentDATFile
	 */
	public File getCurrentDATFile() {
		return currentDATFile;
	}

	public ImageDataUnit getLastWrittenRecord() {
		return lastWrittenRecord;
	}
}
