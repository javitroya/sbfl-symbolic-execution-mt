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

public class FaultLocalizationMT_Main {

/**************************** GLOBAL PARAMETERS ********************************/
	
/* In the block of lines XX-YY, comment them all and uncomment the following lines depending on the case study to be run:
 * UML2ER with unit-test contracts: lines 51 and 62
 * UML2ER with integration contracts: lines 52 and 63
 * RSS2ATOM with unit-test contracts: lines 53 and 64
 * UML2Kiltera with unit-test contracts (you may have a "java.lang.OutOfMemoryError: Java heap space" if your computer's memory is not enough): lines 54 and 65
 * UML2Kiltera with integration contracts (you may have a "java.lang.OutOfMemoryError: Java heap space" if your computer's memory is not enough): lines 55 and 66
 * GM2Autosar with unit-test contracts: lines 56 and 67
 * GM2Autosar with integration contracts: lines 57 and 68
 * Families2Persons with unit-test contracts: lines 58 and 69
 * Families2Persons with integration contracts: lines 59 and 70
 */
	
	/**We need the canonical path in order to navigate the subfolders**/
	final static String executionsCanPath = "UML2ER_utc"; //Case study: UML2ER unit-test contracts
//	final static String executionsCanPath = "UML2ER_ic"; //Case study: UML2ER integration contracts
//	final static String executionsCanPath = "RSS2ATOM_utc"; //Case study: RSS2ATOM unit-test contracts
//	final static String executionsCanPath = "UML2Kiltera_utc"; //Case study: UML2Kiltera unit-test contracts
//	final static String executionsCanPath = "UML2Kiltera_ic"; //Case study: UML2Kiltera integration contracts
//	final static String executionsCanPath = "GM2Autosar_utc"; //Case study: GM2Autosar unit-test contracts
//	final static String executionsCanPath = "GM2Autosar_ic"; //Case study: GM2Autosar integration contracts
//	final static String executionsCanPath = "F2P_utc"; //Case study: Families2Persons unit-test contracts
//	final static String executionsCanPath = "F2P_ic"; //Case study: Families2Persons integration contracts
	
	/**We need the output file of the SyVOLT tool**/
	final static String syvoltOutput = "UML2ERunit_mutation_testing.xml"; //Case study: UML2ER unit-test contracts
//	final static String syvoltOutput = "UML2ER_mutation_testing.xml"; //Case study: UML2ER integration contracts
//	final static String syvoltOutput = "RSSunit_mutation_testing.xml"; //Case study: RSS2ATOM unit-test contracts
//	final static String syvoltOutput = "Kilteraunit_mutation_testing.xml"; //Case study: UML2Kiltera unit-test contracts. This file was too large for upload. It can be found at: https://drive.google.com/open?id=1u8IGL8sGZjs16zKg57JXoLt-2gLKX8zM 
//	final static String syvoltOutput = "Kiltera_mutation_testing.xml"; //Case study: UML2Kiltera integration contracts. This file was too large for upload. It can be found at: https://drive.google.com/open?id=1wSqx2EbQAHIZ4Hw-GIl4aLOnq7H6gTo7 
//	final static String syvoltOutput = "GMunit_mutation_testing.xml"; //Case study: GM2Autosar unit-test contracts
//	final static String syvoltOutput = "GM_mutation_testing.xml"; //Case study: GM2Autosar integration contracts
//	final static String syvoltOutput = "F2Punit_mutation_testing.xml"; //Case study: Families2Persons unit-test contracts. This file was too large for upload. It can be found at: https://drive.google.com/open?id=1AkYgTj6xPDrG5gRBBQYmwJjE0WT3VmF3
//	final static String syvoltOutput = "F2P_mutation_testing.xml"; //Case study: Families2Persons integration contracts. This file was too large for upload. It can be found at: https://drive.google.com/open?id=13SbuCx2_ue5QDeJKZ7opC3fuL91lfrso
	
	/**This is for setting the name of the csv files that contain the results**/
	final static String Results_File_Name = "suspiciousnessResults.csv";
	/**************************** END OF GLOBAL PARAMETERS 
	 * @throws SAXException 
	 * @throws ParserConfigurationException ********************************/

	

	public static void main(String[] args) throws IOException, InterruptedException, ParserConfigurationException, SAXException  {
		long startTime = System.nanoTime();
		
		List<String> rulesNames = new ArrayList<String>(); //In this list we will store the names of the rules of the MT
		List<String> contractsNames = new ArrayList<String>(); //In this list we will store the names of the contracts
		List<int[]> listResultVector = new ArrayList<int[]>(); //In this list we will keep all the result vectors
		/*The following is the structure where we store the result vectors. It will have, for each constraint (key of the map),
		 * a list with as many strings as PCs. For each of them, we'll have 'S' if the condition is satisfied,
		 * 'F' if it's not satisfied, and 'U' if we don't have such information
		 */
		Map<String, List<String>> resultVectors = new HashMap<String, List<String>>();
		Map<String, int[]> rulesExecutions = new HashMap<String, int[]>();//See description below
		/* This is a HashMap with the name of a rule and an array with the number of executions of such rule in each PC. 
		 * Map<RuleName, List[Executions in Scenario1, Executions in Scenario2,...]>. Example:
		 * {"Rule1", [1,2,0,3,4]}, {"Rule2", [2,0,4,3,1]}, ...
		 */
		
		/*We create three files for storing the summary of the results in the BC, WC and AC scenarios.
		 * This is for putting all results together in the same CSV files for later processing
		 */
		File resultBC = new File(executionsCanPath + "/results/" + "/results_BC.csv");
		if (!resultBC.exists())
			resultBC.createNewFile();
		FileWriter fwBC = new FileWriter(resultBC.getAbsoluteFile());
		BufferedWriter bwBC = new BufferedWriter(fwBC);
		//We write the first row with the names of the 18 techniques
		bwBC.write(";Cohen;Braun-Banquet;Simple Matching;Kulcynski2;Barinel;Arithmetic Mean;Mountford;Zoltar;Ochiai;Phi;Op2;Russel Rao;Baroni-Urbani & Buser;Pierce;Ochiai2;Rogers & Tanimoto;DStar;Tarantula");
		//We complete the first row with information about the PCs
		bwBC.write(";;Num PCs;Sat;NonSat;Und;Percent-Und");
		bwBC.write("\n");
		File resultWC = new File(executionsCanPath + "/results/" + "/results_WC.csv");
		if (!resultWC.exists())
			resultWC.createNewFile();
		FileWriter fwWC = new FileWriter(resultWC.getAbsoluteFile());
		BufferedWriter bwWC = new BufferedWriter(fwWC);		
		//We write the first row with the names of the 18 techniques
		bwWC.write(";Cohen;Braun-Banquet;Simple Matching;Kulcynski2;Barinel;Arithmetic Mean;Mountford;Zoltar;Ochiai;Phi;Op2;Russel Rao;Baroni-Urbani & Buser;Pierce;Ochiai2;Rogers & Tanimoto;DStar;Tarantula");
		//We complete the first row with information about the PCs
		bwWC.write(";;Num PCs;Sat;NonSat;Und;Percent-Und");
		bwWC.write("\n");
		File resultAC = new File(executionsCanPath + "/results/" + "/results_AC.csv");
		if (!resultAC.exists())
			resultAC.createNewFile();
		FileWriter fwAC = new FileWriter(resultAC.getAbsoluteFile());
		BufferedWriter bwAC = new BufferedWriter(fwAC);
		//We write the first row with the names of the 18 techniques
		bwAC.write(";Cohen;Braun-Banquet;Simple Matching;Kulcynski2;Barinel;Arithmetic Mean;Mountford;Zoltar;Ochiai;Phi;Op2;Russel Rao;Baroni-Urbani & Buser;Pierce;Ochiai2;Rogers & Tanimoto;DStar;Tarantula");
		//We complete the first row with information about the PCs
		bwAC.write(";;Num PCs;Sat;NonSat;Und;Percent-Und");
		bwAC.write("\n");
		
		System.out.println("let's start");
		/**Let us save in rulesNames the names of all the rules of the MT **/
		rulesNames = getRulesNames();
		System.out.println("Names of the rules: " + rulesNames);
		/**Let us save in contractsNames the names of all the contracts of the MT **/
		contractsNames = getContractsNames();
		System.out.println("Names of the contracts: " + contractsNames);
		/**So far, we have in rulesNames the names of the rules. We obtained them from the first <rules> block of the .xml file.
		 * Same for the contracts names**/
		
		/**From here, we are going to do the same for all experiments in the .xml file. By experiment we mean each of the
		 * mutants. In the .xml file, mutants are organized in <mutation_set>. Each <mutation_set> contains different mutants
		 * where in all the same rule is mutated. Therefore, for the results, we are going to obtain results for each pair
		 * of [Rule mutated, specific mutation]. The information of the rule mutated is in the <mutation_set> (rule_name)
		 * and the information of the specific mutation is in the <mutation> (operation).
		 * Therefore, we need to iterate each <mutation_set> and, inside, each <mutation> 
		 */
		
		//The following variable is a NodeList with all <mutation_set>
		NodeList nodeListMutationSet = getNodeListMutationSet();
		//Let's do the iteration. We iterate first over <mutation_set>
		for (int i=0; i<nodeListMutationSet.getLength(); i++){
			String ruleMutated = nodeListMutationSet.item(i).getAttributes().getNamedItem("rule_name").getNodeValue();
			//Now we iterate over <mutation>
			Node nodeMutationSet = nodeListMutationSet.item(i);
			Element elementMutationSet = (Element) nodeMutationSet;
			NodeList nodeListMutation = elementMutationSet.getElementsByTagName("mutation");
			for (int k=0; k<nodeListMutation.getLength(); k++){
				String mutationOperation = nodeListMutation.item(k).getAttributes().getNamedItem("operation").getNodeValue();
				Node nodeMutation = nodeListMutation.item(k);
				/*Despite there is a "num_pcs" field in the <contract_satisfaction> of the .xml, I prefer to compute
				it with this method, since I think that number is not always correct*/
				int numPCs = getNumberPCs(nodeMutation);
				
				/**So far, we have the name of the rule mutated in "ruleMutated" and the mutation operation in "mutationOperation"
				 * From here on, everything we do is for each <mutation>
				 */
				
				/**This is for storing the number of PCs where each contract is (i) Satisfied, (ii) Not Satisfied and (iii) Undefined.
				 * For this, we create a Map<String, Int[]>. The key is the contract name and the array contains,
				 * in this order, [0] num contracts satisfied, [1] num contracts not satisfied, [2] num contracts undefined	 */
				Map<String, int[]> contractsSatisfaction = getContractsSatisfaction(nodeMutation, numPCs);				
				
				/**Let us store the number of time each rule is executed for each PC**/
				rulesExecutions = getRulesExecutions(rulesNames, nodeMutation, numPCs);
				///////////////Simply for printing in the console:
				//System.out.println("Rules executions: " + rulesExecutions);
				
				Iterator<String> it = rulesExecutions.keySet().iterator();
				//System.out.println("***In the following it is printed, for each rule, how many times it is executed in each of the " + numPCs + " executions (the number might not appear, but it's there***");
				while (it.hasNext()){
					String next = it.next();
					int[] executions = rulesExecutions.get(next);
//					System.out.print("\nRule: " + next + ", number of executions: ");
					for (int n : executions){
//						System.out.print(n + " ");
					}
				}
				//System.out.println("\n");	
				
				/** For each contract, we store a map where, for each PC, we write "F" if the constraint fails, "S" if it does not fail and "U" if 
				 * it does not appear in the PC
				 */
				resultVectors = getResultVectors(contractsNames, nodeMutation, numPCs);
				
				//System.out.println("RESULT VECTORS " + resultVectors);
				
				///////////////Simply for printing in the console:
				//System.out.println("***For each of the " + numPCs + " executions, the following displays, for each OCL constraint, a '1' if the constraint fails and a '0' if it does not***");
				//System.out.println("Please note, when no OCL constraint fails, the suspiciousness results will be 0 or NaN");
//				int k1 = 1;
//				for (int[] array : listResultVector){
//					System.out.print("OCL" + k1 + ": ");
//					for (int n : array){
//						System.out.print(n);
//					}
//					k1++;
//				}
				/////////////
				
				 /**Here we calculate the different values for the formulas: Ncf, Nuf, Ncs, etc **/		
				 /* For each Contract, we will keep for each rule a value for each variable. For example:
				  * "Contract1": {"Rule1", {"Ncf", 9}, {"Nuf", 0}, {"Ncs", 5}, ...}, {"Rule2", {"Ncf", 3}, {"Nuf", 5}, {"Ncs", 4}, ...}
				 *  "Contract2": {"Rule1", {"Ncf", 4}, {"Nuf", 1}, {"Ncs", 2}, ...}, {"Rule2", {"Ncf", 3}, {"Nuf", 5}, {"Ncs", 4}, ...}
				 */
				 Map<String, Map<String, Map<String, Double>>> measures = getMeasures(resultVectors, rulesNames, rulesExecutions);
						
				///////////////Simply for printing in the console:	
//				System.out.println("\n\n***The following displays the structure where, for each contract, it stores the metrics collected for each rule***");
//				System.out.println("Measures in the mutation " + ruleMutated + "<->" + mutationOperation + ": " + measures + "\n");
//				for(String contract : measures.keySet()){
//					System.out.println(contract + ": " + measures.get(contract));
//				}
				/////////////
				
				/**For each contract, we have to calculate the suspiciousness of each rule with the different formulae**/
				/* Contract1"={"Rule1", 0.9345}, {"Rule2", 0.123},...
				 * "Contract2"={"Rule1", 0.445}, {"Rule2", 0.001},...
				 */
				Map<String,Map<String, Double>> suspTarantula = getSuspiciousness(measures, "tarantula");
				Map<String,Map<String, Double>> suspOchiai = getSuspiciousness(measures, "ochiai");
				Map<String,Map<String, Double>> suspOchiai2 = getSuspiciousness(measures, "ochiai2");
				Map<String,Map<String, Double>> suspBraunBanquet = getSuspiciousness(measures, "braunbanquet");
				Map<String,Map<String, Double>> suspMountford = getSuspiciousness(measures, "mountford");
				Map<String,Map<String, Double>> suspArithmeticMean = getSuspiciousness(measures, "arithmeticmean");
				Map<String,Map<String, Double>> suspZoltar = getSuspiciousness(measures, "zoltar");
				Map<String,Map<String, Double>> suspSimpleMatching = getSuspiciousness(measures, "simplematching");
				Map<String,Map<String, Double>> suspRusselRao = getSuspiciousness(measures, "russelrao");
				Map<String,Map<String, Double>> suspKulcynski2 = getSuspiciousness(measures, "kulcynski2");
				Map<String,Map<String, Double>> suspCohen = getSuspiciousness(measures, "cohen");
				Map<String,Map<String, Double>> suspPierce = getSuspiciousness(measures, "pierce");
				Map<String,Map<String, Double>> suspBaroniEtAl = getSuspiciousness(measures, "baronietal");
				Map<String,Map<String, Double>> suspPhi = getSuspiciousness(measures, "phi");
				Map<String,Map<String, Double>> suspRogersTanimoto = getSuspiciousness(measures, "rogerstanimoto");
				Map<String,Map<String, Double>> suspOp2 = getSuspiciousness(measures, "op2");
				Map<String,Map<String, Double>> suspBarinel = getSuspiciousness(measures, "barinel");
				Map<String,Map<String, Double>> suspDStar = getSuspiciousness(measures, "DStar");
				
				/**We create an structure where to put all the measurements with the different techniques together**/
				/**Those added in the following are those that will appear in the results**/
				Map<String,Map<String,Map<String, Double>>> allSuspiciousness = new HashMap<String,Map<String,Map<String, Double>>>();
				allSuspiciousness.put("Tarantula", suspTarantula);
				allSuspiciousness.put("Ochiai", suspOchiai);
				allSuspiciousness.put("Ochiai2", suspOchiai2);
				allSuspiciousness.put("Braun-Banquet", suspBraunBanquet);
				allSuspiciousness.put("Mountford", suspMountford);
				allSuspiciousness.put("Arithmetic Mean", suspArithmeticMean);
				allSuspiciousness.put("Zoltar", suspZoltar);
				allSuspiciousness.put("Simple Matching", suspSimpleMatching);
				allSuspiciousness.put("Russel Rao", suspRusselRao);
				allSuspiciousness.put("Kulcynski2", suspKulcynski2);
				allSuspiciousness.put("Cohen", suspCohen);
				allSuspiciousness.put("Pierce", suspPierce);
				allSuspiciousness.put("Baroni-Urbani & Buser", suspBaroniEtAl);
				allSuspiciousness.put("Phi", suspPhi);
				allSuspiciousness.put("Rogers & Tanimoto", suspRogersTanimoto);
				allSuspiciousness.put("Op2", suspOp2);
				allSuspiciousness.put("Barinel", suspBarinel);
				allSuspiciousness.put("DStar", suspDStar);
				
				/////////////Simply for printing in the console:	
//				System.out.println("\nThe structure where the results for the 'Tarantula' method are saved is: " + suspTarantula);
//				System.out.println("The structure where the results for the 'Ochiai' method are saved is: " + suspOchiai);
//				//System.out.println("The structure where the results for the 'Ochiai2' method are saved is: " + suspOchiai2);
//				//System.out.println("The structure where the results for the 'Braun-Banquet' method are saved is: " + suspBraunBanquet);
//				System.out.println("The structure where the results for the 'Mountford' method are saved is: " + suspMountford);
				///////////
				
				
				
				
				/**Finally, we print the results of the suspiciousness in several CSV files**/
				if (printResultsInCSVBooks(resultVectors, allSuspiciousness, measures, rulesExecutions,ruleMutated,mutationOperation,numPCs,contractsSatisfaction,bwBC,bwWC,bwAC)){
					String mutationID = ruleMutated + "--" + mutationOperation; //This is the ID for the specific mutation
					System.out.println("\nThe results have been generated successfully for mutation ||  " + mutationID + " ||");
					System.out.println("A summary of the results can be found in " + executionsCanPath + "/results" + "/results_" + mutationID + "/" + mutationID + "--" + Results_File_Name);
					System.out.println("Also, a file for each non satisfied contract with coverage matrix, error vector and suspiciousness-based rankings is generated in " + executionsCanPath + "results" + "/results_" + mutationID + "/" + mutationID + "--<contratcName>.csv");	
				}
			}
		}
		bwBC.close();
		bwWC.close();
		bwAC.close();
		
		/*After the analysis for suspiciousness values, we also do the analysis requested by Bentley:
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
		
		System.out.println("\n\n+++++++++++++++++++++++++++++++++++++ THE EXECUTION HAS TERMINATED ++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("Apart from all the outputs described above, the following files have been generated:\n");
		System.out.println(executionsCanPath + "/results_AC.csv: EXAM scores values for all mutants and all 18 techniques in the average-case scenario");
		System.out.println(executionsCanPath + "/results_BC.csv: EXAM scores values for all mutants and all 18 techniques in the best-case scenario");
		System.out.println(executionsCanPath + "/results_WC.csv: EXAM scores values for all mutants and all 18 techniques in the worst-case scenario");
		System.out.println(executionsCanPath + "/contractsPercentages.csv: Statistics of the contracts according to the classification given in the paper");
		
		long totalTime = System.nanoTime() - startTime;
		System.out.println("\n The whole execution has taken " + TimeUnit.NANOSECONDS.toMillis(totalTime) + " miliseconds.");
		
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
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
	 * @param nodeMutation is a Node that contains the <mutation> structure
	 * @param numPCs is the number of PCs in this <mutation>
	 * @return It returns the number of PCs where each contract is (i) Satisfied, (ii) Not Satisfied and (iii) Undefined.
	 *	 The result is a Map<String, int[]>. The key is the contract name and the array contains, in this order:
	 *		 [0] num contracts satisfied 
	 *		 [1] num contracts not satisfied 
	 *		 [2] num contracts undefined
	 *
	 */
	private static Map<String, int[]> getContractsSatisfaction(Node nodeMutation, int numPCs) {
		Map<String, int[]> result = new HashMap<String, int[]>();
		Element elementMutation = (Element) nodeMutation;
		NodeList nodeListContractSatisfaction = elementMutation.getElementsByTagName("contract_satisfaction");
		Node nodeContractSatisfaction = nodeListContractSatisfaction.item(0); //There is only one
		//int numPCs = Integer.parseInt(nodeContractSatisfaction.getAttributes().getNamedItem("num_pcs").getNodeValue());

		Element elementContractSatisfaction = (Element) nodeContractSatisfaction;
		NodeList nodeListContract = elementContractSatisfaction.getElementsByTagName("contract");
		for (int i=0; i<nodeListContract.getLength(); i++){
			String contractName = nodeListContract.item(i).getAttributes().getNamedItem("name").getNodeValue();
			int[] value = new int[3];
			int satisfied = Integer.parseInt(nodeListContract.item(i).getAttributes().getNamedItem("succeed").getNodeValue());
			int notSatisfied = Integer.parseInt(nodeListContract.item(i).getAttributes().getNamedItem("failed").getNodeValue());
			int undefined = numPCs - satisfied - notSatisfied;
			value[0] = satisfied; value[1] = notSatisfied; value[2] = undefined;
			result.put(contractName, value);
		}		
		return result;
	}

	/**
	 * @param nodeMutation is a Node that contains the <mutation> structure
	 * @return this method returns the number of <PC> that we have in the block <rules_for_each_path_condition> in 
	 * the Node <mutation> received as parameter
	 */
	private static int getNumberPCs(Node nodeMutation) {
		Element elementMutation = (Element) nodeMutation;
		NodeList nodeListRulesForEachPathCondition = elementMutation.getElementsByTagName("rules_for_each_path_condition");
		Node nodeRulesForEachPathCondition = nodeListRulesForEachPathCondition.item(0); //There is only one
		Element elementRulesForEachPathCondition = (Element) nodeRulesForEachPathCondition;
		NodeList nodeListPC = elementRulesForEachPathCondition.getElementsByTagName("PC");
		int numPC = nodeListPC.getLength();
		return numPC;
	}


	/**
	 * @return  This method returns the NodeList with all <mutation_set>
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	public static NodeList getNodeListMutationSet() throws ParserConfigurationException, SAXException, IOException{
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
		
		//Let's take all <mutation_set> blocks and store in a NodeList. This is to be returned
		NodeList nodeListMutationSet = document.getElementsByTagName("mutation_set");
		
		return nodeListMutationSet;
	}
	
	/**
	 * 
	 * @return  This method returns a list with the names of the rules of a model transformation (MT).
	 * Such names are taken from the file with the output of the SyVOLT tool. Specifically, and since such
	 * file is an xml where the name of the rules are in <rule> tag within many <rules> tag where all show the names of the rules,
	 * we take the first <rules> block and from it, we take the names of the rules
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	public static List<String >getRulesNames() throws IOException, ParserConfigurationException, SAXException{
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
		
		//Let's take all <rules> blocks and store in a NodeList
		NodeList nodeListRules = document.getElementsByTagName("rules");
		//Let's leave only the first <rules> block in a Node
		Node nodeRules = nodeListRules.item(0);
		//We need to convert it to an Element in order to navigate it
		Element elementRules = (Element) nodeRules;
		//Now let's get a NodeList with all <rule> inside <rules>
		NodeList nodeListRule = elementRules.getElementsByTagName("rule");
		//Let's print the rule names:
		for (int i=0; i<nodeListRule.getLength(); i++){
			String ruleName = nodeListRule.item(i).getAttributes().getNamedItem("name").getNodeValue();
//			System.out.println(ruleName);
			result.add(ruleName);
		}
		
		/******This is a sample code to print all contract names from all contracts blocks*******/		
//		//We take all contracts blocks:
//		NodeList nodeListContracts = document.getElementsByTagName("contracts");
//		//We print how many block there are
//		System.out.println(nodeListContracts.getLength());
//		
//		for (int k=0; k<nodeListContracts.getLength();k++){
//			//We take each node contracts
//			Node nodeContracts = nodeListContracts.item(k);
//			//We need to cast it to an element contracts
//			Element elementContracts = (Element) nodeContracts;
//			//Now we get nodeList of contract inside contracts:
//			NodeList nodeListContract = elementContracts.getElementsByTagName("contract");
//			//Now let's print everything:
//			System.out.println("We are in the contracts in position " + k);
//			for (int j=0; j<nodeListContract.getLength(); j++)
//				System.out.println(nodeListContract.item(j).getAttributes().getNamedItem("name").getNodeValue());
//		}
		/******************************** End of sample code ********************************************/

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
//		System.out.println(nodeListContractSatisfaction.getLength());
		//We take only the first <contract_satisfaction> block
		Node nodeContractSatisfaction = nodeListContractSatisfaction.item(0);
		//We need to convert it to an Element in order to navigate it
		Element elementContractSatisfaction = (Element) nodeContractSatisfaction;
		//Now we get a NodeList with all <contract> inside <contract_satisfaction>
		NodeList nodeListContract = elementContractSatisfaction.getElementsByTagName("contract");
		//Let's print the contract names and store it in the array:
		for (int i=0; i<nodeListContract.getLength(); i++){
			String contractName = nodeListContract.item(i).getAttributes().getNamedItem("name").getNodeValue();
//			System.out.println(contractName);
			result.add(contractName);
		}

		return result;
	}
	
	/**
	 * 
	 * @param rulesNames is a list with the names of the rules of the MT
	 * @param nodeMutation is the Node that represents the corresponding <mutation> block
	 * @param numPCs indicates the number of <PC> that we have in the <rules_for_each_path_condition> of the <mutation> block received as parameter
	 * @return It returns a Map where the key is the name of the rule and the value is an array with the number of executions of such rule in each path. 
	 * Map<RuleName, List[Executions in path1, Executions in path2,...]>. Example:
	 * {"Rule1", [1,2,0,3,4]}, {"Rule2", [2,0,4,3,1]}, ...
	 * @throws IOException 
	 */
	@SuppressWarnings("null")
	public static Map<String, int[]> getRulesExecutions(List<String> rulesNames, Node nodeMutation, int numPCs) throws IOException{
		Map<String, int[]> result = new HashMap<String, int[]>();
		/* The next two variables are for storing name of path plus rules executed, in the same order */
		List<String> allPaths = new ArrayList<String>();
		List<List<String>> allRules = new ArrayList<List<String>>();
		Map<String, List<String>> pathsRules = new HashMap<String, List<String>>();
		
		//We initialize the Map and set 0 executions for each rule in each scenario
		// e.g.: {"Rule1", [0,0,0,..]}, {"Rule2", [0,0,0,...]}...
		for (String ruleName: rulesNames) {
			int[] initializeExecutions = new int[(int) numPCs];
			for (int i=0; i<numPCs;i++) initializeExecutions[i]=0;
			result.put(ruleName, initializeExecutions);
		}

//		//Let's first access the block <rules_for_each_path_condition>, because we need to access each PC
		Element elementMutation = (Element) nodeMutation;
		NodeList nodeListRulesForEachPathCondition = elementMutation.getElementsByTagName("rules_for_each_path_condition");
		Node nodeRulesForEachPathCondition = nodeListRulesForEachPathCondition.item(0); //There is only one
		Element elementRulesForEachPathCondition = (Element) nodeRulesForEachPathCondition;
		NodeList nodeListPC = elementRulesForEachPathCondition.getElementsByTagName("PC");
		
		/*We use paths and rules to store all rules that appear in each path, since both lists have the same order, such as:
		 * [PC0, PC1, PC2...]
		 * [[Rule1, Rule3, Rule7], [Rule2, Rule3, Rule4], [Rule3, Rule9, Rule10]]
		 * This eases the next step
		 */
		for (int j=0; j<nodeListPC.getLength(); j++){
			String pathName = "PC" + nodeListPC.item(j).getAttributes().getNamedItem("num").getNodeValue();
			String rules = nodeListPC.item(j).getAttributes().getNamedItem("rules").getNodeValue();
			String[] rulesArray = rules.split(",");
			allPaths.add(pathName);
			allRules.add(Arrays.asList(rulesArray));
		}

		for (String ruleName: rulesNames){
			//Now we use the paths and rules objects for preparing the result
			List<Integer> rulesOccurrences = new ArrayList<Integer>();
			for (String path : allPaths){
				Integer count = 0;
				List<String> rules = allRules.get(allPaths.indexOf(path)); //List with all the rules of the path
				for (String rule : rules){
					if (rule.equals(ruleName)) count++;
				}
				rulesOccurrences.add(count);
			}
			//Convert List<Integer> to int[]
			int[] rulesOccurrencesArray = new int[rulesOccurrences.size()];
		      for(int i=0;i<rulesOccurrencesArray.length;i++) {
		    	  rulesOccurrencesArray[i] = rulesOccurrences.get(i);
		      }
			result.put(ruleName, rulesOccurrencesArray); 
		}
		return result;
	}
	
	/**
	 * 
	 * @param rulesNames is a list with the names of the rules of the MT
	 * @param numPCs is the number of path conditions we have in the file
	 * @return It returns a Map where the key is the name of the rule and the value is an array with the number of executions of such rule in each path. 
	 * Map<RuleName, List[Executions in path1, Executions in path2,...]>. Example:
	 * {"Rule1", [1,2,0,3,4]}, {"Rule2", [2,0,4,3,1]}, ...
	 * @throws IOException 
	 */
	@SuppressWarnings("null")
	public static Map<String, int[]> getRulesExecutionsOld(List<String> rulesNames, int numPCs) throws IOException{
		Map<String, int[]> result = new HashMap<String, int[]>();
		/* The next two variables are for storing name of path plus rules executed, in the same order */
		List<String> allPaths = new ArrayList<String>();
		List<List<String>> allRules = new ArrayList<List<String>>();
		Map<String, List<String>> pathsRules = new HashMap<String, List<String>>();
		//We initialize the Map and set 0 executions for each rule in each scenario
		// e.g.: {"Rule1", [0,0,0,..]}, {"Rule2", [0,0,0,...]}...
		for (String ruleName: rulesNames) {
			int[] initializeExecutions = new int[(int) numPCs];
			for (int i=0; i<numPCs;i++) initializeExecutions[i]=0;
			result.put(ruleName, initializeExecutions);
		}
		
		//We read the output SyVOLT file until we find "Rules in each path condition:"
		FileReader reader = new FileReader(executionsCanPath + "/" + syvoltOutput);
		BufferedReader br = new BufferedReader(reader);		
		String line = br.readLine();
		while (!line.equals("Rules in each path condition:")){ //Let's search for the line containing "Rules:"
			line = br.readLine();
		}
		
		/*We use paths and rules to store all rules that appear in each path, since both lists have the same order, such as:
		 * [PC0, PC1, PC2...]
		 * [[Rule1, Rule3, Rule7], [Rule2, Rule3, Rule4], [Rule3, Rule9, Rule10]]
		 * This eases the next step
		 */
		line = br.readLine();
		while (line != null && !line.equals("")){
			String[] str = line.split(":",2);
			String path = str[0]; //This contains the path name
			String rules = str[1].substring(1); //This contains all rules separated by comma
			String[] rulesArray = rules.split(",");
			allPaths.add(path);
			allRules.add(Arrays.asList(rulesArray));
			line = br.readLine();
		}
		
		for (String ruleName: rulesNames){
			//Now we use the paths and rules objects for preparing the result
			List<Integer> rulesOccurrences = new ArrayList<Integer>();
			for (String path : allPaths){
				Integer count = 0;
				List<String> rules = allRules.get(allPaths.indexOf(path)); //List with all the rules of the path
				for (String rule : rules){
					if (rule.equals(ruleName)) count++;
				}
				rulesOccurrences.add(count);
			}
			//Convert List<Integer> to int[]
			int[] rulesOccurrencesArray = new int[rulesOccurrences.size()];
		      for(int i=0;i<rulesOccurrencesArray.length;i++) {
		    	  rulesOccurrencesArray[i] = rulesOccurrences.get(i);
		      }
			result.put(ruleName, rulesOccurrencesArray); 
		}

		return result;
	}
	
	/**
	 * @param contractsNames is a list with the names of the contracts
	 * @param nodeMutation is the Node that represents the corresponding <mutation> block
	 * @param numPCs is the number of paths conditions that we have for the MT in the <mutation block> 
	 * @return It is a map that for each contract that we have, we store a list, for each path, we 
	 * write 'F' if the constraint fails, 'S' if it does not fail and 'U' if we do not have such information. 
	 * E.g: {"Contract01", ["S","S","U","F","U","U"]}, {"Contract02", ["F","F","S","U","U","U"]}
	 * @throws IOException
	 * @throws ParserException
	 */	
	public static Map<String, List<String>> getResultVectors(List<String> contractsNames, Node nodeMutation, int numPCs) throws IOException{
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		
		/* The following two structures will be used for temporarily storing the contracts that are
		 * either Sat and Non Sat for each PC. Like this:
		 * contractsSatPerPC = {"PC1", [Contract1, Contract2, Contract3]}, {"PC2", [Contract1, Contract4, Contract7]}.
		 * contractsSatPerPC = {"PC1", [Contract4, Contract5, Contract7]}, {"PC2", [Contract2, Contract3, Contract6]}.
		 * They are used for easing the processing.
		 */
		Map<String, List<String>> contractsSatPerPC = new HashMap<String, List<String>>();
		Map<String, List<String>> contractsNonSatPerPC = new HashMap<String, List<String>>();
		
		//Let's first access the block <contracts_for_each_path_condition>, because we need to access each PC
		Element elementMutation = (Element) nodeMutation;
		NodeList nodeListContractsForEachPathCondition = elementMutation.getElementsByTagName("contracts_for_each_path_condition");
		Node nodeContractsForEachPathCondition = nodeListContractsForEachPathCondition.item(0); //There is only one
		Element elementContractsForEachPathCondition = (Element) nodeContractsForEachPathCondition;
		NodeList nodeListPC = elementContractsForEachPathCondition.getElementsByTagName("PC");
		
		//Now let's iterate over the list of PCs and add content to contractsSatPerPC and contractsNonSatPerPC
		for (int i=0; i<nodeListPC.getLength(); i++){
			String pathName = "PC" + nodeListPC.item(i).getAttributes().getNamedItem("num").getNodeValue();
			String satContracts = nodeListPC.item(i).getAttributes().getNamedItem("sat_contracts").getNodeValue();
			String nonSatContracts = nodeListPC.item(i).getAttributes().getNamedItem("nonsat_contracts").getNodeValue();
			//We have the names of the contracts separated by comma, let's split them and put them in arrays
			String[] contractsSatArray = satContracts.split(",");
			String[] contractsNonSatArray = nonSatContracts.split(",");
			//Let's convert the arrays to lists
			List<String> contractsSatList = Arrays.asList(contractsSatArray);
			List<String> contractsNonSatList = Arrays.asList(contractsNonSatArray);
			//Finally we put them in the map structures
			contractsSatPerPC.put(pathName, contractsSatList);
			contractsNonSatPerPC.put(pathName, contractsNonSatList);
			
		}
		
		String value;
		for (String contract : contractsNames){
			//Here we take both maps and navigate them to create the final structure
			List<String> resultVector = new ArrayList<String>();
			for (int i=0; i<numPCs; i++){ //Names of PCs are PCi, so we take that into account for the next
				String pc = "PC" + i;
				if (contractsSatPerPC.get(pc).contains(contract)){
					value = "S";
				} else if (contractsNonSatPerPC.get(pc).contains(contract)){
					value = "F";
				} else {
					value = "U";
				}
				resultVector.add(value);
			}
			result.put(contract, resultVector);
		}	
		return result;
	}
	
	/**
	 * 
	 * @param numPaths is the number of paths conditions that we have for the MT. 
	 * @return It is a map that for each contract that we have, we store a list, for each path, we 
	 * write 'F' if the constraint fails, 'S' if it does not fail and 'U' if we do not have such information. 
	 * E.g: {"Contract01", ["S","S","U","F","U","U"]}, {"Contract02", ["F","F","S","U","U","U"]}
	 * @throws IOException
	 * @throws ParserException
	 */
	public static Map<String, List<String>> getResultVectorsOld(long numPCs) throws IOException{
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		List<String> contracts = new ArrayList<String>(); //For storing the contracts names
		
		/* The following two structures will be used for temporarily storing the contracts that are
		 * either Sat and Non Sat for each PC. Like this:
		 * contractsSatPerPC = {"PC1", [Contract1, Contract2, Contract3]}, {"PC2", [Contract1, Contract4, Contract7]}.
		 * contractsSatPerPC = {"PC1", [Contract4, Contract5, Contract7]}, {"PC2", [Contract2, Contract3, Contract6]}.
		 * They are used for easing the processing.
		 */
		Map<String, List<String>> contractsSatPerPC = new HashMap<String, List<String>>();
		Map<String, List<String>> contractsNonSatPerPC = new HashMap<String, List<String>>();
		
		//We read the output SyVOLT file until we find "Contracts:"
		FileReader reader = new FileReader(executionsCanPath + "/" + syvoltOutput);
		BufferedReader br = new BufferedReader(reader);		
		String line = br.readLine();
		while (!line.equals("Contracts:")){ //Let's search for the line containing "Contracts:"
			line = br.readLine();
		}
		//Let's store the names of the contracts
		line = br.readLine();
		while (line!=null && !line.equals("")){
			contracts.add(line);
			line = br.readLine();
		}
//		System.out.println("contracts" + contracts);
		
		//We read the output SyVOLT file until we find "Contracts per path condition:"
		FileReader reader2 = new FileReader(executionsCanPath + "/" + syvoltOutput);	
		BufferedReader br2  = new BufferedReader(reader2);		
		line = br2.readLine();
		while (!line.equals("Contracts per path condition:")){ //Let's search for the line containing "Rules:"
			line = br2.readLine();
		}
		line = br2.readLine();
		
		while (line != null && !line.equals("")){
			//if (!line.equals("") && !line.substring(0,0).equals(" ") && !line.substring(0, 1).equals("--") ) { //If it is, then it is a comment or an empty line
			//Let us make sure we are in a PC line:
				if (line.substring(0, 2).equals("PC")){
					String pc = line.split(":",2)[0]; //This is the name of the PC
					line = br2.readLine(); //We must be in the "Sat" line
					String contractsSatString = line.split(":",2)[1].substring(1); //This contains all contracts separated by comma
					String[] contractsSatArray = contractsSatString.split(",");
					List<String> contractsSatList = Arrays.asList(contractsSatArray);
					contractsSatPerPC.put(pc, contractsSatList);
					
					line = br2.readLine(); //We must be in the "Non Sat" line
					String contractsNonSatString = line.split(":",2)[1].substring(1); //This contains all contracts separated by comma
					String[] contractsNonSatArray = contractsNonSatString.split(",");
					List<String> contractsNonSatList = Arrays.asList(contractsNonSatArray);
					contractsNonSatPerPC.put(pc, contractsNonSatList);
	
					line = br2.readLine();
				}
			//}
		}
		
		String value;
		for (String contract : contracts){
			//Aquí tengo que coger las dos listas e ir recorriéndolas para crear la estructura final
			List<String> resultVector = new ArrayList<String>();
			for (int i=0; i<numPCs; i++){ //Names of PCs are PCi, so we take that into account for the next
				String pc = "PC" + i;
				if (contractsSatPerPC.get(pc).contains(contract)){
					value = "S";
				} else if (contractsNonSatPerPC.get(pc).contains(contract)){
					value = "F";
				} else {
					value = "U";
				}
				resultVector.add(value);
			}
			result.put(contract, resultVector);
		}	
		return result;
	}
	
	/**
	 * 
	 * @param resultVectors. It is a map that for each contract that we have, we store a list, for each path, we 
	 * write 'F' if the constraint fails, 'S' if it does not fail and 'U' if we do not have such information. 
	 * E.g: {"Contract01", ["S","S","U","F","U","U"]}, {"Contract02", ["F","F","S","U","U","U"]}
	 * @param rulesNames. This is a list with the names of the rules of the MT
	 * @param rulesExecutions. This variable is a Map where the key is the name of a rule and the value is an array with the number of executions of such rule in each PC. 
	 * Map<RuleName, List[Executions in Scenario1, Executions in Scenario2,...]>. Example:
	 * {"Rule1", [1,2,0,3,4]}, {"Rule2", [2,0,4,3,1]}, ...
	 * @return It calculates all the measures needed for computing the suspiciousness of the rules (Ncf, Nuf, Ncs, Nus...).
	 *  It returns a Map that for each contract, it keeps for each rule a value for each variable. For example:
     * "Contract1": {"Rule1", {"Ncf", 9}, {"Nuf", 0}, {"Ncs", 5}, ...}, {"Rule2", {"Ncf", 3}, {"Nuf", 5}, {"Ncs", 4}, ...}
	 *  "Contract2": {"Rule1", {"Ncf", 4}, {"Nuf", 1}, {"Ncs", 2}, ...}, {"Rule2", {"Ncf", 3}, {"Nuf", 5}, {"Ncs", 4}, ...}
	 */
	public static Map<String, Map<String, Map<String, Double>>> getMeasures(Map<String, List<String>> resultVectors, List<String> rulesNames, Map<String, int[]> rulesExecutions){
		Map<String, Map<String, Map<String, Double>>> result = new HashMap<String, Map<String, Map<String, Double>>>();
		//We have to do the calculations for the resultVector of each PC
		for (String contract : resultVectors.keySet()){
			List<String> resultVector = resultVectors.get(contract);
//			System.out.println("RESULT VECTOR DE " + contract + ": " + resultVector);
			Map<String, Map<String, Double>> measuresPerRulePerContractTogether = new HashMap<String, Map<String, Double>>();
			for (String ruleName : rulesNames){
				//Within each expression, we have to calculate the measures for each rule
				int ncf = 0, nuf = 0, ncs = 0, nus = 0, nc = 0, nu = 0, ns = 0, nf = 0;
				/** These measures have to be for each contract
				 * ncf : number of failed PC that cover a rule
				 * nuf : number of failed PC that do not cover a rule
				 * ncs : number of successful PC that cover a rule
				 * nus : number of successful PC that do not cover a rule
				 * nc : number of PCs that cover a rule
				 * nu : number of PCs that do not cover a rule
				 * ns : number of successful PCs
				 * nf : number of failed PCs
				 */
				int[] ruleExecutions = rulesExecutions.get(ruleName);
//				System.out.println("RULE NAME: " + ruleName + ". EXECUTIONS: " + ruleExecutions);
				for (int j=0; j<ruleExecutions.length; j++){
					if (resultVector.get(j).equals("F")){ // The contract is in "Non Sat" in PCj
						nf++;
						if (ruleExecutions[j]>0){
							ncf++;
							nc++;
						} else {
							nuf++;
							nu++;
						}
					} else if (resultVector.get(j).equals("S")){ // The contract is in "Sat" in PCj
						ns++;
						if (ruleExecutions[j]>0){
							ncs++;
							nc++;
						} else {
							nus++;
							nu++;
						}
					//}
						/* When the information of a contract is neither satisfied nor not satisfied for a PC, we cannot add information
						 * about ncf, nuf, ncs, nus, ns and nf. However, at least, we can add information of nc and nu, since
						 * they are only about whether PC covers the rule
					  */					
					} else {
						if (ruleExecutions[j]>0){
							//ncs++;
							nc++;
						} else {
							//nus++;
							nu++;
						}
					}
				}
				Map<String, Double> measuresPerRulePerContract = new HashMap<String, Double>();
				measuresPerRulePerContract.put("Ncf", (double)ncf);
				measuresPerRulePerContract.put("Nuf", (double)nuf);
				measuresPerRulePerContract.put("Ncs", (double)ncs);
				measuresPerRulePerContract.put("Nus", (double)nus);
				measuresPerRulePerContract.put("Nc", (double)nc);
				measuresPerRulePerContract.put("Nu", (double)nu);
				measuresPerRulePerContract.put("Ns", (double)ns);
				measuresPerRulePerContract.put("Nf", (double)nf);
				
				measuresPerRulePerContractTogether.put(ruleName, measuresPerRulePerContract);
			}
			result.put(contract, measuresPerRulePerContractTogether);
		}
		return result;
	}
		
		
	/**
	 * 
	 * @param measures.  These are measures needed for computing the suspiciousness of the rules (Ncf, Nuf, Ncs, Nus...).
	 *  It is a Map that for each OCL expression, it keeps for each rule a value for each variable. For example:
     * "OCL1": {"Rule1", {"Ncf", 9}, {"Nuf", 0}, {"Ncs", 5}, ...}, {"Rule2", {"Ncf", 3}, {"Nuf", 5}, {"Ncs", 4}, ...}
	 *  "OCL2": {"Rule1", {"Ncf", 4}, {"Nuf", 1}, {"Ncs", 2}, ...}, {"Rule2", {"Ncf", 3}, {"Nuf", 5}, {"Ncs", 4}, ...}
	 *  @param technique. It is a String specifying with which technique should the suspiciousness be calculated
	 * @return. It returns a Map that for each OCL expression and for each rule, it stores the corresponding suspiciousness
	 * according to the technique specified in the technique parameter
	 * 	"OCL1"={"Rule1", 0.9345}, {"Rule2", 0.123},...
	 *  "OCL2"={"Rule1", 0.445}, {"Rule2", 0.001},...
	 */
	public static Map<String, Map<String, Double>> getSuspiciousness(Map<String, Map<String, Map<String, Double>>> measures, String technique){
		Map<String, Map<String, Double>> result = new HashMap<String, Map<String, Double>>();
		//System.out.println("***The information above is presented below for each OCL constraint and for each rule. Please bear in mind that when an OCL constraint never fails, the metrics will be either 0.0 or NaN***");
		for (Map.Entry<String, Map<String, Map<String, Double>>> measuresOfOcl : measures.entrySet()){
			//System.out.println("\nThe results for " + measuresOfOcl.getKey() + " with the technique '" + technique + "' are:");
			Map<String, Double> suspPerRule = new HashMap<String, Double>();
			for(Map.Entry<String, Map<String, Double>> measuresOfRule : measuresOfOcl.getValue().entrySet()){
				Map<String, Double> valuesForRule = measuresOfRule.getValue();
				Double susp = 0.0;
				if (technique.equals("ochiai")){
					if (valuesForRule.get("Ncf") == 0.0){
						susp = 0.0;
					} else if (Math.sqrt(valuesForRule.get("Nf")*(valuesForRule.get("Ncf")+valuesForRule.get("Ncs")))==0.0){
						susp = 1.0;
					} else {
						susp = valuesForRule.get("Ncf") / Math.sqrt(valuesForRule.get("Nf")*(valuesForRule.get("Ncf")+valuesForRule.get("Ncs")));
					}
				}else if (technique.equals("tarantula")){
					/*According to Empirical Evaluation of the Tarantula Automatic FaultLocalization	Technique,
					 * If any of the denominators evaluates to zero, we assign zero to that fraction. */
					if ((valuesForRule.get("Nf"))==0 || valuesForRule.get("Ncs") == 0.0 && valuesForRule.get("Ncf")==0.0){
						susp = 0.0;
					}else if (valuesForRule.get("Ns") == 0.0){
						susp = (valuesForRule.get("Ncf")/valuesForRule.get("Nf"))  / (valuesForRule.get("Ncf")/valuesForRule.get("Nf"));
					} else {
						susp = (valuesForRule.get("Ncf")/valuesForRule.get("Nf"))  / (valuesForRule.get("Ncf")/valuesForRule.get("Nf") + valuesForRule.get("Ncs")/valuesForRule.get("Ns"));
					}
				} else if (technique.equals("ochiai2")){
					if (valuesForRule.get("Ncf") * valuesForRule.get("Nus") == 0.0){
						susp = 0.0;
					} else if (Math.sqrt((valuesForRule.get("Ncf") + valuesForRule.get("Ncs")) * 
							(valuesForRule.get("Nus") + valuesForRule.get("Nuf")) * 
							(valuesForRule.get("Ncf") + valuesForRule.get("Nuf")) *
							(valuesForRule.get("Ncs") + valuesForRule.get("Nus")))==0.0){
						susp = 1.0;
					} else {
						susp = (valuesForRule.get("Ncf") * valuesForRule.get("Nus")) / (0.1 + Math.sqrt((valuesForRule.get("Ncf") + valuesForRule.get("Ncs")) * 
																										(valuesForRule.get("Nus") + valuesForRule.get("Nuf")) * 
																										(valuesForRule.get("Ncf") + valuesForRule.get("Nuf")) *
																										(valuesForRule.get("Ncs") + valuesForRule.get("Nus"))));
					}
				} else if (technique.equals("braunbanquet")){
					susp = valuesForRule.get("Ncf") / Math.max(valuesForRule.get("Ncf") + valuesForRule.get("Ncs"), valuesForRule.get("Ncf") + valuesForRule.get("Nuf"));
				} else if (technique.equals("mountford")){
					if (valuesForRule.get("Ncf") == 0.0){
						susp = 0.0;
					} else if (0.5 * ((valuesForRule.get("Ncf")*valuesForRule.get("Ncs"))+(valuesForRule.get("Ncf")*valuesForRule.get("Nuf"))) + (valuesForRule.get("Ncs") * valuesForRule.get("Nuf"))==0.0){
						susp = 1.0;
					} else {
						susp = valuesForRule.get("Ncf") / (0.5 * ((valuesForRule.get("Ncf")*valuesForRule.get("Ncs"))+(valuesForRule.get("Ncf")*valuesForRule.get("Nuf"))) + (valuesForRule.get("Ncs") * valuesForRule.get("Nuf")));
					}
				} else if (technique.equals("arithmeticmean")){
					if (2*((valuesForRule.get("Ncf")*valuesForRule.get("Nus"))-(valuesForRule.get("Nuf")*valuesForRule.get("Ncs"))) == 0.0){
						susp = 0.0;
					} else if ((valuesForRule.get("Ncf")+valuesForRule.get("Ncs"))*(valuesForRule.get("Nus")+valuesForRule.get("Nuf"))+(valuesForRule.get("Ncf")+valuesForRule.get("Nuf"))*(valuesForRule.get("Ncs")+valuesForRule.get("Nus"))==0.0){
						susp = 1.0;
					} else {
						susp = 2*((valuesForRule.get("Ncf")*valuesForRule.get("Nus"))-(valuesForRule.get("Nuf")*valuesForRule.get("Ncs")))/
								((valuesForRule.get("Ncf")+valuesForRule.get("Ncs"))*(valuesForRule.get("Nus")+valuesForRule.get("Nuf"))+(valuesForRule.get("Ncf")+valuesForRule.get("Nuf"))*(valuesForRule.get("Ncs")+valuesForRule.get("Nus")));					
					}
				} else if (technique.equals("zoltar")){
					if (valuesForRule.get("Ncf") == 0.0){
						susp = 0.0;
					} else if (valuesForRule.get("Ncf")+valuesForRule.get("Nuf")+valuesForRule.get("Ncs")+((10000*valuesForRule.get("Nuf")*valuesForRule.get("Ncs"))/valuesForRule.get("Ncf"))==0.0){
						susp = 1.0;
					} else {
						susp = valuesForRule.get("Ncf") / (valuesForRule.get("Ncf")+valuesForRule.get("Nuf")+valuesForRule.get("Ncs")+((10000*valuesForRule.get("Nuf")*valuesForRule.get("Ncs"))/valuesForRule.get("Ncf")));
					}
				
				} else if (technique.equals("simplematching")){
					susp = (valuesForRule.get("Ncf")+valuesForRule.get("Nus"))/(valuesForRule.get("Ncf")+valuesForRule.get("Ncs")+valuesForRule.get("Nus")+valuesForRule.get("Nuf"));
					
				} else if (technique.equals("russelrao")){
					susp = valuesForRule.get("Ncf") / (valuesForRule.get("Ncf")+valuesForRule.get("Nuf")+valuesForRule.get("Ncs")+valuesForRule.get("Nus"));
				
				} else if (technique.equals("kulcynski2")){
					if (valuesForRule.get("Ncf") == 0.0){
						susp = 0.0;
					} else if (valuesForRule.get("Ncf")+valuesForRule.get("Nuf") ==0.0 || valuesForRule.get("Ncf")+valuesForRule.get("Ncs")==0.0){
						susp = 1.0;
					} else {
						susp = 0.5 * (valuesForRule.get("Ncf")/(valuesForRule.get("Ncf")+valuesForRule.get("Nuf"))+valuesForRule.get("Ncf")/(valuesForRule.get("Ncf")+valuesForRule.get("Ncs")));
					}				
				} else if (technique.equals("cohen")){
					if (2*valuesForRule.get("Ncf")*valuesForRule.get("Nus")-2*valuesForRule.get("Nuf")*valuesForRule.get("Ncs") == 0.0){
						susp = 0.0;
					} else if ((valuesForRule.get("Ncf")+valuesForRule.get("Ncs"))*(valuesForRule.get("Nus")+valuesForRule.get("Ncs"))+(valuesForRule.get("Ncf")+valuesForRule.get("Nuf"))*(valuesForRule.get("Nuf")+valuesForRule.get("Nus")) ==0.0){
						susp = 1.0;
					} else {
						susp = (2*valuesForRule.get("Ncf")*valuesForRule.get("Nus")-2*valuesForRule.get("Nuf")*valuesForRule.get("Ncs"))/((valuesForRule.get("Ncf")+valuesForRule.get("Ncs"))*(valuesForRule.get("Nus")+valuesForRule.get("Ncs"))+(valuesForRule.get("Ncf")+valuesForRule.get("Nuf"))*(valuesForRule.get("Nuf")+valuesForRule.get("Nus")));
					}
				} else if (technique.equals("pierce")) {
					if (valuesForRule.get("Ncf") * valuesForRule.get("Nuf") + valuesForRule.get("Nuf") * valuesForRule.get("Ncs") == 0.0){
						susp = 0.0;
					} else if (valuesForRule.get("Ncf") * valuesForRule.get("Nuf") + 2*valuesForRule.get("Nuf") * valuesForRule.get("Nus") + valuesForRule.get("Ncs") * valuesForRule.get("Nus") ==0.0){
						susp = 1.0;
					} else {
						susp = (valuesForRule.get("Ncf") * valuesForRule.get("Nuf") + valuesForRule.get("Nuf") * valuesForRule.get("Ncs")) / (valuesForRule.get("Ncf") * valuesForRule.get("Nuf") + 2*valuesForRule.get("Nuf") * valuesForRule.get("Nus") + valuesForRule.get("Ncs") * valuesForRule.get("Nus"));					
					}
				} else if (technique.equals("baronietal")){
					susp = (Math.sqrt(valuesForRule.get("Ncf") * valuesForRule.get("Nus"))+valuesForRule.get("Ncf"))/
								(Math.sqrt(valuesForRule.get("Ncf") * valuesForRule.get("Nus")) + valuesForRule.get("Ncf") + valuesForRule.get("Ncs") + valuesForRule.get("Nuf"));
				}  else if (technique.equals("phi")){
					if (valuesForRule.get("Ncf") * valuesForRule.get("Nus") - valuesForRule.get("Nuf")*valuesForRule.get("Ncs") == 0.0){
						susp = 0.0;
					} else if (Math.sqrt((valuesForRule.get("Ncf")+valuesForRule.get("Ncs"))*(valuesForRule.get("Ncf")+valuesForRule.get("Nuf"))*(valuesForRule.get("Ncs")+valuesForRule.get("Nus"))*(valuesForRule.get("Nuf")+valuesForRule.get("Nus"))) ==0.0){
						susp = 1.0;
					} else {
						susp = (valuesForRule.get("Ncf") * valuesForRule.get("Nus") - valuesForRule.get("Nuf")*valuesForRule.get("Ncs"))/
							(Math.sqrt((valuesForRule.get("Ncf")+valuesForRule.get("Ncs"))*(valuesForRule.get("Ncf")+valuesForRule.get("Nuf"))*(valuesForRule.get("Ncs")+valuesForRule.get("Nus"))*(valuesForRule.get("Nuf")+valuesForRule.get("Nus"))));
					}
				} else if (technique.equals("rogerstanimoto")){
					susp = (valuesForRule.get("Ncf") + valuesForRule.get("Nus"))/
							(valuesForRule.get("Ncf") + valuesForRule.get("Nus") + 2*(valuesForRule.get("Nuf") + valuesForRule.get("Ncs")));
				} else if (technique.equals("op2")){
					susp = valuesForRule.get("Ncf") - valuesForRule.get("Ncs") / (valuesForRule.get("Ns") + 1);
				} else if (technique.equals("barinel")){
					susp = 1 - valuesForRule.get("Ncs") / (valuesForRule.get("Ncs") + valuesForRule.get("Ncf"));
				} else if (technique.equals("DStar")){
					if (Math.pow(valuesForRule.get("Ncf"),2) == 0.0){
						susp = 0.0;
					} else if ((valuesForRule.get("Ncs") + valuesForRule.get("Nf") - valuesForRule.get("Ncf")) ==0.0){
						susp = 1.0;
					} else {
						susp = Math.pow(valuesForRule.get("Ncf"),2) /
								(valuesForRule.get("Ncs") + valuesForRule.get("Nf") - valuesForRule.get("Ncf"));
					}				
				}else if (technique.equals("confidence")){
					if ((valuesForRule.get("Nf"))==0){
						susp = valuesForRule.get("Ncs")/valuesForRule.get("Ns");
					} else if ((valuesForRule.get("Ns"))==0){
						susp = valuesForRule.get("Ncf")/valuesForRule.get("Nf");
					} else {
						susp = Math.max(valuesForRule.get("Ncf")/valuesForRule.get("Nf"), valuesForRule.get("Ncs")/valuesForRule.get("Ns"));
					}		
				}
				
				suspPerRule.put(measuresOfRule.getKey(), susp);
				
			}
			result.put(measuresOfOcl.getKey(), suspPerRule);
		}
		return result;
	}
	
	/** 
	 * @param resultVectors. It is a map that for each contract that we have, we store a list, for each path, we 
	 * write 'F' if the constraint fails, 'S' if it does not fail and 'U' if we do not have such information. 
	 * E.g: {"Contract01", ["S","S","U","F","U","U"]}, {"Contract02", ["F","F","S","U","U","U"]}
	 * @param allSuspiciousness. Structure with all the suspiciousness for all the different techniques. It is a
	 * Map<String,Map<String,Map<String, Double>>> where the first String is the name of the technique, for instance "Tarantula", and the rest is the different
	 * parameters for each rule and each contract:
	 * "Contract1"={"Rule1", 0.9345}, {"Rule2", 0.123},...
	 * "Contract2"={"Rule1", 0.445}, {"Rule2", 0.001},...
	 * @param measures. Structure that keeps the values of the different parameters for each OCL expression, e.g.:
	 * "Contract1": {"Rule1", {"Ncf", 9}, {"Nuf", 0}, {"Ncs", 5}, ...}, {"Rule2", {"Ncf", 3}, {"Nuf", 5}, {"Ncs", 4}, ...}
	 * "Contract2": {"Rule1", {"Ncf", 4}, {"Nuf", 1}, {"Ncs", 2}, ...}, {"Rule2", {"Ncf", 3}, {"Nuf", 5}, {"Ncs", 4}, ...}
	 * @param rulesExecutions. This is a Map<String, int[]> with the name of a rule and an array with the number of executions of such rule in each PC. 
	 * Map<RuleName, List[Executions in Scenario1, Executions in Scenario2,...]>. Example:
	 * {"Rule1", [1,2,0,3,4]}, {"Rule2", [2,0,4,3,1]}, ...
	 * @param ruleMutated is the rule of the mutation_set. i.e., the rule being mutated in the experiment
	 * @param mutationOperator is the name of the mutation as specified in the <mutation>
	 * @param PC is the number of path conditions for this specific mutation
	 * @param contractsSatisfaction. It is a Map<String, int[]> where, for each contract, it indicates in how many PCs the contract is
	 * (i) satisfied, (ii) not satisfied and (iii) undefined. For instance: <"Contract1", [20,45,98]>
	 * @param bwBC. BufferedWriter related to the file where we write the summary of the best case results
	 * @param bwWC. BufferedWriter related to the file where we write the summary of the worst case results
	 * @param bwAC. BufferedWriter related to the file where we write the summary of the average case results
	 * @return This method generates the CSV file with the results of those contracts that have failed and also prints in the console which contracts have not been satisfied.
	 * Furthermore, it creates one CSV file for each contract that fails with the detailed information of rules executed for each input model and information of the different parameters
	 * @throws IOException
	 */
	public static boolean printResultsInCSVBooks(Map<String, List<String>> resultVectors, Map<String, Map<String, Map<String,Double>>> allSuspiciousness, 
													Map<String, Map<String, Map<String, Double>>> measures, Map<String, int[]> rulesExecutions, String ruleMutated, 
													String mutationOperator, int numPCs, Map<String, int[]> contractsSatisfaction, BufferedWriter bwBC, BufferedWriter bwWC, BufferedWriter bwAC) throws IOException{
		/**This method will write in the main CSV file and will delegate to other method to write the individual CSV files for each contract that fails**/
		boolean res = true;
		String mutationID = ruleMutated + "--" + mutationOperator; //This is the ID for the specific mutation
		//In the following variable we keep the name of the rule that has been mutated. We use a List<String> and not just a String to adapt it to the previous
		//implementation. Furthermore, this can be useful if we need to add nore faulty rules in the future
		List<String> buggyRules = new ArrayList<String>();
		buggyRules.add(ruleMutated);
		/*This structure is used for printing the summary of EXAM metric altogether:
		 * <Contract1, <Tarantule, 0.22><Ochiai, 0.67>>...
		 */
		Map<String, Map<String, Double>> oclExamTechniqueBC = new HashMap<String, Map<String, Double>>();
		Map<String, Map<String, Double>> oclExamTechniqueWC = new HashMap<String, Map<String, Double>>();
		Map<String, Map<String, Double>> oclExamTechniqueAVG = new HashMap<String, Map<String, Double>>();
		List<String> failedContracts = new ArrayList<String>();
		
		//We create the "Results" folder if it does not exist
		Path path = Paths.get(executionsCanPath + "/results/" + "results_" + mutationID);
        //if directory exists?
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                //fail to create directory
                e.printStackTrace();
            }
        }
		
		
		File result = new File(executionsCanPath + "/results/" + "/results_" + mutationID + "/" + mutationID + "--" + Results_File_Name);
		if (result.exists()) result.delete();
		result.createNewFile();
		FileWriter fw = new FileWriter(result.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
		bw.write(mutationID + " In the following it is displayed the suspiciousness-based rankings, according to different coefficients, for those contracts for which at least a PC is not satisfied. \n\n\n");
		System.out.println("\n*****************************************************************************************");
		System.out.println("The contracts that have not been satisfied in mutation || " + mutationID + " || are: ");
		boolean allSatisfied = true;
		
		List<String> techniques = new ArrayList<String>(allSuspiciousness.keySet());
		//for (String technique : techniques) oclExamTechnique.put(technique, new HashMap<String, Double>()); //We initialize the oclExamTechnique variable
		
		//We iterate for each contract
		for(String contract : resultVectors.keySet()){
			List<String> resultVector = resultVectors.get(contract);
			boolean satisfied = true;
			int j=0;
			while (j < numPCs && satisfied){
				if (resultVector.get(j).equals("F")) satisfied = false;
				j++;
			}
			if (!satisfied){ // We only write data for those contracts that are not satisfied
				failedContracts.add(contract);
				System.out.println(contract + " ");
				bw.write("The data below is for contract: " + contract + "\n");; //We write the row with the contract
				List<String> rules = new ArrayList<String>(allSuspiciousness.get("Ochiai").get(contract).keySet()); // We get the list of rules
				
				//The following three variables are used for computing the EXAM metric for the evaluation
				int numRules = allSuspiciousness.get("Ochiai").get(contract).keySet().size();
				Map<String, List<Double>> suspValuesPerTechnique = new HashMap<String, List<Double>>();
				Map<String, Double> suspBuggyRuleMap = new HashMap<String, Double>();
				
				for (String technique : techniques){
					bw.write(";" + technique); //We write the row with the technique's names
					List<Double> suspValues = new ArrayList<Double>();
					suspValuesPerTechnique.put(technique, suspValues); //We insert a map for each technique to fill it in while we write in the excel
				}
				bw.write("\n");
				
				//We write the values of suspiciousness for each rule and each technique:
				for (String ruleName : rules){
					bw.write(ruleName); //We write the name of the rule
					for (String technique : techniques){
						double susp = allSuspiciousness.get(technique).get(contract).get(ruleName);
						bw.write(";" + susp);
						List<Double> currentSuspValues = suspValuesPerTechnique.get(technique);
						currentSuspValues.add(susp);
						//if (ruleName.equals(Buggy_rule)) suspBuggyRuleMap.put(technique, susp);
						//Journal Extension: we now consider there might be more than one buggy rules
						if (buggyRules.contains(ruleName)){
							Double suspBuggy = suspBuggyRuleMap.get(technique);
							if (suspBuggy == null || suspBuggy < susp){
								suspBuggyRuleMap.put(technique, susp);
							}
						}
					}
					bw.write("\n");
				}
				
				//For computing the EXAM metric of the evaluation
				/**Journal Extension: 
				 * For the computation of the EXAM metric, we do not take into account the confidence anymore.
				 * Now, in case of ties, we compute the best case, worst case, and the average case. If there are more
				 * than one buggy rule, we consider the time in finding the first buggy rule, i.e., any of the buggy rules.**/				
				//Variables where to keep the values of the EXAM scores of the different techniques
				Map<String, Double> techniqueExamBC = new HashMap<String, Double>();
				Map<String, Double> techniqueExamWC = new HashMap<String, Double>();
				Map<String, Double> techniqueExamAVG = new HashMap<String, Double>();
				for (String technique : techniques){
					int repeated = 0;
					int comparator = 0;
					Double suspBuggyRule = suspBuggyRuleMap.get(technique);
					int index = 0;
					for (Double susp : suspValuesPerTechnique.get(technique)){
						/*compareTo is for comparing Double values:
						 * a.compareTo(b) == 0 if the values are the same
						 * a.compareTo(b) < 0 if a<b
						 * a.compareTo(b) > 0 if a>b
						 */
						if (susp.compareTo(suspBuggyRule) > 0) comparator++;
						if (susp.compareTo(suspBuggyRule) == 0) repeated ++;
						index++;
					}
					double examBC = (double) (comparator + 1) / numRules; //For now we consider that EXAM > 0
					double examWC = (double) (comparator + repeated) / numRules;
					double examAVG = (double) (examBC + examWC) / 2;
					//For updating the oclExamTechnique variables:
					techniqueExamBC.put(technique, examBC);
					techniqueExamWC.put(technique, examWC);
					techniqueExamAVG.put(technique, examAVG);
				}
				

				//For printing the EXAM values:
				bw.write("EXAM-BC");
				for (String technique : techniques) bw.write(";" + techniqueExamBC.get(technique));
				bw.write("\nEXAM-WC");
				for (String technique : techniques) bw.write(";" + techniqueExamWC.get(technique));
				bw.write("\nEXAM-AVG");
				for (String technique : techniques) bw.write(";" + techniqueExamAVG.get(technique));
				
				//We store the values in variables for printing them at the end of the cvs file
				oclExamTechniqueBC.put(contract, techniqueExamBC);
				oclExamTechniqueWC.put(contract, techniqueExamWC);
				oclExamTechniqueAVG.put(contract, techniqueExamAVG);
			
				bw.write("\n");
				bw.write("\n");					
				allSatisfied = false;
				
				//For every contract that is not satisfied, we create an CSV file with all the details
				printParticularResultsInCSVBook(contract, resultVectors, allSuspiciousness, measures, rulesExecutions,ruleMutated,mutationOperator,numPCs);
			}
		}
		
		
		/**For writing the summary of all EXAM scores at the end of the csv file
		 * And for writing the three files with the three summaries: BC, WC, AC**/
		if (allSatisfied){
			bw.write("In this case, all contracts are satisfied, so nothing is displayed.\n");
			System.out.println("none.\nAll contracts are satisfied!");
			res = false;
		} else {
			//We write the EXAM metrics all together
			/**The following commented code is for printing the EXAM scores by technique, e.g.: Cohen-BC, Cohen-WS, Cohen-AVG, Kulcy-BC, Kulcy-WS, Kulcy-AVG**/
			bw.write("\n***Summary of the quality of the different techniques according to the EXAM metric***\n");
			
			/** The following block of code is for printing information of how many PCs are (i) satisfied, (ii) not satisfied
			 * and (iii) undefined for every contract */
			bw.write("\nFirst, it is shown information where, for each contract, we see in how many PCs it is satisfied, not satisfied or undefined\n");
			bw.write(";Num PCs;Sat;NonSat;Und;Percent-Und\n");
			for (String failedContract : failedContracts){
				int sat = contractsSatisfaction.get(failedContract)[0];
				int notSat = contractsSatisfaction.get(failedContract)[1];
				int undef = contractsSatisfaction.get(failedContract)[2];
				double percentage = undef * 100 / numPCs;
				bw.write(failedContract + ";" + numPCs + ";" + sat + ";" + notSat + ";" + undef + ";" + "=" + undef + "* 100 / " + numPCs + "\n");
			}
			
			
			bw.write("\nBEST CASE");
			for (String technique : techniques) {
				bw.write(";" + technique + "-BC");
			}
			bw.write("\n");

			for (String failedContract : failedContracts){
				bw.write(failedContract);
				//For writing in the file with the summary in the BC scenario:
				bwBC.write(mutationID + " - " + failedContract);
				for (String technique : techniques){
					bw.write(";" + oclExamTechniqueBC.get(failedContract).get(technique));
					
					//For writing in the file with the summary in the BC scenario:
					bwBC.write(";" + oclExamTechniqueBC.get(failedContract).get(technique));
				}
				//At the end of the line in the file with the summary in the BC scenarios we add the percentage of undefined PCs
				int sat = contractsSatisfaction.get(failedContract)[0];
				int notSat = contractsSatisfaction.get(failedContract)[1];
				int undef = contractsSatisfaction.get(failedContract)[2];
				double percentage = undef * 100 / numPCs;
				bwBC.write("; ;" + numPCs + ";" + sat + ";" + notSat + ";" + undef + ";" + "=" + undef + "* 100 / " + numPCs);
				
				bw.write("\n");
				bwBC.write("\n");
			}
			
			bw.write("\nWORST CASE");
			for (String technique : techniques) {
				bw.write(";" + technique + "-WC");
			}
			bw.write("\n");
			for (String failedContract : failedContracts){
				bw.write(failedContract);
				//For writing in the file with the summary in the WC scenario:
				bwWC.write(mutationID + " - " + failedContract);
				for (String technique : techniques){
					bw.write(";" + oclExamTechniqueWC.get(failedContract).get(technique));
					
					//For writing in the file with the summary in the WC scenario:
					bwWC.write(";" + oclExamTechniqueWC.get(failedContract).get(technique));
				}
				//At the end of the line in the file with the summary in the WC scenarios we add the percentage of undefined PCs
				int sat = contractsSatisfaction.get(failedContract)[0];
				int notSat = contractsSatisfaction.get(failedContract)[1];
				int undef = contractsSatisfaction.get(failedContract)[2];
				double percentage = undef * 100 / numPCs;
				bwWC.write("; ;" + numPCs + ";" + sat + ";" + notSat + ";" + undef + ";" + "=" + undef + "* 100 / " + numPCs);
				
				bw.write("\n");
				bwWC.write("\n");
			}
			
			bw.write("\nAVG CASE");
			for (String technique : techniques) {
				bw.write(";" + technique + "-AVG");
			}
			bw.write("\n");
			for (String failedContract : failedContracts){
				//For writing in the file with the summary in the AC scenario:
				bwAC.write(mutationID + " - " + failedContract);
				bw.write(failedContract);
				for (String technique : techniques){
					bw.write(";" + oclExamTechniqueAVG.get(failedContract).get(technique));
					
					//For writing in the file with the summary in the AC scenario:
					bwAC.write(";" + oclExamTechniqueAVG.get(failedContract).get(technique));
				}
				//At the end of the line in the file with the summary in the AC scenarios we add the percentage of undefined PCs
				int sat = contractsSatisfaction.get(failedContract)[0];
				int notSat = contractsSatisfaction.get(failedContract)[1];
				int undef = contractsSatisfaction.get(failedContract)[2];
				double percentage = undef * 100 / numPCs;
				bwAC.write("; ;" + numPCs + ";" + sat + ";" + notSat + ";" + undef + ";" + "=" + undef + "* 100 / " + numPCs);
				
				bw.write("\n");
				bwAC.write("\n");
			}
		}
		
		bw.close();
		// If a .csv file was created but, actually, all contracts were satisfied, we need to delete it (the file and the folder):
		if (!res) {
			result.delete();
			File folder = new File(path.toAbsolutePath().toString());
			String[] files = folder.list();
			//We need to delete any file that could have been created inside the folder before deleting the folder
			for(String s : files){
				File currentFile = new File(folder.getPath(), s);
				currentFile.delete();
			}
			Files.delete(path);
		}
		return res;
	}

	
	
	
	/**
	 * @param contract. Name of the contract
	 * @param resultVectors. It is a map that for each contract that we have, we store a list, for each path, we 
	 * write 'F' if the constraint fails, 'S' if it does not fail and 'U' if we do not have such information. 
	 * E.g: {"Contract01", ["S","S","U","F","U","U"]}, {"Contract02", ["F","F","S","U","U","U"]}
	 * @param allSuspiciousness. Structure with all the suspiciousness for all the different techniques. It is a
	 * Map<String,Map<String,Map<String, Double>>> where the first String is the name of the technique, for instance "Tarantula", and the rest is the different
	 * parameters for each rule and each ocl constraint:
	 * "Contract1"={"Rule1", 0.9345}, {"Rule2", 0.123},...
	 * "Contract2"={"Rule1", 0.445}, {"Rule2", 0.001},...
	 * @param measures. Structure that keeps the values of the different parameters for each OCL expression, e.g.:
	 * "Contract1": {"Rule1", {"Ncf", 9}, {"Nuf", 0}, {"Ncs", 5}, ...}, {"Rule2", {"Ncf", 3}, {"Nuf", 5}, {"Ncs", 4}, ...}
	 * "Contract2": {"Rule1", {"Ncf", 4}, {"Nuf", 1}, {"Ncs", 2}, ...}, {"Rule2", {"Ncf", 3}, {"Nuf", 5}, {"Ncs", 4}, ...}
	 * @param rulesExecutions. This is a Map<String, int[]> with the name of a rule and an array with the number of executions of such rule in each scenario. 
	 * Map<RuleName, List[Executions in Scenario1, Executions in Scenario2,...]>. Example:
	 * {"Rule1", [1,2,0,3,4]}, {"Rule2", [2,0,4,3,1]}, ...
	 * * @param ruleMutated is the rule of the mutation_set. i.e., the rule being mutated in the experiment
	 * @param mutationOperator is the name of the mutation as specified in the <mutation>
	 * @param PC is the number of path conditions for this specific mutation
	 * @return This method generates the CSV file with the results of those OCL expressions that have failed and also prints in the console with OCL expressions have not been satisfied.
	 * Furthermore, it creates one CSV file for each OCL expression that fails with the detailed information of rules executed for each input model and information of the different parameters
	 * @throws IOException
	 */
		public static File printParticularResultsInCSVBook(String contract, Map<String, List<String>> resultVectors, Map<String, Map<String, Map<String,Double>>> allSuspiciousness, 
							Map<String, Map<String, Map<String, Double>>> measures, Map<String, int[]> rulesExecutions, String ruleMutated, String mutationOperator, int numPCs) throws IOException {
			String mutationID = ruleMutated + "--" + mutationOperator; //This is the ID for the specific mutation
			File result = new File(executionsCanPath + "/results/" + "/results_" + mutationID + "/" + mutationID + "--" + contract + ".csv");
			if (result.exists()) result.delete();
			result.createNewFile();
			FileWriter fw = new FileWriter(result.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			//Let's add an explanation of the different parameters used in the different suspiciousness techniques
			for (int i = 0; i<=numPCs; i++) bw.write(";");
			bw.write("Ncf: Number of failed PCs that cover the rule\n");
			for (int i = 0; i<=numPCs; i++) bw.write(";");
			bw.write("Nus: Number of successful PCs that cover the rule\n");
			for (int i = 0; i<=numPCs; i++) bw.write(";");
			bw.write("Ncs: Number of successful PCs that cover the rule\n");
			for (int i = 0; i<=numPCs; i++) bw.write(";");
			bw.write("Nus: number of successful PCs that do not cover the rule\n");
			for (int i = 0; i<=numPCs; i++) bw.write(";");
			bw.write("Nc: total number of PCs that cover the rule\n");
			for (int i = 0; i<=numPCs; i++) bw.write(";");
			bw.write("Nu:total number of PCs that do not cover the rule\n");
			for (int i = 0; i<=numPCs; i++) bw.write(";");
			bw.write("Ns: total number of successful PCs\n");
			for (int i = 0; i<=numPCs; i++) bw.write(";");
			bw.write("Nf: total number of failed test cases\n\n");		
			
			//Let's write the first line as header
			bw.write("The data below is for contract: " + contract + "\n\n");
			
			//Now we write the line with the columns purpose: rules, test cases, parameters and different techniques
			bw.write("Rules;");
			for (int i = 0; i< numPCs; i++) bw.write("PC"+i+";");
			bw.write("Ncf;Nuf;Ncs;Nus;Nc;Nu;Ns;Nf;");
			List<String> techniques = new ArrayList<String>(allSuspiciousness.keySet());
			for (String technique : techniques) bw.write(technique + ";");
			bw.write("\n");
			
			//For each rule, we have to add one line with all its paramters: first if it's executed, then values of the parameters and last the values for suspiciousness of the different techniques
			List<String> rules = new ArrayList<String>(rulesExecutions.keySet());
			for (String rule : rules){
				//We write the rule name
				bw.write(rule + ";");
				//We write whether it's executed or not ---> POSSIBLE EXTENSION: write the number of times it is executed
				int[] executions = rulesExecutions.get(rule);
				for (int i = 0; i < numPCs; i++) {
					if (executions[i] > 0) bw.write("X");
					bw.write(";");
				}
				//Now we write the values of the different parameters
				bw.write(measures.get(contract).get(rule).get("Ncf").toString() + ";");
				bw.write(measures.get(contract).get(rule).get("Nuf").toString() + ";");
				bw.write(measures.get(contract).get(rule).get("Ncs").toString() + ";");
				bw.write(measures.get(contract).get(rule).get("Nus").toString() + ";");
				bw.write(measures.get(contract).get(rule).get("Nc").toString() + ";");
				bw.write(measures.get(contract).get(rule).get("Nu").toString() + ";");
				bw.write(measures.get(contract).get(rule).get("Ns").toString() + ";");
				bw.write(measures.get(contract).get(rule).get("Nf").toString() + ";");
				//And finally we write the suspiciousness for the different techniques
				for (String technique : techniques){
					bw.write(allSuspiciousness.get(technique).get(contract).get(rule).toString() + ";");
				}
				bw.write("\n");
			}
			
			//Finally, we have to add a line stating for which test cases the contract has failed and for which ones it has passed
			List<String> resultVector = resultVectors.get(contract);
			for (String sat : resultVector){
				if (sat.equals("F")){
					bw.write(";Fail");
				} else if (sat.equals("S")){
					bw.write(";Satisfied");
				} else {
					bw.write(";Undef");
				}			
			}
			bw.close();
			return result;
		}
}

