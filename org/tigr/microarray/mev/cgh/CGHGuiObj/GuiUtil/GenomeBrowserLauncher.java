/*
 * GenomeBrowserLauncher.java
 *
 * Created on July 10, 2003, 2:20 AM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.GuiUtil;

import java.io.IOException;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cgh.CGHDataObj.ICGHDataRegion;

import edu.stanford.ejalbert.BrowserLauncher;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class GenomeBrowserLauncher {

	private static final int ENSEMBLE = 1;
	private static final int UCSC = 2;
	private static final int NCBI = 3;

    /** Creates a new instance of GenomeBrowserLauncher */
    public GenomeBrowserLauncher() {
    }

    /** Lanches a browser to dislpay Ensembl
     * @param dataRegion the data region to display
     */
    public static void launchEnsembl(ICGHDataRegion dataRegion, int species){
        String chromosome = getChromosomeName(dataRegion.getChromosomeIndex() + 1, species);
        String sp = getSpeciesName(ENSEMBLE, species);
        int start = dataRegion.getStart();
        int stop = dataRegion.getStop();
        String url = "http://www.ensembl.org/"+sp+"/contigview?chr="+chromosome+"&vc_start="+start+"&vc_end"
        +stop+"&x=15&y=8";

        try{
            BrowserLauncher.openURL(url);
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    /** Lanches a browser to dislpay UCSC's Golden Path
     * @param dataRegion
     */
    public static void launchGoldenPath(ICGHDataRegion dataRegion, int species){
        String chromosome = getChromosomeName(dataRegion.getChromosomeIndex() + 1, species);
        String sp = getSpeciesName(UCSC, species);
        int start = dataRegion.getStart();
        int stop = dataRegion.getStop();
        String url = "http://genome.ucsc.edu/cgi-bin/hgTracks?db="+sp+"&position=chr"+chromosome+":"+start+"-"+stop;

        try{
            BrowserLauncher.openURL(url);
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    /** Lanches a browser to dislpay NCBI's map viewer
     * @param dataRegion
     */
    public static void launchNCBIMapViewer(ICGHDataRegion dataRegion, int species){
        String chromosome = getChromosomeName(dataRegion.getChromosomeIndex() + 1, species);
        String sp = getSpeciesName(NCBI, species);
        int start = dataRegion.getStart();
        int stop = dataRegion.getStop();
        String url = "http://www.ncbi.nlm.nih.gov/mapview/maps.cgi?org="+sp+"&chr="+chromosome+"&BEG="+start+"&END="+stop;
        try{
            BrowserLauncher.openURL(url);
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    /** Lanches a browser to dislpay NCBI's map viewer
     * @param dataRegion
     */
    public static void launchNCBIMapViewer(ICGHDataRegion[] dataRegions, int species){
        String chromosome = getChromosomeName(dataRegions[0].getChromosomeIndex() + 1, species);
        String sp = getSpeciesName(NCBI, species);
        int start = dataRegions[0].getStart();
        int stop = dataRegions[dataRegions.length-1].getStop();
        String url = "http://www.ncbi.nlm.nih.gov/mapview/maps.cgi?org="+sp+"&chr="+chromosome+"&BEG="+start+"&END="+stop;
        try{
            BrowserLauncher.openURL(url);
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    private static String getChromosomeName(int chr, int species){
        String chromosome = chr + "";
        if("20".equals(chromosome) && (species == TMEV.CGH_SPECIES_MM)){
        	chromosome = "X";
        }else if("21".equals(chromosome) && (species == TMEV.CGH_SPECIES_MM)){
            chromosome = "Y";
        }else if("23".equals(chromosome) && (species == TMEV.CGH_SPECIES_HS)){
            chromosome = "X";
        }else if("24".equals(chromosome) && (species == TMEV.CGH_SPECIES_HS)){
            chromosome = "Y";
        }
        return chromosome;
    }

    private static String getSpeciesName(int browserSource, int sp){
    	String species = "";
    	switch (sp) {
    		case TMEV.CGH_SPECIES_HS:
    			switch(browserSource){
	    			case ENSEMBLE: species = "Homo_sapiens"; break;
	    			case UCSC: species = "hg17"; break;
	    			case NCBI: species = "human"; break;
    			}
    			break;
    		case TMEV.CGH_SPECIES_MM:
    			switch(browserSource){
	    			case ENSEMBLE: species = "Mus_musculus"; break;
	    			case UCSC: species = "mm6"; break;
	    			case NCBI: species = "mouse"; break;
				}
				break;
    	}
    	return species;
    }
}
