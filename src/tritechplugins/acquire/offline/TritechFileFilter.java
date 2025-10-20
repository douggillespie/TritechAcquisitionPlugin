package tritechplugins.acquire.offline;

import PamUtils.PamFileFilter;

public class TritechFileFilter extends PamFileFilter {

	public TritechFileFilter() {
		super("Tritech Gemini image files", ".glf");
		addFileType(".ecd");
		addFileType(".aris");
	}

}
