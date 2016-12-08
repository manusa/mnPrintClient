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
 * PrintClient.java
 *
 * Created on 2016-12-06, 10:21
 */
package com.marcnuri.printclient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Created by Marc Nuri <marc@marcnuri.com> on 2016-12-06.
 */
public class PrintClient {

//**************************************************************************************************
//  Fields
//**************************************************************************************************
	private static final long DEFAULT_POLL_TIME = 1000L;
	private final String printServerUrl;
	private String jSessionId;
	private boolean sslTrustAll;
	private long pollTime = DEFAULT_POLL_TIME;


//**************************************************************************************************
//  Constructors
//**************************************************************************************************
	public PrintClient(String printServerUrl) {
		this.printServerUrl = printServerUrl;
	}

//**************************************************************************************************
//  Abstract Methods
//**************************************************************************************************

//**************************************************************************************************
//  Overridden Methods
//**************************************************************************************************

//**************************************************************************************************
//  Other Methods
//**************************************************************************************************
	private void pollServer() throws IOException{
		//Don't check ssl security, validate any certificate. Useful for self-signed certificates
		//or certificates from non-authorities.
		if(getSslTrustAll()) {
			try {
				disableSSLCertificateChecking();
			} catch (GeneralSecurityException e) {
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "",
						e);
			}
		}
		final URL url = new URL(printServerUrl);
		final HttpURLConnection request = (HttpURLConnection)url.openConnection();
		//Place JSESSIONID in request header to emulate a user session
		if(getjSessionId() != null && !getjSessionId().isEmpty()) {
			request.setRequestProperty("Cookie", String.format("JSESSIONID=%s",getjSessionId()));
		}

		request.connect();
		final JsonParser jp = new JsonParser(); //from gson
		final JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
		JsonArray rootobj = root.getAsJsonArray(); //May be an array, may be an object.
		request.disconnect();
	}

	public void simpleTest(){
		try {
			pollServer();
		} catch (java.io.IOException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "", e);
		}
	}


//**************************************************************************************************
//  Getter/Setter Methods
//**************************************************************************************************
	public String getjSessionId() {
		return jSessionId;
	}

	public void setjSessionId(String jSessionId) {
		this.jSessionId = jSessionId;
	}

	public long getPollTime() {
		return pollTime;
	}

	public void setPollTime(long pollTime) {
		this.pollTime = pollTime;
	}

	public boolean getSslTrustAll() {
		return sslTrustAll;
	}

	public void setSslTrustAll(boolean sslTrustAll) {
		this.sslTrustAll = sslTrustAll;
	}

//**************************************************************************************************
//  Static Methods
//**************************************************************************************************
	public static void main(String[] args) {
		String printServerUrl = null;//-url
		String jSessionId = null;//-jsessionid
		boolean sslTrustAll = false;//-sslTrust
		for (int a = 0; a < args.length; a++) {
			if (args[a].equals("-url") && a + 1 < args.length) {
				printServerUrl = args[a+1];
			}
			if (args[a].equals("-jsessionid") && a + 1 < args.length) {
				jSessionId = args[a+1];
			}
			if (args[a].equals("-sslTrust") && a + 1 < args.length) {
				if(args[a+1].equalsIgnoreCase("y")){
					sslTrustAll = true;
				}
			}
		}
		final PrintClient pc = new PrintClient(printServerUrl);
		pc.setjSessionId(jSessionId);
		pc.setSslTrustAll(sslTrustAll);
		pc.simpleTest();
	}

	private static void disableSSLCertificateChecking() throws GeneralSecurityException {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				// Not implemented
			}

			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				// Not implemented
			}
		} };
		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}
//**************************************************************************************************
//  Inner Classes
//**************************************************************************************************

}
