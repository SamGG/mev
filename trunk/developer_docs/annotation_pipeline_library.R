#check if a bioconductor package is already installed. If it's not,
#install it. 
loadLibrary <- function(libname) {
	if(!do.call("require", as.list(c(package=libname, lib.loc="C:/Users/eleanora/Documents/R/win64-library/2.11")))) {
		print(paste("calling bioclite on", libname))
		biocLite(libname)
		print(paste("calling library on", libname))
		do.call("library", as.list(c(package=libname, lib.loc="C:/Users/eleanora/Documents/R/win64-library/2.11")))
	}
}

#Create all of the annotation files (regular annotation, EASE directory) for a supplied array or genome annotation package from Bioconductor.
createAllAnnotationFiles <- function(prefix, packagename, arrayname=NULL) {
	tryCatch({
	
		
		
		#get data from package
		organism <- {
			tryCatch(
				{get(paste(prefix, "ORGANISM", sep=""))
				},
				error=function(e){
					print(paste(e,"\nCan't get organism for", packagename))
					NULL
				}
			)
		}
		if(is.null(organism)) {
			return(FALSE)
		}
		print(organism)
		if(is.null(arrayname)) {
			arrayname <- prefix
		}
		
		#If the annotation and EASE file already exist, don't recreate them
		EASEdirname <- paste(organism, "_", arrayname, sep="")
		easezipfilename <- paste(arrayname, "_EASE.zip", sep="")
		archivename <- paste(arrayname, ".zip", sep="")  
		
		if(
			file.exists(paste(organism, "/", archivename, sep="")) && 
			file.exists(paste(organism, "/", easezipfilename, sep=""))) {
			print(paste(archivename, "and", easezipfilename, "already exist. Not re-creating."))
			return(TRUE)
		} else {
			print(paste(archivename, "or", easezipfilename, "do not exist. Creating."))
		}
		
		accessionNumbers <- as.list(get(paste(prefix, "ACCNUM", sep="")))
		chrloc <- as.list(get(paste(prefix, "CHRLOC", sep="")))
		chrlocend <- as.list(get(paste(prefix, "CHRLOCEND", sep="")))
		chr <- as.list(get(paste(prefix, "CHR", sep="")))
		refseq <- tryCatch(
			{
				as.list(get(paste(prefix, "REFSEQ", sep="")))
			},
			error=function(e) {
				rep(NA, times=length(accessionNumbers))
			}
		)
		entrez <- tryCatch(
			{
				as.list(get(paste(prefix, "ENTREZID", sep="")))
			},
			error=function(e) {
				rep(NA, times=length(accessionNumbers))
			}
		)
		unigene <- tryCatch(
			{
				as.list(get(paste(prefix, "UNIGENE", sep="")))
			},
			error=function(e) {
				rep(NA, times=length(accessionNumbers))
			}
		)
		symbol <- as.list(get(paste(prefix, "SYMBOL", sep="")))
		genename <- as.list(get(paste(prefix, "GENENAME", sep=""))) 
		goTable <- toTable(get(paste(prefix, "GO2PROBE", sep="")))
		arrayKeggMapping <- toTable(get(paste(prefix, "PATH", sep="")))
		annfilename <- paste(arrayname, ".txt", sep="")  
		
		#add check for missing values for non-human arrays
		
		if(!file.exists(organism)) {
			
			dir.create(organism)
		} else {
			print(paste("directory", organism, "exists already"))
		}
		parseBioconductorToFile(
			species=organism, 
			arrayname=arrayname,
			annpackage=packagename,  #get from ann package
			filename=paste(organism, "/", annfilename, sep=""),
			probelist=names(accessionNumbers),
			chrlocstart=chrloc,
			chrlocend=chrlocend, 
			chr=chr, 
			genbankids=accessionNumbers, 
			refseqids=refseq, 
			entrezids=entrez, 
			unigeneids=unigene, 
			genesymbols=symbol, 
			genename=genename, 
			gotable=goTable
		)
		#Zip annfilename - get unix syntax
		setwd(organism)
		capture.output(
			system(paste("7za a", archivename, annfilename))
		)		
		#delete unzipped file
		unlink(annfilename)
		setwd("..")
	   
		#Make BN directory and files
#		dir.create(paste(prefix, "_BN", sep=""))
		#writeBNfiles(location=paste(organism, "/", prefix, "_BN/", sep=""))
#		writeBNAccessionFile(filename=paste(BNFoldername, "/affyID_accession.txt", sep=""), names(accessionNumbers), accessionNumbers)

		
		#EASE FILES
		#Make EASE file structure
		#zip file into package
		setwd(organism)
		dir.create(EASEdirname)
		dir.create(paste(EASEdirname, "/Data", sep=""))
		classfiledir <- paste(EASEdirname, "/Data/Class/", sep="")
		dir.create(classfiledir)

		writeClassFile(
			filename=paste(classfiledir, "GO Biological Process.txt", sep=""), 
			gotable=goTable[which(goTable$Ontology == "BP"),],
			entrez
		)
		writeClassFile(
			filename=paste(classfiledir, "GO Molecular Function.txt", sep=""), 
			gotable=goTable[which(goTable$Ontology == "MF"),],
			entrez
		)
		writeClassFile(
			filename=paste(classfiledir, "GO Cellular Component.txt", sep=""), 
			gotable=goTable[which(goTable$Ontology == "CC"),],
			entrez
		)

		#write file "KEGG pathways.txt" with entrez/KEGG path names
		writeKEGGFile(
			paste(classfiledir, "KEGG pathways.txt", sep=""), 
			entrez, 
			arrayKeggMapping, 
			keggmapping
		)

		dir.create(paste(EASEdirname, "/Data/Convert", sep=""))

		probesetaccfilename <- paste(EASEdirname, "/Data/Convert/Probesets_", arrayname, ".txt", sep="")
		writeConvertFile(probesetaccfilename, entrez, names(accessionNumbers))

		gbaccfilename <- paste(EASEdirname, "/Data/Convert/GB_accessions_", arrayname, ".txt", sep="")
		writeConvertFile(gbaccfilename, entrez, accessionNumbers)


		dir.create(paste(EASEdirname, "/Lists", sep=""))

		dir.create(paste(EASEdirname, "/Lists/GB_accessions_", arrayname, sep=""))
		gbaccdir <- paste(EASEdirname, "/Lists/GB_accessions_", arrayname, "/Populations/", sep="")
		dir.create(gbaccdir)
		writeEasePopulationFile(
			filename=paste(gbaccdir, "population.txt", sep=""), 
			unlist(accessionNumbers)
		)

		dir.create(paste(EASEdirname, "/Lists/Probesets_", arrayname, sep=""))
		probesetdir <- paste(EASEdirname, "/Lists/Probesets_", arrayname, "/Populations/", sep="")
		dir.create(probesetdir)
		writeEasePopulationFile(
			filename=paste(probesetdir, "population.txt", sep=""), 
			names(accessionNumbers)
		)
		
		setwd(EASEdirname)
		#Zip annfilename - get unix syntax
		print(paste("7za a -r",  paste("\"..\\", easezipfilename, "\"", sep=""), paste("\"",  ".\"", sep="")))
		capture.output(
			system(paste("7za a -r",  paste("\"..\\", easezipfilename, "\"", sep=""), paste("\"",  ".\"", sep="")))
		)
		#delete source files
		setwd("..")
		unlink(EASEdirname, recursive = TRUE)
		setwd("..")
		
	},
	error = function(e) {
		#After we figure out how to structure the directories on the ftp server 
		#set up the script so that it deletes any possibly-bad files that are made when the 
		#script fails. 
		#unlink("oranism/arrayname", recursive = TRUE)
		print(e)
		return(FALSE)
	}
	)
	return(TRUE)
}

#Write the standard annotation file loadable by MeV. 
parseBioconductorToFile <- function(
							species, 
							arrayname, 
							annpackage, 
							filename, probelist, chrlocstart, chrlocend, chr, 
							genbankids, refseqids, entrezids, unigeneids, genesymbols, genename, gotable
							) {
#	test for length 0
#	if(length(unigeneids) == 0) {
#		unigeneids <- rep("NA", length=length(probelist))
#	}
	
	#using go2probe because it only gives ids directly annotated to 
	#this term, rather than to the subtree
	goterms <- lapply(probelist, function(x) {
						gotable$go_id[which(gotable$probe_id == x)]
					}
				)
	names(goterms) <- probelist 
	goterms[which(sapply(goterms, function(x) {length(x) <= 0}))] <- NA

	#check that these are the same length
	strands <- sapply(chrlocstart,
					function(x) {
						if(length(x) <= 1 && is.na(x)) {
							NA
						} else {
							if(x[[1]]>=0) {
								"+"
							} else {
								"-"
							}
						}
					}	
				)
	cols <- cbind(
		chr=chr, 
		start=lapply(chrlocstart, function(x){x[[1]]}), 
		end=lapply(chrlocend, function(x){x[[1]]}), 
		strand=strands
	)
	chrlocs <- apply(cols, 1, 
		function(x) {
			if(is.na(x$start)) {
				NA
			} else {
				if(as.character(x$strand) == "+") {
					paste("chr", x$chr, ":", x$start, "-", x$end, "(", x$strand, ")", sep="")
				} else {
					paste("chr", x$chr, ":", (-1*x$start), "-", (-1*x$end), "(", x$strand, ")", sep="")
				}
			}
		}
	)
	
				if(!(
				length(genbankids) == length(probelist) &&
				length(refseqids) == length(probelist)  &&
				length(entrezids) == length(probelist)  &&
				length(unigeneids) == length(probelist)  &&
				length(genesymbols) == length(probelist)  &&
				length(genename) == length(probelist)  &&
				length(chrlocs) == length(probelist)  &&
				length(goterms) == length(probelist) 
			)) {
				stop(paste("datasets not of equal length", 
				"probelist:", length(probelist),
				"refseqids:", length(refseqids),
				"entrezids:", length(entrezids),
				"unigeneids:", length(unigeneids),
				"genesymbols:", length(genesymbols),
				"genename:", length(genename),
				"chrlocs:", length(chrlocs),
				"goterms:", length(goterms)
				))
			}
			
	#Write the file.
	tryCatch (
		{
			outcon <- file(filename, open="w")
			#write annotation header
			writeLines(paste("# Array: ", arrayname, sep=""), con=outcon, sep="\n")
			writeLines(paste("# Organism: ", species, sep=""), con=outcon)
			writeLines("# Bioconductor version: 13.0", con=outcon)
			writeLines(paste("# Package version: ", packageDescription(annpackage)$Version, sep=""), con=outcon, sep="\n")
			
			writeLines("# Fields: ", con=outcon, sep="\n")
			writeLines("#    1. CLONE_ID", con=outcon, sep="\n")
			writeLines("#    2. GENBANK_ACC", con=outcon, sep="\n")
			writeLines("#    3. REFSEQ_ACC", con=outcon, sep="\n")
			writeLines("#    4. ENTREZ_ID", con=outcon, sep="\n")
			writeLines("#    5. UNIGENE_ID", con=outcon, sep="\n")
			writeLines("#    6. GENE_SYMBOL", con=outcon, sep="\n")
			writeLines("#    7. GENE_TITLE", con=outcon, sep="\n")
			writeLines("#    8. CHR:TX_START-TX_END(STRAND)", con=outcon, sep="\n")
			writeLines("#    9. GO_TERMS", con=outcon, sep="\n")
			
			for(i in 1:length(probelist)) {
				annlist <- c(
					probelist[[i]][1], 
					genbankids[[i]][1], 
					paste(refseqids[[i]], collapse="///"), 
					entrezids[[i]][1], 
					unigeneids[[i]][1], 
					genesymbols[[i]][1], 
					genename[[i]][1], 
					chrlocs[i], 
					paste(goterms[[i]], collapse="///"), 
					recursive=TRUE
				)
			   writeLines(annlist, con=outcon, sep="\t")
			   writeLines("\n", con=outcon, sep="")
			}
		}, 
		error = function(e) {
			print(e) 
			return(FALSE)
		},
		finally = {
			close(outcon)
		}
	)
	print(paste("Wrote", arrayname, "annotation to", filename))

	return(TRUE)
}



writeEasePopulationFile <- function(filename, probelist) {
	tryCatch (
		{		
		outcon <- file(filename, open="w")
		writeLines(probelist, con=outcon, sep="\n")
	}, 
		error = function(e) {
			print(e) 
			return(FALSE)
		},
		finally = {
			close(outcon)
		}
	)
}

#create EASE class file: maps entrez ids to go ids.
writeClassFile <- function(filename, gotable, entrezids) {
	temp <- sapply(gotable$probe_id, function(x) {
				entrezids[which(names(entrezids) == x)]
			}
		)
	goterms <- sapply(gotable$go_id, function(x) {
				Term(gotermmapping[[which(allgoids == x)]])
			}
		)

	tryCatch (
		{
			
			outcon <- file(filename, open="w")
			writeLines(paste(temp, goterms, sep="\t"), con=outcon, sep="\n")
			
		}, 
		error = function(e) {
		#	print(e) 
			return(FALSE)
		},
		finally = {
			close(outcon)
		}
	)
	return(TRUE)
}

writeBNAccessionFile <- function(filename, accessions, genbanks) {
	tryCatch (
		{
			outcon <- file("affyID_accession.txt", open="w")		
			writeLines(prefix, sep="\n")		
			writeLines("Probe ID\tGenbank Acc", sep="\n")
			writeLines(paste(accessions, genbanks, sep="\t"), con=outcon, sep="\n")
		}, 
		error = function(e) {
			print(e) 
			return(FALSE)
		},
		finally = {
			close(outcon)
		}
	)
	return(TRUE)
}

#convert files.
#convert
#EASEEntrezSupportDataFile\Human_affy_HG-U133A_2009-05-26\Data\Convert\Probesets_affy_HG-U133A.txt
#entrez in column 1, probeid in column 2
#EASEEntrezSupportDataFile\Human_affy_HG-U133A_2009-05-26\Data\Convert\GB_accessions_affy_HG-U133A.txt
#entrez in column 1, genbank? in column 2

writeConvertFile <- function(filename, entrezids, probeids) {
	tryCatch (
		{
			outcon <- file(filename, open="w")
			#write annotation header
			writeLines(paste(entrezids, probeids, sep="\t"), con=outcon, sep="\n")
		}, 
		error = function(e) {
			print(e) 
			return(FALSE)
		},
		finally = {
			close(outcon)
		}
	)
	return(TRUE)
}

#Write out a file mapping entrez ids to KEGG pathway names.
#filename: name of the file in current working directory to be written out
#entrezids: mapping of probe ids to entrez ids (probe ids are names of list items). Take from an annotation database with eg hgu133aENTREZID.
#arrayKeggMapping: mapping of probe ids to KEGG path ids. From eg toTable(hgu133aPATH)
#keggmapping: mapping of KEGG path ids to path names. From KEGGPATHID2NAME
writeKEGGFile <- function(filename, entrezids, arrayKeggMapping, keggmapping) {

	#Get affy proobe ids mapped to entrez ids. 
	entrezlist <- sapply(arrayKeggMapping$probe_id, function(x) {
					entrezids[which(names(entrezids) == x)]
				}
	)

	#convert KEGG ids to KEGG path names
	temp <- sapply(arrayKeggMapping$path_id, function(x) {
				keggmapping[which(names(keggmapping) == x)]
			}
		)
	probeKeggnamemapping <- data.frame(cbind(entrez=entrezlist, paths=temp), row.names=NULL)

	tryCatch (
		{
			outcon <- file(filename, open="w")
			writeLines(paste(probeKeggnamemapping$entrez, probeKeggnamemapping$paths, sep="\t"), con=outcon, sep="\n")
			
		}, 
		error = function(e) {
		#	print(e) 
			return(FALSE)
		},
		finally = {
			close(outcon)
		}
	)
	return(TRUE)
}

writeBNfiles <- function() {

	affyID_accession.txt
	all_ppi.txt
	gbGO.txt
	res.txt
	symArtsGeneDb.txt
	symArtsPubmed.txt
#	All belong in BN zipfile
}

writeBNAccessionFile <- function(filename, accessions, genbanks) {
	tryCatch (
		{
			outcon <- file(filename, open="w")		
			writeLines(prefix, con=outcon, sep="\n")		
			writeLines("Probe ID\tGenbank Acc", con=outcon, sep="\n")
			writeLines(paste(accessions, genbanks, sep="\t"), con=outcon, sep="\n")
		}, 
		error = function(e) {
			print(e) 
			return(FALSE)
		},
		finally = {
			close(outcon)
		}
	)
	return(TRUE)
}

writeBNGOFile <- function(filename, genbanks, golists) {
	tryCatch (
		{
			outcon <- file(filename, open="w")		
			writeLines(prefix, con=outcon, sep="\n")		
			writeLines("Genbank Acc\tGO", con=outcon, sep="\n")
			writeLines(paste(genbanks, paste(golists, sep=" "), sep="\t"), con=outcon, sep="\n")
		}, 
		error = function(e) {
			print(e) 
			return(FALSE)
		},
		finally = {
			close(outcon)
		}
	)
	return(TRUE)
}

writeBResFile <- function(filename, genbanks, golists) {
	tryCatch (
		{
			outcon <- file(filename, open="w")		
			writeLines(prefix, con=outcon, sep="\n")		
			writeLines("Probe ID\tClone Name\tGenbank Acc\tUniGene ID\tEntrezGene ID\tGene Symbol & Name\tGene Synonyms\tHuman TC\tHuman GC\tRefSeq Acc\tTC PubMed Ref\tGO\tTGI Annotation\tPhy Map\tGenetic Marker\tMouse ortholog\tRat ortholog\tZebrafish ortholog\tXenopus ortholog\tCattle ortholog\tElegans ortholog\tYeast ortholog\tDog ortholog\tChicken ortholog", con=outcon, sep="\n")
			writeLines(paste(genbanks, paste(golists, sep=" "), sep="\t"), con=outcon, sep="\n")
		}, 
		error = function(e) {
			print(e) 
			return(FALSE)
		},
		finally = {
			close(outcon)
		}
	)
	return(TRUE)
}
writeBNSymartsGeneDbFile <- function(filename, genbanks, golists) {
	tryCatch (
		{
			outcon <- file(filename, open="w")		
			writeLines(paste(genbanks, paste(golists, sep=" "), sep="\t"), con=outcon, sep="\n")
		}, 
		error = function(e) {
			print(e) 
			return(FALSE)
		},
		finally = {
			close(outcon)
		}
	)
	return(TRUE)
}
writeBNSymartsPubmedFile <- function(filename, symbols, pubmedids) {
	tryCatch (
		{
			outcon <- file(filename, open="w")		
			writeLines(paste(symbols, pubmedids, sep="\t"), con=outcon, sep="\n")
		}, 
		error = function(e) {
			print(e) 
			return(FALSE)
		},
		finally = {
			close(outcon)
		}
	)
	return(TRUE)
}


