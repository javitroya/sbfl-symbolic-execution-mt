package es.us.eii.fault.loc.syvolt.mt.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;




public class InfoAboutContracts {

/**************************** GLOBAL PARAMETERS ********************************/
	
	/**We need the canonical path in order to navigate the subfolders**/
	//final static String executionsCanPath = "models_Family2Company";
	//final static String executionsCanPath = "models_UML2ER";
	final static String executionsCanPath = "models_GM";
	/**This is for setting the name of the csv files that contain the results**/
	final static String Results_File_Name = "suspiciousnessResults.csv";
	
	/**We need the output file of the SyVOLT tool**/
	//final static String syvoltOutput = "20190428_F2P_integration_mutation_testing.xml";
	//final static String syvoltOutput = "20190427-F2P-Unit-mutation_testing.xml";
	
	//final static String syvoltOutput = "20190427_ULM2ER_mutation_testing.xml";
	final static String syvoltOutput = "GM_mutation_testing.xml";
	/**************************** END OF GLOBAL PARAMETERS 
	 * @throws SAXException 
	 * @throws ParserConfigurationException ********************************/

	

	public static void main(String[] args) throws IOException, InterruptedException, ParserConfigurationException, SAXException  {
		long startTime = System.nanoTime();
		
			
		/*We do the analysis requested by Bentley:
		 * Given a transformation, for each contract, collect the percentage of mutants where:
			•	the contract is satisfied (or invalid) on all PCs (i.e., failed=0 and succeed>0)
			•	the contract is not satisfied (or invalid) on all PCs (i.e., failed>0 and succeed=0)
			•	the contract is invalid on all PCs (i.e., failed=0 and succeed=0)
			•	the contract has a mix of satisfied, not satisfied, and invalid on the set of PCs (i.e., failed>0 and succeed>0)
		 */
		//For this, we first obtain a structure with all neded data:
		Map<String,List<List<Integer>>> infoAboutContractsAndMutants = getInfoAboutContractsAndMutants();
		//Then we calculate the percentage ready to be printed:
		Map<String,List<Double>> infoAboutContractsAndMutantsToBePrinted = getInfoAboutContractsAndMutantsToBePrinted(infoAboutContractsAndMutants);
		Iterator<String> iteratorContractsNames = infoAboutContractsAndMutantsToBePrinted.keySet().iterator();
		//And finally, we create a CSV file and write in it all results
		writeInfoAboutCotnractsAndMutants(infoAboutContractsAndMutantsToBePrinted);
		
		long totalTime = System.nanoTime() - startTime;
		System.out.println("\n The whole execution has taken " + TimeUnit.NANOSECONDS.toMillis(totalTime) + " miliseconds.");
	}
	
	/***
	 * 
	 * @param infoAboutContractsAndMutantsToBePrinted: read info of method getInfoAboutContractsAndMutantsToBePrinted() to know what this entry is
	 * This method creates a CSV file called "contractsPercentages" that contains, for each contract, the information about:
	 *  •	percentage of mutants where the contract is satisfied (or invalid) on all PCs (Mutants where the contract has failed=’0’ and succeed>’0’)
		•	percentage of mutants where the contract is not satisfied (or invalid) on all PCs (Mutants where the contract has failed>’0’ and succeed=’0’)
		•	percentage of mutants where the contract is invalid on all PCs (Mutants where the contract has failed=’0’ and succeed=’0’)
		•	percentage of mutants where the contract has a mix of satisfied, not satisfied, and invalid on the set of PCs. (Mutants where the contract has failed>’0’ and succeed>’0’)
	 */
	private static void writeInfoAboutCotnractsAndMutants(
			Map<String, List<Double>> infoAboutContractsAndMutantsToBePrinted) throws IOException {
		//For rounding the results:
		DecimalFormat df = new DecimalFormat("0.000");
		//We create the file where to print the results
		File contractsInfoFile = new File(executionsCanPath + "/results/" + "/contractsPercentages.csv");
		if (!contractsInfoFile.exists())
			contractsInfoFile.createNewFile();
		FileWriter fwContractsInfo = new FileWriter(contractsInfoFile.getAbsoluteFile());
		BufferedWriter bwContractsInfo = new BufferedWriter(fwContractsInfo);
		//We write four rows explaining the data
		bwContractsInfo.write("Case1;Percentage of mutants where the contract is satisfied (or invalid) on all PCs (i.e., failed=0 and succeed>0)\n");
		bwContractsInfo.write("Case2;Percentage of mutants where the contract is not satisfied (or invalid) on all PCs (i.e., failed>0 and succeed=0)\n");
		bwContractsInfo.write("Case3;Percentage of mutants where the contract invalid on all PCs (i.e., failed=0 and succeed=0)\n");
		bwContractsInfo.write("Case4;Percentage of mutants where the contract has a mix of satisfied, not satisfied, and invalid on the set of PCs (i.e., failed>0 and succeed>0)");
		bwContractsInfo.write("\n\nContract;Case1;Case2;Case3;Case4");
		//Now we iterate over the set of keys (contractNames) and write the corresponding info in the file
		//We write one row per contract. Columns represent the contractName and the percentages
		Iterator<String> iteratorContractsNames = infoAboutContractsAndMutantsToBePrinted.keySet().iterator();
		while(iteratorContractsNames.hasNext()){
			String contractName = iteratorContractsNames.next();
			bwContractsInfo.write("\n" + contractName); // System.out.println("CONTRACT: " + contractName);
			bwContractsInfo.write(";" + df.format(infoAboutContractsAndMutantsToBePrinted.get(contractName).get(0)));
			bwContractsInfo.write(";" + df.format(infoAboutContractsAndMutantsToBePrinted.get(contractName).get(1)));
			bwContractsInfo.write(";" + df.format(infoAboutContractsAndMutantsToBePrinted.get(contractName).get(2)));
			bwContractsInfo.write(";" + df.format(infoAboutContractsAndMutantsToBePrinted.get(contractName).get(3)));
		}		
		bwContractsInfo.close();
	}

	/***
	 * 
	 * @param infoAboutContractsAndMutants: read info of method getInfoAboutContractsAndMutants() to know what this entry is
	 * @return It returns a Map<String, List<Double>>, where the key is the name of each contract and the List<Double> is
	 * a List with exactly four values. These four values correspond in this order, to the percentage of mutants where:
	 *  •	the contract is satisfied (or invalid) on all PCs (Mutants where the contract has failed=’0’ and succeed>’0’)
		•	the contract is not satisfied (or invalid) on all PCs (Mutants where the contract has failed>’0’ and succeed=’0’)
		•	the contract is invalid on all PCs (Mutants where the contract has failed=’0’ and succeed=’0’)
		•	the contract has a mix of satisfied, not satisfied, and invalid on the set of PCs. (Mutants where the contract has failed>’0’ and succeed>’0’)
	 */
	private static Map<String, List<Double>> getInfoAboutContractsAndMutantsToBePrinted(
			Map<String, List<List<Integer>>> infoAboutContractsAndMutants) {
		Map<String, List<Double>> result = new HashMap<String, List<Double>>();
		
		Iterator<String> iteratorContractsNames = infoAboutContractsAndMutants.keySet().iterator();
		while(iteratorContractsNames.hasNext()){
			int case1 = 0; //This will sum when, for a mutant, failed=0 and suceed>0
			int case2 = 0; //This will sum when, for a mutant, failed>0 and suceed=0
			int case3 = 0; //This will sum when, for a mutant, failed=0 and suceed=0
			int case4 = 0; //This will sum when, for a mutant, failed>0 and suceed>0
			
			String contractName = iteratorContractsNames.next();
			List<List<Integer>> numPcsSuceedFailedList = infoAboutContractsAndMutants.get(contractName);
			int numMutants = numPcsSuceedFailedList.size();
			//Recall that each list in numPcsSuceedFailedList has 3 values: number of PCs, succeed and failed
			for (List<Integer> numPcsSuceedFailed : numPcsSuceedFailedList){
				int succeed = numPcsSuceedFailed.get(1);
				int failed = numPcsSuceedFailed.get(2);
				if (failed==0){
					if (succeed>0){
						case1++;
					} else if (succeed==0){
						case3++;
					}
				} else if (failed>0){
					if (succeed==0){
						case2++;
					} else if (succeed>0){
						case4++;
					}
				}
			}
			double result1; //This will store, for the present contract, percentage of mutants where failed=0 and suceed>0
			double result2; //This will store, for the present contract, percentage of mutants where failed>0 and suceed=0
			double result3; //This will store, for the present contract, percentage of mutants where failed=0 and suceed=0
			double result4; //This will store, for the present contract, percentage of mutants where failed>0 and suceed>0
			result1 = Double.valueOf(case1) * 100.00 / Double.valueOf(numMutants);
			result2 = Double.valueOf(case2) * 100.00 / Double.valueOf(numMutants);
			result3 = Double.valueOf(case3) * 100.00 / Double.valueOf(numMutants);
			result4 = Double.valueOf(case4) * 100.00 / Double.valueOf(numMutants);
			List<Double> resultTemp = new ArrayList<Double>();
			resultTemp.add(result1);resultTemp.add(result2);resultTemp.add(result3);resultTemp.add(result4);
			result.put(contractName, resultTemp);
		}		
		return result;
	}
	
	/**
	 * 
	 * @return  This method returns a Map<String,List<List<Integer>>>. 
	 * The key is the contract Name. The value is a List of Lists. Each of the inner lists has only three values for the 
	 * contract in a specific mutant:
	 * The first one is the total number of PCs
	 * The second one is the succeed value 
	 * The third one is the failed value 
	 * Each list contains the three values of a specific mutant.
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	public static Map<String,List<List<Integer>>> getInfoAboutContractsAndMutants() throws IOException, ParserConfigurationException, SAXException{
		/*In the following object we are going to keep the data about contracts that we read from the XML file.
		 * The key is the contract Name. The value is a List of Lists. Each of the inner lists has only three values.
		 * The first one is the total number of PCs, the second one is the succeed value and the third one is the failed value. 
		 * Each list contains the three values of a specific mutant.
		 */
		Map<String,List<List<Integer>>> result = new HashMap<String, List<List<Integer>>>();
		
		//We are reading a XML file: https://howtodoinjava.com/xml/read-xml-dom-parser-example/
		//https://stackoverflow.com/questions/6604876/parsing-xml-with-nodelist-and-documentbuilder
		File file = new File(executionsCanPath + "/" + syvoltOutput);
		//Get Document Builder
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		//Build Document
		Document document = documentBuilder.parse(file);
		//Normalize the XML Structure; It's just too important !!
		document.getDocumentElement().normalize();
		//Here comes the root node
		Element root = document.getDocumentElement();
		//System.out.println(root.getNodeName());
		
		//Let's take all contracts names first:
		List<String> contractNames = getContractsNames();
		//Now we initialize the contractsData structure:
		for (String s : contractNames){
			List<List<Integer>> initializeList = new ArrayList<List<Integer>>();
			result.put(s, initializeList);
		}
		
		//Let's take all <contract_satisfaction> blocks and store them in a NodeList
		NodeList nodeListContractSatisfaction = document.getElementsByTagName("contract_satisfaction");
		/*Let's iterate over them. Each of them represents the data of a specific mutant. Besides, each of them
		 * has one entry per contract (name, succeed, failed)
		 */
		for (int i=0; i<nodeListContractSatisfaction.getLength(); i++){
			Node nodeContractSatisfaction = nodeListContractSatisfaction.item(i);
			//Let's get first the number of PCs in case we need them later:
			int numPCs = Integer.parseInt(nodeContractSatisfaction.getAttributes().getNamedItem("num_pcs").getNodeValue());
			//We need to transform the Node to an Element in order to access its inner nodes:
			Element elementContractSatisfaction = (Element) nodeContractSatisfaction;
			//Now we access all contracts in the contract_satisfaction
			NodeList nodeListContract = elementContractSatisfaction.getElementsByTagName("contract");
			//And we iterate them in order to add them in the contractsData object:
			for (int j=0; j<nodeListContract.getLength(); j++){
				List<Integer> succeedFailedValues = new ArrayList<Integer>();
				Node nodeContract = nodeListContract.item(j);
				String contractName = nodeContract.getAttributes().getNamedItem("name").getNodeValue();
				succeedFailedValues.add(numPCs);
				succeedFailedValues.add(Integer.parseInt(nodeContract.getAttributes().getNamedItem("succeed").getNodeValue()));
				succeedFailedValues.add(Integer.parseInt(nodeContract.getAttributes().getNamedItem("failed").getNodeValue()));
				//Finally, we add the three values to our object contractsData:
				List<List<Integer>> tempList = result.get(contractName);
				tempList.add(succeedFailedValues);
				result.put(contractName,  tempList);
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @return  This method returns a list with the names of the contracts.
	 * Such names are taken from the file with the output of the SyVOLT tool. Specifically, and since such
	 * file is an xml where the name of the contracts are in <contract> tag within many <contract_satisfaction>.
	 * We take the first <contract_satisfaction> block and from it, we take the names of the contracts
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	public static List<String >getContractsNames() throws IOException, ParserConfigurationException, SAXException{
		List<String> result = new ArrayList<String>();
		
		//We are reading a XML file: https://howtodoinjava.com/xml/read-xml-dom-parser-example/
		//https://stackoverflow.com/questions/6604876/parsing-xml-with-nodelist-and-documentbuilder
		File file = new File(executionsCanPath + "/" + syvoltOutput);
		//Get Document Builder
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		//Build Document
		Document document = documentBuilder.parse(file);
		//Normalize the XML Structure; It's just too important !!
		document.getDocumentElement().normalize();
		//Here comes the root node
		Element root = document.getDocumentElement();
		//System.out.println(root.getNodeName());
			
		//We take all contracts_satisfaction blocks:
		NodeList nodeListContractSatisfaction = document.getElementsByTagName("contract_satisfaction");
		//We print how many blocks there are
		System.out.println(nodeListContractSatisfaction.getLength());
		//We take only the first <contract_satisfaction> block
		Node nodeContractSatisfaction = nodeListContractSatisfaction.item(0);
		//We need to convert it to an Element in order to navigate it
		Element elementContractSatisfaction = (Element) nodeContractSatisfaction;
		//Now we get a NodeList with all <contract> inside <contract_satisfaction>
		NodeList nodeListContract = elementContractSatisfaction.getElementsByTagName("contract");
		//Let's print the contract names and store it in the array:
		for (int i=0; i<nodeListContract.getLength(); i++){
			String contractName = nodeListContract.item(i).getAttributes().getNamedItem("name").getNodeValue();
			System.out.println(contractName);
			result.add(contractName);
		}

		return result;
	}
}

