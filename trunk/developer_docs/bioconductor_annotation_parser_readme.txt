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
	"hgu133a.db"
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




testBNFiles <- function(packagename) {
	biocLite(packagename)
	do.call("library", as.list(packagename))
	prefix=strsplit(packagename, ".", fixed=TRUE)[[1]][1]
	
	accessionNumbers <- as.list(get(paste(prefix, "ACCNUM", sep="")))
	chrloc <- as.list(get(paste(prefix, "CHRLOC", sep="")))
	chrlocend <- as.list(get(paste(prefix, "CHRLOCEND", sep="")))
	chr <- as.list(get(paste(prefix, "CHR", sep="")))
	refseq <- as.list(get(paste(prefix, "REFSEQ", sep="")))
	entrez <- as.list(get(paste(prefix, "ENTREZID", sep="")))
	unigene <- as.list(get(paste(prefix, "UNIGENE", sep="")))
	symbol <- as.list(get(paste(prefix, "SYMBOL", sep="")))
	genename <- as.list(get(paste(prefix, "GENENAME", sep=""))) 
	goTable <- toTable(get(paste(prefix, "GO2PROBE", sep="")))
	arrayKeggMapping <- toTable(get(paste(prefix, "PATH", sep="")))

	pmids <- as.list(get(paste(prefix, "PMID", sep=""))) 

	
	BNFoldername <- paste(prefix, "_BN", sep="")
	dir.create(BNFoldername)
	writeBNAccessionFile(
		filename=paste(BNFoldername, "/affyID_accession.txt", sep=""), 
		names(accessionNumbers), 
		accessionNumbers
	)
	writeBNPPIFile(
		filename=paste(BNFoldername, "/all_ppi.txt", sep=""), 
		names(accessionNumbers), 
		accessionNumbers
	)
	
	#TODO create gonameforthisid from GO.db package
	gomapping <- paste(goTable$go_id, "(", gonameforthisid, ")", sep="")
	
	writeBNGOFile(
		filename=paste(BNFoldername, "/gbGO.txt", sep=""), 
		accessionNumbers, 
		gomapping
	)
	
	writeBNResFile(
		filename = paste(BNFoldername, "/res.txt", sep=""), 
		probeids = names(accessionNumbers),
		cloneids = rep("", times=length(accessionNumbers))
		genbank = accessionNumbers,
		unigene = unigene,
		entrez = entrez
		symbolAndName = symbol, #do something with symbols - add name?
		synonyms = something,#find synonyms?
		humantc = NULL,
		humangc = NULL,
		refseq = refseq,
		tcpubmed = NULL, 
		gomaps = gomapping, 
		location = formattedLocation,
		geneticMarker = NULL,
		
	)
	
	writeBNSymartsGeneDbFile(
		filename=paste(BNFoldername, "/symArtsGeneDb.txt", sep=""), 
		symbol, 
		someid?
	)

		#TODO Emailed Raktim and ask about inconsistency in PMID lists. 
		#Waiting on reply 9/22/11.
	pmidlists <- lapply(names(symbol), function(x) {pmids[[which(names(pmids) == x)]]})
	names(pmidlists) <- names(symbol)
	test <- lapply(
				unique(symbol), 
				function(x) {
					c(
						pmidlists[
							which(
								names(pmidlists) %in% 
								names(symbol[which(symbol == x)])
							)
						], 
						recursive=TRUE
					)
				}
			)
	names(test) <- unique(symbol)
	test2 <- lapply(symbol, function(x) {pmids[which(symbol == x)]})
	which(symbol == "AAAS")
	length(symbol)
	length(unique(symbol))
	writeBNSymartsPubmedFile(
		filename=paste(BNFoldername, "/symArtsPubmed.txt", sep=""), 
		names(test), 
		pubmedids=lapply(test, paste, collapse=",") 
	)
}


testBNFiles("hgu133plus2.db")