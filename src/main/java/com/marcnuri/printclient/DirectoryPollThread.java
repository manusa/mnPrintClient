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
 * DirectoryPollThread.java
 *
 * Created on 2017-01-22, 17:30
 */
package com.marcnuri.printclient;

import com.sun.pdfview.PDFFile;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Marc Nuri <marc@marcnuri.com> on 2017-01-22.
 */
public class DirectoryPollThread extends AbstractPollThread {

//**************************************************************************************************
//  Fields
//**************************************************************************************************
	private static final Logger LOG = Logger.getLogger(DirectoryPollThread.class.toString());
	private static final String PDF_EXTENSION = "PDF";

//**************************************************************************************************
//  Constructors
//**************************************************************************************************
	public DirectoryPollThread(PrintClient printClient) {
		super(printClient);
	}

//**************************************************************************************************
//  Abstract Methods
//**************************************************************************************************

//**************************************************************************************************
//  Overridden Methods
//**************************************************************************************************
	@Override
	protected final List<PrintTask> poll() throws IOException {
		final List<PrintTask> ret = new ArrayList<PrintTask>();
		final File directoryF = new File(getPrintClient().getDirectory());
		//Check for processed directory
		final File directoryProcessed = new File(directoryF, getPrintClient().getProcessedDirectory());
		if(!directoryProcessed.exists() || directoryProcessed.isDirectory()){
			directoryProcessed.mkdirs();
		}
		if(directoryF.exists() && directoryF.isDirectory()){
			for(File f : directoryF.listFiles()) {
				if(f.isFile() && f.getName().length() > PDF_EXTENSION.length()
						&& f.getName().toUpperCase().endsWith(PDF_EXTENSION)){
					final PrintTask toAdd = new PrintTask(
							f.getAbsolutePath(),
							getPrintClient().getDefaultPrinterName(),
							getPrintClient().getDefaultCopies());
					ret.add(toAdd);
				}
			}
		}
		if(ret.size() > 0) {
			LOG.info(String.format("Detected %s new documents to print", ret.size()));
		}
		return ret;
	}

	@Override
	protected final void print(PrintTask pt, PrinterJob pjob) throws IOException, PrinterException {
		final File printFile = new File(pt.getUrl());
		//Load PDF in memory
		final InputStream is = new FileInputStream(printFile);
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
			final String fileName = printFile.getName();
			pjob.setJobName(String.format("printclient.marcnuri.com - %s", fileName));
			pjob.setCopies(pt.getCopies());
			//////////////////////////////////////////////////////////////
			AbstractPollThread.print(pjob,pdfFile);
			LOG.info(String.format("Printed %s", fileName));
			//////////////////////////////////////////////////////////////
			final File destFile = new File(getPrintClient().getDirectory() + File.separator
					+ getPrintClient().getProcessedDirectory(),fileName);
			if(destFile.exists()){
				destFile.delete();
			}
			printFile.renameTo(destFile);
		}
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
