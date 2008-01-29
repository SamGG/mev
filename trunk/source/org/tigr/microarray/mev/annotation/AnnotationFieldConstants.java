/**
 * 
 */
package org.tigr.microarray.mev.annotation;

/**
 * @author Raktim
 *
 *@Revision
 *Sarita commented out some of the Annotation Constants
 *Reason: We will be downloading annotation from the TGI-FTP site.
 *The files hosted at the ftp site do not match our format.
 *Some of the fields like TX_START, TX_END requires a standard delimiter
 *to be in the data, in order for AnnotationFileReader to be able
 *to parse out the information.  
 *
 *After SOAP service is implemented at the Resourcerer end and we start
 *getting back annotation files in the format we want,we would uncomment these fields and their
 *getter/setter methods.
 *
 */

public class AnnotationFieldConstants {
	public static final String CLONE_ID = "CLONE_ID";
	public static final String CHR = "CHR";
	public static final String TX_START = "TX_START";
	public static final String TX_END = "TX_END";
	public static final String REFSEQ_ACC = "REFSEQ_ACC";
	public static final String GENBANK_ACC = "GENBANK_ACC";
	public static final String ENTREZ_ID = "ENTREZ_ID";
	public static final String CDS_START = "CDS_START";
	public static final String CDS_END = "CDS_END";
	public static final String PROTEIN_ACC = "PROTEIN_ACC";
	public static final String GENE_SYMBOL = "GENE_SYMBOL";
	public static final String GENE_TITLE = "GENE_TITLE";
	public static final String CYTOBAND = "CYTOBAND";
	public static final String STRAND = "STRAND";
	public static final String SEQUENCE = "SEQUENCE";
	public static final String UNIGENE_ID = "UNIGENE_ID";
	public static final String GO_TERMS = "GO_TERMS";
	public static final String BIO_CARTA = "BIO_CARTA";
	public static final String KEGG = "KEGG";
	public static final String DESC = "DESC";
}
