package com.eoinoc.playlistgen;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class FindVenuePlaylistServlet extends AbstractAppEngineAuthorizationCodeServlet {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(FindVenuePlaylistServlet.class.getName());

    public void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws IOException,ServletException {
    	
    	// Get the stored credentials using the Authorization Flow
	    AuthorizationCodeFlow authFlow = initializeFlow();
	    Credential credential = authFlow.loadCredential(getUserId(req));
	    Utils.getYoutubeService(credential);
	    
	    
        String venueKey = convertToKeyFormat(req.getParameter("venue"));
        String venueName = req.getParameter("venue");
        String month = req.getParameter("month");
        
        PrintWriter respWriter = resp.getWriter();
        resp.setStatus(200);
        resp.setContentType("text/html");
        respWriter.println("Generating playlist...");
        
        /*String content = req.getParameter("content");
        Date date = new Date();
        Entity playlist = new Entity("Playlist", venueKey);
        playlist.setProperty("date", date);
        playlist.setProperty("content", content);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(playlist);*/
        CheckListingsService.createPlaylist(venueKey, venueName, month);
       
        
        resp.sendRedirect("/finished.html");
    }
    
    private String convertToKeyFormat(String key){
    	key = key.replace(" ", "_");
    	key = key.toLowerCase();
    	
    	return key;
    }
    
    @Override
	  protected AuthorizationCodeFlow initializeFlow() throws ServletException, IOException {
	    return Utils.initializeFlow();
	  }

	  @Override
	  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
	    return Utils.getRedirectUri(req);
	  }
}