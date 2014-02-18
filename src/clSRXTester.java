import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.day2daydevelopment.SRX.SRXHelper;

public class clSRXTester {
	public static void main(String args[]) {
		try {
			Options opt = new Options();
			opt.addOption("h", false, "Print help for this application");
			Option optSRX = new Option("srx", true, "Path to the SRX rules file");
			optSRX.setRequired(true);
			opt.addOption(optSRX);
			Option optInput= new Option("input", true, "Path to the input directory/file");
			optInput.setRequired(true);
			opt.addOption(optInput);
			
			CommandLineParser parser = new GnuParser();
			CommandLine cl = null;
			try {
				cl = parser.parse(opt, args);
			} catch (MissingOptionException ex) {
				System.out.println(ex.getMessage());
				HelpFormatter hf = new HelpFormatter();
				hf.printHelp("clSRXTester.jar -input INPUT_PATH -srx SRX_PATH", opt);
				return;
			}
			
			if (cl.hasOption("h")) {
				HelpFormatter hf = new HelpFormatter();
				hf.printHelp("clSRXTester.jar -input INPUT_PATH -srx SRX_PATH", opt);
			} else {
				String srxPath = null;
				if (cl.hasOption("srx")) {
					srxPath = cl.getOptionValue("srx");
				}
				
				String startPath = null;
				if (cl.hasOption("input")) {
					startPath = cl.getOptionValue("input");
				}
				
				if (srxPath == null || startPath == null) {
					System.out.println("Invalid parameters");
					HelpFormatter hf = new HelpFormatter();
					hf.printHelp("clSRXTester.jar -input INPUT_PATH -srx SRX_PATH", opt);
				}
				else {
					clSRXTester srxTester = new clSRXTester();
					srxTester.beginProcessingFiles(srxPath, startPath);
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public void beginProcessingFiles(String srxPath, String startPath) {
		File srxFile = new File(srxPath);
		if (!srxFile.exists()) {
			System.out.println(srxPath + " does not exist");
			return;
		}
		File startFile = new File(startPath);
		if (!startFile.exists()) {
			System.out.println(startPath + " does not exist");
			return;
		}
		
		//Check for directory
		if (startFile.isDirectory()) {
			//Get files in directory
			File[] listOfFiles = startFile.listFiles(); 

			for (int i = 0; i < listOfFiles.length; i++) 
			{
				if (listOfFiles[i].isFile()) 
				{
					String fileName = listOfFiles[i].getName();
					//Make sure processing a txt file
					if (fileName.endsWith(".txt") || fileName.endsWith(".TXT"))
					{
						File inputFile = listOfFiles[i];
						File outputFile = null;
						try {
							//Get the input file path to generate output file path .stxt file
							String inputFilePath = inputFile.getCanonicalPath();
							String outputFilePath = inputFilePath.substring(0, inputFilePath.length() - 3) + "stxt";
							outputFile = new File(outputFilePath);
						} catch (IOException e) {
							System.out.println("Unable to create output file");
							e.printStackTrace();
						}
						
						if (outputFile != null && inputFile != null) {
							processInputFile(srxFile, inputFile, outputFile);
						}
					}
					else {
						System.out.println("Invalid input file " + fileName + " Expecting a .txt file");
					}
				}
			}
		}
		else {
			String fileName = startFile.getName();
			//Make sure processing a txt file
			if (fileName.endsWith(".txt") || fileName.endsWith(".TXT"))
			{
				File outputFile = null;
				try {
					//Get the input file path to generate output file path .stxt file
					String inputFilePath = startFile.getCanonicalPath();
					String outputFilePath = inputFilePath.substring(0, inputFilePath.length() - 3) + "stxt";
					outputFile = new File(outputFilePath);
				} catch (IOException e) {
					System.out.println("Unable to create output file");
					e.printStackTrace();
				}
				
				if (outputFile != null && startFile != null) {
					processInputFile(srxFile, startFile, outputFile);
				}
			}
			else {
				System.out.println("Invalid input file " + fileName + " Expecting a .txt file");
			}
		}
	}
	
	/**
	 * @param inputFile The file to segment
	 * @param outputFile The file location to save segmented file to
	 */
	public void processInputFile(File srxFile, File inputFile, File outputFile) {
		//Get the segmented output from SRXHelper.segmentFile
		List<String> output = null;
		try {
			output = SRXHelper.segmentFile(null, srxFile.getCanonicalPath(), inputFile.getCanonicalPath(), "en");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
        
        //Write out the output sentences
        FileOutputStream fos;
		try {
			fos = new FileOutputStream(outputFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
			Iterator<String> iter = output.iterator();
			while (iter.hasNext()) {
				String out = iter.next();
				osw.append(out + System.getProperty("line.separator"));
			}
			osw.flush();
	        osw.close();
		} catch (UnsupportedEncodingException e) {
			System.out.println("UnsupportedEncodingException while writing output: " + e.getMessage());
			return;
		} catch (IOException e) {
			System.out.println("IOException while writing output: " + e.getMessage());
			return;
		} catch (Exception e) {
			System.out.println("Exception while writing output: " + e.getMessage());
			return;
		}
        
		System.out.println("Processing complete. Output file available at " + outputFile.getName());
	}
}
