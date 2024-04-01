package tritechplugins.display.swing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.JPopupMenu;

import PamView.GeneralProjector;
import PamView.paneloverlay.overlaymark.ExtMouseAdapter;
import PamView.paneloverlay.overlaymark.MarkDataSelector;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;
import tritechplugins.detect.threshold.RegionDataUnit;
import tritechplugins.detect.track.TrackLinkDataUnit;

public class SonarsPanelMarker extends OverlayMarker {

	private SonarsPanel sonarsPanel;
	private int imageIndex;

	public SonarsPanelMarker(SonarsPanel sonarsPanel, GeneralProjector projector, int imageIndex) {
		super(sonarsPanel, 0xFF, projector);
		this.sonarsPanel = sonarsPanel;
		this.imageIndex = imageIndex;
	}

	@Override
	public String getMarkerName() {
		return sonarsPanel.getDataSelectorName() + " Panel " + imageIndex;
	}

	@Override
	protected boolean completeMark(MouseEvent e) {
		boolean ans = super.completeMark(e);
		sonarsPanel.repaint();
		return ans;
	}
	
	/*
	 * Can we make a mark ? This is a default behaviour - ctrl is down 
	 * and there is at least one observer, but this coul dbe overridden. 
	 */
	@Override
	public boolean isCanMark(javafx.scene.input.MouseEvent e) {
		return (e.isControlDown());
	}

	@Override
	public List<PamDataUnit> getSelectedMarkedDataUnits(OverlayMark overlayMark, MarkDataSelector markDataSelector,
			int minOverlap) {
		List<PamDataUnit> selectedData = super.getSelectedMarkedDataUnits(overlayMark, markDataSelector, minOverlap);
		if (selectedData == null) {
			return null;
		}
		/**
		 * Bit of a fudge to use the super detections (tracks) of REgions since it's really those
		 * that we want to group, not the regions. 
		 */
		HashSet<PamDataUnit> set = new HashSet<>();
		List<PamDataUnit> newSelection;
		for (PamDataUnit aData : selectedData) {
			if (aData instanceof RegionDataUnit) {
				SuperDetection track = ((RegionDataUnit) aData).getSuperDetection(TrackLinkDataUnit.class);
				if (track != null) {
					set.add(track);
				}
				else {
					/*
					 * If it's not part of a track, then it will be a manually added
					 * data unit, so add it to the list anyway. 
					 */
					set.add(aData);
				}
			}
			else {
				set.add(aData);
			}
		}
		newSelection = new ArrayList<PamDataUnit>(set);
		return newSelection;
	}
	

	@Override
	public List<MenuItem> getPopupMenuItems(MouseEvent e) {
		List<MenuItem> markItems = super.getPopupMenuItems(e);
		if (markItems == null) {
			return null;
		}

		//		List<MenuItem> panelItems = 
		SonarImagePanel imagePanel = sonarsPanel.getImagePanel(imageIndex);
		if (imagePanel != null) {
			java.awt.event.MouseEvent swingMouse = ExtMouseAdapter.swingMouse(e); 
			JPopupMenu panelItems = imagePanel.getImageMenu(swingMouse);
			if (panelItems != null) {
				List<MenuItem> fxMenu = PamUtilsFX.getSwingMenuItems(panelItems);
				for (MenuItem mio : fxMenu) {
					if (mio != null) {
						markItems.add(mio);	
					}
					else {
//						System.out.println("null item");
					}
				}
			}
		}
		return markItems;
	}

	/**
	 * @return the imageIndex
	 */
	public int getImageIndex() {
		return imageIndex;
	}

}
