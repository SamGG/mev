#Get list of files from #ftp://occams.dfci.harvard.edu/pub/bio/MeV_Etc/R_MeV_Support_devel/R2.11/win/attract/annotationSupported.txt
setwd("C:/Users/eleanora/workspace/annotation/")
source("../mev_quickfix/developer_docs/annotation_pipeline_library.R")
source('http://bioconductor.org/biocLite.R')

#Need KEGG database to do KEGG pathway mapping files. 
loadLibrary("KEGG.db")
keggmapping <- as.list(KEGGPATHID2NAME)
loadLibrary("GO.db")
gotermmapping <- as.list(GOTERM)
allgoids <- GOID(GOTERM)

#Term(gotermmapping[[which(GOID(GOTERM) == "GO:0001891")]])

packagenamelist <- readLines(con="annotationSupported.txt")

outcon <- file("annotation_script.log", open="w")
successList <- file("supported_arrays.txt", open="w")
bnsuccesslist <- file("supported_arrays_bn.txt", open="w")
for(packagename in packagenamelist) {
#for(packagename in packagenamelist[grep(".db$", packagenamelist, perl=TRUE)]) {
	print(paste("starting in directory", getwd()))
	success <- FALSE
	tryCatch(
		{
			#Maybe I can just change this to biocLite(packagename)
			loadLibrary(packagename)
			prefix <- strsplit(packagename, ".db", fixed=TRUE)[[1]][1]
			success <- createAllAnnotationFiles(
				prefix=prefix, 
				packagename=packagename, 
			)
		#zip EASE dir
		print(paste("Making EASE files for package", packagename))
			if(success != TRUE) {
				writeLines(paste("Failed to produce annotation for", packagename), con=outcon)
				#clean up possibly-broken files
			} else {
				writeLines(paste(packagename, " successful"), con=outcon)
				organism <- get(paste(prefix, "ORGANISM", sep=""))
				writeLines(
					paste("Animal", organism, prefix, sep="\t"), 
					con=successList
				)
			}
		},
		error=function(e){
			print(paste("failed to produce files for package", packagename))
			print(e)
		}
	)
	
	print(paste("Making BN files for package", packagename))
	#Make BN directory and files
	BNFoldername <- paste(prefix, "_BN", sep="")
	BNZipfilename <- paste(BNFoldername, ".zip", sep="")
	
	if(!file.exists(paste(organism, "/", BNZipfilename, sep=""))) {
		print(paste("no file ", getwd(), "/", organism, "/", BNZipfilename, ". Creating.", sep=""))
		tryCatch({
			setwd(organism)
			createBNFiles(prefix=prefix, BNFoldername=BNFoldername)
			capture.output(
				system(paste("7za a -r",  paste("\"", BNZipfilename, "\"", sep=""), paste("\"", BNFoldername, "\"", sep="")))
			)
			unlink(BNFoldername, recursive = TRUE)
			setwd("..")
			writeLines(
					paste("Animal", organism, prefix, sep="\t"), 
					con=bnsuccesslist
				)
		},
		error=function(e) {
			print(e)
			print(paste("failed to produce BN files for package", packagename))
			setwd("..")
		})
	} else {
		print(paste(BNZipfilename, "exists. Skipping."))
		writeLines(
					paste("Animal", organism, prefix, sep="\t"), 
					con=bnsuccesslist
				)
	}
	
	print(paste("finishing in directory", getwd()))
}
close(successList)
close(outcon)
close(bnsuccesslist)


