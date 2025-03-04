package com.hdw.model;

import java.net.URI;

/** Contains some data related to a HLS EXTM3U playlist.
 *  Here are stored the playlist URI and the media resolution.
 *  @author Felipe André - felipeandre.eng@gmail.com
 *  @version 1.6 - 03/MAR/2025 */
public class Chunklist {
	
	private final URI chunkURI;
	private int width, height;
	
	/** Main constructor setting parameters.
	 *  @param chunkURI - chunklist URI
	 *  @param width - video width
	 *  @param height - video height */
	public Chunklist(final URI chunkURI, final int width, final int height) {
		
		this.chunkURI = chunkURI;
		this.width    = width;
		this.height   = height;
		
	}
	
	/** Constructor created to support direct media links. When a direct media link
	 *  is informed (instead of playlist link), it does not contain resolution info.
	 *  @param chunkURI - chunklist URI */
	public Chunklist(final URI chunkURI) {
		this(chunkURI,0,0);
	}
	
	/** @return Video width. */
	public int getWidth() {
		return this.width;
	}

	/** @return Formatted video resolution string. */
	public String getResolution() {
		return (this.width == 0) ? "unknown" : String.format("%dx%d", this.width, this.height);
	}
	
	/** @return Chunklist URI. */
	public String getURI() {
		return this.chunkURI.toString();
	}
	
	/** @return 'true' if it has no resolution (came from a direct media link) and 'false' otherwise. */
	public boolean hasNoResolution() {
		return this.width == 0;
	}

	/** Resolution setter.
	 *  @param width - media width
	 *  @param height - media height */
	public void setResolution(final int width, final int height) {
		this.width  = width ;
		this.height = height;
	}

}
