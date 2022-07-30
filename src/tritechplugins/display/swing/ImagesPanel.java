package tritechplugins.display.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import PamUtils.PamCalendar;

/**
 * Intermediate panel which sits in the sonarsPanel (why ?) because
 * the sonarspanel used corner layout to handle the pop out controls and 
 * this panel needs to use the different sonars layout. It doesn't though since
 * it can be made transparent, so the drawing can take place in the underlying
 * sonarspanel. 
 * @author dg50
 *
 */
public class ImagesPanel extends JPanel {



	private SonarsPanel sonarsPanel;

	public ImagesPanel(SonarsPanel sonarsPanel, LayoutManager layout) {
		super(layout);
		this.sonarsPanel = sonarsPanel;
		setOpaque(false);
		setBackground(new Color(0.f,0.f,0.f,0.f));
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		Graphics2D g2d = (Graphics2D) g;
		
//		long currentTime = sonarsPanel.getCurrentScrollTime();
//		if (currentTime != 0) {
//
//			Font font = g2d.getFont();
//			g2d.setColor(getForeground());
//			String timeString = PamCalendar.formatDBDateTime(currentTime, true);
//			int sz = font.getSize();
//			FontMetrics fm = g2d.getFontMetrics();
//			Rectangle2D stringRect = fm.getStringBounds(timeString, g2d);
//			if (stringRect.getWidth() < getWidth() / 8) {
//				sz *=2;
//			}
//			if (stringRect.getWidth() < getWidth() / 4) {
//				sz = sz*3/2;
//			}
//			
//			font = new Font(font.getName(), Font.BOLD, sz);
//			g2d.setFont(font);
//			g2d.drawString(timeString, 3, getHeight()-3);
//		}
	}


}
