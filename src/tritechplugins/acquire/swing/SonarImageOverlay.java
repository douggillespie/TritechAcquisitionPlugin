package tritechplugins.acquire.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import Array.ArrayManager;
import Array.Streamer;
import Map.MapRectProjector;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamView.ColourArray;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamKeyItem;
import PamView.PanelOverlayDraw;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import tritechgemini.fileio.MultiFileCatalog;
import tritechgemini.imagedata.FanImageData;
import tritechgemini.imagedata.FanPicksFromData;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechgemini.imagedata.ImageFanMaker;
import tritechplugins.acquire.ImageDataBlock;
import tritechplugins.acquire.SonarPosition;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.display.swing.FanDataImage;

public class SonarImageOverlay extends SonarOverlayDraw {
	
	private TritechAcquisition tritechAcquisition;
	private ImageDataBlock imageDataBlock;
	
	private HashMap<Integer, ImageFanMaker> fanMakers = new HashMap<>(); 
	
	private int nColours = 256;
	ColourArray colourArray = ColourArray.createMergedArray(nColours, Color.BLACK, Color.GREEN);

	public SonarImageOverlay(TritechAcquisition tritechAcquisition, ImageDataBlock imageDataBlock) {
		super(null);
		this.tritechAcquisition = tritechAcquisition;
		this.imageDataBlock = imageDataBlock;
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		// do nothing here. Drawing is done of a single image, or images, in the predraw function. 
		return null;
	}
	
	@Override
	public boolean preDrawAnything(Graphics g, PamDataBlock pamDataBlock, GeneralProjector projector) {
		if (projector instanceof MapRectProjector == false) {
			return true;
		}
		//		defaultSymbol = new PamSymbol(PamSymbolType.SYMBOL_SQUARE, 10, 10, true, Color.RED, Color.WHITE);
		MapRectProjector mapProj = (MapRectProjector) projector;
		long mapTime = mapProj.getMapPanelRef().getSimpleMapRef().getMapTime();
		
		// find out how many sonars there are and get the closest image for each. 
		int[] sonarIds = tritechAcquisition.getSonarIds();
		if (sonarIds == null) {
			return true;
		}
		MultiFileCatalog fileCatalog = imageDataBlock.findFileCatalog();
		for (int i = 0; i < sonarIds.length; i++) {
			GeminiImageRecordI sonarRecord = fileCatalog.findRecordForTime(sonarIds[i], mapTime);
			if (sonarRecord != null) {
//				if (sonarRecord.isFullyLoaded() == false) {
//					sonarRecord = fileCatalog.getRecord(sonarRecord.getRecordNumber(), true);
//				}
				drawSonarImageOnMap(g, mapProj, sonarRecord);
			}
		}
		
		return true;
	}

	/**
	 * Draw a sonar image on the map, correctly georeferenced. 
	 * @param g
	 * @param mapProj
	 * @param sonarRecord
	 */
	private void drawSonarImageOnMap(Graphics g, MapRectProjector mapProj, GeminiImageRecordI sonarRecord) {
		if (sonarRecord == null || sonarRecord.getImageData()== null) {
			return;
		}
		
		Graphics2D g2d = (Graphics2D) g.create();
		
//		double maxRange = sonarRecord.getMaxRange();
//		double maxAng = Math.abs(sonarRecord.getBearingTable()[0]);
//		SonarPosition sonarPos = getSonarPosition(sonarRecord.getDeviceId());
//		/*
//		 *  get the four corners of the image, then work out a transform to draw into 
//		 *  that rectangle. 
//		 */
//		double xm = maxRange*Math.sin(maxAng); // half width of image. 
//		LatLong origin = getStreamerOrigin(0, sonarRecord.getRecordTime());
//		if (origin == null) {
//			origin = new LatLong(0,0);
//		}
//		LatLong sonarOrigin = origin.addDistanceMeters(sonarPos.getX(), sonarPos.getY());
//		double sonarR = Math.toRadians(sonarPos.getHead());
//		/*
//		 *  find the middle of the image rectangle and it's extent, then set up a transform
//		 *  to draw a rotated image about that centre ? 
//		 */
//		Coordinate3d apexXY = mapProj.getCoord3d(sonarOrigin);
//		Coordinate3d centre = mapProj.getCoord3d(sonarOrigin.travelDistanceMeters(sonarPos.getHead(), maxRange/2));
//
//		g.drawLine((int) apexXY.x, (int) apexXY.y, (int) centre.x, (int) centre.y);
//		double scale = mapProj.getPixelsPerMetre();
//		Rectangle r = new Rectangle((int) (apexXY.x-xm*scale), (int) (apexXY.y-maxRange*scale), 
//				(int) (xm*2*scale), (int) (maxRange*scale));
//		AffineTransform rt = AffineTransform.getRotateInstance(sonarR, apexXY.x, apexXY.y);
//		AffineTransform current = g2d.getTransform();
//		if (current != null) {
//			current.concatenate(rt);
//			g2d.setTransform(current);
//		}
//		else {
//			g2d.setTransform(rt);
//		}
//		
//		
//		g2d.setColor(Color.RED);
//		g2d.drawRect(r.x, r.y, r.width, r.height);
		Rectangle r = setupDrawRectangle(g2d, mapProj, sonarRecord);

		int pix = (int) (r.width);
		pix = Math.min(pix,  sonarRecord.getnRange()*2);
		ImageFanMaker fanMaker = getFanMaker(sonarRecord.getDeviceId());
		FanImageData fanImageData = fanMaker.createFanData(sonarRecord, pix);
		if (fanImageData == null) {
			return;
		}	
		FanDataImage aFanImage = new FanDataImage(fanImageData, colourArray, true, 1);
		BufferedImage bi = aFanImage.getBufferedImage();
		g2d.drawImage(bi,r.x, r.y+r.height, r.x+r.width, r.y,0,0,bi.getWidth(),bi.getHeight(),null);
		
	}
	@Override
	public SonarPosition getSonarPosition(int sonarId) {
		SonarPosition sonarPos = tritechAcquisition.getDaqParams().getSonarPosition(sonarId);
		return sonarPos;
	}

	private ImageFanMaker getFanMaker(int sonarId) {
		ImageFanMaker fanMaker = fanMakers.get(sonarId);
		if (fanMaker == null) {
			fanMaker =  new FanPicksFromData(4);
			fanMakers.put(sonarId, fanMaker);
		}
		return fanMaker;
	}
	

	
	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		if (parameterTypes[0] == ParameterType.LATITUDE
				&& parameterTypes[1] == ParameterType.LONGITUDE) {
			return true;
		}
		return false;
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector, int keyType) {
		return null;
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		// TODO Auto-generated method stub
		return null;
	}



}
