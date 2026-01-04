package tritechplugins.acquire.swing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import Array.ArrayManager;
import Array.Streamer;
import Map.MapRectProjector;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PanelOverlayDraw;
import PamguardMVC.PamDataUnit;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.SonarPosition;

/**
 * Base class for sonar drawing. 
 * @author dg50
 *
 */
public abstract class SonarOverlayDraw extends PanelOverlayDraw {

	public SonarOverlayDraw(PamSymbol defaultSymbol) {
		super(defaultSymbol);
		// TODO Auto-generated constructor stub
	}

	public LatLong getStreamerOrigin(int streamerInd, long timeMillis) {
		Streamer streamer = ArrayManager.getArrayManager().getCurrentArray().getStreamer(streamerInd);
		return streamer.getHydrophoneLocator().getStreamerLatLong(timeMillis);
	}
	
	/**
	 * Get a rectangle to draw into and apply an appropriate rotation transform to it. 
	 * This is the outline of the fan image and will have been rotated into
	 * the correct frame, so it's possible to draw into this rectangle with cartesian coordinates in the 
	 * sonar frame I think. 
	 * @param g
	 * @param mapRectProjector
	 * @param imageRecord
	 * @return
	 */
	public Rectangle setupDrawRectangle(Graphics2D g2d, MapRectProjector mapProj, GeminiImageRecordI sonarRecord) {
		double maxRange = sonarRecord.getMaxRange();
		double maxAng = Math.abs(sonarRecord.getBearingTable()[0]);
		SonarPosition sonarPos = getSonarPosition(sonarRecord.getDeviceId());
		/*
		 *  get the four corners of the image, then work out a transform to draw into 
		 *  that rectangle. 
		 */
		double xm = maxRange*Math.sin(maxAng); // half width of image. 
		LatLong origin = getStreamerOrigin(0, sonarRecord.getRecordTime());
		if (origin == null) {
			origin = new LatLong(0,0);
		}
		LatLong sonarOrigin = origin.addDistanceMeters(sonarPos.getX(), sonarPos.getY());
		double sonarR = Math.toRadians(sonarPos.getHead());
		/*
		 *  find the middle of the image rectangle and it's extent, then set up a transform
		 *  to draw a rotated image about that centre ? 
		 */
		Coordinate3d apexXY = mapProj.getCoord3d(sonarOrigin);
		Coordinate3d centre = mapProj.getCoord3d(sonarOrigin.travelDistanceMeters(sonarPos.getHead(), maxRange/2));

//		g.drawLine((int) apexXY.x, (int) apexXY.y, (int) centre.x, (int) centre.y);
		double scale = mapProj.getPixelsPerMetre();
		Rectangle r = new Rectangle((int) (apexXY.x-xm*scale), (int) (apexXY.y-maxRange*scale), 
				(int) (xm*2*scale), (int) (maxRange*scale));
		AffineTransform rt = AffineTransform.getRotateInstance(sonarR, apexXY.x, apexXY.y);
		AffineTransform current = g2d.getTransform();
		if (current != null) {
			current.concatenate(rt);
			g2d.setTransform(current);
		}
		else {
			g2d.setTransform(rt);
		}
		
		
		return r;
	}
	
	public abstract SonarPosition getSonarPosition(int sonarId);

}
