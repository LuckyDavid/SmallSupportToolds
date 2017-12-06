package builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import OutputDocuments.CSVAttribute;
import OutputDocuments.CSVEntry;

public class XMLBuilder {

	public static Document baselineDocument;
	public static List<CSVEntry> csvEntries;
	
	public static void main(String[] args) {
		
		// Load baseline document
		loadBaselineDocument("Baseline.xml");
		
		// Load input CSV file
		loadInputCSVFile("Input.csv");
		
		System.out.println("CSVFile loaded successfully");
		
		// Apply transformations
		Iterator<CSVEntry> itr = csvEntries.iterator();
		int outputDocNumber = 1;
		
		// For each entry create a new document
		while(itr.hasNext()){
			CSVEntry entry = itr.next();
			List<CSVAttribute> attributes = entry.getAttributes();
			Iterator<CSVAttribute> itrattr = attributes.iterator();
			
			while(itrattr.hasNext()){
				CSVAttribute attribute = itrattr.next();
				
				System.out.println("Get Column " + attribute.getColumn() + " get value " + attribute.getValue());
				
				// Get the node element by tag name directly
				Node nodeToUpdate = baselineDocument.getElementsByTagName(attribute.getColumn()).item(0);
				nodeToUpdate.setTextContent(attribute.getValue());
				
			}
			
			// write the content into xml file
			writeOutputDocument("OutputDocument" + outputDocNumber + ".xml");
			
			outputDocNumber++;
		}
		
		
		
			
		System.out.println("Done!");
		
	}
	
	private static void loadBaselineDocument(String filepath){
		try{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		baselineDocument = docBuilder.parse(filepath);
		}catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
		}
	}
	
	private static void writeOutputDocument(String outputDocumentName){
		try{
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(baselineDocument);
			StreamResult result = new StreamResult(new File(outputDocumentName));
			transformer.transform(source, result);
		}catch (TransformerException tfe) {
			tfe.printStackTrace();
		} 
	}
	
	private static void loadInputCSVFile(String csvFile){
		String headerOfFile = "";
        String line = "";
        String cvsSplitBy = ";";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

        	//setting up file header
        	headerOfFile = br.readLine();
        	String[] columnsOfFileToCreate = headerOfFile.split(cvsSplitBy);
        	
        	//defining number of attributes for each entry
        	int numberOfAttributes = columnsOfFileToCreate.length;
        	csvEntries = new ArrayList<CSVEntry>();
        	
        	System.out.println("number of entries " + numberOfAttributes);
        	
            while ((line = br.readLine()) != null) {

                // use semicolon as separator
                String[] valuesOfFileToCreate = line.split(cvsSplitBy);
                CSVEntry entry = new CSVEntry();
                for(int i = 0; i<numberOfAttributes; i++){
                	entry.addAttribute(new CSVAttribute(columnsOfFileToCreate[i],valuesOfFileToCreate[i]));
                	System.out.println("[Column " + columnsOfFileToCreate[i] + " , value " + valuesOfFileToCreate[i] + "]");
                }

                csvEntries.add(entry);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	/*
	 * 
		Node idioma = baselineDocument.getElementsByTagName("Idioma").item(0);
		
		idioma.setTextContent("English");
		
	 * 
	 */
}
