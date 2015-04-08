package com.github.ytsejam5.nsfconverter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import com.marklogic.xcc.AdhocQuery;
import com.marklogic.xcc.Content;
import com.marklogic.xcc.ContentCreateOptions;
import com.marklogic.xcc.ContentFactory;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;

public final class MarkLogicNSFWriter implements NSFWriter {
	
	private Session mlSession = null;

	public void init(ServletConfig config, HttpServletRequest request) {
		try {
			String marklogicURL = config.getInitParameter("marklogic-url");
			URI uri = new URI(marklogicURL);
		    ContentSource contentSource = ContentSourceFactory.newContentSource(uri);
		    
		    mlSession = contentSource.newSession();
		    
//TODO
//		    String txid = request.getParameter("txid");
//		    if (txid == null){
		    	mlSession.setTransactionMode(com.marklogic.xcc.Session.TransactionMode.AUTO);
//		    }
		    
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
			
		} catch (XccConfigException e) {
			throw new IllegalStateException(e);
		}
	}

	public void close() {
		if (mlSession != null && !mlSession.isClosed()){
			mlSession.close();
		}
	}

	public void outRelatedObject(String documentURI, InputStream document, Map permissionMap, String[] collection, int weight) throws IOException {
		
		ContentCreateOptions options = new ContentCreateOptions();
//TODO
//		options.setPermissions(permissions);
		options.setCollections(collection);
		options.setQuality(weight);

		Content content = ContentFactory.newContent(documentURI, document, options);
		try {
			mlSession.insertContent(content);
			
		} catch (RequestException e) {
			throw new IllegalStateException(e);
		}
	}

	public void outMainObject(String documentURI, String document, Object permission, String[] collection, int weight) throws IOException {
		String relatedObjectDirectory = NSFConverterUtils.getRelatedObjectIDBase(documentURI);	
		directoryDelete(relatedObjectDirectory);
		
		ContentCreateOptions options = new ContentCreateOptions();
//TODO
//		options.setPermissions(permissions);
		options.setCollections(collection);

		Content content = ContentFactory.newContent(documentURI, document, options);
		try {
			mlSession.insertContent(content);
			
		} catch (RequestException e) {
			throw new IllegalStateException(e);
		}
	}

	private void directoryDelete(String directoryURI) {
		try {
			String query = "xdmp:directory-delete(\"" + NSFConverterUtils.escapeXQuery(directoryURI) + "\")";
			AdhocQuery request = mlSession.newAdhocQuery(query);
			mlSession.submitRequest(request);
			
		} catch (RequestException e) {
			throw new IllegalStateException(e);
		}
	}

	public String getViewableURL(String objectID) {
		return "/retrieve?document-uri=" + objectID;
	}
}
