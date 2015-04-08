package com.github.ytsejam5.nsfconverter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

public interface NSFWriter {

	public void init(ServletConfig servletConfig, HttpServletRequest request);

	public void close();
	
	public String getViewableURL(String id);
	
	public void outRelatedObject(String documentURI, InputStream document, Map permission, String[] collection, int weight) throws IOException ;

	public void outMainObject(String documentURI, String document, Object permission,	String[] collection, int weight) throws IOException;

}
