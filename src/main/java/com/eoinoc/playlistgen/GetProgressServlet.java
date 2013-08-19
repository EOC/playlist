package com.eoinoc.playlistgen;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetProgressServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(GetProgressServlet.class.getName());

	@Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws IOException,ServletException {
    	String text = "some text";

    	resp.setContentType("text/plain");  // Set content type of the response so that jQuery knows what it can expect.
    	resp.setCharacterEncoding("UTF-8"); // You want world domination, huh?
    	resp.getWriter().write(text);       // Write response body.
    }
	
	@Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws IOException,ServletException {
		doPost(req,resp);
    }
}
