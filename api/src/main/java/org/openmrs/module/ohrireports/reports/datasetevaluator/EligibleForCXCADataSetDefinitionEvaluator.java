package org.openmrs.module.ohrireports.reports.datasetevaluator;

import static org.openmrs.module.ohrireports.OHRIReportsConstants.ADHERENCE_UUID;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.ART_START_DATE;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.COLPOSCOPY_TEST;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.CRYOTHERAPY_TREATMENT;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.CYTOLOGY_TEST;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.HPV_TEST;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.LEEP_TREATMENT;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.NEGATIVE;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.POSITIVE;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.SERVICE_DELIVERY_POINT_NUMBER_MRN;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.UNIQUE_ANTIRETROVAIRAL_THERAPY_UAN;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.VIA_TEST;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.joda.time.DateTime;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ConceptService;
import org.openmrs.module.ohrireports.reports.datasetdefinition.EligibleForCXCADataSetDefinition;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
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
	private ConceptService conceptService;

	@Autowired
	private EvaluationService evaluationService;

	private EvaluationContext evalContext;

	private Concept artStarted;
	private Map<Integer, String> reasonForEligibility = new HashMap<>();

	@Override
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evalContext)
			throws EvaluationException {
		this.evalContext = evalContext;
		artStarted = conceptService.getConceptByUuid(ART_START_DATE);

		eligForCXCADataSetDefinition = (EligibleForCXCADataSetDefinition) dataSetDefinition;

		SimpleDataSet dataSet = new SimpleDataSet(eligForCXCADataSetDefinition, evalContext);
		if ( Objects.isNull(eligForCXCADataSetDefinition.getEndDate()) )
			eligForCXCADataSetDefinition.setEndDate(Calendar.getInstance().getTime());

		populateReasonForEligibility();

		Map<Person, Integer> eligibleFemaleForCXCA = getListOfEligiblesForCXCA();

		int sequence = 1;

		eligibleFemaleForCXCA.forEach((p, k) -> {
			DataSetRow dataSetRow = new DataSetRow();
			// Creating data column and adding to that.
			dataSetRow.addColumnValue(new DataSetColumn("PatientName", "Patient Name", String.class),
					p.getPersonName());
			dataSetRow.addColumnValue(new DataSetColumn("seq", "#", Integer.class), sequence);
			dataSetRow.addColumnValue(new DataSetColumn("MRN", "MRN", String.class), getPatientMRN(p));
			dataSetRow.addColumnValue(new DataSetColumn("UAN", "UAN", Integer.class), getPatientUAN(p));
			dataSetRow.addColumnValue(new DataSetColumn("AGE", "Age", Integer.class), p.getAge());
			dataSetRow.addColumnValue(new DataSetColumn("Next Visit Date", "Age", Integer.class), p.getAge());
			dataSetRow.addColumnValue(new DataSetColumn("LastFollowUp", "Last Follow Up", Date.class),
					GetLastFollowUp(p));
			dataSetRow.addColumnValue(new DataSetColumn("LastARTRegimen ", "Last ART Regimen ", String.class),
					getLastArtRegiment(p));
			dataSetRow.addColumnValue(new DataSetColumn("Adherence", "Adherence", String.class), getAdherence(p));
			dataSetRow.addColumnValue(new DataSetColumn("ReasonForEligibility", "Reason For Eligibility", String.class),
					reasonForEligibility.get(k));
			dataSet.addRow(dataSetRow);
			// Next Visit Date
		});
		return dataSet;
	}

	private void populateReasonForEligibility() {
		reasonForEligibility.put(1, "Never screened for CxCa");
		reasonForEligibility.put(2, "VIA Screened Negative-Need Re-screening (after 2 years)");
		reasonForEligibility.put(3, "HPV Screened Negative-Need Re-screening  (after 3 years)");
		reasonForEligibility.put(4, "Cytology Screened Negative-Need Re-screening (after 3 years)");
		reasonForEligibility.put(5, "HPV Positive but VIA Negative-Need Re-screening (after 1 year)");
		reasonForEligibility.put(6, "Treated with Cryotherapy/ Thermocoagulation/ LEEP (after 6 months)");
		reasonForEligibility.put(7, "Positive Result with No Treatment Date");

	}

	private String getAdherence(Person patient) {
		// TODO: adherence and should be updated
		Concept adhConcept = evaluationService.evaluateToObject(
				new HqlQueryBuilder()
						.select("obs.valueCoded")
						.from(Obs.class, "obs")
						.whereBetweenInclusive("obs.obsDatetime",
						 eligForCXCADataSetDefinition.getStartDate(),
								eligForCXCADataSetDefinition.getEndDate())
						.whereEqual("obs.concept", conceptService.getConceptByUuid(ADHERENCE_UUID)),
				null, evalContext);
		return Objects.isNull(adhConcept) ? "" : adhConcept.getName().getName();

	}

	private String getPatientUAN(Person patient) {
		return evaluationService.evaluateToObject(
				new HqlQueryBuilder()
						.select("obs.valueText")
						.from(Obs.class, "obs")
						.whereEqual("obs.concept",
								conceptService.getConceptByUuid(UNIQUE_ANTIRETROVAIRAL_THERAPY_UAN))
						.and()
						.whereEqual("obs.person", patient)
						.whereBetweenInclusive("obs.obsDatetime",
								eligForCXCADataSetDefinition.getStartDate(), eligForCXCADataSetDefinition.getEndDate())
						.limit(1)
						.orderDesc("obs.obsDatetime"),
				String.class, evalContext);
	}

	private String getLastArtRegiment(Person person) {
		// TODO: art regiment concept to be created
		Concept regConcept = evaluationService.evaluateToObject(new HqlQueryBuilder()
				.select("obs.valueCoded")
				.from(Obs.class, "obs")
				.whereEqual("obs.person", person)
				.whereBetweenInclusive("obs.obsDatetime",
						eligForCXCADataSetDefinition.getStartDate(),
						eligForCXCADataSetDefinition.getEndDate())
				.limit(1)
				.orderDesc("obs.obsDatetime"), Concept.class, evalContext);
		return Objects.isNull(regConcept) ? "" : regConcept.getName().getName();

	}

	private Object GetLastFollowUp(Person person) {
		EncounterType encounterType = evaluationService
				.evaluateToObject(new HqlQueryBuilder()
						.select("encounter.encounterType")
						.from(Encounter.class, "encounter")
						.whereEqual("encounter.patient", person)
						.whereBetweenInclusive("encounter.encounterDatetime",
								eligForCXCADataSetDefinition.getStartDate(),
								eligForCXCADataSetDefinition.getEndDate())
						.limit(1), EncounterType.class, evalContext);

		return Objects.isNull(encounterType) ? "" : encounterType.getName();
	}

	private String getPatientMRN(Person person) {
		return evaluationService.evaluateToObject(new HqlQueryBuilder()
				.select("obs.valueText")
				.from(Obs.class, "obs")
				.whereEqual("obs.concept", conceptService.getConceptByUuid(SERVICE_DELIVERY_POINT_NUMBER_MRN))
				.and()
				.whereEqual("obs.person", person)
				.whereBetweenInclusive("obs.obsDatetime",
						eligForCXCADataSetDefinition.getStartDate(), eligForCXCADataSetDefinition.getEndDate())
				.limit(1)
				.orderDesc("obs.obsDatetime"), String.class, evalContext);
	}

	private Map<Person, Integer> getListOfEligiblesForCXCA() {
		// Get list of female >=15 age
		Map<Person, Integer> personList = getListOfEligiblesForCXCA(loadPatients());

		return personList;
	}

	private Map<Person, Integer> getListOfEligiblesForCXCA(List<Person> loadPatients) {
		Map<Person, Integer> eligiblePersons = new HashMap<>();
		for (Person person : loadPatients) {
		
			// Case 1 - Never screened for CxCa – If there is no Screening Date/Sample
			// Collection Date for any of the Screening Strategies - HPV, VIA, Cytology,
			// Colposcopy and Biopsy.
			if (!hasScreenedForCXCA(person)) {
				eligiblePersons.put(person, 1);
			}
			// Case 2 - VIA Screened Negative-Need Re-screening – 2 years after VIA
			// Screening Result = Negative
			else if (isVIAScreenedNegative(person)) {
				eligiblePersons.put(person, 2);
			}
			// Case 3 - HPV Screened Negative-Need Re-screening - 3 years after HPV
			// Screening Result = Negative
			else if (isHPVScreenedNegative(person)) {
				eligiblePersons.put(person, 3);
			}
			// Case 4 - Cytology Screened Negative-Need Re-screening - 3 years after
			// Cytology Result = Negative
			else if (isCytologyScreenedNegative(person)) {
				eligiblePersons.put(person, 4);

			}
			// Case 5 - HPV Positive but VIA Negative-Need Re-screening - Eligible for
			// rescreen after 1 year from VIA screening date.
			else if (isHPVPositiveAndVIANegative(person)) {
				eligiblePersons.put(person, 5);
			}
			// Case 6 - Treated with Cryotherapy/ Thermocoagulation/ LEEP - Eligible after 6
			// months of the latest treatment Date.
			else if (isTreatedWithCryotherapy(person)) {
				eligiblePersons.put(person, 6);

			}
			// Case 7 - Positive Result with No Treatment Date - Current Date will be
			// eligibility date.
			else if (isPositiveResultWithNoTreatment(person)) {
				eligiblePersons.put(person, 7);

			}
		}
		return eligiblePersons;
	}

	private boolean isPositiveResultWithNoTreatment(Person person) {
		if (!isPositiveInOneOfTheTest(person))
			return false;

		return evaluationService
				.evaluateToObject(
						new HqlQueryBuilder()
				.select("COUNT(obs)")
				.from(Obs.class, "obs")
								.whereEqual("obs.person", person)
								.and()
								.whereIn("obs.concept", getListOfCXCATreatments())
								.whereBetweenInclusive("obs.obsDatetime",
										eligForCXCADataSetDefinition.getStartDate(),
										eligForCXCADataSetDefinition.getEndDate()),
						Integer.class, evalContext) > 0;
	}

	private boolean isPositiveInOneOfTheTest(Person person) {
		return evaluationService.evaluateToObject(new HqlQueryBuilder()
				.select("COUNT(obs)")
				.from(Obs.class, "obs")
				.whereIn("obs.concept", getListOfCXCATest())
				.and()
				.whereEqual("obs.valueCoded", conceptService.getConceptByUuid(POSITIVE))
				.whereEqual("obs.person", person)
				.whereBetweenInclusive("obs.obsDatetime",
						eligForCXCADataSetDefinition.getStartDate(),
						eligForCXCADataSetDefinition.getEndDate()),
				Integer.class, evalContext) > 0;
	}

	private List<Concept> getListOfCXCATest() {
		return Arrays.asList(
				conceptService.getConceptByUuid(HPV_TEST),
				conceptService.getConceptByUuid(COLPOSCOPY_TEST),
				conceptService.getConceptByUuid(VIA_TEST));
	}

	private boolean isTreatedWithCryotherapy(Person person) {
		return evaluationService
				.evaluateToObject(
						new HqlQueryBuilder()
								.select("COUNT(obs)")
								.from(Obs.class, "obs")
								.whereEqual("obs.person", person)
								.whereIn("obs.concept", getListOfCXCATreatments())
								.whereBetweenInclusive("obs.obsDatetime",
										eligForCXCADataSetDefinition.getStartDate(),

										formatDate(eligForCXCADataSetDefinition.getEndDate(),Calendar.MONTH,-6)),
						Integer.class, evalContext) > 0;
	}

	private List<Concept> getListOfCXCATreatments() {
		return Arrays.asList(
				conceptService.getConceptByUuid(LEEP_TREATMENT),
				conceptService.getConceptByUuid(CRYOTHERAPY_TREATMENT));
	}

	private boolean isHPVPositiveAndVIANegative(Person person) {
		if (isHPVPositive(person)) {
			return evaluationService
					.evaluateToObject(
							new HqlQueryBuilder()
									.select("COUNT(obs)")
									.from(Obs.class, "obs")
									.whereEqual("obs.concept", conceptService.getConceptByUuid(CYTOLOGY_TEST))
									.and()
									.whereEqual("obs.valueCode", conceptService.getConceptByUuid(NEGATIVE))
									.and()
									.whereBetweenInclusive("obs.obsDatetime",
											eligForCXCADataSetDefinition.getStartDate(),
											formatDate(eligForCXCADataSetDefinition.getEndDate(),-3))
									.whereEqual("obs.person", person),
							Integer.class, evalContext) > 0;
		}
		return false;
	}

	private boolean isHPVPositive(Person person) {
		return evaluationService
				.evaluateToObject(
						new HqlQueryBuilder()
								.select("COUNT(obs)")
								.from(Obs.class, "obs")
								.whereEqual("obs.concept", conceptService.getConceptByUuid(HPV_TEST))
								.and()
								.whereEqual("obs.valueCode", conceptService.getConceptByUuid(NEGATIVE))
								.and()
								.whereEqual("obs.person", person)
								.whereBetweenInclusive("obs.obsDatetime",
										eligForCXCADataSetDefinition.getStartDate(),
										eligForCXCADataSetDefinition.getEndDate()),
						Integer.class, evalContext) > 0;
	}

	private boolean isCytologyScreenedNegative(Person person) {
		return evaluationService
				.evaluateToObject(
						new HqlQueryBuilder()
								.select("COUNT(obs)")
								.from(Obs.class, "obs")
								.whereEqual("obs.concept", conceptService.getConceptByUuid(CYTOLOGY_TEST))
								.and()
								.whereEqual("obs.valueCode", conceptService.getConceptByUuid(NEGATIVE))
								.and()
								.whereBetweenInclusive("obs.obsDatetime",
										eligForCXCADataSetDefinition.getStartDate(),
										formatDate(eligForCXCADataSetDefinition.getEndDate(),-3))
								.whereEqual("obs.person", person),
						Integer.class, evalContext) > 0;
	}

	private boolean isHPVScreenedNegative(Person person) {
		return evaluationService
				.evaluateToObject(
						new HqlQueryBuilder()
								.select("COUNT(obs)")
								.from(Obs.class, "obs")
								.whereEqual("obs.concept", conceptService.getConceptByUuid(HPV_TEST))
								.and()
								.whereEqual("obs.valueCode", conceptService.getConceptByUuid(NEGATIVE))
								.and()
								.whereBetweenInclusive("obs.obsDatetime",
										eligForCXCADataSetDefinition.getStartDate(),
										formatDate(eligForCXCADataSetDefinition.getEndDate(),-3))
								.whereEqual("obs.person", person),
						Integer.class, evalContext) > 0;
	}

	private boolean isVIAScreenedNegative(Person person) {
		return evaluationService
				.evaluateToObject(
						new HqlQueryBuilder()
								.select("COUNT(obs)")
								.from(Obs.class, "obs")
								.whereEqual("obs.concept", conceptService.getConceptByUuid(VIA_TEST))
								.and()
								.whereEqual("obs.valueCode", conceptService.getConceptByUuid(NEGATIVE))
								.and()
								.whereLessOrEqualTo("obs.obsDatetime",
										formatDate(eligForCXCADataSetDefinition.getEndDate(),-2))
								.whereEqual("obs.person", person)
								.whereBetweenInclusive("obs.obsDatetime",
										eligForCXCADataSetDefinition.getStartDate(),
										eligForCXCADataSetDefinition.getEndDate()),
						Integer.class, evalContext) > 0;
	}

	private boolean hasScreenedForCXCA(Person person) {
		return evaluationService
				.evaluateToObject(
						new HqlQueryBuilder()
								.select("COUNT(obs)")
								.from(Obs.class, "obs")
								.whereIn("obs.concept", Arrays.asList(
										conceptService.getConceptByUuid(HPV_TEST),
										conceptService.getConceptByUuid(VIA_TEST),
										conceptService.getConceptByUuid(CYTOLOGY_TEST),
										conceptService.getConceptByUuid(COLPOSCOPY_TEST)))
								.and()
								.whereBetweenInclusive("obs.obsDatetime",
										eligForCXCADataSetDefinition.getStartDate(),
										eligForCXCADataSetDefinition.getEndDate())
								.whereEqual("obs.person", person),
						Integer.class, evalContext) > 0;
	}

	

	private List<Person> loadPatients() {
		HqlQueryBuilder queryBuilder = new HqlQueryBuilder();
		
		queryBuilder
				.select("obs.person")
				.from(Obs.class, "obs")
				.whereEqual("obs.person.gender", "F")
				.and()
				.whereEqual("obs.concept", artStarted)
				.and()
				.whereBetweenInclusive("obs.valueDatetime",
						eligForCXCADataSetDefinition.getStartDate(),
						eligForCXCADataSetDefinition.getEndDate())
				.and()
				.whereLessOrEqualTo("obs.person.birthdate", formatDate(eligForCXCADataSetDefinition.getEndDate(),-15));

		return evaluationService.evaluateToList(queryBuilder, Person.class, evalContext);

	}

	private Date formatDate(Date date,int value){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, value);
		return calendar.getTime();
	}
	private Date formatDate(Date date,int CalenderType,int value){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(CalenderType, value);
		return calendar.getTime();
	}
}
