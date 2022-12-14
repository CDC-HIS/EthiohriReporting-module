package org.openmrs.module.ohrireports.reports.datasetevaluator;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.annotation.Handler;
import org.openmrs.module.ohrireports.reports.datasetdefinition.EligibleForCXCADataSetDefinition;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

/* ==========================================================================================
 * Report Tasks
 * ==========================================================================================
 * Eligible for CxCa
 * To list all female patients >=15 years old and Eligible for Cervical Cancer Screening 
 * Report Data Elements (Grid Data Elements) 
 * Concept ID 
 * Patient Name 
 * MRN 
 * UAN 
 * Age 
 * Last Follow-up Date 
 * ART Start Date 
 * Last ART Regimen 
 * Adherence 
 * Next Visit Date 
 * Last Follow-up Status 
 * Mobile/Home Tel.No 
 
 * Reason for Eligibility 
 * ====================================================================================================================================
 * Case 1 - Never screened for CxCa – If there is no Screening Date/Sample Collection Date for any of the Screening Strategies - HPV, VIA, Cytology, Colposcopy and Biopsy.   

 * Case 2 - VIA Screened Negative-Need Re-screening – 2 years after VIA Screening Result = Negative   

 * Case 3 - HPV Screened Negative-Need Re-screening - 3 years after HPV Screening Result = Negative  

 * Case 4 - Cytology Screened Negative-Need Re-screening - 3 years after Cytology Result = Negative 

 * Case 5 - HPV Positive but VIA Negative-Need Re-screening - Eligible for rescreening after 1 year from VIA screening date.   

 * Case 6 - Treated with Cryotherapy/ Thermocoagulation/ LEEP - Eligible after 6 months of the latest treatment Date.   

 * Case 7 - Positive Result with No Treatment Date - Current Date will be eligibility date.  

 *** There should be an option to alphabetically order the line list report using all the grid data elements	

 * Key Assumptions 
 * =================================================================================================================
 * The list will show those with Eligibility date <= Current Date as default. 

 * If specific period selected the list will show those Eligible patients whose next appointment date is within the specified period 

 * The default value for all the filtration criteria will be ‘All’ 
 
 * The report query range will include data on the start and end date of the reporting period  

 * he user will have the option to filter the report using the criteria listed on the Filtration Criteria section of this document. 

 * Pseudo Algorithm 
 * =====================================================================================================================
 * Before a patient record may be included into the report dataset, the following pseudo algorithm must pass: 
 * If ART Started = Yes 
 *     AND 
 * If there is an ART Start Date and the ART Start Date is <= the reporting date 
 *     AND 
 * If Patient’s Age >=15 years old 
 *    AND 
 * If Patient’s sex=Female 
 *    AND 
 * If There is no Screening Date/Sample Collection Date for any of the Screening Strategies - HPV, VIA, Cytology, Colposcopy and Biopsy 
 *    AND   
 * If Date of VIA Screening Result = Negative + 2 years <= Reporting End Date 
 *    AND    
 * If Date of HPV Screening Result = Negative + 3 years <= Reporting End Date 
 *    AND 
 * If Date of Cytology Screening Result = Negative + 3 years <= Reporting End Date 
 *    AND 
 * If Date of HPV Positive but VIA Negative + 1 year <= Reporting End Date 
 *    AND   
 * If Date of Latest treatment with Cryotherapy/ Thermocoagulation/ LEEP + 6 months <= Reporting End Date 
 *    AND   
 * If Positive Result with No Treatment Date  
 *    ONLY THEN 
 * count the record 
 * Filtration Criteria 
 * Next Visit Date (range)-------- From(Date) – To(Date) 
 * Follow-up Status (All, Alive on ART, Restart, Stop) 
 * =========================================================================================================
 * Reason for Eligibility: 
 * ========================================================================================================= 
 * All
 * Never screened for CxCa 
 * VIA Screened Negative-Need Re-screening 
 * HPV Screened Negative-Need Re-screening 
 * Cytology Screened Negative-Need Re-screening 
 * HPV Positive but VIA Negative- Need Re-screening 
 * Treated with Cryotherapy/Thermocoagulation/LEEP 
 * Positive Result with No Treatment Date 
 * Disaggregation 
 * Not Applicable 
 */
@Handler(supports = { EligibleForCXCADataSetDefinition.class })
public class EligibleForCXCADataSetDefinitionEvaluator implements DataSetEvaluator {
	
	private EligibleForCXCADataSetDefinition eligForCXCADataSetDefinition;
	
	@Autowired
	private EvaluationService evaluationService;
	
	private EvaluationContext evalContext;
	
	@Override
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evalContext) throws EvaluationException {
		this.evalContext = evalContext;
		eligForCXCADataSetDefinition = (EligibleForCXCADataSetDefinition) dataSetDefinition;
		SimpleDataSet dataSet = new SimpleDataSet(eligForCXCADataSetDefinition, evalContext);
		List<Patient> eligibleFemaleForCXCA = getListOfEligiblesForCXCA();
		
		return dataSet;
	}
	
	private List<Patient> getListOfEligiblesForCXCA() {
		//Get list of female >=15 age
		List<Person> personList = getListOfEligiblesForCXCA(loadPatients());
		
		return null;
	}
	
	private List<Person> getListOfEligiblesForCXCA(List<Person> loadPatients) {
        List<Person> eligiblePersons = new ArrayList<>();
        for (Person person : loadPatients) {
            if(!isArtStarted(person))
                continue;
            //Case 1 - Never screened for CxCa – If there is no Screening Date/Sample Collection Date for any of the Screening Strategies - HPV, VIA, Cytology, Colposcopy and Biopsy. 
            if(!hasScreenedForCXCA(person)){
              eligiblePersons.add(person);              
            }
            //Case 2 - VIA Screened Negative-Need Re-screening – 2 years after VIA Screening Result = Negative 
            else if (isVIAScreenedNegative(person)) {
              eligiblePersons.add(person);                
            }
            //Case 3 - HPV Screened Negative-Need Re-screening - 3 years after HPV Screening Result = Negative 
            else if(isHPVScreenedNegative(person)){
              eligiblePersons.add(person);                
            }
            //Case 4 - Cytology Screened Negative-Need Re-screening - 3 years after Cytology Result = Negative
            else if(isCytologyScreenedNegative(person)){
              eligiblePersons.add(person);
            }
            //Case 5 - HPV Positive but VIA Negative-Need Re-screening - Eligible for rescreening after 1 year from VIA screening date.
            else if(isHPVPositiveAndVIANegative(person)){
              eligiblePersons.add(person);
            }
            //Case 6 - Treated with Cryotherapy/ Thermocoagulation/ LEEP - Eligible after 6 months of the latest treatment Date.  
            else if(isTreatedWithCryotherapy(person)){
              eligiblePersons.add(person);

            }
            //Case 7 - Positive Result with No Treatment Date - Current Date will be eligibility date.
            else if(isPositiveResultWithNoTreatment(person)){
              eligiblePersons.add(person);
                
            }
        }
        return null;
    }
	
	private boolean isPositiveResultWithNoTreatment(Person person) {
		return false;
	}
	
	private boolean isTreatedWithCryotherapy(Person person) {
		return false;
	}
	
	private boolean isHPVPositiveAndVIANegative(Person person) {
		return false;
	}
	
	private boolean isCytologyScreenedNegative(Person person) {
		return false;
	}
	
	private boolean isHPVScreenedNegative(Person person) {
		return false;
	}
	
	private boolean isVIAScreenedNegative(Person person) {
		return false;
	}
	
	private boolean hasScreenedForCXCA(Person person) {
		return false;
	}
	
	private boolean isArtStarted(Person person) {
		return false;
	}
	
	private List<Person> loadPatients() {
		HqlQueryBuilder queryBuilder = new HqlQueryBuilder();
		queryBuilder.select("person").from(Person.class, "person").whereEqual("person.gender", "F")
		        .whereGreaterOrEqualTo("person.birthdate", DateTime.now().plusYears(15));
		return evaluationService.evaluateToList(queryBuilder, Person.class, evalContext);
	}
	
}
