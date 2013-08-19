package com.eoinoc.playlistgen;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;





public class CheckListingsService {

	/** Global instance properties filename. */
	private static String PROPERTIES_FILENAME = "youtube.properties";

	/** Global instance of the max number of videos we want returned (50 = upper limit per page). */
	private static final long NUMBER_OF_VIDEOS_RETURNED = 3;
	
	/** Global instance of Youtube object to make all API requests. */
	private static YouTube youtubeAPIv3;
	
	private static final Logger log = Logger.getLogger(CheckListingsService.class.getName());
	private static SimpleDateFormat formatter = new SimpleDateFormat("E d MMM");
	
	public CheckListingsService() {
		
	}
	
	public static List<String> getListings (String venueName, String month) throws URISyntaxException {
		try{
		    	Document doc = Jsoup.connect("http://www.roisindubh.net/listings/?startDate=2013-"+month+"-01").get();
				/*File sampleFile = new File(CheckListingsService.class.getResource("/sample.html").toURI());
				Document doc = Jsoup.parse(sampleFile, "UTF-8");*/
				Elements listings = doc.select("div.column.highlightDetails >h2");
				Elements dates = doc.select("div.column.highlightDetails >h3");
				List<String> bandNames = new ArrayList();
		    		    	
		    	for(int i=0;i<listings.size();i++){
		    		Element band = listings.get(i);  
		    		Element date = dates.get(i);
		    		
		    		if(!((band.text().toLowerCase()).contains("silent")
		    				||(band.text().toLowerCase()).contains("strange brew")
		    				||(band.text().toLowerCase()).contains("open mic"))){
		    			String[] dateSplitter = date.text().split("/");
		    			
		    			Date gigDate = null;
		    			try{
		    				gigDate = formatter.parse(dateSplitter[0]);
		    			} catch (ParseException ex) {
		    				System.out.println("Could not parse date");
		    			}  			
		    			
		    			bandNames.add(band.text());
		    		    /*Entity song = new Entity("Song", playlistKey);
		    		    song.setProperty("band", band.text());
		    		    datastore.put(song);*/
		    		}
		    	}
		    return bandNames;
		}
		catch(IOException ex) {
			ex.printStackTrace();
			return new ArrayList();
		}
	}
	
	public static List<SearchResult> getYoutube(String query) throws Exception {
		 
		 youtubeAPIv3 = Utils.getYoutubeService(null);
		 
		 String queryTerm = query;

	      YouTube.Search.List search = youtubeAPIv3.search().list("id,snippet");
	      /*
	       * It is important to set your developer key from the Google Developer Console for
	       * non-authenticated requests (found under the API Access tab at this link:
	       * code.google.com/apis/). This is good practice and increased your quota.
	       */
	      String apiKey = "AIzaSyDqQen7Xu8JxCe4YR6WdRezUgylU0fLMDc";
	      search.setKey(apiKey);
	      search.setQ(queryTerm);
	      /*
	       * We are only searching for videos (not playlists or channels). If we were searching for
	       * more, we would add them as a string like this: "video,playlist,channel".
	       */
	      search.setType("video");
	      /*
	       * This method reduces the info returned to only the fields we need and makes calls more
	       * efficient.
	       */
	      search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
	      search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
	      SearchListResponse searchResponse = search.execute();

	      List<SearchResult> searchResultList = searchResponse.getItems();

	      /*if (searchResultList != null) {
	        prettyPrint(searchResultList.iterator(), queryTerm);
	      }*/
	      
	      return searchResultList;
	}
	
	/**
	   * Creates YouTube Playlist and adds it to the authorized account.
	   */
	  private static String insertPlaylist(String venueName, String month) throws IOException {

	    /*
	     * We need to first create the parts of the Playlist before the playlist itself.  Here we are
	     * creating the PlaylistSnippet and adding the required data.
	     */
	    PlaylistSnippet playlistSnippet = new PlaylistSnippet();
	    playlistSnippet.setTitle(venueName+"-"+month);
	    playlistSnippet.setDescription("A playlist of bands playing at the "+venueName+" in "+getMonthForInt(Integer.parseInt(month)));

	    // Here we set the privacy status (required).
	    PlaylistStatus playlistStatus = new PlaylistStatus();
	    playlistStatus.setPrivacyStatus("public");

	    /*
	     * Now that we have all the required objects, we can create the Playlist itself and assign the
	     * snippet and status objects from above.
	     */
	    Playlist youTubePlaylist = new Playlist();
	    youTubePlaylist.setSnippet(playlistSnippet);
	    youTubePlaylist.setStatus(playlistStatus);

	    /*
	     * This is the object that will actually do the insert request and return the result.  The
	     * first argument tells the API what to return when a successful insert has been executed.  In
	     * this case, we want the snippet and contentDetails info.  The second argument is the playlist
	     * we wish to insert.
	     */
	    YouTube.Playlists.Insert playlistInsertCommand =
	        youtubeAPIv3.playlists().insert("snippet,status", youTubePlaylist);
	    Playlist playlistInserted = playlistInsertCommand.execute();

	    // Pretty print results.

	    System.out.println("New Playlist name: " + playlistInserted.getSnippet().getTitle());
	    System.out.println(" - Privacy: " + playlistInserted.getStatus().getPrivacyStatus());
	    System.out.println(" - Description: " + playlistInserted.getSnippet().getDescription());
	    System.out.println(" - Posted: " + playlistInserted.getSnippet().getPublishedAt());
	    System.out.println(" - Channel: " + playlistInserted.getSnippet().getChannelId() + "\n");
	    return playlistInserted.getId();

	  }

	  /**
	   * Creates YouTube PlaylistItem with specified video id and adds it to the specified playlist id
	   * for the authorized account.
	   *
	   * @param playlistId assign to newly created playlistitem
	   * @param videoId YouTube video id to add to playlistitem
	   */
	  private static String insertPlaylistItem(String playlistId, String videoId) throws IOException {

	    /*
	     * The Resource type (video,playlist,channel) needs to be set along with the resource id. In
	     * this case, we are setting the resource to a video id, since that makes sense for this
	     * playlist.
	     */
	    ResourceId resourceId = new ResourceId();
	    resourceId.setKind("youtube#video");
	    resourceId.setVideoId(videoId);

	    /*
	     * Here we set all the information required for the snippet section.  We also assign the
	     * resource id from above to the snippet object.
	     */
	    PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
	    playlistItemSnippet.setTitle("First video in the test playlist");
	    playlistItemSnippet.setPlaylistId(playlistId);
	    playlistItemSnippet.setResourceId(resourceId);

	    /*
	     * Now that we have all the required objects, we can create the PlaylistItem itself and assign
	     * the snippet object from above.
	     */
	    PlaylistItem playlistItem = new PlaylistItem();
	    playlistItem.setSnippet(playlistItemSnippet);

	    /*
	     * This is the object that will actually do the insert request and return the result.  The
	     * first argument tells the API what to return when a successful insert has been executed.  In
	     * this case, we want the snippet and contentDetails info.  The second argument is the
	     * playlistitem we wish to insert.
	     */
	    YouTube.PlaylistItems.Insert playlistItemsInsertCommand =
	        youtubeAPIv3.playlistItems().insert("snippet,contentDetails", playlistItem);
	    
	    PlaylistItem returnedPlaylistItem = null;
	    		
	    try {
	    	returnedPlaylistItem = playlistItemsInsertCommand.execute();
	    } catch (GoogleJsonResponseException ex) {
	    	ex.printStackTrace();
	    }

	    // Pretty print results.
/*
	    System.out.println("New PlaylistItem name: " + returnedPlaylistItem.getSnippet().getTitle());
	    System.out.println(" - Video id: " + returnedPlaylistItem.getSnippet().getResourceId().getVideoId());
	    System.out.println(" - Posted: " + returnedPlaylistItem.getSnippet().getPublishedAt());
	    System.out.println(" - Channel: " + returnedPlaylistItem.getSnippet().getChannelId());
	    */
	    
	    return returnedPlaylistItem.getId();
	}
	  
	private static String getMonthForInt(int num) {
	        String month = "wrong";
	        DateFormatSymbols dfs = new DateFormatSymbols();
	        String[] months = dfs.getMonths();
	        if (num >= 1 && num <= 12 ) {
	            month = months[num-1];
	        }
	        return month;
	}
	
	public static void createPlaylist(String venueNameKey, String venueName, String month) {

		try {
			List<String> listings = getListings(venueNameKey, month);
			
			if(listings.size()>0) {
				youtubeAPIv3 = Utils.getYoutubeService(null);
			
				// Creates a new playlist in the authorized user's channel.
				String playlistId = insertPlaylist(venueName,month);
		    
				for(String bandName:listings) {
					try {
						List<SearchResult> results = getYoutube(bandName);
						log.info("Adding videos for "+bandName);
						if(results.size()>0){
						int i = 1;
						// If a valid playlist was created, adds a new playlistitem with a video to that playlist.
							for(SearchResult sr : results){
								log.info("Adding video "+i+" of "+results.size());
								ResourceId rId = sr.getId();
								insertPlaylistItem(playlistId, rId.getVideoId());
								Thread.sleep(200);
								i++;
							}
						}
					} catch (GoogleJsonResponseException e) {
				      System.err.println("There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());
				      e.printStackTrace();
				    }  catch (Throwable t) {
				      System.err.println("Throwable: " + t.getMessage());
				      t.printStackTrace();
				    }
				}
				
		        Key venueKey = KeyFactory.createKey("Venue", venueName);
		        Date date = new Date();
		        Entity playlist = new Entity("Playlist", venueKey);
		        playlist.setProperty("venue",venueName);
		        playlist.setProperty("dateCreated", date);
		        playlist.setProperty("playlist", playlistId);
		        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		        datastore.put(playlist);
			}
		}
		catch (IOException e) {
		      System.err.println("IOException: " + e.getMessage());
		      e.printStackTrace();
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
	}
}