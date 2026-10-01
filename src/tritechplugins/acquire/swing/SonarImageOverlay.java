package tritechplugins.acquire.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import Array.ArrayManager;
import Array.Streamer;
import Layout.PamAxis;
import Map.MapRectProjector;
import PamController.PamController;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamView.ColourArray;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PanelOverlayDraw;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.PamSymbolOptions;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import tritechgemini.fileio.MultiFileCatalog;
import tritechgemini.imagedata.FanImageData;
import tritechgemini.imagedata.FanPicksFromData;
import tritechgemini.imagedata.SonarImageRecordI;
import tritechgemini.imagedata.ImageFanMaker;
import tritechplugins.acquire.ImageDataBlock;
import tritechplugins.acquire.ImageDataUnit;
import tritechplugins.acquire.SonarDaqParams;
import tritechplugins.acquire.SonarPosition;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.display.swing.FanDataImage;

public class SonarImageOverlay extends SonarOverlayDraw {
	
	private TritechAcquisition tritechAcquisition;
	private ImageDataBlock imageDataBlock;
	
	private HashMap<Integer, ImageFanMaker> fanMakers = new HashMap<>(); 
	
	private HashMap<GeneralProjector, SonarImageSymbolOptions> lastSymbolOptions = new HashMap();
	
	private int nColours = 256;
	private ColourArray colourArray = ColourArray.createMergedArray(nColours, Color.BLACK, Color.GREEN);
	
	private PamAxis mapAxis;
	private boolean isViewer;
	
	private static PamSymbol defaultSymbol = new PamSymbol(PamSymbolType.SYMBOL_TRIANGLEU, 5, 5, false, Color.RED, Color.RED);

	public SonarImageOverlay(TritechAcquisition tritechAcquisition, ImageDataBlock imageDataBlock) {
		super(defaultSymbol);
		this.tritechAcquisition = tritechAcquisition;
		this.imageDataBlock = imageDataBlock;
		mapAxis = new PamAxis(0, 1, 0, 1, 0, 1, PamAxis.BELOW_RIGHT, null, PamAxis.LABEL_NEAR_MAX, "%d");
		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
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
		PamSymbolChooser symbolChooser = projector.getPamSymbolChooser();
		SonarImageSymbolOptions symbolOptions = null;
		if (symbolChooser instanceof SonarImageSymbolChooser) {
			 symbolOptions = ((SonarImageSymbolChooser) symbolChooser).getSymbolOptions();
			 if (symbolOptions != null && symbolOptions != lastSymbolOptions.get(projector)) {
				 lastSymbolOptions.put(projector, symbolOptions);
				 colourArray = ColourArray.createStandardColourArray(nColours, symbolOptions.colourMap);
				 setTransparancy(colourArray, symbolOptions.transparency);
			 }
		}
		if (symbolOptions == null) {
			symbolOptions = new SonarImageSymbolOptions();
		}
		
		//		defaultSymbol = new PamSymbol(PamSymbolType.SYMBOL_SQUARE, 10, 10, true, Color.RED, Color.WHITE);
		MapRectProjector mapProj = (MapRectProjector) projector;
		long mapTime = mapProj.getMapPanelRef().getSimpleMapRef().getMapTime();
		
		// find out how many sonars there are and get the closest image for each. 
		int[] sonarIds = tritechAcquisition.getSonarIds();
		if (sonarIds == null) {
			return true;
		}
		SonarImageRecordI[] toDraw = getDrawingRecords(sonarIds, mapTime);
		for (int i = 0; i < sonarIds.length; i++) {
			if (toDraw[i] != null && symbolOptions.showImage) {
				drawSonarImageOnMap(g, mapProj, toDraw[i], symbolOptions);
			}
			if (symbolOptions.showGrid) {
				drawSonarGridOnMap(g, mapProj, sonarIds[i], toDraw[i], symbolOptions);
			}
		}
		
		return true;
	}
	
	/**
	 * Get image records for drawing. In viewer mode this will be from the catalogue, in
	 * normal mode this will be the last record for each sonar 
	 * @param sonarIds
	 * @param mapTime
	 * @return array of drawing records same length as sonarIds. Individual references may be null
	 */
	private SonarImageRecordI[] getDrawingRecords(int[] sonarIds, long mapTime) {
		if (sonarIds == null) {
			return null;
		}
		SonarImageRecordI[] toDraw = new SonarImageRecordI[sonarIds.length];
		if (isViewer) {
			MultiFileCatalog fileCatalog = imageDataBlock.findFileCatalog();
			for (int i = 0; i < sonarIds.length; i++) {
				SonarImageRecordI sonarRecord = null;
				if (fileCatalog != null) {
					toDraw[i] = fileCatalog.findRecordForTime(sonarIds[i], mapTime);
				}
			}
		}
		else {
			// normal mode, so get the last record for each sonar. 
			for (int i = 0; i < sonarIds.length; i++) {
				ImageDataUnit dataUnit = imageDataBlock.getLastSonarImage(sonarIds[i]);
				if (dataUnit != null) {
					/*
					 *  only use it here if it's less than a few seconds old, so that display clears
					 *  if a sonar stops sending data.  
					 */
					if (mapTime - dataUnit.getTimeMilliseconds() < 5000) {
						toDraw[i] = dataUnit.getGeminiImage();
					}
				}
			}
		}
		return toDraw;
	}

	private void setTransparancy(ColourArray colourArray, int transparancy) {
		if (transparancy == 0) {
			return;
		}
		int n = colourArray.getNumbColours();
		Color[] cols = colourArray.getColours();
		for (int i = 0; i < n; i++) {
			Color aCol = cols[i];
			cols[i] = new Color(aCol.getRed(), aCol.getBlue(), aCol.getGreen(), 255-transparancy);
		}
		
	}

	private void drawSonarGridOnMap(Graphics g, MapRectProjector mapProj, int sonarId, SonarImageRecordI sonarRecord,
			SonarImageSymbolOptions symbolOptions) {
		double maxR = getMaxRange(sonarId, sonarRecord);
		if (maxR < 0) {
			return;
		}
		long mapTime = mapProj.getMapPanelRef().getSimpleMapRef().getMapTime();
		LatLong origin = getStreamerOrigin(0, mapTime);
		SonarPosition sonarPos = getSonarPosition(sonarId);
		origin = origin.addDistanceMeters(sonarPos.getX(), sonarPos.getY());
		Coordinate3d originXY = mapProj.getCoord3d(origin);
		double sonarHead = Math.toRadians(sonarPos.getHead()+mapProj.getMapRotationDegrees());
		double maxAng = Math.toRadians(60);
		double[] steps = {-1., -.5, 0., .5, 1.0};
		double scale = mapProj.getPixelsPerMetre();
		Color col = PamColors.getInstance().getForegroudColor(PamColor.AXIS);
		if (symbolOptions.transparency > 0) {
			col = new Color(col.getRed(), col.getGreen(), col.getBlue(), 255-symbolOptions.transparency);
		}
		g.setColor(col);
		for (int i = 0; i < steps.length; i++) {
			double xe = originXY.x + maxR*scale*Math.sin(maxAng*steps[i]+sonarHead);
			double ye = originXY.y - maxR*scale*Math.cos(maxAng*steps[i]+sonarHead);
			g.drawLine((int) originXY.x, (int) originXY.y, (int) xe, (int) ye);

			if (i == steps.length-1) {
				mapAxis.setRange(0, maxR);
				mapAxis.drawAxis(g, (int) originXY.x, (int) originXY.y, (int) xe, (int) ye);
			}
		}
//		now draw the arcs, including the outer one. 
		ArrayList<Double> axVals = mapAxis.getAxisValues();
		if (axVals.size() == 0 || axVals.get(axVals.size()-1) < maxR) {
			axVals.add(maxR);
		}
		int a1 = (int) (90-Math.toDegrees(maxAng)-sonarPos.getHead()-mapProj.getMapRotationDegrees());
		int a2 = (int) Math.ceil(Math.toDegrees(maxAng*2));
		for (int i = 0; i < axVals.size(); i++) {
			double r = scale * axVals.get(i);
			Rectangle rect = new Rectangle((int) (originXY.x-r), (int) (originXY.y-r), (int) (2*r), (int) (2*r));
			g.setColor(col);
			g.drawArc((int) (originXY.x-r), (int) (originXY.y-r), (int) (2*r), (int) (2*r), a1, a2);
		}
		
	}

	private double getMaxRange(int sonarId, SonarImageRecordI sonarRecord) {
		if (sonarRecord != null) {
			return sonarRecord.getMaxRange();
		}
		else {
			SonarDaqParams params = tritechAcquisition.getDaqParams().getSonarParams(sonarId);
			if (params == null) {
				return -1;
			}
			else {
				return params.getRange();
			}
		}
	}

	/**
	 * Draw a sonar image on the map, correctly georeferenced. 
	 * @param g
	 * @param mapProj
	 * @param sonarRecord
	 * @param symbolOptions 
	 */
	private void drawSonarImageOnMap(Graphics g, MapRectProjector mapProj, SonarImageRecordI sonarRecord, SonarImageSymbolOptions symbolOptions) {
		if (sonarRecord == null || sonarRecord.getImageData()== null) {
			return;
		}
		
		Graphics2D g2d = (Graphics2D) g.create();

		SonarPosition sonarPos = getSonarPosition(sonarRecord.getDeviceId());
		
//		g2d.setColor(Color.RED);
		//		g2d.drawRect(r.x, r.y, r.width, r.height);
		Rectangle r = setupDrawRectangle(g2d, mapProj, sonarRecord);
		int pix = (int) (r.width);
		if (pix < 4) {
			return;
		}
		pix = Math.min(pix,  sonarRecord.getnRange()*2);
		ImageFanMaker fanMaker = getFanMaker(sonarRecord.getDeviceId());
		FanImageData fanImageData = fanMaker.createFanData(sonarRecord, pix);
		if (fanImageData == null) {
			return;
		}	
		FanDataImage aFanImage = new FanDataImage(fanImageData, colourArray, true, symbolOptions.displayGain);
		BufferedImage bi = aFanImage.getBufferedImage();

		if (sonarPos.isFlipLR()) {
			g2d.drawImage(bi, r.x+r.width, r.y+r.height, r.x, r.y,0,0,bi.getWidth(),bi.getHeight(),null);
		}
		else {
			g2d.drawImage(bi, r.x, r.y+r.height, r.x+r.width, r.y,0,0,bi.getWidth(),bi.getHeight(),null);
		}
		
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
