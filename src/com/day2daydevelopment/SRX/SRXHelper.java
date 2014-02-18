package com.day2daydevelopment.SRX;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import net.sf.okapi.common.ISegmenter;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Range;
import net.sf.okapi.lib.segmentation.SRXDocument;

/**
 * Provide an interface to SRX segmentation using Okapi's SRX library
 * @author Marc
 * @see net.sf.okapi.lib.segmentation.SRXDocument 
 */
public class SRXHelper {
	//Instantiate logger
	private static Logger log = Logger.getLogger(SRXHelper.class.getName());
	
	/**
	 * Use a file path process the sentences and return a list of segmented sentences.  Assumes that sentences are separated by new line
	 * 
	 * @param segmenter The segmenter to be used.  If null then use srxFilePath to load the rules into a segmenter 
	 * @param srxFilePath The path to the SRX rules
	 * @param inputFile Path to the file that needs to be parsed
	 * @param locale The locale code for the language of the rules.  the value should be a BCP-47 value (e.g. "de", "fr-ca", etc.)
	 * @return List<String> Segmented sentences in a List
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws Exception
	 */
	public static List<String> segmentFile(ISegmenter segmenter, String srxFilePath, String inputFile, String locale) throws IOException, FileNotFoundException, UnsupportedEncodingException, Exception {
		//Load the SRXRules if segmenter isn't already set
		if (segmenter == null) {
			segmenter = SRXHelper.loadSRXRules(srxFilePath, locale);
		}
		
		FileInputStream file = null;
		try {
			file = new FileInputStream(inputFile);
		}
		catch (FileNotFoundException ex) {
			log.info("Unable to find input file: " + inputFile);
			throw ex;
		}
		
		BufferedReader br;
		String scan;
		List<String> fileData = new ArrayList<String>();
		try {
        	br = new BufferedReader(new InputStreamReader(file, "UTF8"));
	        while((scan = br.readLine()) != null)
	        {
	        	fileData.add(scan);
	        }
	        br.close();
		} catch (UnsupportedEncodingException e) {
			log.info("UnsupportedEncodingException while reading input file: " + e.getMessage());
			throw e;
		} catch (IOException e) {
        	log.info("IOException occurred while reading input file: " + inputFile);
        	throw e;
        } catch (Exception e) {
			log.info("Exception while reading input file: " + e.getMessage());
			throw e;
		}
		
		return segmentFileStringArray(segmenter, srxFilePath, fileData, locale);
	}
	
	/**
	 * Using a String as input split the string on new lines and process the data
	 * 
	 * @param segmenter The segmenter to be used.  If null then use srxFilePath to load the rules into a segmenter 
	 * @param srxFilePath The path to the SRX rules
	 * @param inputText The input string to be split and segmented
	 * @param locale The locale code for the language of the rules.  the value should be a BCP-47 value (e.g. "de", "fr-ca", etc.)
	 * @return List<String> Segmented sentences in a List
	 * @throws IOException
	 */
	public static List<String> segmentFileString(ISegmenter segmenter, String srxFilePath, String inputText, String locale) throws IOException {
		log.info("Being loadSRXRules with arguments [srxFilePath: " + srxFilePath + "; locale: " + locale + "]");
		//Load the SRXRules if segmenter isn't already set
		if (segmenter == null) {
			segmenter = SRXHelper.loadSRXRules(srxFilePath, locale);
		}
		
		List<String> inputData = new ArrayList<String>(Arrays.asList(inputText.split("(?:\r\n|\r|\n)")));
		return segmentFileStringArray(segmenter, srxFilePath, inputData, locale);
	}
	
	/**
	 * Take a List of sentences as input, segment them and return the segmented list
	 * 
	 * @param segmenter The segmenter to be used.  If null then use srxFilePath to load the rules into a segmenter 
	 * @param srxFilePath The path to the SRX rules
	 * @param inputData A List of strings that need to be segmented
	 * @param locale The locale code for the language of the rules.  the value should be a BCP-47 value (e.g. "de", "fr-ca", etc.)
	 * @return List<String> Segmented sentences in a List
	 * @throws IOException
	 */
	public static List<String> segmentFileStringArray(ISegmenter segmenter, String srxFilePath, List<String> inputData, String locale) throws IOException {
		List<String> outputData = new ArrayList<String>();
		
		//Load the SRXRules if segmenter isn't already set
		if (segmenter == null) {
			segmenter = SRXHelper.loadSRXRules(srxFilePath, locale);
		}
		
		//iterate over inputData, segment the string, save segmented string to outputData
		for (int i = 0; i < inputData.size(); i++) {
			String input = inputData.get(i);
			outputData.add(segmentLine(segmenter, srxFilePath, input, locale));
		}
		return outputData;
	}
	
	/**
	 * Segment a sentence
	 * 
	 * @param segmenter The segmenter to be used.  If null then use srxFilePath to load the rules into a segmenter 
	 * @param srxFilePath The path to the SRX rules
	 * @param input The sentence to segment
	 * @param locale The locale code for the language of the rules.  the value should be a BCP-47 value (e.g. "de", "fr-ca", etc.)
	 * @return a segmented sentence
	 * @throws IOException
	 */
	public static String segmentLine(ISegmenter segmenter, String srxFilePath, String input, String locale) throws IOException {
		//Load the SRXRules if segmenter isn't already set
		if (segmenter == null) {
			segmenter = SRXHelper.loadSRXRules(srxFilePath, locale);
		}
		
		String output = "";
		
		int count = segmenter.computeSegments(input);
		if (segmenter.oneSegmentIncludesAll() || count == 1) {
			output = "[" + input + "]";
		}
		else {
			List<Range> ranges = segmenter.getRanges();
    		Iterator<Range> iter = ranges.iterator();
    		while (iter.hasNext()) {
    			Range range = iter.next();
    			output += "[" + input.substring(range.start, range.end) + "]";
    		}
		}
		
		return output;
	}
	
	/**
	 * Get a segmenter using a file path to an SRX file
	 * 
	 * @param srxFilePath Path to the SRX file
	 * @param locale The locale code for the language of the rules.  the value should be a BCP-47 value (e.g. "de", "fr-ca", etc.)
	 * @return An ISegmenter object
	 * @throws IOException
	 */
	private static ISegmenter loadSRXRules(String srxFilePath, String locale) throws IOException {
		log.info("Being loadSRXRules with arguments [srxFilePath: " + srxFilePath + "; locale: " + locale + "]");
		if (locale == null) {
			locale = "en";
		}
		SRXDocument rules = new SRXDocument();
		rules.loadRules(srxFilePath);
		ISegmenter segmenter = rules.compileLanguageRules(LocaleId.fromString(locale),  null);
		return segmenter;
	}
}
