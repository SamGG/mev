package org.tigr.microarray.mev.annotation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

import static org.junit.Assert.*;

import org.junit.*;
import org.tigr.microarray.mev.annotation.InsufficientArgumentsException;
import org.tigr.microarray.mev.annotation.PublicURL;
import org.tigr.microarray.mev.annotation.URLNotFoundException;


public class TestPublicURL extends PublicURL {

	@BeforeClass
	public static void testSetupURLs() {
		//Load urls into PublicURL...
		if(PublicURL.isUrlLoaded())
			fail("URLs are already loaded");
		//TODO Get this datafile from the web instead of hard-coding?
		String testAnnotations = "GB#	http://www.ncbi.nlm.nih.gov/sites/entrez?db=nucleotide&cmd=search&term=FIELD1	GenBank Accession Number\n" + 
								"TC#	http://compbio.dfci.harvard.edu/tgi/cgi-bin/tgi/tc_report.pl?gudb=mouse&tc=FIELD1	TIGR Gene Indices - Mouse\n" + 
								"THC#	http://compbio.dfci.harvard.edu/tgi/cgi-bin/tgi/tc_report.pl?gudb=human&tc=FIELD1	TIGR Gene Indices - Human\n" + 
								"LocusLink	http://www.ncbi.nlm.nih.gov/sites/entrez?db=nuccore&cmd=search&term=FIELD1	NCBI LocusLink\n" + 
								"UniGene	http://www.ncbi.nlm.nih.gov/sites/entrez?db=unigene&cmd=search&term=FIELD1	NCBI UniGene\n" + 
								"GO	http://www.ebi.ac.uk/ego/GTerm?id=FIELD1	Gene Ontology Terms (EBI)\n" + 
								"KEGG	http://www.genome.ad.jp/dbget-bin/show_pathway?FIELD1	KEGG\n" + 
								"GenMAPP	http://www.genmapp.org/MAPPSet-FIELD1	GenMAPP\n" + 
								"Pfam	http://pfam.wustl.edu/cgi-bin/getdesc?acc=FIELD1	Pfam(St. Louis)\n" + 
								"GENBANK_ACC	http://www.ncbi.nlm.nih.gov/sites/entrez?db=nucleotide&cmd=search&term=FIELD1	GenBank Accession Number\n" + 
								"UNIGENE_ID	http://www.ncbi.nlm.nih.gov/sites/entrez?db=unigene&cmd=search&term=FIELD1	NCBI UniGene\n" + 
								"REFSEQ_ACC	http://www.ncbi.nlm.nih.gov/sites/entrez?db=nuccore&cmd=search&term=FIELD1	NCBI Refseq Search\n" + 
								"ENTREZ_ID	http://www.ncbi.nlm.nih.gov/sites/entrez?db=gene&cmd=search&term=FIELD1	NCBI Entrez ID search\n" + 
								"GENE_SYMBOL	http://www.ncbi.nlm.nih.gov/sites/entrez?db=gene&cmd=search&term=FIELD1	NCBI Gene Symbol Search\n" + 
								"PROTEIN_ACC	http://www.ncbi.nlm.nih.gov/sites/entrez?db=protein&cmd=search&term=FIELD1	NCBI Protein Search\n" + 
								"GO_TERMS	http://www.ebi.ac.uk/ego/GSearch?q=FIELD1	Gene Ontology Terms (EBI)\n" + 
								"TGI_TC	http://compbio.dfci.harvard.edu/tgi/cgi-bin/tgi/tc_report.pl?gudb=FIELD&tc=FIELD	TIGR Gene Indices\n" + 
								"TGI_GC	http://compbio.dfci.harvard.edu/tgi/cgi-bin/tgi/tc_report.pl?gudb=FIELD&tc=FIELD	TIGR Gene Indices\n" + 
								"TGI_ORTH	http://compbio.dfci.harvard.edu/tgi/cgi-bin/tgi/tc_report.pl?gudb=FIELD&tc=FIELD	TIGR Gene Indices\n" +
								"GENE_TITLE	http://www.ncbi.nlm.nih.gov/sites/entrez?db=gene&cmd=search&term=FIELD1	NCBI free text search";
		//Single-lookup go terms by ID
		//		GO_TERMS	http://www.ebi.ac.uk/ego/GTerm?id=FIELD1	Gene Ontology Terms (EBI)

		FileOutputStream fout;
		File urlFile;
		try {
			urlFile = File.createTempFile("junit_mev_annotations", ".txt");
			fout = new FileOutputStream(urlFile);
	        new PrintStream(fout).print(testAnnotations);
	        if(! (PublicURL.loadURLs(urlFile) == 0))
				fail("Couldn't load urls from annotation_URLs.txt");
		} catch (IOException ioe) {
			fail("could not create temp outfile.");
		}
	}
	
	@Test 
	/**
	 * 
	 */
	public void testGetURL() {
		String[] targetURLs = new String[]{
				"http://compbio.dfci.harvard.edu/tgi/cgi-bin/tgi/tc_report.pl?gudb=Zebrafish&tc=TC3041860",
				"http://compbio.dfci.harvard.edu/tgi/cgi-bin/tgi/tc_report.pl?gudb=Zebrafish&tc=TC3041860",
				"http://www.ebi.ac.uk/ego/GSearch?q=GO:0004714",
				"http://www.ebi.ac.uk/ego/GSearch?q=GO:0004714,GO:0005566",
				"http://www.ncbi.nlm.nih.gov/sites/entrez?db=unigene&cmd=search&term=Hs.631988",
				"http://www.ncbi.nlm.nih.gov/sites/entrez?db=nucleotide&cmd=search&term=U48705", 
				"http://www.ncbi.nlm.nih.gov/sites/entrez?db=gene&cmd=search&term=DDR1",
				"http://compbio.dfci.harvard.edu/tgi/cgi-bin/tgi/tc_report.pl?gudb=Zebrafish&tc=TC3041860",
				"http://www.ncbi.nlm.nih.gov/sites/entrez?db=gene&cmd=search&term=discoidin domain receptor family",
				"http://www.ncbi.nlm.nih.gov/sites/entrez?db=gene&cmd=search&term=780",
				"http://www.ncbi.nlm.nih.gov/sites/entrez?db=nuccore&cmd=search&term=NM_001954"
		};
		
		String[] fields = new String[]{
				AnnotationConstants.TGI_ORTH, 
				AnnotationConstants.TGI_TC,
				AnnotationConstants.GO_TERMS,
				AnnotationConstants.GO_TERMS,
				AnnotationConstants.UNIGENE_ID,
				AnnotationConstants.GENBANK_ACC,
				AnnotationConstants.GENE_SYMBOL,
				AnnotationConstants.TGI_GC,
				AnnotationConstants.GENE_TITLE, 
				AnnotationConstants.ENTREZ_ID,
				AnnotationConstants.REFSEQ_ACC
		};
		
		String[][] args = new String[][] {
				new String[]{"Zebrafish", "TC3041860"},
				new String[]{"Zebrafish", "TC3041860"},
				new String[]{"GO:0004714"},
				new String[]{"GO:0004714", "GO:0005566"},
				new String[]{"Hs.631988"},
				new String[]{"U48705"},
				new String[]{"DDR1"},
				new String[]{"Zebrafish", "TC3041860"},
				new String[]{"discoidin domain receptor family"},
				new String[]{"780"},
				new String[]{"NM_001954"}
		};

		for(int i=0; i<targetURLs.length; i++) {
			try {
				String url = PublicURL.getURL(fields[i], args[i]);
				if(!url.equals(targetURLs[i])) {
					System.out.println("URL does not match for " + fields[i] + ": " + url);
					fail("URL does not match: " + url);
				}
			} catch (InsufficientArgumentsException iae) {
				System.out.println("InsufficientArgumentsException for fields length " + fields.length + " and args length " + args.length);
				iae.printStackTrace();
				fail(iae.getMessage());
			} catch (URLNotFoundException unfe) {
				System.out.print("URLNotFoundException for " + fields[i] + " and ");
				for(int j=0; j<args[i].length; j++) {
					System.out.print(args[i][j] + " ");
				}
				System.out.println("");
				unfe.printStackTrace();
				fail(unfe.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
//		try  {
////			targetURL = "http://compbio.dfci.harvard.edu/tgi/cgi-bin/tgi/tc_report.pl?gudb=Zebrafish&tc=TC3041860";
//			String testURL = PublicURL.getURL(AnnotationConstants.TGI_TC, new String[]{"Zebrafish", "TC3041860"});
//			if(!testURL.equals(targetURL)){
//				System.out.println("TC URL does not match: \n " + testURL + "\n " + targetURL);
//				fail();
//			}
//		} catch (InsufficientArgumentsException iae) {
//			iae.printStackTrace();
//			fail(iae.getMessage());
//		} catch (URLNotFoundException unfe) {
//			unfe.printStackTrace();
//			fail(unfe.getMessage());
//		}
	}
	@Test
	public void testGetURL_TGI_orth() {
		try {
			String testURL = PublicURL.getURL_TGI_orth("Zebrafish_TC304186");
		if(!testURL.equals("http://compbio.dfci.harvard.edu/tgi/cgi-bin/tgi/tc_report.pl?gudb=Zebrafish&tc=TC304186"))
			fail("URL is " + testURL + " should be http://compbio.dfci.harvard.edu/tgi/cgi-bin/tgi/tc_report.pl?gudb=Zebrafish&tc=TC304186");
		} catch (InsufficientArgumentsException iae) {
			fail();
		} catch (URLNotFoundException unfe) {
			fail();
		}
	}
	@Test
	public void testFillParameters() {
		String urlTemplate = "http://compbio.dfci.harvard.edu/tgi/cgi-bin/tgi/tc_report.pl?gudb=FIELD&tc=FIELD";
		Vector<Integer> paramSites = PublicURL.getParamIndices("FIELD", urlTemplate);
		Integer[] indx = new Integer[paramSites.size()];
		paramSites.toArray(indx);
		String outputURL = PublicURL.fillParameters(urlTemplate, new String[] {"Zebrafish", "TC304186"}, indx);
		if(!outputURL.equals("http://compbio.dfci.harvard.edu/tgi/cgi-bin/tgi/tc_report.pl?gudb=Zebrafish&tc=TC304186"))
			fail();
	}
	

}
