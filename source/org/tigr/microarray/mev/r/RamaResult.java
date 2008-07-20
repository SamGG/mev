/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Feb 17, 2006
 */
package org.tigr.microarray.mev.r;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.tigr.microarray.mev.TMEV;

/**
 * @author iVu
 */
public class RamaResult {
	private double[] gamma1;
	private double[] gamma2;
	private double[] qLo;
	private double[] qUp;
	
	private double shift;
	
	private int minIter;
	private int B;
	
	private boolean allOut;
	private boolean hasGenes;
	
	private String[] genes;
	
	private String startTime;
	private String endTime;
	
	
	public RamaResult( double[] g1, double[] g2, double[] qLoP, double[] qUpP,
			double shiftP, String startTimeP, String endTimeP ) {
		this.gamma1 = g1;
		this.gamma2 = g2;
		this.qLo = qLoP;
		this.qUp = qUpP;
		this.allOut = true;
		this.shift = shiftP;
		this.hasGenes = false;
		this.startTime = startTimeP;
		this.endTime = endTimeP;
	}
	public RamaResult( double[] g1, double[] g2, double shiftP, 
			String startTimeP, String endTimeP ) {
		this.gamma1 = g1;
		this.gamma2 = g2;
		this.allOut = false;
		this.shift = shiftP;
		this.hasGenes = false;
		this.startTime = startTimeP;
		this.endTime = endTimeP;
	}


    /**
     * 
     * @param gamma1
     * @param gamma2
     * @param genes
     */
    public void saveRamaResult( JFrame frame ) {
        String currentPath = TMEV.getDataPath();
        RamaTextFileFilter textFilter = new RamaTextFileFilter();
        JFileChooser chooser = new JFileChooser( currentPath );
        chooser.addChoosableFileFilter( textFilter );
        if( chooser.showSaveDialog( frame ) == JFileChooser.APPROVE_OPTION ) {
            File saveFile;

            if( chooser.getFileFilter() == textFilter ) {
                //make sure to add .txt
                String path = chooser.getSelectedFile().getPath();
                //System.out.println( "path" + path );
                if( path.toLowerCase().endsWith("txt") ) {
                    //great, already ok
                    saveFile = new File(path);
                } else {
                    //add it
                    String subPath;
                    int period = path.lastIndexOf(".txt");
                    //System.out.println( "period:" + period );
                    if( period != -1 ) {
                        //System.out.println("period found");
                        subPath = path.substring(0, period);
                    } else {
                        //System.out.println("period not found");
                        subPath = path;
                    }
                    String newPath = subPath + ".txt";
                    //System.out.println( "newPath:" + newPath );
                    saveFile = new File(newPath);
                }
            } else {
                saveFile = chooser.getSelectedFile();
            }
            StringBuffer sb = new StringBuffer();
            if( this.hasGenes ) {
            	sb.append( "GeneName" );
            }
            if( allOut ) {
            	sb.append( Rama.TAB );
            	sb.append( "qLow" );
            	sb.append( Rama.TAB );
            	sb.append( "qUp" );
            }
            sb.append( Rama.TAB );
            sb.append( "IntensityA" );
            sb.append( Rama.TAB );
            sb.append( "IntensityB" );
            sb.append( Rama.END_LINE );
            
            for( int i = 0; i < this.gamma1.length; i++ ) {
            	if( allOut ) {
            		sb.append( this.qLo[ i ] );
            		sb.append( Rama.TAB );
            		sb.append( this.qUp[ i ] );
            		sb.append( Rama.TAB );
            	}
            	if( this.hasGenes ) {
            		sb.append( this.genes[ i ] );
            	}
                sb.append( Rama.TAB );
            	sb.append( this.gamma1[ i ] );
                sb.append( Rama.TAB );
            	sb.append( this.gamma2[ i ] );
				sb.append( Rama.END_LINE );
            }
            
            this.writeFile(saveFile, sb.toString());
        } else {
            //System.out.println("User cancelled Gene List Save");
        }
    }//onSaveGeneList()


    /**
     * Write the String s to File f
     * 
     * @param f
     * @param s
     */
    private void writeFile(File f, String s) {
        try {
            FileWriter fw = new FileWriter(f);
            fw.write(s);
            fw.flush();
            fw.close();
        } catch( IOException e ) {
            e.printStackTrace();
        }
    }//writeFile()
	
	
    public void setGenes( String[] genesP ) {
    	this.genes = genesP;
    	if( genesP != null && genesP.length == this.gamma1.length ) {
    		this.hasGenes = true;
    	} else {
    		this.hasGenes = false;
    	}
    }
    public void setB( int BP ) {
    	this.B = BP;
    }
    public void setMinIter( int minIterP ) {
    	this.minIter = minIterP;
    }
    
    
    public int getB() { 
    	return this.B;
    }
    public int getMinIter() {
    	return this.minIter;
    }
	public double[] getGamma1() {
		return this.gamma1;
	}
	public double[] getGamma2() { 
		return this.gamma2;
	}
	public double[] getQLo() {
		return this.qLo;
	}
	public double[] getQUp() {
		return this.qUp;
	}
	public String[] getGenes() {
		return this.genes;
	}
	public double getShift() {
		return this.shift;
	}
	public String getStartTime() {
		return this.startTime;
	}
	public String getEndTime() {
		return this.endTime;
	}
	public String[] getResultHeader() {
		String[] toReturn;
		
		int iCol;
		if( this.allOut && this.hasGenes ) {
			toReturn = new String[ 5 ];
			toReturn[ 0 ] = "GeneName";
			toReturn[ 1 ] = "qLo";
			toReturn[ 2 ] = "qUp";
			toReturn[ 3 ] = "Gamma1";
			toReturn[ 4 ] = "Gamma2";
		} else if( this.allOut && !this.hasGenes ) {
			toReturn = new String[ 4 ];
			toReturn[ 0 ] = "qLo";
			toReturn[ 1 ] = "qUp";
			toReturn[ 2 ] = "Gamma1";
			toReturn[ 3 ] = "Gamma2";
		} else if( !this.allOut && this.hasGenes ) {
			toReturn = new String[ 3 ];
			toReturn[ 0 ] = "GeneName";
			toReturn[ 1 ] = "Gamma1";
			toReturn[ 2 ] = "Gamma2";
		} else {
			toReturn = new String[ 2 ];
			toReturn[ 0 ] = "Gamma1";
			toReturn[ 1 ] = "Gamma2";
		}
		
		return toReturn;
	}//getResultHeader();
	public Object[][] getResultTable() {
		int iCol;
		if( this.allOut && this.hasGenes ) {
			iCol = 5;
		} else if( this.allOut && !this.hasGenes ) {
			iCol = 4;
		} else if( !this.allOut && this.hasGenes ) {
			iCol = 3;
		} else {
			iCol = 2;
		}
		
		Object[][] toReturn = new Object[ this.gamma1.length ][ iCol ];
		for( int i = 0; i < this.gamma1.length; i ++ ) {
			int jIndex = 0;
			
			if( this.hasGenes ) {
				toReturn[ i ][ jIndex ] = this.genes[ i ];
				jIndex ++;
			}
			if( this.allOut ) {
				toReturn[ i ][ jIndex ] = Double.toString( this.qLo[ i ] );
				jIndex ++;
				toReturn[ i ][ jIndex ] = Double.toString( this.qUp[ i ] );
				jIndex ++;
			}
			toReturn[ i ][ jIndex ] = Double.toString( this.gamma1[ i ] );
			jIndex ++;
			toReturn[ i ][ jIndex ] = Double.toString( this.gamma2[ i ] );
			jIndex ++;
		}
		
		return toReturn;
	}//getResultTable();
}
