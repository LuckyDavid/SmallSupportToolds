package builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import OutputDocuments.CSVAttribute;
import OutputDocuments.CSVEntry;

public class XMLBuilder {

	public static Document baselineDocument;
	public static List<CSVEntry> csvEntries;
	public static String OutputfilePrefix = "OutputDocument";
	public static String Delimiter = ";";

	/*
	 * Build Mode:
	 * 1 - Simple - Parses CSV file and produces one XML file per entry
	 * 2 - Recursive - Parses CSV file and produces one single XML file with a sub-list of elements (one per entry CSV)
	 */
	public static String BuildMode = "1";
	public static List<String> MandatoryFields;

	/*
	 * Fields related with Recursive mode only
	 */
	public static List<String> AggregatedEqualFields;
	public static List<String> AggregatedSumFields;
	public static List<String> AggregatedCountFields;

	public static List<String> RecursiveElements;



	public static void main(String[] args) {

		// Set configurations
		setConfigurations();

		// Load baseline document
		baselineDocument = loadDocument("Baseline.xml");

		// Load input CSV file
		loadInputCSVFile("Input.csv");

		System.out.println("CSVFile loaded successfully");

		// build XML according to selected mode
		switch(BuildMode)
		{
		case "1":
			// Build XML using Simple Mode
			buildXMLSimple(csvEntries, baselineDocument);
			break;
		case "2":
			// Build XML using Recursive Mode
			buildXMLRecursive(csvEntries, baselineDocument);
			break;
		default:
			System.out.println("Build: no match");
		}


		System.out.println("Done!");

	}

	private static Document loadDocument(String filepath){
		try{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			if(filepath != null){
				return docBuilder.parse(filepath);
			}else{
				return docBuilder.newDocument();
			}

		}catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
		}
		return null;
	}

	private static void writeOutputDocument(String outputDocumentName, Document inputDocument){
		try{
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(inputDocument);
			StreamResult result = new StreamResult(new File(outputDocumentName));
			transformer.transform(source, result);
		}catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

	private static void loadInputCSVFile(String csvFile){
		String headerOfFile = "";
		String line = "";
		String cvsSplitBy = Delimiter;

		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

			//setting up file header
			headerOfFile = br.readLine();
			String[] columnsOfFileToCreate = headerOfFile.split(cvsSplitBy);

			//defining number of attributes for each entry
			int numberOfAttributes = columnsOfFileToCreate.length;
			csvEntries = new ArrayList<CSVEntry>();

			System.out.println("number of entries " + numberOfAttributes);

			while ((line = br.readLine()) != null) {
				boolean ignoreEntry = false;

				// use csvSplitBy as separator
				String[] valuesOfFileToCreate = line.split(cvsSplitBy,-1);
				CSVEntry entry = new CSVEntry();
				for(int i = 0; i<valuesOfFileToCreate.length; i++){
					CSVAttribute attribute = new CSVAttribute(columnsOfFileToCreate[i],valuesOfFileToCreate[i]);
					System.out.println("About to evaluate: [ " + attribute.getColumn() + " , " + attribute.getValue() + "]");
					// If mandatory attribute is empty then ignore this line
					if(MandatoryFields.contains(attribute.getColumn()) && attribute.getValue().isEmpty()){
						ignoreEntry = true;
						System.out.println("[Column " + columnsOfFileToCreate[i] + " , value " + valuesOfFileToCreate[i] + "] -- IGNORED");
						break;
					}
					entry.addAttribute(attribute);
					System.out.println("[Column " + columnsOfFileToCreate[i] + " , value " + valuesOfFileToCreate[i] + "] -- ADDED");
				}

				if(!ignoreEntry){
					csvEntries.add(entry);
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void setConfigurations(){
		InputStream input = null;
		Properties prop = new Properties();
		String tempName;
		String delimiter;
		String mode;
		String mandatory;

		try{
			input = new FileInputStream("config.properties");

			//load properties file
			prop.load(input);
			tempName = prop.getProperty("OutputFilePrefix");
			if(tempName!=null){
				OutputfilePrefix = tempName;
			}

			delimiter = prop.getProperty("CSVDelimiter");
			if(delimiter!=null){
				Delimiter = delimiter;
			}

			mode = prop.getProperty("Mode");
			if(mode!=null){
				BuildMode = mode;
			}

			mandatory = prop.getProperty("MandatoryFields");
			if(mandatory!=null){
				MandatoryFields = Arrays.asList(mandatory.split(Delimiter));
			}

			AggregatedEqualFields = Arrays.asList(prop.getProperty("AggregatedEqual").split(Delimiter));

			AggregatedSumFields = Arrays.asList(prop.getProperty("AggregatedSum").split(Delimiter));

			AggregatedCountFields = Arrays.asList(prop.getProperty("AggregatedCount").split(Delimiter));

			RecursiveElements = Arrays.asList(prop.getProperty("RecursiveElements").split(Delimiter));

		} catch(IOException ex){
			ex.printStackTrace();
		}finally{
			if(input!=null){
				try{
					input.close();
				}catch(IOException ex){
					ex.printStackTrace();
				}
			}
		}

	}

	private static void buildXMLSimple(List<CSVEntry> inputEntries, Document inputDocument){
		// Apply transformations
		Iterator<CSVEntry> itr = inputEntries.iterator();
		int outputDocNumber = 1;

		// For each entry create a new document
		while(itr.hasNext()){
			CSVEntry entry = itr.next();

			writeOnXML(entry,inputDocument);

			// write the content into xml file
			writeOutputDocument(OutputfilePrefix + outputDocNumber + ".xml",inputDocument);

			outputDocNumber++;
		}
	}

	private static void writeOnXML(CSVEntry entry, Document inputDocument){
		List<CSVAttribute> attributes = entry.getAttributes();
		Iterator<CSVAttribute> itrattr = attributes.iterator();

		while(itrattr.hasNext()){
			CSVAttribute attribute = itrattr.next();

			String nodeName = attribute.getColumn();
			String content = attribute.getValue();

			NodeList nodesToUpdate = inputDocument.getElementsByTagName(nodeName);
			System.out.println("NodeName: " + nodeName + " NodeList length: " + nodesToUpdate.getLength());
			// All nodes, irrelevant of position
			for(int i = 0; i<nodesToUpdate.getLength(); i++){
				// Get the node element by tag name directly
				System.out.println("Updating on XML: " + i + ", Node Name: " + nodeName + " with content " + content);
				nodesToUpdate.item(i).setTextContent(content);
			}

		}
	}

	private static void buildXMLRecursive(List<CSVEntry> inputEntries, Document inputDocument) {
		// Populate Aggregated Fields
		CSVEntry aggregatedFields = new CSVEntry();

		// AggregatedEqualFiedls: These fields are always the same (i.e. the same as the first entry)
		CSVEntry inputFirstEntry = inputEntries.get(0);

		for(int i = 0; i < AggregatedEqualFields.size(); i++){
			CSVAttribute equalAttribute = inputFirstEntry.getAttribute(AggregatedEqualFields.get(i));
			aggregatedFields.addAttribute(equalAttribute);
		}

		// AggregatedSumFiedls: These fields require values to be added
		for(int i = 0; i < AggregatedSumFields.size(); i++){
			String sumAttribute = AggregatedSumFields.get(i);
			float sumValue = 0;
			for(int j = 0; j < inputEntries.size(); j++){
				sumValue += Float.parseFloat(inputEntries.get(j).getAttribute(sumAttribute).getValue());
			}
			aggregatedFields.addAttribute(new CSVAttribute(sumAttribute, ""+sumValue));
		}

		// AggregatedCountFiedls: These fields require entries to be counted
		for(int i = 0; i < AggregatedCountFields.size(); i++){
			aggregatedFields.addAttribute(new CSVAttribute(AggregatedCountFields.get(i), ""+inputEntries.size()));
		}

		// Populate the aggregated fields on the document
		writeOnXML(aggregatedFields, inputDocument);


		// Loop through all nodes to be multiplied
		for(int i = 0; i < RecursiveElements.size(); i++){
			String recursiveElementName = RecursiveElements.get(i);

			// Create template document to be used for cloning
			Document recursiveDocTemplate = loadDocument(null);
			// Setup template: create the root element node
	        Element element = recursiveDocTemplate.createElement("root");
	        recursiveDocTemplate.appendChild(element);
	        // Moving base node (to be multiplied) into the template
	        Node parentNode = inputDocument.getElementsByTagName(recursiveElementName).item(0).getParentNode();
			Node recursiveNodeTemplate = recursiveDocTemplate.adoptNode(inputDocument.getElementsByTagName(recursiveElementName).item(0));
			recursiveDocTemplate.getDocumentElement().appendChild(recursiveNodeTemplate);

			// For each input entry: Populate the node (based on template) and append it to the main document
			for(int j = 0; j < inputEntries.size(); j++){
				Document cloneDocument = recursiveDocTemplate; // Clone Document
				writeOnXML(inputEntries.get(j), cloneDocument); // Populate the node within the document
				Node cloneNode = cloneDocument.getElementsByTagName(recursiveElementName).item(0);

				// Import Node filled out back into the main document
				Node importNode = inputDocument.importNode(cloneNode, true);
				parentNode.appendChild(importNode);
			}

		}
		// write the content into xml file
		writeOutputDocument(OutputfilePrefix + ".xml",inputDocument);
	}
}
