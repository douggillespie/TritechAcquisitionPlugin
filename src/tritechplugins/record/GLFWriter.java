package tritechplugins.record;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import PamUtils.FileParts;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import binaryFileStorage.CountingOutputStream;
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

	private static String dateformat = "yyyy-MM-dd-hhmmss";
	
	private ImageDataBlock databuffer;
	
	private GLFFileCatalog glfFileCatalog;
	
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
			glfFileCatalog.writeGLFReecord(glfRecord, outputStream);
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
			new Thread(zipper).run();
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
			try {
				FileOutputStream fos = new FileOutputStream(glfFile);
				ZipOutputStream zos = new ZipOutputStream(fos);
				zos.setLevel(ZipOutputStream.STORED);
				ZipEntry zEnt = new ZipEntry(datFile.getName());
				zos.putNextEntry(zEnt);
				
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(datFile));
				int n = 1048576;
				byte[] data = new byte[n];
				int read;
				while ((read = bis.read(data)) > 0) {
					zos.write(data, 0, read);
				}
				zos.close();
				fos.close();
				bis.close();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
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
}
