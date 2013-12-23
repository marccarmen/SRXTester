SRXTester
=========
This is a simple GUI that is built to demonstrate the SRX library included in Okapi ( http://www.opentag.com/okapi/wiki/index.php?title=Main_Page )  In the GUI you can select a file with the segementation rules, an input file, and where to store the output files.

All of the jars from the Okapi library have been copied into the lib folder of this project.  It is very likely that most of these jars could be removed but I have not taken the time to determine which library(s) are necessary for SRX functionality.

In the data directory are three files.  
* DefaultRules.srx is a segmentation file from the Okapi project
* ep-00-01-17.en is a sample text file from the EuroParl corpus 
* ep-00-01-17.en.out is the output file using this DefaultRules.srx and ep-00-01-17.en files
