/*
 * DirectoryPollThread.java
 *
 * Created on 2017-01-22, 17:30
 */
package com.marcnuri.printclient;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marc Nuri <marc@marcnuri.com> on 2017-01-22.
 */
public class DirectoryPollThread extends AbstractPollThread {

//**************************************************************************************************
//  Fields
//**************************************************************************************************
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
		if(directoryF.exists() && directoryF.isDirectory()){
			for(File f : directoryF.listFiles()) {
				if(f.isFile() && f.getName().length() > PDF_EXTENSION.length()
						&& f.getName().toUpperCase().endsWith(PDF_EXTENSION)){
					final PrintTask toAdd = new PrintTask(f.getAbsolutePath(),
							getPrintClient().getDefaultPrinterName(),
							getPrintClient().getDefaultCopies());
					ret.add(toAdd);
				}
			}
		}
		return ret;
	}

	@Override
	protected final void print(PrintTask pt, PrinterJob pjob) throws IOException, PrinterException {

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
