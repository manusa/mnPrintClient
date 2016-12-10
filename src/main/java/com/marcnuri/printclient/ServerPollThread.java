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
import com.eclipsesource.json.JsonValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Marc Nuri <marc@marcnuri.com> on 2016-12-10.
 */
public class ServerPollThread extends TimerTask{

//**************************************************************************************************
//  Fields
//**************************************************************************************************
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
				pollServer();
			} catch (IOException e) {
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "", e);
			}
			polling.set(false);
		}
	}

//**************************************************************************************************
//  Other Methods
//**************************************************************************************************
	private void pollServer() throws IOException {
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

		}

	//		Json
	//		final int response = request.getResponseCode();
	//		final JsonParser jp = new JsonParser(); //from gson
	//		final JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
	//		request.disconnect();
	//		final JsonArray rootobj = root.getAsJsonArray(); //May be an array, may be an object.
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
