package com.hdw.controller;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import com.hdw.model.*;
import org.apache.commons.io.*;

/** Provides useful methods to handle with HLS EXTM3U formatted files.
 *  @author Felipe André - felipeandre.eng@gmail.com
 *  @version 2.20 - 03/MAR/2025 */
public class PlaylistParser {

	/** Retrieves a list of {@link Chunklist} from a given url.
	 *  @param url - HSL formatted URL
	 *  @return A list containing all the {@link Chunklist} available to download through the given URL.
	 *  @throws JSONException when, for some reason, the valid URL could not be reached or the given link doesn't provide a proper JSON.
	 *  @throws IOException when the attempt to connect to the URL fails. 
	 *  @throws URISyntaxException if the provided <code>url</code> is not formatted strictly according to RFC2396 and cannot be converted to a URI. 
	 *  @throws InterruptedException if the connection operation is interrupted for some reason. */
	public static ArrayList<Chunklist> getConfig(final URL url) throws IOException, URISyntaxException, InterruptedException {
		
		// Connecting to the URL
		ArrayList<Chunklist> playlist = null;
		
		HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();			// Connection timeout set to 5s
		HttpRequest request = HttpRequest.newBuilder(url.toURI()).timeout(Duration.ofSeconds(10)).build();	// Download timeout set to 10s
		
		// Connecting...
		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		
		// Getting the response
		switch (response.statusCode()) {
				
			// Expired link
			case 410:
				throw new IOException("The provided playlist link has expired!");
					
			// Not found
			case 404:
				throw new IOException("The provided playlist link is not online!");
			
			// Success
			case 200:
						
				// Here I download the online 'playlist' file to a String
				InputStream stream = response.body();
				String rawPlaylist = IOUtils.toString(stream, StandardCharsets.UTF_8);
				
				// Closing web connection
				stream.close();
				
				// Extracting data from the downloaded playlist string
				playlist = parse(url,rawPlaylist);
				
				// Sorting list
				Comparator<Chunklist> comparator = (Chunklist c1, Chunklist c2) -> Integer.compare(c2.getWidth(),c1.getWidth());
				playlist.sort(comparator);
						
				break;
				
		}
				
		return playlist;
		
	}
	
	/** Extracts resolution and playlist URL data from the raw playlist string and creates a list containing these values.
	 *  @param url - main HLS formatted URL
	 *  @param rawPlaylist - playlist downloaded from the given URL to a String
	 *  @return A list of {@link Chunklist} extracted from the given 'rawPlaylist'.
	 *  @throws MalformedURLException if no URL protocol is specified, or an unknown protocol is found, or 'chunkfile' is null.  
	 *  @throws URISyntaxException if the provided <code>url</code> is not formatted strictly according to RFC2396 and cannot be converted to a URI. */
	private static ArrayList<Chunklist> parse(final URL url, final String rawPlaylist) throws MalformedURLException, URISyntaxException {
		
		ArrayList<Chunklist> playlist = new ArrayList<Chunklist>();
		
		String[] lines = rawPlaylist.split("\n");
		
		// Iterating over playlist text lines
		for (int i=0; i<lines.length; ) {
			
			// Detects direct media link
			if (lines[i].contains("EXT-X-KEY") || lines[i].contains("EXT-X-INDEPENDENT-SEGMENTS")) {
				
				Chunklist chunklist = new Chunklist(url, 0, 0);
				playlist.add(chunklist);
				
				break;
				
			}
			
			// Detects playlist link
			if ((lines[i].contains("EXT-X-STREAM-INF")) && (lines[i].contains("RESOLUTION"))) {
				
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
				URL    chunkURL  = url.toURI().resolve(chunkFile).toURL();
				
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
