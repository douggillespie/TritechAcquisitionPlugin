package tritechplugins.display.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.component.PamSettingsIconButton;

public class FineScrollControl implements SonarDisplayDecoration {

	private SonarsOuterPanel sonarsOuterPanel;
	
	private JButton backOneFrame, forwardOneFrame, back10Frames, forward10Frames;

	private JPanel mainPanel;

	public FineScrollControl(SonarsOuterPanel sonarsOuterPanel) {
		this.sonarsOuterPanel = sonarsOuterPanel;
		
		PamSymbol back1 = new PamSymbol(PamSymbolType.SYMBOL_TRIANGLEL, 12, 12, true, 
				Color.BLACK, Color.BLACK);
		back1.setIconHorizontalAlignment(PamSymbol.ICON_HORIZONTAL_CENTRE);
		PamSymbol back2 = new PamSymbol(PamSymbolType.SYMBOL_DOUBLETRIANGLEL, 12, 12, true, 
				Color.BLACK, Color.BLACK);
		back2.setIconHorizontalAlignment(PamSymbol.ICON_HORIZONTAL_CENTRE);
		PamSymbol forward1 = new PamSymbol(PamSymbolType.SYMBOL_TRIANGLER, 12, 12, true, 
				Color.BLACK, Color.BLACK);
		forward1.setIconHorizontalAlignment(PamSymbol.ICON_HORIZONTAL_CENTRE);
		PamSymbol forward2 = new PamSymbol(PamSymbolType.SYMBOL_DOUBLETRIANGLER, 12, 12, true, 
				Color.BLACK, Color.BLACK);
		forward2.setIconHorizontalAlignment(PamSymbol.ICON_HORIZONTAL_CENTRE);
		
		backOneFrame = new JButton(back1);
		back10Frames = new JButton(back2);
		forwardOneFrame = new JButton(forward1);
		forward10Frames = new JButton(forward2);
		sizeButton(backOneFrame);
		sizeButton(back10Frames);
		sizeButton(forwardOneFrame);
		sizeButton(forward10Frames);
		
		mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		mainPanel.add(back10Frames, c);
		c.gridx++;
		mainPanel.add(backOneFrame, c);
		c.gridx++;
		mainPanel.add(forwardOneFrame,c);
		c.gridx++;
		mainPanel.add(forward10Frames,c);
		c.gridx++;
		backOneFrame.setToolTipText("Scroll ack one frame");
		back10Frames.setToolTipText("Scroll back 10 frames");
		forwardOneFrame.setToolTipText("Scroll forward one frame");
		forward10Frames.setToolTipText("Scroll forward 10 frames");
		
		backOneFrame.addActionListener(new ScrollByFrames(-1));
		back10Frames.addActionListener(new ScrollByFrames(-10));
		forwardOneFrame.addActionListener(new ScrollByFrames(1));
		forward10Frames.addActionListener(new ScrollByFrames(10));
		
		backOneFrame.setFocusable(false);
		back10Frames.setFocusable(false);
		forwardOneFrame.setFocusable(false);
		forward10Frames.setFocusable(false);
		
	}
	
	private void sizeButton(JButton b) {
		Dimension sz = b.getMaximumSize();
		sz.width = sz.height;
		b.setPreferredSize(sz);
		
	}
	
	private class ScrollByFrames implements ActionListener {

		private int scrollFrames;

		public ScrollByFrames(int nFrames) {
			this.scrollFrames = nFrames;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			sonarsOuterPanel.scrollByFrames(scrollFrames);
		}
		
	}

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}

	@Override
	public void destroyComponent() {
		// TODO Auto-generated method stub
		
	}

}
