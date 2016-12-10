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

import java.util.Timer;

/**
 *
 * Created by Marc Nuri <marc@marcnuri.com> on 2016-12-06.
 */
public class PrintClient {

//**************************************************************************************************
//  Fields
//**************************************************************************************************
	private static final long DEFAULT_POLL_TIME = 5000L;
	private static final int HTTP_STATUS_CODE_UNAUTHORIZED = 401;
	private final Timer timer;
	private boolean started;
	private String printServerUrl;
	private String cookie;
	private boolean sslTrustAll;
	private long pollTime = DEFAULT_POLL_TIME;


//**************************************************************************************************
//  Constructors
//**************************************************************************************************
	public PrintClient(String printServerUrl) {
		this.started = false;
		this.printServerUrl = printServerUrl;
		this.timer = new Timer();
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
	private void start(){
		if(!started) {
			timer.schedule(new ServerPollThread(this), 0L, getPollTime());
			started=true;
		}
	}


//**************************************************************************************************
//  Getter/Setter Methods
//**************************************************************************************************
	public String getPrintServerUrl() {
		return printServerUrl;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
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
		String cookie = null;//-jsessionid
		boolean sslTrustAll = false;//-sslTrust
		for (int a = 0; a < args.length; a++) {
			if (args[a].equals("-url") && a + 1 < args.length) {
				printServerUrl = args[a+1];
			}
			if (args[a].equals("-cookie") && a + 1 < args.length) {
				cookie = args[a+1];
			}
			if (args[a].equals("-sslTrust")) {
				sslTrustAll = true;
			}
		}
		final PrintClient pc = new PrintClient(printServerUrl);
		pc.setCookie(cookie);
		pc.setSslTrustAll(sslTrustAll);
		pc.start();
	}


//**************************************************************************************************
//  Inner Classes
//**************************************************************************************************

}
