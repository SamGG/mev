12.17.2004
Ant Build ReadMe


Contents:
         *Ant Script for MeV Compilation (Intro, overview)
         *About Using build.xml (use of the script)
         *Modification/Customization of build.xml (mods for feature or
          module development)

		

Ant Script for MeV Compilation
------------------------------

Ant is a java based tools to support the building of java projects in a
controlled manner.  A comprehensive description of Ant, a manual, and Ant
executable and source code is available at: http://ant.apache.org.
Installation instructions are also available at the above site.
Installation of Ant is fairly easy and mostly involves specification
or modification of environment variables as described in the installation
instructions. 

Once installed Ant will use the build.xml file to direct MeV compilation
and jar construction.  Before using build.xml, you will need to update the file
so that the "javac-location" property  refers to a java compiler.



About Using build.xml
---------------------

build.xml defines Ant 'targets' that can be called to compile MeV.
The building process uses multiple targets but three main targets
can be specified to control the building process.  These commands can
be called on the command line from within the devel/ant_script directory
or from within an IDE with support for Ant.

>ant build-all (or just >ant, since build-all is the default target)

will build the entire code base of MeV and will produce the following
jar files:

1.) mev-util.jar
2.) mev-algorithm-support.jar
3.) mev-gui-support.jar
4.) mev-base.jar

5.) mev-algorithm-impl.jar   //algorithm module implementation jars
6.) mev-algorithm-impl.jar

7.) images.jar     //updates jar of org.tigr.images
8.) dialogHelp.jar //updates jar of mev html help pages


>ant build-base

will produce the six jars that support the module implementations
(jars 1-4, and 7 and 8 above).  These contain utilities, basic mev classes, 
support classes, images, and html help pages.


>ant build-modules

will produce the two jars that contain the module algorithm and
gui implementations (jars 5 and 6 above).  These jar files are the
most likely to be modified when developing a new module.


Note that the output of the script execution can be redireted to
an output file. (e.g. >ant build-all >output_log.txt)

**See mev-jar-readme.txt in devel/devel_docs to see a description
of the contents of the six jar files above.



Modification/Customization of build.xml
---------------------------------------

To enable inclusion of new analysis modules and features in MeV it
is often necessary to modify build.xml to compile and jar the new
classes.  Use mev-jar-readme.txt to identify which jars should
contain the new classes or packages.  Modifications can be broadly
placed into two main categories, 1.) features and utilities, and
2.) new analysis modules. 


1.) Feature and Utility Enhancements
    --------------------------------
For feature enhancements usually the mev-base and/or support jars 
will be the final destination of the new classes.  Within the build.xml
script, roughly the first third contains targets for the compilation
and jar creation of utilities, gui and algorithm support, and mev
package classes.  Each of these targets contains a descriptive comment
listing the packages included in the compilation or build.  If the
work is within an existing package, build.xml might not have to change
to support compilation and jar creation. If the work is in a new package,
the appropriate targets should include the new package.  Be sure to modify
a target for compilation and a target for jar creation.

Here is a sample target with comment used to compile org.tigr.util. 

    <!-- TARGET ============================================		
         	Target Name: util
		Depends: init
         	Target Description: Compiles org/tigr/util and packages below
         	Compilation Summary:
				org/tigr/util
				org/tigr/util/awt
				org/tigr/util/swing
    -->
    <target name="util" depends="init">
        <javac srcdir="${tigr.dir}/util" destdir="${dest.dir}">
	  	<classpath>
                <pathelement location="${lib.dir}/jai_core.jar"/>
                <pathelement location="${lib.dir}/jai_codec.jar"/>
                <pathelement location="${lib.dir}/images.jar"/>
            </classpath>
        </javac>
    </target>

Note that all packages below org.tigr.util will be compiled.  Some targets
set the sourcepath attribute of the javac task to "" which effectively disables
directory searching for compilation.  In these instances specific classes
or packages to include or exclude within the srcdir will be explicitly specified.



2.) New Analysis Module Development
    -------------------------------

Most developers will be primarily concerned with the development and integration
of new analysis modules.  Description of the details of interface implementation
to create a new module is handled elsewhere.  This document will focus on
compilation and jar construction of new modules.

There are three required modifications to build.xml to support compilation and
jar construction of the new module.

	1.) Create Targets for Compilation
	2.) Include a new module property
	3.) Modify the 'algorithm-modules' and 'modules-only' targets to compile
	    the new targets created in step 1.

1.) Create targets to permit compilation of the module's algorithm class or package
and the corresponding gui.impl package.  This pair of targets will direct compilation
of the new module.  Target pairs for each module are found in the lower half of build.xml.
Creation of a new target pair can be easily done by using an existing target pair as
a template.  The last two targets in the script are generic targets that can be
modified to suite the new module.  Here is an example target pair for the KMC module:

    <target name="KMC" depends="KMC-GUI" if="KMC">
        <javac sourcepath="" srcdir="${alg.impl.dir}" destdir="${dest.dir}">
		<classpath refid="module.build.class.path"/>
            <include name="KMC.java"/>
	  </javac>
	  <propertyfile file="${alg.properties.file}">
            <entry key="KMC" value="org.tigr.microarray.mev.cluster.algorithm.impl.KMC"/>
        </propertyfile>
    </target>

    <target name="KMC-GUI">
        <javac srcdir="${gui.impl.dir}/kmc" destdir="${dest.dir}">
		<classpath refid="module.build.class.path"/>
	  </javac>
	  <propertyfile file="${gui.properties.file}">
		<entry key="gui.names" value="KMC:" operation="+"/>
            <entry key="KMC.name" value="KMC"/>
            <entry key="KMC.class" value="org.tigr.microarray.mev.cluster.gui.impl.kmc.KMCGUI"/>
            <entry key="KMC.smallIcon" value="analysis16.gif"/>
            <entry key="KMC.largeIcon" value="kmc_button.gif"/>
            <entry key="KMC.tooltip" value="k-Means/Medians Clustering"/>
        </propertyfile>
    </target>

Note that the first target compiles the algorithm while the second target shown above
compiles the supporting gui implementation package.  Note that the first target has the
corresponding gui target in the depends attribute and the target execution (first
target) has a conditional that controls execution of the target.  This 'if' attribute
is evaluated based on the inclusion of the target in the module properties list
in step 2.  Most of the changes to make to these targets involve changing package names,
file paths, and property files entries.  Note that in addition to compilation each module
has associated properties that are entered into a 'factory.properties' file.  These
properties specify algorithm names, gui and algoirthm class names, button icons, and tool tips.


2.) Modify the module selection properties to include the new target.  Near the top
of the script there are elements that list the algorithm targets to include in the build.
Here is a section of this list as an example:

    <property name="HCL" value="y"/>
    <property name="ST" value="y"/>
    <property name="SOTA" value="y"/>
<!--    <property name="RN" value="y"/>  excluded from the build using comment -->
    <property name="KMC" value="y"/>

A new property with the new module's target name should be included in this list.
Any module that is commented out will be excluded from the compilation and will be
absent in the MeV distribution.


3.) Modify the 'algorithm-modules' target and the 'modules-only' target to include
a dependency on the new module's compilation targets from step 1.  These two targets
are near the beginning of the script and each has a 'depends' attribute.  One target
is for compilation of only the modules while the other target is for base and 
module compilation.  In both cases the depends attribute contains a list of target
names in the order that they will appear in the MeV interface.  To modify this,
simply insert the target name of the new algorithm compilation target created in step 1.
The list is comma delimited and inclusion of the new target name will place the target
in the target execution tree.

Here is the algorithm-modules target, one of the two targets to modify in step three above.
Note the algorithm list in the depends attribute should be modified to include the new module.

    <target name="algorithm-modules" depends="build-base,HCL,ST,SOTA,RN,KMC,KMCS,CAST,QTC,GSH,SOM,FOM,PTM,TTEST,SAM,OWA,TFA,SVM,KNNC,DAM,GDM,PCA,TRN,EASE">
    </target>


 







 



