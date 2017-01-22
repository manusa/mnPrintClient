/*
 * AbstractPollThread.java
 *
 * Created on 2017-01-22, 17:31
 */
package com.marcnuri.printclient;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Marc Nuri <marc@marcnuri.com> on 2017-01-22.
 */
public abstract class AbstractPollThread extends TimerTask {

//**************************************************************************************************
//  Fields
//**************************************************************************************************
	private final PrintClient printClient;
	private boolean polling;

//**************************************************************************************************
//  Constructors
//**************************************************************************************************
	public AbstractPollThread(PrintClient printClient) {
		this.printClient = printClient;
		polling = false;
	}

//**************************************************************************************************
//  Abstract Methods
//**************************************************************************************************
	protected abstract List<PrintTask> poll() throws IOException;
	protected abstract void print(PrintTask pt) throws IOException, PrinterException;

//**************************************************************************************************
//  Overridden Methods
//**************************************************************************************************
	@Override
	public void run() {
		//If task is scheduled using a repeating period this is unnecessary
		if(!polling){
			polling=true;
			try {
				final List<PrintTask> tasks = poll();
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
	protected final void print(List<PrintTask> tasks){
		for(PrintTask pt : tasks){
			try {
				print(pt);
			} catch (Exception e) {
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "",
						e);
			}
		}
	}

//**************************************************************************************************
//  Getter/Setter Methods
//**************************************************************************************************
	protected final PrintClient getPrintClient() {
		return printClient;
	}

//**************************************************************************************************
//  Static Methods
//**************************************************************************************************

//**************************************************************************************************
//  Inner Classes
//**************************************************************************************************
	protected static final class ClipPrint implements Printable {

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

	protected static final class Clip {

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

		protected static Clip scaledFromPdf(PageFormat pageFormat, PDFFile pdf, int pageNumber){
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
