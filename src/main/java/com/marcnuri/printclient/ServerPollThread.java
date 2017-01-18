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
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaTray;
import java.awt.*;
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
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	private final PrintClient printClient;
	private final Pattern contentDispositionP;
	private boolean polling;

//**************************************************************************************************
//  Constructors
//**************************************************************************************************
	public ServerPollThread(PrintClient printClient) {
		this.printClient = printClient;
		contentDispositionP = Pattern.compile("filename[^;=\\n]*=((['\"]).*?\\2|[^;\\n]*)");
		polling = false;
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
		if(!polling){
			polling=true;
			try {
				final List<PrintTask> tasks = pollServer();
				print(tasks);
			} catch (IOException e) {
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "", e);
			}
			polling=false;
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

	private void print(List<PrintTask> tasks){
		for(PrintTask pt : tasks){
			try {
				print(pt);
			} catch (Exception e) {
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "",
						e);
			}
		}
	}

	private void print(PrintTask pt) throws IOException, PrinterException{
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
			//////////////////////////////////////////////////////////////
			//Create PrinterJob
			final PrinterJob pjob= PrinterJob.getPrinterJob();
			//Set printer name
			final PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
			for(PrintService srv : PrintServiceLookup.lookupPrintServices(null, null)){
				if(srv.getName().equalsIgnoreCase(pt.getPrinterName())){
					//Important to traverse media's so that they are available later on.
					final Media[] res = (Media[]) srv.getSupportedAttributeValues(
							Media.class, null, null);
					for (Media temp : res) {
						if (temp instanceof MediaTray) {
							Object trayCat = srv.getSupportedAttributeValues(
									((MediaTray) temp).getCategory(), null, null);
							trayCat = null;
						}
					}
					//Full Traverse Of possible services:
					for (Class c : srv.getSupportedAttributeCategories()) {
						Object temp = srv.getSupportedAttributeValues(c, null, null);
					}
					pjob.setPrintService(srv);
					break;
				}
			}
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
			final PageFormat pageFormat = PrinterJob.getPrinterJob().defaultPage();
			final Paper paper = pageFormat.getPaper();
			paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
			pageFormat.setPaper(paper);
			pjob.validatePage(pageFormat);
			if(pdfFile.getNumPages() > 0){
				final List<Clip> clips = new ArrayList<Clip>();
				for (int p = 1; p < pdfFile.getNumPages() + 1; p++) {
					//Clip pdf to several pages
//					clips.addAll(Clip.
//							fromPdf(pageFormat, pdfFile, p));
					//Scale page to fit printer page
					clips.add(Clip.
							scaledFromPdf(pageFormat, pdfFile, p));
				}
				final Book book = new Book();
				book.append(new ClipPrint(pdfFile, clips),
						pageFormat, clips.size());
				pjob.setPageable(book);
				pjob.print();
			}
		}
	};
//**************************************************************************************************
//  Getter/Setter Methods
//**************************************************************************************************

//**************************************************************************************************
//  Static Methods
//**************************************************************************************************

//**************************************************************************************************
//  Inner Classes
//**************************************************************************************************
	private static final class ClipPrint implements Printable {

		private final PDFFile file;
		private final List<Clip> clips;

		public ClipPrint(PDFFile file,
						 List<Clip> clips) {
			this.file = file;
			this.clips = clips;
		}

		public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
				throws PrinterException {
			// don't bother if the page number is out of range.
			if ((pageIndex >= 0) && (pageIndex < clips.size())) {
				final Clip clip = clips.get(pageIndex);
				final PDFPage page = file.getPage(clip.pageNumber);
				final Graphics2D g2 = (Graphics2D) graphics;
				g2.setRenderingHint(
						RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				g2.setRenderingHint(
						RenderingHints.KEY_RENDERING,
						RenderingHints.VALUE_RENDER_QUALITY);
				final PDFRenderer pgs = new PDFRenderer(page, g2,
						new Rectangle(
								(int) pageFormat.getImageableX(),
								(int) pageFormat.getImageableY(),
								(int) pageFormat.getImageableWidth(),
								(int) pageFormat.getImageableHeight()),
						clip.clip, Color.WHITE);
				try {
					page.waitForFinish();
					pgs.run();
				} catch (InterruptedException ie) {
				}
				return PAGE_EXISTS;
			} else {
				return NO_SUCH_PAGE;
			}
		}
	}

	private static final class Clip {

		/**
		 * In pdf Coordinates
		 *
		 * PDF COORDINATES ARE (0,0) in (left,bottom)
		 */
		private final Rectangle clip;
		/**
		 * The number of the page in the PDF FIle
		 */
		private final int pageNumber;

		private Clip(Rectangle clip, int pageNumber) {
			this.clip = clip;
			this.pageNumber = pageNumber;
		}

		private static List<Clip> fromPdf(PageFormat pageFormat, PDFFile pdf,
										  int pageNumber) {
			final PDFPage pdfPage = pdf.getPage(pageNumber);
			//Page width in the final pageFormat
			final double pWidth = pageFormat.getImageableWidth();
			//Page Height in the final page format
			final double pHeight = pageFormat.getImageableHeight();
			//Original PDF WIDTH (PDF SIZE)
			final double pdfWidth = pdfPage.getBBox().getWidth();
			//Original PDF HEIGHT
			final double pdfHeight = pdfPage.getBBox().getHeight();
			//HEIGHT OF A SCALED PDF when changing the width and keeping aspect ratio
			final double scaledHeight = (pdfHeight * (pWidth / pdfWidth));
			//Maximum number of tiles (integer)
			final int tiles = (int) Math.ceil(scaledHeight / pHeight);
			//Tile height
			final double pdfTileHeight = pdfHeight / (scaledHeight / pHeight);
			final List<Clip> clips = new ArrayList<Clip>(tiles);
			//PDF COORDINATES ARE (0,0) in (left,bottom) (We go from the top down)
			int y = (int) pdfHeight;
			int x = 0;
			for (int it = 0; it < tiles; it++) {
				y -= (int) (pdfTileHeight);
				final Rectangle temp = new Rectangle(x, y,
						(int) pdfWidth, (int) pdfTileHeight);
				clips.add(new Clip(temp, pageNumber));
			}
			return clips;
		}

		private static Clip scaledFromPdf(PageFormat pageFormat, PDFFile pdf, int pageNumber){
//			final PDFPage pdfPage = pdf.getPage(pageNumber);
//			//Page width in the final pageFormat
//			final double pWidth = pageFormat.getImageableWidth();
//			//Page Height in the final page format
//			final double pHeight = pageFormat.getImageableHeight();
//			//Original PDF WIDTH
//			final double pdfWidth = pdfPage.getBBox().getWidth();
//			//Original PDF HEIGHT
//			final double pdfHeight = pdfPage.getBBox().getHeight();
//			//Scale ratio
//			final double widthRatio = pWidth /pdfWidth;
//			final double heightRatio = pHeight/pdfHeight;
			return new Clip(null, pageNumber);
		}
	}
}
