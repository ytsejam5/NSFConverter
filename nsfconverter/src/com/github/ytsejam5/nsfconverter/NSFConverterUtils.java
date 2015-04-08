package com.github.ytsejam5.nsfconverter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.Format;

import lotus.domino.MIMEEntity;
import lotus.domino.MIMEHeader;
import lotus.domino.NotesException;

import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility;

public class NSFConverterUtils {

	private NSFConverterUtils(){}
	
	public static boolean isMainObject(MIMEEntity entry) throws NotesException{
		return getContentID(entry).equals("");
	}
	
	public static boolean isMultipartContent(MIMEEntity entry) throws NotesException{
		String contentType = entry.getContentType();
		return contentType.startsWith("multipart");
	}
	
	public static String getContentDisposition(MIMEEntity entry)
			throws NotesException {
		return getHeaderValue(entry, "Content-Disposition");
	}

	public static String getContentID(MIMEEntity entry) throws NotesException {
		return getHeaderValue(entry, "Content-ID");
	}
	
	public static String getHeaderValue(MIMEEntity entry, String headerName) throws NotesException{
		MIMEHeader header = entry.getNthHeader(headerName);
		if (header == null){
			return "";
		}
		
		return trim(header.getHeaderVal(), "<", ">");
	}
	
	public static boolean isAttachment(MIMEEntity entry) throws NotesException {
		return getContentDisposition(entry).equals("attachment");
	}
	
	public static String getFileName(MIMEEntity entry) throws NotesException {
		return getHeaderParameterValue(entry, "Content-Type", "name");
	}

	public static String getHeaderParameterValue(MIMEEntity entry, String headerName, String parameterName) throws NotesException {
		StringBuilder sb = new StringBuilder();

		try {
			String filenameHeaderValue = entry.getNthHeader(headerName).getParamVal(parameterName, true);
			String[] lines = filenameHeaderValue.split("\n");
			
			for (int i = 0; i < lines.length; i++) {
				sb.append(MimeUtility.decodeText(trim(lines[i].trim(), "\"", "\"")));	
			}
			
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
		
		return sb.toString();
	}

	public static String trim(String string, String start, String end) {
		if (string.startsWith(start)){
			string = string.substring(start.length());
		}
		if (string.endsWith(end)){
			string = string.substring(0, string.length() - end.length());
		}
		
		return string;
	}
	
	public static String getContentType(MIMEEntity entry) throws NotesException {
		return entry.getContentType() + "/" + entry.getContentSubType();
	}
	
	private static Format format = new DecimalFormat("000");
	public static String getDuplicatedObjectName(String original, int index){
		return original.replaceFirst("^(.*)(.[^\\.]+)?$", "$1" + format.format(index) + "$2");
	}

	public static InputStream emptyStream() {
		return new ByteArrayInputStream(new byte[]{});
	}
	
	public static String getRelatedObjectIDBase(String mainObjectID) {
		return mainObjectID + "_related/";
	}

	public static String escapeXQuery(String string) {
		string = string.replaceAll("\"", "\"\"");
		string = string.replaceAll("&", "&amp;");
		
		return string;
	}

	public static String getFileBaseName(String fileName) {
		int dotPosition = fileName.lastIndexOf(".");
		if (dotPosition < 0){
			return fileName;
		}
		
		return fileName.substring(0, dotPosition);
	}

	public static String getFileExtention(String fileName) {
		int dotPosition = fileName.lastIndexOf(".");
		if (dotPosition < 0){
			return "";
		}
		
		return fileName.substring(dotPosition);
	}
}
