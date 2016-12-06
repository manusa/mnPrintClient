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

/**
 * Created by Marc Nuri <marc@marcnuri.com> on 2016-12-06.
 */
public class PrintClient {

//**************************************************************************************************
//  Fields
//**************************************************************************************************
	private final String printServerUrl;
	private final String printerName;


//**************************************************************************************************
//  Constructors
//**************************************************************************************************
	public PrintClient(String printServerUrl, String printerName) {
		this.printServerUrl = printServerUrl;
		this.printerName = printerName;
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

//**************************************************************************************************
//  Getter/Setter Methods
//**************************************************************************************************

//**************************************************************************************************
//  Static Methods
//**************************************************************************************************
	public static void main(String[] args) {
		String printServerUrl = null;//-u
		String printerName = null; //-n
		for (int a = 0; a < args.length; a++) {
			if (args[a].equals("-u") && a + 1 < args.length) {
				printServerUrl = args[a+1];
			}
			if (args[a].equals("-n") && a + 1 < args.length) {
				printerName = args[a+1];
			}
		}
		final PrintClient pc = new PrintClient(printServerUrl, printerName);
	}

//**************************************************************************************************
//  Inner Classes
//**************************************************************************************************

}
