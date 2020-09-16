package com.hdw.controller;

import java.io.*;
import java.net.*;
import java.util.*;
import com.hdw.model.*;
import org.apache.commons.io.*;

/** Provides useful methods to handle with HLS EXTM3U formatted files.
 *  @author Felipe André - felipeandresouza@hotmail.com
 *  @version 1.5, 16/09/2020 */
public class PlaylistParser {

	/** Retrieves a list of {@link Chunklist} from a given url.
	 *  @param url - HSL formatted URL
	 *  @return A list containing all the {@link Chunklist} available to download through the given URL.
	 *  @throws JSONException when, for some reason, the valid URL could not be reached or the given link doesn't provide a proper JSON.
	 *  @throws IOException when the attempt to connect to the URL fails. */
	public static ArrayList<Chunklist> getConfig(final URL url) throws IOException {
		
		// Connecting to the URL
		ArrayList<Chunklist> playlist = null;
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setConnectTimeout( 5000);	// Connection timeout set to 5s
		connection.setReadTimeout   (10000);	// Download timeout set to 10s
		
		// Getting the response
		switch (connection.getResponseCode()) {
				
			// Expired link
			case 410:
				throw new IOException("The provided playlist link has expired!");
					
			// Not found
			case 404:
				throw new IOException("The provided playlist link is not online!");
			
			// Success
			case 200:
						
				// Here I download the online 'playlist' file to a String
				InputStream stream = connection.getInputStream();
				String rawPlaylist = IOUtils.toString(stream,"UTF-8");
				
				// Closing web connection
				stream.close();
				
				// Extracting data from the downloaded playlist string
				playlist = parse(url,rawPlaylist);
				
				// Sorting list
				Comparator<Chunklist> comparator = (Chunklist c1, Chunklist c2) -> Integer.compare(c2.getWidth(),c1.getWidth());
				playlist.sort(comparator);
						
				break;
				
		}
				
		connection.disconnect();
		
		return playlist;
		
	}
	
	/** Extracts resolution and playlist URL data from the raw playlist string and creates a list containing these values.
	 *  @param url - main HLS formatted URL
	 *  @param rawPlaylist - playlist downloaded from the given URL to a String
	 *  @return A list of {@link Chunklist} extracted from the given 'rawPlaylist'.
	 *  @throws MalformedURLException if no URL protocol is specified, or an unknown protocol is found, or 'chunkfile' is null.  */
	private static ArrayList<Chunklist> parse(final URL url, final String rawPlaylist) throws MalformedURLException {
		
		ArrayList<Chunklist> playlist = new ArrayList<Chunklist>();
		
		String[] lines = rawPlaylist.split("\n");
		
		// Iterating over playlist text lines
		for (int i=0; i<lines.length; ) {
			
			// Detects direct media link
			if (lines[i].contains("EXT-X-KEY")) {
				
				Chunklist chunklist = new Chunklist(url, 0, 0);
				playlist.add(chunklist);
				
				break;
				
			}
			
			// Detects playlist link
			if (lines[i].contains("RESOLUTION")) {
				
				// Extracting resolution (raw text)
				String resolution = lines[i].substring(lines[i].indexOf("RESOLUTION"));
				
				// Extracting resolution (int)
				Scanner res = new Scanner(resolution); res.useDelimiter("[^0-9]+");
				int width   = res.nextInt();
				int height  = res.nextInt();
				
				// Cleaning resources
				res.close();
				
				// Retrieving current playlist name
				String chunkFile = lines[++i];
				URL    chunkURL  = new URL(url, chunkFile);
				
				// Creating chunklist object with extracted data...
				Chunklist chunklist = new Chunklist(chunkURL,width,height);
				
				// ...and adding to the list
				playlist.add(chunklist);
				
			}
			else
				i++;
			
		}
		
		return playlist;
		
	}
	
}
