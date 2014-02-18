import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
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

import com.day2daydevelopment.SRX.SRXHelper;

/**
 * This is a simple class that demonstrates the Okapi SRX library.  
 * 
 * 2013/02/17 - Refactored out the SRX processing into external class 
 *  
 * @author Marc
 * @see com.day2daydevelopment.SRX.SRXHelper
 */
public class SRXTester extends JPanel implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	static private final String newline = System.getProperty("line.separator");
	JButton srxButton, inputButton, outputButton, processButton;
	JLabel srxLabel, inputLabel, outputLabel;
	File srxFile, startFile;
	JTextArea log;
	JFileChooser fc_file, fc_both;
	
	public SRXTester() {  
		super(new BorderLayout());
		
        log = new JTextArea(5, 20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);
        
        //Create a new file chooser
        String location = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile().substring(1);
        location = location.replace("%20", " ");
        fc_file = new JFileChooser(location);
        fc_file.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        fc_both = new JFileChooser(location);
        fc_both.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        
        //srx button
        srxButton = new JButton("Select SRX Rules");
        srxButton.addActionListener(this);
        
        //Create an input button
        inputButton = new JButton("Select Input");
        inputButton.addActionListener(this);
        
        //create process button
        processButton = new JButton("Process Data");
        processButton.addActionListener(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
        buttonPanel.add(srxButton);
        buttonPanel.add(inputButton);
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
			int returnVal = fc_both.showOpenDialog(SRXTester.this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				startFile = fc_both.getSelectedFile();
				inputLabel.setText(startFile.getAbsolutePath());
				log.append("Input file selected: " + startFile.getName() + newline);
			}
			log.setCaretPosition(log.getDocument().getLength());
		} else if (e.getSource() == srxButton) {
			int returnVal = fc_file.showOpenDialog(SRXTester.this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				srxFile = fc_file.getSelectedFile();
				srxLabel.setText(srxFile.getAbsolutePath());
				log.append("SRX Rules selected: " + srxFile.getName() + newline);
			}
			log.setCaretPosition(log.getDocument().getLength());
		} else if (e.getSource() == processButton) {
			if (srxButton == null) {
				JOptionPane.showMessageDialog(SRXTester.this, "Please select a file with the SRX Segmentation Rules"); 
			} else if (startFile == null) {
				JOptionPane.showMessageDialog(SRXTester.this, "Please select an Input File");
			} else {
				log.append("Beginning Sentence Segmentation" + newline);
				beginProcessingFiles();
			}
			log.setCaretPosition(log.getDocument().getLength());
		}
	}
	
	/**
	 * Determine if startFile is directory or individual file and process accordingly
	 * Technically this method requires a .txt file to process
	 */
	public void beginProcessingFiles() {
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
							log.append("Unable to create output file" + newline);
							e.printStackTrace();
						}
						
						if (outputFile != null && inputFile != null) {
							processInputFile(inputFile, outputFile);
						}
					}
					else {
						log.append("Invalid input file " + fileName + " Expecting a .txt file" + newline);
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
					log.append("Unable to create output file" + newline);
					e.printStackTrace();
				}
				
				if (outputFile != null && startFile != null) {
					processInputFile(startFile, outputFile);
				}
			}
			else {
				log.append("Invalid input file " + fileName + " Expecting a .txt file" + newline);
			}
		}
	}
	
	/**
	 * @param inputFile The file to segment
	 * @param outputFile The file location to save segmented file to
	 */
	public void processInputFile(File inputFile, File outputFile) {
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
				osw.append(out + newline);
			}
			osw.flush();
	        osw.close();
		} catch (UnsupportedEncodingException e) {
			log.append("UnsupportedEncodingException while writing output: " + e.getMessage() + newline);
			return;
		} catch (IOException e) {
			log.append("IOException while writing output: " + e.getMessage() + newline);
			return;
		} catch (Exception e) {
			log.append("Exception while writing output: " + e.getMessage() + newline);
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
