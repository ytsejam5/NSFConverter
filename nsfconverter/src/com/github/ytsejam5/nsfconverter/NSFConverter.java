package com.github.ytsejam5.nsfconverter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.MIMEEntity;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

import com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility;

public final class NSFConverter {
	
	private Session notesSession = null;

	public NSFConverter() {
		NotesThread.sinitThread();

		try {
			notesSession = NotesFactory.createSession();	
			
		} catch (NotesException e) {
			throw new IllegalStateException(e);
		} 	
	}

	public void convert(File nsfFile, NSFWriter writer) throws NotesException, IOException {
		
		NSFDocumentHandler documentHandler = new NSFDocumentHandler();
		
		Database database = null;
		DocumentCollection dc = null;
		
		try {
			notesSession.setConvertMIME(false);
			
			database = notesSession.getDatabase("", nsfFile.getPath());		
			dc = database.getAllDocuments();
		
			for (Document doc = dc.getFirstDocument(); doc != null; doc = dc.getNextDocument(doc)) {
				documentHandler.handleDocument(doc, writer);
			}

		} finally {
			try { if (dc != null) { dc.recycle(); } }
			finally { if (database != null) { database.recycle(); } }
		}
	}

	public void close() throws NotesException {			
		try {
			if (notesSession != null) {
				notesSession.recycle();
			}
			
		} finally {
			NotesThread.stermThread();
		}
	}
	
	class NSFDocumentHandler {
		
		private Document document = null;
		private NSFWriter writer = null;
		private Map<String, String> contentIDRelatedFileNamesMap = null;
		private List<String> attachementFileNames = null;
		
		private void handleDocument(Document document, NSFWriter writer) throws NotesException, IOException {	
			document.convertToMIME(Document.CVT_RT_TO_HTML, 0);

			this.document = document;
			this.writer = writer;
		
			this.contentIDRelatedFileNamesMap = new HashMap<String, String>();
			this.attachementFileNames = new LinkedList<String>();
			for (MIMEEntity entry = document.getMIMEEntity(); entry != null; entry = entry.getNextSibling()){
				fillRelatedFileNames(entry);
			}	

			for (MIMEEntity entry = document.getMIMEEntity(); entry != null; entry = entry.getNextSibling()){
				handleEntry(entry);	
			}
		}

		private void fillRelatedFileNames(MIMEEntity entry) throws NotesException {		
			if (NSFConverterUtils.isMultipartContent(entry)){
				for (MIMEEntity child = entry.getFirstChildEntity(); child != null; child = child.getNextSibling()){
					fillRelatedFileNames(child);
				}

			} else if (!NSFConverterUtils.isMainObject(entry)){
				String contentID = NSFConverterUtils.getContentID(entry);
				String fileName = NSFConverterUtils.getFileName(entry);
				contentIDRelatedFileNamesMap.put(contentID, fileName);
				
				if (NSFConverterUtils.isAttachment(entry)){
					attachementFileNames.add(fileName);
				}
			}
		}
		
		private void handleEntry(MIMEEntity entry) throws NotesException, IOException {
			if (NSFConverterUtils.isMultipartContent(entry)){
				for (MIMEEntity child = entry.getFirstChildEntity(); child != null; child = child.getNextSibling()){
					handleEntry(child);
				}

			} else if (NSFConverterUtils.isMainObject(entry)){
				handleMainObject(entry);
				
			} else {
				handleRelatedObject(entry);
			}
		}

		private void handleMainObject(MIMEEntity entry) throws NotesException, IOException {
			entry.decodeContent();

			String content = entry.getContentAsText();
			
			content = complementContentType(content);
			content = replaceRelatedObjectRefs(content);
			content = replaceAttachmentObjectRefs(content);

			String objectID = getMainObjectID();
			String[] collection = new String[]{"document"};
			
			writer.outMainObject(objectID, content, null, collection, 0);
		}

		private void handleRelatedObject(MIMEEntity entry) throws NotesException, IOException {
			entry.decodeContent();
			entry.encodeContent(MIMEEntity.ENC_BASE64);
			
			InputStream is = null;
			
			try {
				String contentID = NSFConverterUtils.getContentID(entry);
				String fileName = (String)contentIDRelatedFileNamesMap.get(contentID);
				String objectID = getRelatedObjectID(fileName);
				String contentType = NSFConverterUtils.getContentType(entry);

				try {
					is = MimeUtility.decode(entry.getInputStream(), "base64");
					writer.outRelatedObject(objectID, is, null, new String[]{ contentType }, 0);
					
				} catch (NullPointerException ignore) {
					// Case of having empty string in a content.
					writer.outRelatedObject(objectID, NSFConverterUtils.emptyStream(), null, new String[]{ contentType }, 0);
				}

			} catch (MessagingException e) {
				throw new IllegalStateException(e);
				
			} finally {
				if (is != null){ is.close(); }
			}
		}
			
		private String complementContentType(String convertedHTML) {
			return "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"></head>" + convertedHTML.substring(6);
		}
		
		private String replaceRelatedObjectRefs(String content) throws NotesException {
			for (Iterator<String> iterator = contentIDRelatedFileNamesMap.keySet().iterator(); iterator.hasNext();) {
				String contentID = (String) iterator.next();
				String fileName = (String)contentIDRelatedFileNamesMap.get(contentID);
				String objectID = getRelatedObjectID(fileName);
				String viewableURL = writer.getViewableURL(objectID);
				
				content = content.replaceAll("cid:" + contentID, viewableURL);
			}
			
			return content;
		}
		
		private String replaceAttachmentObjectRefs(String content) throws NotesException {
			String triggerStart = "<i>(“Y•tƒtƒ@ƒCƒ‹: ";
			String triggerEnd = ")</i>";
			
			for (int position = 0; (position = content.indexOf(triggerStart, position)) > -1; ) {
				int labelStartPosition = position + triggerStart.length();
				int labelEndPosition = content.indexOf(triggerEnd, labelStartPosition + 1);

				String label = content.substring(labelStartPosition, labelEndPosition);
				String fileName = lookupAttachmentFileName(label);
			
				if (fileName != null){
					String objectID = getRelatedObjectID(fileName);
					String viewableURL = writer.getViewableURL(objectID);
					String replacement = "<a href=\"" + viewableURL + "\" target=\"_blank\">" + label + "</a>";
					
					String head = content.substring(0, labelStartPosition);
					String tail = content.substring(labelEndPosition);
					
					content = head + replacement + tail;
					position = labelStartPosition + replacement.length() + triggerEnd.length();

				} else {
					position = labelStartPosition + label.length() + triggerEnd.length();
				}
			}
			
			return content;
		}

		private String lookupAttachmentFileName(String fileName) {
			if (attachementFileNames.contains(fileName)){
				synchronized(attachementFileNames){
					attachementFileNames.remove(fileName);
				}
				
				return fileName;
			}
		
			// Case of including attachment files having duplicated file names.
			String lookedUp = lookupMinimumControlNumberFileName(attachementFileNames, fileName);
			if (lookedUp != null){
				synchronized(attachementFileNames){
					attachementFileNames.remove(lookedUp);
				}
			}
			
			return lookedUp;
		}
		
		private String lookupMinimumControlNumberFileName(List<String> attachementFileNames, String fileName) {
			// Codes below returns a file name like "foobar.002.doc" instead of "foobar.doc" by looking up from mime headers.
			String lookedUp = null;
			
			String fileBaseName = NSFConverterUtils.getFileBaseName(fileName);
			String fileExtension = NSFConverterUtils.getFileExtention(fileName);

			int minimumControlNumber = Integer.MAX_VALUE;
			for (Iterator<String> iterator = attachementFileNames.iterator(); iterator.hasNext(); ) {
				String relatedFileName = (String) iterator.next();
				if (!relatedFileName.startsWith(fileBaseName + ".") || !relatedFileName.endsWith(fileExtension)){
					continue;
				}
				
				String controlNumberString = relatedFileName.substring(
						(fileBaseName + ".").length(), relatedFileName.lastIndexOf(fileExtension));
				if (!controlNumberString.matches("^[\\d]+$")){
					continue;
				}
				
				int controlNumber = Integer.parseInt(controlNumberString);
				if (controlNumber < minimumControlNumber){
					minimumControlNumber = controlNumber;
					lookedUp = relatedFileName;
				}
			}
			
			return lookedUp;
		}
		
		private String getMainObjectID() throws NotesException {
//			String subject = trim(document.getItemValue("Subject").toString(), "[", "]");
//			subject = subject.replaceAll("[\\n|\\r]", "");
//			subject = subject.replaceAll("/", "^");
//			if (subject.equals("")){
//				subject = "(Œ–¼–³‚µ)";
////				System.out.println(document.getUniversalID());
//			}
//			return "/" + subject;
			
			return "/" + document.getUniversalID();
		}
		
		private String getRelatedObjectID(String fileName) throws NotesException {	
			return NSFConverterUtils.getRelatedObjectIDBase(getMainObjectID()) + fileName;
		}
	}
}
