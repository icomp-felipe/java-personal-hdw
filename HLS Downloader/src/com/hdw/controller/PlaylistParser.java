package com.hdw.controller;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

import com.hdw.model.Chunklist;

/** Provides useful methods to handle with HLS EXTM3U formatted files.
 *  @author Felipe André - felipeandre.eng@gmail.com
 *  @version 2.20 - 03/MAR/2025 */
public class PlaylistParser {

	/** Retrieves a list of {@link Chunklist} from a given <code>uri</code>.
	 *  @param uri - HSL formatted URI
	 *  @return A list containing all the {@link Chunklist} available to download through the given URI.
	 *  @throws IOException when the attempt to connect to the URI fails. 
	 *  @throws InterruptedException if the connection operation is interrupted for some reason. */
	public static ArrayList<Chunklist> getConfig(final URI uri) throws IOException, InterruptedException {
		
		ArrayList<Chunklist> playlist = null;
		
		// Setting connection parameters
		HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();	// Connection timeout set to 5s
		HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(5)).build();	// Download timeout set to 5s
		
		// Connecting...
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		
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
						
				// Downloading remote URI content to a UTF-8 String
				String rawPlaylist = response.body();
				
				// Extracting data from the downloaded playlist string
				playlist = parse(uri, rawPlaylist);
				
				// Sorting list
				Comparator<Chunklist> comparator = (Chunklist c1, Chunklist c2) -> Integer.compare(c2.getWidth(),c1.getWidth());
				playlist.sort(comparator);
						
				break;
				
		}
				
		return playlist;
		
	}
	
	/** Extracts resolution and playlist URI data from the raw playlist string and creates a list containing these values.
	 *  @param uri - main HLS formatted URI
	 *  @param rawPlaylist - playlist downloaded from the given URI to a String
	 *  @return A list of {@link Chunklist} extracted from the given 'rawPlaylist'. */
	private static ArrayList<Chunklist> parse(final URI uri, final String rawPlaylist) {
		
		ArrayList<Chunklist> playlist = new ArrayList<Chunklist>();
		
		String[] lines = rawPlaylist.split("\n");
		
		// Iterating over playlist text lines
		for (int i=0; i<lines.length; ) {
			
			// Detects direct media link
			if (lines[i].contains("EXT-X-KEY") || lines[i].contains("EXT-X-INDEPENDENT-SEGMENTS")) {
				
				Chunklist chunklist = new Chunklist(uri, 0, 0);
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
				URI    chunkURI  = uri.resolve(chunkFile);
				
				// Creating chunklist object with extracted data...
				Chunklist chunklist = new Chunklist(chunkURI,width,height);
				
				// ...and adding to the list
				playlist.add(chunklist);
				
			}
			else
				i++;
			
		}
		
		return playlist;
		
	}
	
}
