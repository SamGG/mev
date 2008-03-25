12.19.2004
mev_jars_readme.txt

This document describes the jars that are created
to contain MeV classes to support the execution of MeV.
Several second party jars are included in the lib directory
but this document will focus on the six jars created
specifically from MeV's source code base during a build.

The five jars cover different parts of the source tree and
represent functional or organizational sections of the
parts of the application.  The five jars are located in
the lib directory and are:

1.) mev-util.jar
2.) mev-algoirthm-support.jar
3.) mev-gui-support.jar
4.) mev-base.jar

5.) mev-algorithm-impl.jar
6.) mev-gui-impl.jar


Jar Descriptions and Contents
-----------------------------

1.) Name: mev-util.jar

    Description:  Contains generic utility classes.

    Contents (packages or classes):

	org/tigr/graph
	org/tigr/util
	org/tigr/util/awt
	org/tigr/util/swing



2.) Name: mev-algorithm-support.jar

    Description:  Contains classes that support algorithm execution 

    Contents (packages or classes):

	org/tigr/microarray/mev/cluster
	org/tigr/microarray/mev/cluster/algorithm
	org/tigr/microarray/mev/cluster/algorithm/impl (non-module classes)
	org/tigr/microarray/mev/cluster/algorithm/impl/util



3.) Name: mev-gui-support.jar

    Description:  Contains classes that support mev's gui and gui package implementations. 

    Contents (packages or classes):

	org/tigr/microarray/mev/cluster/gui                        
 	org/tigr/microarray/mev/cluster/gui/helpers 		          
	org/tigr/microarray/mev/cluster/gui/helpers/ktree        
        org/tigr/microarray/mev/cluster/gui/impl/*.class
	org/tigr/microarray/mev/cluster/gui/impl/dialogs
	org/tigr/microarray/mev/cluster/gui/impl/dialogs/dialogHelpUtil
	org/tigr/microarray/mev/cluster/gui/impl/dialogs/normalization
	org/tigr/microarray/mev/cluster/gui/impl/images
	org/tigr/microarray/mev/cluster/gui/impl/util



4.) Name: mev-base.jar

    Description:  Classes to support the mev interface, file loading,
                  scripting, and cluster utilities

    Contents (packages or classes):
    
	org/tigr/microarray/mev/r/
	org/tigr/microarray/mev/persistence/
   	org/tigr/microarray/file
   	org/tigr/microarray/mev/script/
   	org/tigr/microarray/mev/microarray/annotation/
	org/tigr/microarray/util
	org/tigr/microarray/util/awt
	org/tigr/microarray/util/swing				
	org/tigr/microarray/mev				
	org/tigr/microarray/mev/action
	org/tigr/microarray/mev/file
	org/tigr/microarray/mev/file/agilent
	org/tigr/microarray/mev/script
	org/tigr/microarray/mev/script/scriptGUI
	org/tigr/microarray/mev/script/util
	org/tigr/microarray/mev/cluster/clusterUtil
	org/tigr/microarray/mev/cluster/clusterUtil/submit/**
	org/tigr/microarray/mev/annotation
	org/tigr/remote/** 
	
	

5.) Name: mev-algorithm-impl.jar

    Description:  Contains the 'analysis engine' classes of the
	  	  analysis module implementations.

    Contents (packages or classes):

	org/tigr/microarray/mev/cluster/algorithm/impl
	(includes factory.properties)
	(excludes ExperimentUtil and AlgorithmFactoryImpl)



6.) Name: mev-gui-impl.jar

    Description:  Contains the gui components of the
	          analysis module implementations.

    Contents (packages or classes):

	org/tigr/microarray/mev/cluster/gui/impl/<module packages>
	(includes factory.properties)
        (excludes the util and dialogs packages and impl/*.class)








