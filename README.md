# SBFL-Symbolic-Execution-MT
Fault Localization in Model Transformations by Combining Symbolic Execution and Spectrum-Based Analysis

This projects supports and verifies the evaluation performed in [1].

Within this repository there is an Eclipse project, named `SpecBased_FaultLoc_SyVOLT`. This project contains the executable to compute the suspiciousness values with the 18 techniques. 

**Input** 

There is a folder for each of the nine case studies presented in the evaluation of [1]. Inside each of the folders, there is an XML file which is the output provided by the SyVOLT tool, where the symbolic execution of all the mutations and contracts are provided (refer to ). This is the *input* for the spectrum-based fault localization.

**Executable Class**

The executable Java file is available at
`src->es.us.eii.fault.loc.syvolt.mt.main-> FaultLocalizationMT_Main`. To execute it with the different case studies, some lines of code must be commented and uncommented. After doing so, the class can be executed by right-clicking and selecting `Run As` -> `Java Application`. The code contains explanations for executing each case study in lines 38-48 of `FaultLocalizationMT_Main.java`

**Output**

When the execution finishes, the following outputs are provided in the corresponding results folder:
- A folder is created for each mutant for which at least one contract is not satisfied in at least one PC. Inside the folder, we can find:
    - A CSV file for each contract that is not satisfied, where the coverage matrix, error vector and suspiciousness-based rankings for all 18 techniques are computed.
    - A CSV summarizing the suspiciousness-based rankings for all contracts that have not been satisfied. EXAM scores are also provided
- A file named results_AC.csv with EXAM scores values for all mutants and all 18 techniques in the average-case scenario
- A file named results_BC.csv with EXAM scores values for all mutants and all 18 techniques in the best-case scenario
- A file named results_WC.csv with EXAM scores values for all mutants and all 18 techniques in the worst-case scenario
- A file named contractsPercentages.csv with statistics of the contracts according to the classification given in the paper

**z_OverallResults**

Additionally, this folder comprises the set of data that has been analyzed for the paper. This has been obtained from several executions with the different inputs

**References**

[1] Bentley J. Oakes, Javier Troya, Jessie Galasso, Manuel Wimmer. *Fault Localization in Model Transformations by Combining Symbolic Execution and Spectrum-Based Analysis*. Submitted, 2022

