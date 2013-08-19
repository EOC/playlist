package com.eoinoc.playlistgen;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.api.services.youtube.YouTube;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class PlaylistGeneratorServlet extends AbstractAppEngineAuthorizationCodeServlet {
	@Override
	  public void doGet(HttpServletRequest req, HttpServletResponse resp)
	      throws IOException, ServletException {
	    // Get the stored credentials using the Authorization Flow
	    AuthorizationCodeFlow authFlow = initializeFlow();
	    Credential credential = authFlow.loadCredential(getUserId(req));
	    Utils.getYoutubeService(credential);
	    
	    req.getRequestDispatcher("playlist.jsp").forward(req, resp);  
	    
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
