package com.hdw.model;

import java.net.URL;

/** Contains some data related to a HLS EXTM3U playlist.
 *  Here are stored the playlist URL and the media resolution.
 *  @author Felipe André - felipeandresouza@hotmail.com
 *  @version 1.0 - 14/09/2020 */
public class Chunklist {
	
	private final URL chunkURL;
	private int width, height;
	
	/** Main constructor setting parameters.
	 *  @param chunkURL - chunklist URL
	 *  @param width - video width
	 *  @param height - video height */
	public Chunklist(final URL chunkURL, final int width, final int height) {
		
		this.chunkURL = chunkURL;
		this.width    = width;
		this.height   = height;
		
	}
	
	public Chunklist(final URL chunkURL) {
		this(chunkURL,0,0);
	}
	
	/** Video width getter.
	 *  @return Video width. */
	public int getWidth() {
		return this.width;
	}

	/** Returns a video resolution string (width x height).
	 *  @return Formatted video resolution string. */
	public String getResolution() {
		return (this.width == 0) ? "unknown" : String.format("%dx%d", this.width, this.height);
	}
	
	/** Chunklist URL getter.
	 *  @return Chunklist URL. */
	public String getURL() {
		return this.chunkURL.toString();
	}
	
	public boolean hasNoResolution() {
		return this.width == 0;
	}

	public void setResolution(final int width, final int height) {
		this.width  = width ;
		this.height = height;
	}

}
