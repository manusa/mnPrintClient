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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Marc Nuri <marc@marcnuri.com> on 2016-12-10.
 *
 */
public class ServerPollThread extends TimerTask{

//**************************************************************************************************
//  Fields
//**************************************************************************************************
	private static final String PARAMETER_URL = "url";
	private static final String PARAMETER_PRINTER_NAME = "printerName";
	private static final String PARAMETER_COPIES = "copies";
	private final PrintClient printClient;
	private AtomicBoolean polling;

//**************************************************************************************************
//  Constructors
//**************************************************************************************************
	public ServerPollThread(PrintClient printClient) {
		this.printClient = printClient;
		polling = new AtomicBoolean(false);
	}

//**************************************************************************************************
//  Abstract Methods
//**************************************************************************************************

//**************************************************************************************************
//  Overridden Methods
//**************************************************************************************************
	@Override
	public void run() {
		//If task is scheduled using a repeating period this is unnecessary
		if(!polling.get()){
			polling.set(true);
			try {
				final List<PrintTask> tasks = pollServer();
			} catch (IOException e) {
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "", e);
			}
			polling.set(false);
		}
	}

//**************************************************************************************************
//  Other Methods
//**************************************************************************************************
	private List<PrintTask> pollServer() throws IOException {
		final List<PrintTask> tasks = new ArrayList<PrintTask>();
		//Don't check ssl security, validate any certificate. Useful for self-signed certificates
		//or certificates from non-authorities.
		if(printClient.getSslTrustAll()) {
			try {
				SSLHelper.disableSSLCertificateChecking();
			} catch (GeneralSecurityException e) {
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "", e);
			}
		}
		final URL url = new URL(printClient.getPrintServerUrl());
		final HttpURLConnection request = (HttpURLConnection)url.openConnection();
		//Place JSESSIONID in request header to emulate a user session
		if(printClient.getCookie() != null && !printClient.getCookie().isEmpty()) {
			request.setRequestProperty("Cookie", printClient.getCookie());
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
//  Getter/Setter Methods
//**************************************************************************************************

//**************************************************************************************************
//  Static Methods
//**************************************************************************************************

//**************************************************************************************************
//  Inner Classes
//**************************************************************************************************

}
