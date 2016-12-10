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
 * PrintTask.java
 *
 * Created on 2016-12-10, 19:55
 */
package com.marcnuri.printclient;

/**
 * Created by Marc Nuri <marc@marcnuri.com> on 2016-12-10.
 *
 */
public class PrintTask {

//**************************************************************************************************
//  Fields
//**************************************************************************************************
	private final String url;
	private final String printerName;
	private final Integer copies;

//**************************************************************************************************
//  Constructors
//**************************************************************************************************
	public PrintTask(String url, String printerName, Integer copies) {
		this.url = url;
		this.printerName = printerName;
		this.copies = copies;
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
	public String getUrl() {
		return url;
	}

	public String getPrinterName() {
		return printerName;
	}

	public Integer getCopies() {
		return copies;
	}

//**************************************************************************************************
//  Static Methods
//**************************************************************************************************

//**************************************************************************************************
//  Inner Classes
//**************************************************************************************************

}
