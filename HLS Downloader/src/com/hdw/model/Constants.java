package com.hdw.model;

import javax.swing.filechooser.FileNameExtensionFilter;

/** Contains the constants used alongside this program.
 *  @author Felipe André - felipeandresouza@hotmail.com
 *  @version 1.0 - 16/09/2020 */
public class Constants {
	
	/** Contains file formats used by file choosers UI.
	 *  @author Felipe André - felipeandresouza@hotmail.com
	 *  @version 1.0 - 16/09/2020 */
	public static class Format {
		
		public static final FileNameExtensionFilter MP4 = new FileNameExtensionFilter("MPEG-4 Video File (.mp4)","mp4");
		public static final FileNameExtensionFilter TXT = new FileNameExtensionFilter("Plain Text File (.txt)","txt");
		
	}

}
