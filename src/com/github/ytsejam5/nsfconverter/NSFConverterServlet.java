package com.github.ytsejam5.nsfconverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lotus.domino.NotesException;

public class NSFConverterServlet extends HttpServlet {

	private static final long serialVersionUID = -3458098410535176941L;
	
	@Override
	public void log(String msg) {
		System.out.println(msg);
		super.log(msg);
	}
	
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doProcess(request, response);
	}
	
	private void doProcess(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		File nsfFile = createTempNSF(request);
		if (nsfFile.length() == 0){
			throw new IllegalArgumentException("Invalid input.");
		}
		
		NSFConverter converter = new NSFConverter();
		
		NSFWriter writer = createNFSWriter();
		writer.init(getServletConfig(), request);
		
		try {	
			converter.convert(nsfFile, writer);
			
		} catch (NotesException e) {
			throw new IllegalStateException(e);
			
		} finally {
			try {
				converter.close();
			
			} catch (NotesException e) {
				throw new IllegalStateException(e);
				
			} finally {
				try {
					writer.close();
					
				} finally {
					if (nsfFile.exists()){
						nsfFile.delete();
					}
				}
			}	
		}
	}

	private File createTempNSF(HttpServletRequest request)
			throws IOException, FileNotFoundException {
		String tempfilePrefix = getServletConfig().getInitParameter("tempfile-prefix");
		String tempfileSuffix = getServletConfig().getInitParameter("tempfile-suffix");
		
		File nsfFile = File.createTempFile(tempfilePrefix, tempfileSuffix);
		
		OutputStream os = null;
		
		try {
			InputStream is = request.getInputStream();
			os = new FileOutputStream(nsfFile);
			
			byte[] buffer = new byte[128 * 1024 * 1024];
		    for (int length; (length = is.read(buffer)) >= 0; ) {
		    	os.write(buffer, 0, length);
		    }
			
		    os.flush();
			
		} finally {
			if (os != null){
				os.close();
			}
		}
		
		return nsfFile;
	}
	
	private NSFWriter createNFSWriter() {		
		String writerClass = getServletConfig().getInitParameter("writer-class");
	
		NSFWriter writer = null;
		
		try {
			writer = (NSFWriter)Class.forName(writerClass).newInstance();
			
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
			
		} catch (InstantiationException e) {
			throw new IllegalStateException(e);
			
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
		
		return writer;
	}
}
