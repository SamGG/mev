#Get list of files from #ftp://occams.dfci.harvard.edu/pub/bio/MeV_Etc/R_MeV_Support_devel/R2.11/win/attract/annotationSupported.txt
setwd("C:/Users/eleanora/workspace/annotation/")
source("../mev_bioconductor_annotation/annotation_pipeline_library.R")
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
for(packagename in packagenamelist[grep(".db$", packagenamelist, perl=TRUE)]) {
	success <- FALSE
	tryCatch(
		{
			#Maybe I can just change this to biocLite(packagename)
			loadLibrary(packagename)
			prefix <- strsplit(packagename, ".", fixed=TRUE)[[1]][1]
			success <- createAllAnnotationFiles(
				prefix=prefix, 
				packagename=packagename, 
			)
		#zip EASE dir
		print("doing ease stuff")
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
}
close(successList)
close(outcon)
