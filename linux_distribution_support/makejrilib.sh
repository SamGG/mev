#! /bin/sh

## This script needs to be run from the directory it lives in ##

## Needs CRAN R & Sun/oracle Java ## 

## To install R:
## yum info R
## sudo yum install R 

## Manually set R_HOME & JAVA_HOME if auto configure fails ##

echo " >>>>>>>>>>>>>>  Running auto conf to check R_HOME, JAVA_HOME and other JRI setting compatibility"
cd jri
./configure

echo " >>>>>>>>>>>>>>  Compiling libjri.so & JRI.jar"
make

cd ..
ls -l jri/libjri.so jri/src/*.jar
echo " >>>>>>>>>>>>>>  Copying files to MeV lib directory"
cp jri/libjri.so lib
cp jri/src/JRI.jar lib
ls -l lib/libjri.so lib/JRI.jar
echo " >>>>>>>>>>>>>> Removing compiled files from jri folder"
rm jri/libjri.so
rm jri/src/JRI.jar
echo " >>>>>>>>>>>>>> DONE !!!"
