TODO


#####
# New Properties values. These values should go into 1)www.tm4.org/mev_support/support_file_url_4_8.properties and 
# source org/tigr/microarray/mev/resources/support_file_url.properties
#####
cytoscape_webstart=gaggle.systemsbiology.net/2007-04/cy/blankSlate/cy2.6.0
cytoscape_lib_dir=/2007-04/jars_cy2.6.0/
kegg_server=occams.dfci.harvard.edu/
keg_dir= pub/bio/MeV/kegg/
resourcerer_annotation=ftp://occams.dfci.harvard.edu/pub/bio/MeV/annotation/bioconductor_2_6/
ease_entrez_support_file_location=ftp://occams.dfci.harvard.edu/pub/bio/MeV/annotation/bioconductor_2_6/
ease_support_file_location=ftp://occams.dfci.harvard.edu/pub/bio/MeV/annotation/bioconductor_2_6/
ease_implies_file_location=ftp://occams.dfci.harvard.edu/pub/bio/MeV/annotation/bioconductor_2_6/
gsea_support_file_location=ftp://gseaftp.broad.mit.edu/pub/gsea/gene_sets/
resourcerer_supported_annotations_list=ftp://occams.dfci.harvard.edu/pub/bio/MeV/annotation/bioconductor_2_6/supported_arrays.txt
bn_support_file_location=ftp://occams.dfci.harvard.edu/pub/bio/MeV/annotation/bioconductor_2_6/

source("")
#At Windows command line, for R v2.11, Bioconductor v2.6: 
C:\Progra~1\R\R-2.11.1-x64\bin\R.exe CMD BATCH bioconductor_annotation_parser.R

Or run C:\Progra~1\R\R-2.11.1-x64\bin\R.exe and command 
source("../mev_quickfix/developer_docs/bioconductor_annotation_parser.R")

packagename <- "zebrafish.db"
prefix <- strsplit(packagename, ".", fixed=TRUE)[[1]][1]
 

			
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




fixBNfiles
setwd("C:/Users/eleanora/workspace/annotation/")
orgfolderlist <- list.files(path = ".")
for(orgfolder in orgfolderlist) {
	print(orgfolder)
	tryCatch(
		{	
			setwd(orgfolder)
			BNfilelist <- list.files(path=".", pattern = "_BN.zip")
			for(thisBNfile in BNfilelist) {
				BNFoldername <- strsplit(thisBNfile, ".zip")[[1]]
				system(paste("7za x",  thisBNfile))
				file.copy(
					paste(BNFoldername, "/symArtsPubmed.txt", sep=""), 
					paste(BNFoldername, "/symArtsGeneDb.txt", sep="")
				)
				unlink(thisBNfile)
				system(paste("7za a -r",  paste(BNFoldername,".zip", sep=""), BNFoldername))
				unlink(BNFoldername, recursive=TRUE)
			}
			setwd("..")
		}, error=function(e) {
			print(paste("couldn't cd to folder", orgfolder))
		})
}

