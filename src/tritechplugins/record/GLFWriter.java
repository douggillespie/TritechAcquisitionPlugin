package tritechplugins.record;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import PamUtils.FileParts;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import binaryFileStorage.CountingOutputStream;
import tritechgemini.fileio.CatalogException;
import tritechgemini.fileio.GLFFileCatalog;
import tritechgemini.fileio.LittleEndianDataOutputStream;
import tritechgemini.fileio.UnzippedWriter;
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
		
//	public static void main(String[] args) {
//		GLFWriter glfWriter = new GLFWriter(null);
//		glfWriter.test();
////		glfWriter.compareData();
//		glfWriter.checkGCatalog();
//	}
	
	private void checkGCatalog() {
//		String fn = "C:\\ProjectData\\RobRiver\\Y5\\GLF\\log_2024-09-27-183519b.glf";
		String fn = "C:\\PAMGuardTest\\glftest\\20240927\\log_2024-09-27-183519.glf";

		try {
			GLFFileCatalog.getFileCatalog(fn, true);
		} catch (CatalogException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void compareData() {
		/*
		 * Check the self zipped and original file are identical. They are. 
		 */
		File f1 = new File("C:\\PAMGuardTest\\glftest\\20240927\\log_2024-09-27-183519.glf");
		File f2 = new File("C:\\ProjectData\\RobRiver\\Y5\\GLF\\log_2024-09-27-183519b.glf");
		System.out.printf("\n");
		try {
			long l1 = f1.length();
			long l2 = f2.length();
			if (l1  != l2) {
				System.out.printf("\nDifferent lengths by %d\n", l2-l1);
			}
			BufferedInputStream ip1 = new BufferedInputStream(new FileInputStream(f1));
			BufferedInputStream ip2 = new BufferedInputStream(new FileInputStream(f2));
			int bLen = 65536;
			byte[] b1 = new byte[bLen];
			byte[] b2 = new byte[bLen];
			int r1, r2;
			int totalRead = 0;
			byte[] m1 = new byte[4];
			byte[] m2 = new byte[4];
			while (true) {
				for (int i = 0; i < bLen; i++) {
					b1[i] = b2[i] = 0;
				}
				r1 = ip1.read(b1);
				r2 = ip2.read(b2);
				if (r1+r2 <= 0) {
					break;
				}
				if (r1 != r2) {
					System.out.printf("Different n bytes read\n");
				}
				for (int i = 0; i < Math.max(r1,r2); i++) {
					long mn1 = checkMagic(m1, b1[i]);
					long mn2 = checkMagic(m2, b2[i]);
					if (mn1+mn2 > 0) {
						System.out.printf("Magic numbers at byte %d 0x%08x and 0x%08x\n" ,
								totalRead+i, mn1, mn2);
					}
					if (b1[i] != b2[i]) {						
						System.out.printf("Different data read at location %d %d/%d 0x%02X/0x%02X\n",
								totalRead+i, Byte.toUnsignedInt(b1[i]), Byte.toUnsignedInt(b2[i]),
								Byte.toUnsignedInt(b1[i]), Byte.toUnsignedInt(b2[i]));
					}
				}
				totalRead += r1;
			}
			
			
			ip1.close();
			ip2.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private long checkMagic(byte[] m, byte r) {
		for (int i = 0; i < 3; i++) {
			m[i] = m[i+1];
		}
		m[3] = r;
		long val = m[3]<<24 | m[2]<<16 | m[1]<<8 | m[0];
		if (val == ZipInputStream.CENSIG) {
			return ZipInputStream.CENSIG;
		}
		else if (val == ZipInputStream.ENDSIG) {
			return ZipInputStream.ENDSIG;
		}
		else if (val == ZipInputStream.LOCSIG) {
			return ZipInputStream.LOCSIG;
		}
		return 0;
	}

	private void test() {
		File datFile = new File("C:\\PAMGuardTest\\glftest\\20240927\\data_2024-09-27-183519.dat");
		File glfFile = new File("C:\\PAMGuardTest\\glftest\\20240927\\log_2024-09-27-183519.glf");
//		File datFile = new File("C:\\ProjectData\\RobRiver\\Y5\\GLF\\data_2024-09-27-183519b.dat");
//		File glfFile = new File("C:\\ProjectData\\RobRiver\\Y5\\GLF\\log_2024-09-27-183519b.glf");
//		File datFilea = new File("C:\\ProjectData\\RobRiver\\Y5\\GLF\\data_2024-09-27-183519.dat");
//		File glfFilea = new File("C:\\ProjectData\\RobRiver\\Y5\\GLF\\log_2024-09-27-183519.glf");
		glfFile.delete();
		GLFZipper zipper = new GLFZipper(datFile, glfFile);
		zipper.zipDIY();
		try {
			long sDat = Files.size(datFile.toPath());
			long sGLF = Files.size(glfFile.toPath()) ;
//			long sData = Files.size(datFilea.toPath());
//			long sGLFa = Files.size(glfFilea.toPath()) ;
			System.out.printf("\nFile sizes dat=%d, GLF = %d,  diff = %d\n", 
					sDat, sGLF,  sGLF-sDat);
		}
		catch (Exception e) {
//			e.printStackTrace();
		}
		try {
			GLFFileCatalog.getFileCatalog(glfFile.getAbsolutePath(), true);
		} catch (CatalogException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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

		/**
		 * Zip up the glf file using my own writer.
		 * @return true if no exception. 
		 */
		public boolean zipDIY() {
			UnzippedWriter uw = new UnzippedWriter();
			try {
				uw.writeArcive(glfFile, datFile);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

		@Override
		public void run() {
//			boolean ok = zipOld();
//			boolean ok = zipSys();
//			boolean ok = zipApache();
			boolean ok = zipDIY();
			if (ok) {
				// finally delete the .dat file
				datFile.delete();

				// finally, while we're in a separate thread, catalogue it. 
				// would really be better to do this on the fly, but this will do for now
				try {
					GLFFileCatalog.getFileCatalog(glfFile.getAbsolutePath(), true);
				} catch (CatalogException e) {
					e.printStackTrace();
				}
			}
		}
//		private boolean zipNative() {
//			String cmd = String.format("tar -zvcf \"%s\" \"%s\" ", 
//					glfFile.getAbsolutePath(), datFile.getAbsolutePath());
////			String cmd = String.format("tar -zcf \"%s\" ", 
////					glfFile.getAbsolutePath());
//			System.out.printf("\n");
//			System.out.println(cmd);
//			
//			Process proc = null;
//			int exitVal;
//			try {
//				 proc = Runtime.getRuntime().exec(cmd);
//				 while (proc.isAlive()) {
//					 Thread.sleep(10);
//				 }
//				 exitVal = proc.exitValue();
//				 System.out.println("Tar exit value = " + exitVal);
//			} catch (IOException | InterruptedException e) {
//				e.printStackTrace();
//				return false;
//			}
//			return true;
//		}
//		private boolean zipSys() {
//			// https://stackoverflow.com/questions/1091788/how-to-create-a-zip-file-in-java
//			String zipName = glfFile.getAbsolutePath();
//			zipName = zipName.replace(".glf", ".zip");
//			File zipFile = new File(zipName);
//			
//			URI uri = zipFile.toURI();
//	        Map<String, String> env = new HashMap<>(); 
//	        env.put("create", "true");
//	        
//	        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
//	            Path externalTxtFile = Paths.get(zipName);
//	            Path pathInZipfile = zipfs.getPath(datFile.getAbsolutePath());          
//	            // copy a file into the zip file
//	            Files.copy( externalTxtFile,pathInZipfile, 
//	                    StandardCopyOption.REPLACE_EXISTING ); 
//	        } catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
//			
//			return false;
//		}

//		private boolean zipApache() {
//			try {
//				ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(glfFile);
////				zipOut.setLevel(ZipArchiveOutputStream.STORED);
//				zipOut.setMethod(ZipArchiveOutputStream.STORED);
//				zipOut.setUseZip64(Zip64Mode.Never);
//				ZipArchiveEntry zipEnt = new ZipArchiveEntry(datFile.getName());
////				zipOut.addRawArchiveEntry(zipEnt, );
//				zipEnt.setRawFlag(0);
////				ZipArchiveEntry zipEnt = zipOut.createArchiveEntry(datFile, datFile.getName());
//				zipEnt.setMethod(ZipArchiveOutputStream.STORED);
//				zipOut.putArchiveEntry(zipEnt);
//				Path datPath = datFile.toPath();
//				InputStream input = new BufferedInputStream(new FileInputStream(datFile));
//				byte[] data = new byte[65531];
//				int bytesRead = 0;
//				while (true) {
//					bytesRead = input.read(data);
//					zipOut.write(data, 0, bytesRead);
//					if (bytesRead < data.length) {
//						break;
//					}
//				}
////				IOUtils.copy(input, zipOut);
////				zipEnt.
//				zipOut.closeArchiveEntry();
//				zipOut.finish();
//				zipOut.close();
//				input.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				return false;
//			}
//			
//			return true;
//			
//		}
		private boolean zipOld() {
			try {
				/*
				 * Having a bit of trouble getting correct size data in here. 
				 * Some info at https://stackoverflow.com/questions/1206970/how-to-create-uncompressed-zip-archive-in-java
				 */
				
				FileOutputStream fos = new FileOutputStream(glfFile);
				ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos));
				zos.setMethod(ZipOutputStream.STORED);
				zos.setLevel(Deflater.NO_COMPRESSION);
//				ZipOutputStream.
//				zos.s
				ZipEntry zEnt = new ZipEntry(datFile.getName());
//				zEnt.setSize(datFile.length());
//				zEnt.setMethod(ZipOutputStream.STORED);
				
//				zEnt.
//				zEnt.setCrc(0);
				zEnt.setCompressedSize(datFile.length());
//				System.out.println("Set dat file size to " + datFile.length());
				zEnt.setCompressedSize(datFile.length());
//				zEnt.setCrc(maxFileSize);
//				zEnt.setMethod(0);
				// first get the CRC
				int n = 1048576; // read in megabyte blocks
				byte[] data = new byte[n];
				int read;
				BufferedInputStream bis;
		        CRC32 crc = new CRC32();
				bis = new BufferedInputStream(new FileInputStream(datFile));
				while ((read = bis.read(data)) > 0) {
					crc.update(data, 0, read);
				}
				bis.close();
				zEnt.setCrc(crc.getValue());

				zos.putNextEntry(zEnt);
				
				// then go through again and write it. 
				bis = new BufferedInputStream(new FileInputStream(datFile));
				while ((read = bis.read(data)) > 0) {
					zos.write(data, 0, read);
				}
						
				zos.closeEntry();
//				zos.flush();
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
