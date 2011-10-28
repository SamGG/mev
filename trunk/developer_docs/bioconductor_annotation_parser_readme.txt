TODO
Version numbers - choose folder locations for versions. Put a folder in pipeline with R version number.

Test with MeV dev.
update mev config file that specifies location of EASE, BN, annotation, imples, etc files. 
update file that tells mev where to look for above file.

#####
# New Properties values. These values should go into 1)www.tm4.org/mev_support/support_file_url_4_8.properties and 
# source org/tigr/microarray/mev/resources/support_file_url.properties
#####
cytoscape_webstart=gaggle.systemsbiology.net/2007-04/cy/blankSlate/cy2.6.0
cytoscape_lib_dir=/2007-04/jars_cy2.6.0/
kegg_server=occams.dfci.harvard.edu/
keg_dir= pub/bio/MeV/kegg/
resourcerer_annotation=ftp://occams.dfci.harvard.edu/pub/bio/MeV/pipelinetest/
ease_entrez_support_file_location=ftp://occams.dfci.harvard.edu/pub/bio/MeV/pipelinetest/
ease_support_file_location=ftp://occams.dfci.harvard.edu/pub/bio/MeV/pipelinetest/
ease_implies_file_location=ftp://occams.dfci.harvard.edu/pub/bio/MeV/pipelinetest/
gsea_support_file_location=ftp://gseaftp.broad.mit.edu/pub/gsea/gene_sets/
resourcerer_supported_annotations_list=ftp://occams.dfci.harvard.edu/pub/bio/MeV/pipelinetest/supported_arrays.txt
bn_support_file_location=ftp://occams.dfci.harvard.edu/pub/bio/MeV/pipelinetest/

source("")
#At Windows command line, for R v2.11, Bioconductor v2.6: 
C:\Progra~1\R\R-2.11.1-x64\bin\R.exe CMD BATCH bioconductor_annotation_parser.R

Or run C:\Progra~1\R\R-2.11.1-x64\bin\R.exe and command 
source("../mev_bioconductor_annotation/bioconductor_annotation_parser.R")

packagename <- "zebrafish.db"
prefix <- strsplit(packagename, ".", fixed=TRUE)[[1]][1]

biocLite downloads and installs fine the first time. Then need to call "library " - no errors
So, can I call biocLite again, or does that screw it up? 

			
packagenamelist <- c(
	"hgu133a.db",
	"org.At.tair.db"
)
packagenamelist <- c(
	"human1mduov3bCrlmm",
	"chicken.db",
	"bovine.db",
	"hgu133plus2.db", 
	"hgu133a.db",
	"h10kcod.db",
	"org.At.tair.db", 
	"mgug4122a.db", 
	"hugene10stprobeset.db",
	"canine.db",
	"canine.db0"
)


#Examples of Bioconductor annotation packages that cannot be supported with the current
#pipeline: 
biocLite("BSgenome.Hsapiens.UCSC.hg19")

	packagename <- "hgu133a.db"
	loadLibrary(packagename)
	prefix=strsplit(packagename, ".", fixed=TRUE)[[1]][1]



createBNFiles("hgu133plus2.db")