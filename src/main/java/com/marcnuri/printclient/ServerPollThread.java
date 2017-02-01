/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * ServerPollThread.java
 *
 * Created on 2016-12-10, 19:29
 */
package com.marcnuri.printclient;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;
import com.sun.pdfview.PDFFile;

import java.awt.print.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Marc Nuri <marc@marcnuri.com> on 2016-12-10.
 *
 */
public class ServerPollThread extends AbstractPollThread{

//**************************************************************************************************
//  Fields
//**************************************************************************************************
	private static final String PARAMETER_URL = "url";
	private static final String PARAMETER_PRINTER_NAME = "printerName";
	private static final String PARAMETER_COPIES = "copies";
	private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	private final Pattern contentDispositionP;

//**************************************************************************************************
//  Constructors
//**************************************************************************************************
	public ServerPollThread(PrintClient printClient) {
		super(printClient);
		contentDispositionP = Pattern.compile("filename[^;=\\n]*=((['\"]).*?\\2|[^;\\n]*)");
	}

//**************************************************************************************************
//  Abstract Methods
//**************************************************************************************************

//**************************************************************************************************
//  Overridden Methods
//**************************************************************************************************
	@Override
	protected final void print(PrintTask pt, PrinterJob pjob) throws IOException, PrinterException {
		final URLConnection uc = new URL(pt.getUrl()).openConnection();
		final InputStream is = uc.getInputStream();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		long count = 0;
		int n = 0;
		while (-1 != (n = is.read(buffer))) {
			baos.write(buffer, 0, n);
			count += n;
		}
		is.close();
		baos.close();
		final ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
		if(bb != null){
			final PDFFile pdfFile = new PDFFile(bb);
			//Rest of Print Job properties
			final String contentDisposition = uc.getHeaderField(HEADER_CONTENT_DISPOSITION);
			String fileName = "";
			if(contentDisposition != null && !contentDisposition.isEmpty()){
				final Matcher m = contentDispositionP.matcher(contentDisposition);
				if(m.find()){
					fileName = m.group(1).replace("\"", "");
				}
			}
			pjob.setJobName(String.format("printclient.marcnuri.com - %s", fileName));
			pjob.setCopies(pt.getCopies());
			//////////////////////////////////////////////////////////////
			AbstractPollThread.print(pjob,pdfFile);
		}
	}

	@Override
	protected final List<PrintTask> poll() throws IOException {
		final List<PrintTask> tasks = new ArrayList<PrintTask>();
		//Don't check ssl security, validate any certificate. Useful for self-signed certificates
		//or certificates from non-authorities.
		if(getPrintClient().getSslTrustAll()) {
			try {
				SSLHelper.disableSSLCertificateChecking();
			} catch (GeneralSecurityException e) {
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "", e);
			}
		}
		final URL url = new URL(getPrintClient().getPrintServerUrl());
		final HttpURLConnection request = (HttpURLConnection)url.openConnection();
		//Place JSESSIONID in request header to emulate a user session
		if(getPrintClient().getCookie() != null && !getPrintClient().getCookie().isEmpty()) {
			request.setRequestProperty("Cookie", getPrintClient().getCookie());
		}
		request.connect();
		final JsonValue jsv = Json.parse(new InputStreamReader((InputStream) request.getContent()));
		if(jsv.isArray()){
			final JsonArray jsonArray = jsv.asArray();
			for(JsonValue t : jsonArray.values()){
				if(t.isObject()){
					tasks.add(new PrintTask(
							t.asObject().getString(PARAMETER_URL, null),
							t.asObject().getString(PARAMETER_PRINTER_NAME, null),
							t.asObject().getInt(PARAMETER_COPIES, 0)
					));
				}
			}
		}
		return tasks;
	}

//**************************************************************************************************
//  Other Methods
//**************************************************************************************************


//**************************************************************************************************
//  Getter/Setter Methods
//**************************************************************************************************

//**************************************************************************************************
//  Static Methods
//**************************************************************************************************

//**************************************************************************************************
//  Inner Classes
//**************************************************************************************************

}
