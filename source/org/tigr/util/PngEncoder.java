/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: PngEncoder.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-03-10 15:27:01 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.util;

/**
 * PngEncoder takes a Java Image object and creates a byte string which can be saved as a PNG file.
 * The Image is presumed to use the DirectColorModel.
 *
 * Thanks to Jay Denny at KeyPoint Software
 *    http://www.keypoint.com/
 * who let me develop this code on company time.
 *
 * You may contact me with (probably very-much-needed) improvements,
 * comments, and bug fixes at:
 *
 *   david@catcode.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * A copy of the GNU LGPL may be found at
 * http://www.gnu.org/copyleft/lesser.html,
 *
 * @author J. David Eisenberg
 * @version 1.4, 31 March 2000
 */

import java.awt.Color;
import java.awt.Frame;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.tigr.util.swing.ProgressBar;

public class PngEncoder {
    /** Constant specifying that alpha channel should be encoded. */
    public static final boolean ENCODE_ALPHA=true;
    /** Constant specifying that alpha channel should not be encoded. */
    public static final boolean NO_ALPHA=false;
    /** Constants for filters */
    public static final int FILTER_NONE = 0;
    public static final int FILTER_SUB = 1;
    public static final int FILTER_UP = 2;
    public static final int FILTER_LAST = 2;
    
    protected byte[] pngBytes;
    protected byte[] priorRow;
    protected byte[] leftBytes;
    protected Image image;
    protected int width, height;
    protected int bytePos, maxPos;
    protected int hdrPos, dataPos, endPos;
    protected CRC32 crc = new CRC32();
    protected long crcValue;
    protected boolean encodeAlpha;
    protected int filter;
    protected int bytesPerPixel;
    protected int compressionLevel;
    
    private Frame ParentFrame;
    
    /**
     * Class constructor
     *
     */
    public PngEncoder() {
	this(null, null, false, FILTER_NONE, 0 );
    }
    
    /**
     * Class constructor specifying Image to encode, with no alpha channel encoding.
     *
     * @param image A Java Image object which uses the DirectColorModel
     * @see java.awt.Image
     */
    public PngEncoder( Image image ) {
	this(null, image, false, FILTER_NONE, 0);
    }
    
    /**
     * Class constructor specifying Image to encode, and whether to encode alpha.
     *
     * @param image A Java Image object which uses the DirectColorModel
     * @param encodeAlpha Encode the alpha channel? false=no; true=yes
     * @see java.awt.Image
     */
    public PngEncoder( Image image, boolean encodeAlpha ) {
	this(null, image, encodeAlpha, FILTER_NONE, 0);
    }
    
    /**
     * Class constructor specifying Image to encode, whether to encode alpha, and filter to use.
     *
     * @param image A Java Image object which uses the DirectColorModel
     * @param encodeAlpha Encode the alpha channel? false=no; true=yes
     * @param whichFilter 0=none, 1=sub, 2=up
     * @see java.awt.Image
     */
    public PngEncoder( Image image, boolean encodeAlpha, int whichFilter ) {
	this(null, image, encodeAlpha, whichFilter, 0 );
    }
    
    
    /**
     * Class constructor specifying Image source to encode, whether to encode alpha, filter to use, and compression level.
     *
     * @param image A Java Image object
     * @param encodeAlpha Encode the alpha channel? false=no; true=yes
     * @param whichFilter 0=none, 1=sub, 2=up
     * @param compLevel 0..9
     * @see java.awt.Image
     */
    public PngEncoder(Frame parentFrame, Image image, boolean encodeAlpha, int whichFilter,
    int compLevel) {
	this.image = image;
	this.encodeAlpha = encodeAlpha;
	setFilter( whichFilter );
	if (compLevel >=0 && compLevel <=9) {
	    this.compressionLevel = compLevel;
	}
	ParentFrame=parentFrame;
    }
    
    /**
     * Set the image to be encoded
     *
     * @param image A Java Image object which uses the DirectColorModel
     * @see java.awt.Image
     * @see java.awt.image.DirectColorModel
     */
    public void setImage( Image image ) {
	this.image = image;
	pngBytes = null;
    }
    
    /**
     * Creates an array of bytes that is the PNG equivalent of the current image, specifying whether to encode alpha or not.
     *
     * @param encodeAlpha boolean false=no alpha, true=encode alpha
     * @return an array of bytes, or null if there was a problem
     */
    public byte[] pngEncode( boolean encodeAlpha ) {
	byte[]  pngIdBytes = { -119, 80, 78, 71, 13, 10, 26, 10};
	int     i;
	
	if (image == null) {
	    return null;
	}
	width = image.getWidth( null );
	height = image.getHeight( null );
	//no effect ?? this.image = image;
	
	/*
	 * start with an array that is big enough to hold all the pixels
	 * (plus filter bytes), and an extra 200 bytes for header info
	 */
	pngBytes = new byte[((width+1) * height * 3) + 200];
	
	/*
	 * keep track of largest byte written to the array
	 */
	maxPos = 0;
	
	bytePos = writeBytes( pngIdBytes, 0 );
	hdrPos = bytePos;
	writeHeader();
	dataPos = bytePos;
	if (writeImageData()) {
	    writeEnd();
	    pngBytes = resizeByteArray( pngBytes, maxPos );
	} else {
	    pngBytes = null;
	}
	return pngBytes;
    }
    
    /**
     * Creates an array of bytes that is the PNG equivalent of the current image.
     * Alpha encoding is determined by its setting in the constructor.
     *
     * @return an array of bytes, or null if there was a problem
     */
    public byte[] pngEncode() {
	return pngEncode( encodeAlpha );
    }
    
    /**
     * Set the alpha encoding on or off.
     *
     * @param encodeAlpha  false=no, true=yes
     */
    public void setEncodeAlpha( boolean encodeAlpha ) {
	this.encodeAlpha = encodeAlpha;
    }
    
    /**
     * Retrieve alpha encoding status.
     *
     * @return boolean false=no, true=yes
     */
    public boolean getEncodeAlpha() {
	return encodeAlpha;
    }
    
    /**
     * Set the filter to use
     *
     * @param whichFilter from constant list
     */
    public void setFilter( int whichFilter ) {
	this.filter = FILTER_NONE;
	if (whichFilter <= FILTER_LAST) {
	    this.filter = whichFilter;
	}
    }
    
    /**
     * Retrieve filtering scheme
     *
     * @return int (see constant list)
     */
    public int getFilter() {
	return filter;
    }
    
    /**
     * Set the compression level to use
     *
     * @param level 0 through 9
     */
    public void setCompressionLevel( int level ) {
	if (level >= 0 && level <= 9) {
	    this.compressionLevel = level;
	}
    }
    
    /**
     * Retrieve compression level
     *
     * @return int in range 0-9
     */
    public int getCompressionLevel() {
	return compressionLevel;
    }
    
    /**
     * Increase or decrease the length of a byte array.
     *
     * @param array The original array.
     * @param newLength The length you wish the new array to have.
     * @return Array of newly desired length. If shorter than the
     *         original, the trailing elements are truncated.
     */
    protected byte[] resizeByteArray( byte[] array, int newLength ) {
	byte[]  newArray = new byte[newLength];
	int     oldLength = array.length;
	
	System.arraycopy( array, 0, newArray, 0,
	Math.min( oldLength, newLength ) );
	return newArray;
    }
    
    /**
     * Write an array of bytes into the pngBytes array.
     * Note: This routine has the side effect of updating
     * maxPos, the largest element written in the array.
     * The array is resized by 1000 bytes or the length
     * of the data to be written, whichever is larger.
     *
     * @param data The data to be written into pngBytes.
     * @param offset The starting point to write to.
     * @return The next place to be written to in the pngBytes array.
     */
    protected int writeBytes( byte[] data, int offset ) {
	maxPos = Math.max( maxPos, offset + data.length );
	if (data.length + offset > pngBytes.length) {
	    pngBytes = resizeByteArray( pngBytes, pngBytes.length +
	    Math.max( 1000, data.length ) );
	}
	System.arraycopy( data, 0, pngBytes, offset, data.length );
	return offset + data.length;
    }
    
    /**
     * Write an array of bytes into the pngBytes array, specifying number of bytes to write.
     * Note: This routine has the side effect of updating
     * maxPos, the largest element written in the array.
     * The array is resized by 1000 bytes or the length
     * of the data to be written, whichever is larger.
     *
     * @param data The data to be written into pngBytes.
     * @param nBytes The number of bytes to be written.
     * @param offset The starting point to write to.
     * @return The next place to be written to in the pngBytes array.
     */
    protected int writeBytes( byte[] data, int nBytes, int offset ) {
	maxPos = Math.max( maxPos, offset + nBytes );
	if (nBytes + offset > pngBytes.length) {
	    pngBytes = resizeByteArray( pngBytes, pngBytes.length +
	    Math.max( 1000, nBytes ) );
	}
	System.arraycopy( data, 0, pngBytes, offset, nBytes );
	return offset + nBytes;
    }
    
    /**
     * Write a two-byte integer into the pngBytes array at a given position.
     *
     * @param n The integer to be written into pngBytes.
     * @param offset The starting point to write to.
     * @return The next place to be written to in the pngBytes array.
     */
    protected int writeInt2( int n, int offset ) {
	byte[] temp = { (byte)((n >> 8) & 0xff),
	(byte) (n & 0xff)};
	return writeBytes( temp, offset );
    }
    
    /**
     * Write a four-byte integer into the pngBytes array at a given position.
     *
     * @param n The integer to be written into pngBytes.
     * @param offset The starting point to write to.
     * @return The next place to be written to in the pngBytes array.
     */
    protected int writeInt4( int n, int offset ) {
	byte[] temp = { (byte)((n >> 24) & 0xff),
	(byte) ((n >> 16) & 0xff ),
	(byte) ((n >> 8) & 0xff ),
	(byte) ( n & 0xff )};
	return writeBytes( temp, offset );
    }
    
    /**
     * Write a single byte into the pngBytes array at a given position.
     *
     * @param n The integer to be written into pngBytes.
     * @param offset The starting point to write to.
     * @return The next place to be written to in the pngBytes array.
     */
    protected int writeByte( int b, int offset ) {
	byte[] temp = { (byte) b};
	return writeBytes( temp, offset );
    }
    
    /**
     * Write a string into the pngBytes array at a given position.
     * This uses the getBytes method, so the encoding used will
     * be its default.
     *
     * @param n The integer to be written into pngBytes.
     * @param offset The starting point to write to.
     * @return The next place to be written to in the pngBytes array.
     * @see java.lang.String#getBytes()
     */
    protected int writeString( String s, int offset ) {
	return writeBytes( s.getBytes(), offset );
    }
    
    /**
     * Write a PNG "IHDR" chunk into the pngBytes array.
     */
    protected void writeHeader() {
	int startPos;
	
	startPos = bytePos = writeInt4( 13, bytePos );
	bytePos = writeString( "IHDR", bytePos );
	width = image.getWidth( null );
	height = image.getHeight( null );
	bytePos = writeInt4( width, bytePos );
	bytePos = writeInt4( height, bytePos );
	bytePos = writeByte( 8, bytePos ); // bit depth
	bytePos = writeByte( (encodeAlpha) ? 6 : 2, bytePos ); // direct model
	bytePos = writeByte( 0, bytePos ); // compression method
	bytePos = writeByte( 0, bytePos ); // filter method
	bytePos = writeByte( 0, bytePos ); // no interlace
	crc.reset();
	crc.update( pngBytes, startPos, bytePos-startPos );
	crcValue = crc.getValue();
	bytePos = writeInt4( (int) crcValue, bytePos );
    }
    
    /**
     * Perform "sub" filtering on the given row.
     * Uses temporary array leftBytes to store the original values
     * of the previous pixels.  The array is 16 bytes long, which
     * will easily hold two-byte samples plus two-byte alpha.
     *
     * @param pixels The array holding the scan lines being built
     * @param startPos Starting position within pixels of bytes to be filtered.
     * @param width Width of a scanline in pixels.
     */
    protected void filterSub( byte[] pixels, int startPos, int width ) {
	int i;
	int offset = bytesPerPixel;
	int actualStart = startPos + offset;
	int nBytes = width * bytesPerPixel;
	int leftInsert = offset;
	int leftExtract = 0;
	byte current_byte;
	
	for (i=actualStart; i < startPos + nBytes; i++) {
	    leftBytes[leftInsert] =  pixels[i];
	    pixels[i] = (byte) ((pixels[i] - leftBytes[leftExtract]) % 256);
	    leftInsert = (leftInsert+1) % 0x0f;
	    leftExtract = (leftExtract + 1) % 0x0f;
	}
    }
    
    /**
     * Perform "up" filtering on the given row.
     * Side effect: refills the prior row with current row
     *
     * @param pixels The array holding the scan lines being built
     * @param startPos Starting position within pixels of bytes to be filtered.
     * @param width Width of a scanline in pixels.
     */
    protected void filterUp( byte[] pixels, int startPos, int width ) {
	int     i, nBytes;
	byte    current_byte;
	
	nBytes = width * bytesPerPixel;
	
	for (i=0; i < nBytes; i++) {
	    current_byte = pixels[startPos + i];
	    pixels[startPos + i] = (byte) ((pixels[startPos  + i] - priorRow[i]) % 256);
	    priorRow[i] = current_byte;
	}
    }
    
    /**
     * Write the image data into the pngBytes array.
     * This will write one or more PNG "IDAT" chunks. In order
     * to conserve memory, this method grabs as many rows as will
     * fit into 32K bytes, or the whole image; whichever is less.
     *
     *
     * @return true if no errors; false if error grabbing pixels
     */
    protected boolean writeImageData() {
	int rowsLeft = height;  // number of rows remaining to write
	int startRow = 0;       // starting row to process this time through
	int nRows;              // how many rows to grab at a time
	
	byte[] scanLines;       // the scan lines to be compressed
	int scanPos;            // where we are in the scan lines
	int startPos;           // where this line's actual pixels start (used for filtering)
	
	byte[] compressedLines; // the resultant compressed lines
	int nCompressed;        // how big is the compressed area?
	
	int depth;              // color depth ( handle only 8 or 32 )
	
	PixelGrabber pg;
	
	bytesPerPixel = (encodeAlpha) ? 4 : 3;
	
	Deflater scrunch = new Deflater( compressionLevel );
	ByteArrayOutputStream outBytes =
	new ByteArrayOutputStream(1024);
	
	DeflaterOutputStream compBytes =new DeflaterOutputStream( outBytes, scrunch );
	double Factor=200/(double)height;
	long CurrentProgress=0;
	long OldCurrentProgress=0;
	
	try {
	    ProgressBar MyProgressBar=new ProgressBar(ParentFrame,"Image processing",new Color(0,0,128),new Color(0,128,225),Color.black,200);
	    while (rowsLeft > 0) {
		CurrentProgress=(int)((startRow)*Factor);
		if (CurrentProgress>OldCurrentProgress) {
		    MyProgressBar.set((int)CurrentProgress);
		    OldCurrentProgress=CurrentProgress;
		}
		nRows = Math.min( 32767 / (width*(bytesPerPixel+1)), rowsLeft );
		// nRows = rowsLeft;
		
		int[] pixels = new int[width * nRows];
		
		pg = new PixelGrabber(image, 0, startRow,
		width, nRows, pixels, 0, width);
		try {
		    pg.grabPixels();
		} catch (Exception e) {
		    System.err.println("interrupted waiting for pixels!");
		    return false;
		}
		if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
		    System.err.println("image fetch aborted or errored");
		    return false;
		}
		
		/*
		 * Create a data chunk. scanLines adds "nRows" for
		 * the filter bytes.
		 */
		scanLines = new byte[width * nRows * bytesPerPixel +  nRows];
		
		if (filter == FILTER_SUB) {
		    leftBytes = new byte[16];
		}
		if (filter == FILTER_UP) {
		    priorRow = new byte[width*bytesPerPixel];
		}
		
		scanPos = 0;
		startPos = 1;
		for (int i=0; i<width*nRows; i++) {
		    if (i % width == 0) {
			scanLines[scanPos++] = (byte) filter;
			startPos = scanPos;
		    }
		    scanLines[scanPos++] = (byte) ((pixels[i] >> 16) & 0xff);
		    scanLines[scanPos++] = (byte) ((pixels[i] >>  8) & 0xff);
		    scanLines[scanPos++] = (byte) ((pixels[i]      ) & 0xff);
		    if (encodeAlpha) {
			scanLines[scanPos++] = (byte) ((pixels[i] >> 24) & 0xff );
		    }
		    if ((i % width == width-1) && (filter != FILTER_NONE)) {
			if (filter == FILTER_SUB) {
			    filterSub( scanLines, startPos, width );
			}
			if (filter == FILTER_UP) {
			    filterUp( scanLines, startPos, width );
			}
		    }
		}
		
		/*
		 * Write these lines to the output area
		 */
		compBytes.write( scanLines, 0, scanPos );
		
		
		startRow += nRows;
		rowsLeft -= nRows;
	    }
	    compBytes.close();
	    
	    /*
	     * Write the compressed bytes
	     */
	    compressedLines = outBytes.toByteArray();
	    nCompressed = compressedLines.length;
	    
	    crc.reset();
	    bytePos = writeInt4( nCompressed, bytePos );
	    bytePos = writeString("IDAT", bytePos );
	    crc.update("IDAT".getBytes());
	    bytePos = writeBytes( compressedLines, nCompressed, bytePos );
	    crc.update( compressedLines, 0, nCompressed );
	    
	    crcValue = crc.getValue();
	    bytePos = writeInt4( (int) crcValue, bytePos );
	    scrunch.finish();
	    MyProgressBar.dispose();
	    return true;
	} catch (IOException e) {
	    System.err.println( e.toString());
	    return false;
	}
    }
    
    /**
     * Write a PNG "IEND" chunk into the pngBytes array.
     */
    protected void writeEnd() {
	bytePos = writeInt4( 0, bytePos );
	bytePos = writeString( "IEND", bytePos );
	crc.reset();
	crc.update("IEND".getBytes());
	crcValue = crc.getValue();
	bytePos = writeInt4( (int) crcValue, bytePos );
    }
}

