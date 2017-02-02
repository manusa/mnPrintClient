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
import java.util.logging.ConsoleHandler;
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
	private static final long DEFAULT_POLL_TIME = 5000L;
	private static final int DEFAULT_COPIES = 1;
	private static final String DEFAULT_PROCESSED_DIRECTORY = "processed";
	private static final String ARGUMENT_HELP = "help";
	private static final String ARGUMENT_URL = "url";
	private static final String ARGUMENT_DIRECTORY = "dir";
	private static final String ARGUMENT_PROCESSED_DIRECTORY = "processed";
	private static final String ARGUMENT_COOKIE = "cookie";
	private static final String ARGUMENT_PRINTER_NAME = "printerName";
	private static final String ARGUMENT_COPIES = "copies";
	private static final int HTTP_STATUS_CODE_UNAUTHORIZED = 401;
	private static final String HELP = "\n" +
		"#############################################################################################################\n" +
		"# mnPrintClient                                                                                             #\n" +
		"#############################################################################################################\n" +
		"\n2017 www.marcnuri.com\n\n" +
		"Silently print PDF files from your server by reading a JSON parameter list, or from your local computer by\n" +
		"placing PDF files in the specified directory.\n\n" +
		"Usage: mnprintclient -[dir|url] url [options]\n" +
		"Options:\n" +
		"    -url: Url to poll with a valid JSON response\n" +
		"    -dir: Directory to poll where pdf files are placed to print\n" +
		"    -processed: Directory to move printed pdf files in dir mode\n" +
		"    -cookie: String with cookie to send to server in url mode\n" +
		"    --help: Prints this page"
			;
	private final Timer timer;
	private boolean started;
	private String printServerUrl;
	private String directory;
	private String processedDirectory = DEFAULT_PROCESSED_DIRECTORY;
	private String cookie;
	private boolean sslTrustAll;
	private String defaultPrinterName;
	private Integer defaultCopies = DEFAULT_COPIES;
	private long pollTime = DEFAULT_POLL_TIME;


//**************************************************************************************************
//  Constructors
//**************************************************************************************************
	public PrintClient() {
		this.started = false;
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
			if(getPrintServerUrl() != null && !getPrintServerUrl().isEmpty()){
				timer.schedule(new ServerPollThread(this), 0L, getPollTime());
			}
			if(getDirectory() != null && !getDirectory().isEmpty()){
				timer.schedule(new DirectoryPollThread(this), 0L, getPollTime());
			}
			started=true;
		}
	}


//**************************************************************************************************
//  Getter/Setter Methods
//**************************************************************************************************
	public String getPrintServerUrl() {
		return printServerUrl;
	}

	public void setPrintServerUrl(String printServerUrl) {
		this.printServerUrl = printServerUrl;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getProcessedDirectory() {
		return processedDirectory;
	}

	public void setProcessedDirectory(String processedDirectory) {
		this.processedDirectory = processedDirectory;
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

	public boolean isSslTrustAll() {
		return sslTrustAll;
	}

	public void setSslTrustAll(boolean sslTrustAll) {
		this.sslTrustAll = sslTrustAll;
	}

	public String getDefaultPrinterName() {
		return defaultPrinterName;
	}

	public void setDefaultPrinterName(String defaultPrinterName) {
		this.defaultPrinterName = defaultPrinterName;
	}

	public Integer getDefaultCopies() {
		return defaultCopies;
	}

	public void setDefaultCopies(Integer defaultCopies) {
		this.defaultCopies = defaultCopies;
	}

//**************************************************************************************************
//  Static Methods
//**************************************************************************************************
	public static void main(String[] args) {
		//Grab command-line options
		boolean help = false;
		String printServerUrl = null;//-url
		String directory = null;//-dir
		String processedDirectory = null;//-processed
		String cookie = null;//-cookie
		String defaultPrinterName = null;
		Integer defaultCopies = null;
		boolean sslTrustAll = false;//-sslTrust
		for (int a = 0; a < args.length; a++) {
			if (args[a].equals("--"+ARGUMENT_HELP) && a + 1 < args.length) {
				help = true;
			}
			if (args[a].equals("-"+ARGUMENT_URL) && a + 1 < args.length) {
				printServerUrl = args[a+1];
			}
			if (args[a].equals("-"+ARGUMENT_DIRECTORY) && a + 1 < args.length) {
				directory = args[a+1];
			}
			if (args[a].equals("-"+ARGUMENT_PROCESSED_DIRECTORY) && a + 1 < args.length) {
				processedDirectory = args[a+1];
			}
			if (args[a].equals("-"+ARGUMENT_COOKIE) && a + 1 < args.length) {
				cookie = args[a+1];
			}
			if (args[a].equals("-"+ARGUMENT_PRINTER_NAME) && a + 1 < args.length) {
				defaultPrinterName = args[a+1];
			}
			if (args[a].equals("-"+ARGUMENT_COPIES) && a + 1 < args.length) {
				try {
					defaultCopies = Integer.parseInt(args[a + 1]);
				} catch(NumberFormatException ex){}
			}
			if (args[a].equals("-sslTrust")) {
				sslTrustAll = true;
			}
		}
		if(help | (printServerUrl == null && directory == null)){
			System.out.println(HELP);
			return;
		}
		//Set logger
		Logger.getGlobal().addHandler(new ConsoleHandler());
		Logger.getGlobal().setLevel(Level.INFO);
		//Init PrintClient
		final PrintClient pc = new PrintClient();
		pc.setPrintServerUrl(printServerUrl);
		pc.setDirectory(directory);
		if(processedDirectory != null) {
			pc.setProcessedDirectory(processedDirectory);
		}
		pc.setCookie(cookie);
		pc.setDefaultPrinterName(defaultPrinterName);
		if(defaultCopies != null) {
			pc.setDefaultCopies(defaultCopies);
		}
		pc.setSslTrustAll(sslTrustAll);
		pc.start();
	}


//**************************************************************************************************
//  Inner Classes
//**************************************************************************************************

}
