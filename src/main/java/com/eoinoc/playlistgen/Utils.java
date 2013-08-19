/*
 * Copyright (c) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.eoinoc.playlistgen;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Preconditions;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;

class Utils {

  
  private static GoogleClientSecrets clientSecrets = null;
  private static final Set<String> SCOPES = Collections.singleton(YouTubeScopes.YOUTUBE);
  static final String MAIN_SERVLET_PATH = "/find";
  static final String AUTH_CALLBACK_SERVLET_PATH = "/oauth2callback";
  /** Global instance of the HTTP transport. */
  static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  /** Global instance of the JSON factory. */
  static final JsonFactory JSON_FACTORY = new JacksonFactory();
  
  static YouTube youtubeApi = null;
  static Credential credential = null;
  
  private static final AppEngineDataStoreFactory DATA_STORE_FACTORY =
	      new AppEngineDataStoreFactory();

  static GoogleClientSecrets getClientSecrets() throws IOException {
	    
	  if (clientSecrets == null) {
	        clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
	            new InputStreamReader(Utils.class.getResourceAsStream("/client_secrets.json")));

	    Preconditions.checkArgument(!clientSecrets.getDetails().getClientId().startsWith("Enter ")
	                && !clientSecrets.getDetails().getClientSecret().startsWith("Enter "),
	          "Enter Client ID and Secret from https://code.google.com/apis/console/?api=youtube"
	          + "into youtube-cmdline-playlistupdates-sample/src/main/resources/client_secrets.json");
	    }
	    
	    return clientSecrets;
  }
  
  static YouTube getYoutubeService(Credential cr) {
	  if(youtubeApi==null) {
		  youtubeApi = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, cr)
		.setApplicationName("eocplaylists")
		.build();
	  } 
	  
	  return youtubeApi;
  }

  static GoogleAuthorizationCodeFlow initializeFlow() throws IOException {
	  
	    return new GoogleAuthorizationCodeFlow.Builder(
	            HTTP_TRANSPORT, JSON_FACTORY, getClientSecrets(), SCOPES).setDataStoreFactory(
	            DATA_STORE_FACTORY).setAccessType("offline").build();
  }
  
  static String getRedirectUri(HttpServletRequest req) {
    GenericUrl requestUrl = new GenericUrl(req.getRequestURL().toString());
    requestUrl.setRawPath(AUTH_CALLBACK_SERVLET_PATH);
    return requestUrl.build();
  }
}
