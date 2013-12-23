import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.sf.okapi.common.ISegmenter;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Range;
import net.sf.okapi.lib.segmentation.SRXDocument;

/**
 * This is a simple class that demonstrates the Okapi SRX library
 *  
 * @author Marc
 */
public class SRXTester extends JPanel implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	static private final String newline = "\n";
	JButton srxButton, inputButton, outputButton, processButton;
	JLabel srxLabel, inputLabel, outputLabel;
	File srxFile, inputFile, outputFile;
	JTextArea log;
	JFileChooser fc;
	
	public SRXTester() {  
		super(new BorderLayout());
		
        log = new JTextArea(5, 20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);
        
        //Create a new file chooser
        String location = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile().substring(1);
        location = location.replace("%20", " ");
        fc = new JFileChooser(location);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        //srx button
        srxButton = new JButton("Select SRX Rules");
        srxButton.addActionListener(this);
        
        //Create an input button
        inputButton = new JButton("Select Input");
        inputButton.addActionListener(this);
        
        //create output button
        outputButton = new JButton("Select Output");
        outputButton.addActionListener(this);
        
        //create process button
        processButton = new JButton("Process Input");
        processButton.addActionListener(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
        buttonPanel.add(srxButton);
        buttonPanel.add(inputButton);
        buttonPanel.add(outputButton);
        buttonPanel.add(processButton);
        JPanel labelPanel = new JPanel(new GridLayout(0, 1));
        srxLabel = new JLabel();
        labelPanel.add(srxLabel);
        inputLabel = new JLabel();
        labelPanel.add(inputLabel);
        outputLabel = new JLabel();
        labelPanel.add(outputLabel);
        labelPanel.add(new JLabel(""));
        
        panel.add(buttonPanel, BorderLayout.WEST);
        panel.add(labelPanel, BorderLayout.CENTER);
        
        add(panel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
    }
	
	/**
	 * This function is used for the different buttons in the app.  Depending on which button is 
	 * clicked the specific action is run.
	 * 
	 * @param e The event for the action performed
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == inputButton) {
			int returnVal = fc.showOpenDialog(SRXTester.this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				inputFile = fc.getSelectedFile();
				inputLabel.setText(inputFile.getName());
				log.append("Input file selected: " + inputFile.getName() + newline);
			}
			log.setCaretPosition(log.getDocument().getLength());
		} else if (e.getSource() == outputButton) {
			int returnVal = fc.showOpenDialog(SRXTester.this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				outputFile = fc.getSelectedFile();
				outputLabel.setText(outputFile.getName());
				log.append("Output file selected: " + outputFile.getName() + newline);
			}
			log.setCaretPosition(log.getDocument().getLength());
		} else if (e.getSource() == srxButton) {
			int returnVal = fc.showOpenDialog(SRXTester.this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				srxFile = fc.getSelectedFile();
				srxLabel.setText(srxFile.getName());
				log.append("SRX Rules selected: " + srxFile.getName() + newline);
			}
			log.setCaretPosition(log.getDocument().getLength());
		} else if (e.getSource() == processButton) {
			if (srxButton == null) {
				JOptionPane.showMessageDialog(SRXTester.this, "Please select a file with the SRX Segmentation Rules"); 
			} else if (inputFile == null) {
				JOptionPane.showMessageDialog(SRXTester.this, "Please select an Input File");
			} else if (outputFile == null) {
				JOptionPane.showMessageDialog(SRXTester.this, "Please select an Output File");
			} else {
				log.append("Beginning Sentence Segmentation" + newline);
				processInputFile();
			}
			log.setCaretPosition(log.getDocument().getLength());
		}
	}
	
	/**
	 * Use the rules file, input file, and output file to process and segment the file.
	 */
	public void processInputFile() {
		//If output file doesn't exist then create it
		if (!outputFile.exists()) {
			try {
				outputFile.createNewFile();
			} catch (IOException e) {
				log.append("IOException occurred while trying to create the output file: " + e.getMessage() + newline);
				return;
			}
		}
		
		//Load the SRXDocument rules
		SRXDocument rules = null;
		try {
			rules = new SRXDocument();
			rules.loadRules(srxFile.getCanonicalPath());
		}
		catch (IOException ex) {
			log.append("IOException while loading rules" + newline);
			return;
		}
		ISegmenter segmenter = rules.compileLanguageRules(LocaleId.fromString("en"),  null);
		
		//Read through the input file and save the output to a list
		List<String> output = new ArrayList<String>();
		String scan;
		FileReader file = null;
		try {
			file = new FileReader(inputFile);
		}
		catch (FileNotFoundException ex) {
			log.append("Unable to find input file: " + inputFile.getName() + newline);
			return;
		}
		
        BufferedReader br = new BufferedReader(file);
        try {
        	int sentenceCount = 1;
	        while((scan = br.readLine()) != null)
	        {
	        	if (scan.length() > 0) {
		        	int count = segmenter.computeSegments(scan);
		        	log.append("Sentence #" + sentenceCount + " has " + count + " segments" + newline);
		        	String outSent = "";
		        	if (segmenter.oneSegmentIncludesAll() || count == 1) {
		        		outSent = "[" + scan + "]";
		        	}
		        	else {
		        		List<Range> ranges = segmenter.getRanges();
		        		Iterator<Range> iter = ranges.iterator();
		        		while (iter.hasNext()) {
		        			Range range = iter.next();
		        			outSent += "[" + scan.substring(range.start, range.end) + "]";
		        		}
		        	}
		        	log.append(scan + newline);
		        	log.append(outSent + newline);
		        	output.add(outSent);
	        	}
	        	else {
	        		log.append("Sentence #" + sentenceCount + " is blank" + newline);
	        	}
	        	sentenceCount++;
	        	log.append(newline);
	        }
	        br.close();
        }
        catch (IOException ex) {
        	log.append("IOException occurred while reading input file: " + inputFile.getName() + newline);
        	return;
        }
        
        //Write out the output sentences
        FileWriter fw;
		try {
			fw = new FileWriter(outputFile);
			BufferedWriter bw = new BufferedWriter(fw);
			Iterator<String> iter = output.iterator();
			while (iter.hasNext()) {
				String out = iter.next();
				bw.write(out + newline);
			}
	        bw.close();
		} catch (IOException e) {
			log.append("IOException while writing output: " + e.getMessage() + newline);
			return;
		}
        
        log.append("Processing complete. Output file available at " + outputFile.getName() + newline);
	}
	
	/**
	 * Show the GUI
	 */
	private static void createAndShowGUI() {
		JFrame frame = new JFrame("SRXTester");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.add(new SRXTester());
		
		frame.pack();
		frame.setVisible(true);
		frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	}
	
	public static void main (String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				UIManager.put("swing.boldMetal",  Boolean.FALSE);
				createAndShowGUI();
			}
		});
	}
}
