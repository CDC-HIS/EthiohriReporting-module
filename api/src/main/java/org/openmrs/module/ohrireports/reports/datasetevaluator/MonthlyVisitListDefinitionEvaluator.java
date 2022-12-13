package org.openmrs.module.ohrireports.reports.datasetevaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.*;

import org.hibernate.annotations.Where;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.module.ohrireports.reports.datasetdefinition.MonthlyVisitListDataSetDefinition;
import org.openmrs.module.ohrireports.reports.library.PatientDataLibrary;
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

@Handler(supports = { MonthlyVisitListDataSetDefinition.class })
public class MonthlyVisitListDefinitionEvaluator implements DataSetEvaluator {
	
	@Autowired
	private ConceptService conceptService;
	
	@Autowired
	private EncounterService encounterService;
	
	private MonthlyVisitListDataSetDefinition mDataSetDefinition;
	
	@Autowired
	private EvaluationService evaluationService;
	
	private EvaluationContext evalContext;
	
	private List<Concept> excludedConcepts = new ArrayList<>();
	
	
	/*
	 * Load all encounter with encounter type followup based on the given date range
	 * Get all observation except Transfer out, lost on follow up , dead,discharge
	 * 
	 */
	@Override
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evalContext) throws EvaluationException {
		this.evalContext = evalContext;
		excludedConcepts = getValueCodedConcepts();
		mDataSetDefinition = (MonthlyVisitListDataSetDefinition) dataSetDefinition;
		SimpleDataSet dataSet = new SimpleDataSet(mDataSetDefinition, evalContext);
		List<Encounter> encounters = filteredEncounters();
		Concept patientWeighConcept = conceptService.getConceptByUuid(PATIENT_WEIGHT);
		Concept artStartConcept = conceptService.getConceptByUuid(ART_START_DATE);
		Concept patientStatusConcept = conceptService.getConceptByUuid(PATIENT_STATUS);
		Concept patientMobileNumberConcept = conceptService.getConceptByUuid(PATIENT_PHONE_NUMBER);
		int seq=1;
		for (Encounter encounter : encounters) {
			Patient patient = encounter.getPatient();
			DataSetRow dataSetRow = new DataSetRow();
			dataSetRow.addColumnValue(new DataSetColumn("Seq", "#", Integer.class), seq);
			dataSetRow.addColumnValue(new DataSetColumn("Person Name", "FullName", String.class), patient.getPersonName());
			dataSetRow.addColumnValue(new DataSetColumn("Age", "Age", Integer.class), patient.getAge());
			dataSetRow.addColumnValue(new DataSetColumn("Gender", "Sex", String.class), patient.getGender());
			
			for (Obs obs : encounter.getAllObs()) {
				if (obs.getConcept().equals(patientWeighConcept))
					dataSetRow.addColumnValue(new DataSetColumn("Weight", "Weight", Double.class), obs.getValueNumeric());
				if (obs.getConcept().equals(artStartConcept))
					dataSetRow.addColumnValue(new DataSetColumn("ArtStarted", "Art Start Date", Date.class),
					    obs.getValueDate());
				if (obs.getConcept().equals(patientStatusConcept) && obs.getValueCodedName() != null)
					dataSetRow.addColumnValue(new DataSetColumn("FollowUpStatus", "Follow Up Status", String.class), obs
					        .getValueCodedName().getName());
				if(obs.getConcept().equals(patientMobileNumberConcept))
				dataSetRow.addColumnValue(new DataSetColumn("Mobile", "Mobile", String.class), obs
					        .getValueText());
			}

			seq++;
			dataSet.addRow(dataSetRow);
		}
		return dataSet;
	}
	
	private List<Encounter> getEncountersByDateRange() {
		HqlQueryBuilder queryBuilder = new HqlQueryBuilder().select("enc").from(Encounter.class, "enc")
		        .whereEqual("enc.encounterType", encounterService.getEncounterTypeByUuid(HTS_FOLLOW_UP_ENCOUNTER_TYPE))
		        
		        .whereGreaterEqualOrNull("enc.encounterDatetime", mDataSetDefinition.getFromDate()).and()
		        .whereLessOrEqualTo("enc.encounterDatetime", mDataSetDefinition.getToDate());
		
		return evaluationService.evaluateToList(queryBuilder, Encounter.class, evalContext);
	}
	
	private List<Encounter> filteredEncounters() {
		
		List<Encounter> encounters = getEncountersByDateRange();
		List<Encounter> filteredEncounters = new ArrayList<Encounter>();
		boolean isValidEncounter = true;
		for (Encounter encounter : encounters) {
			for (Obs obs : encounter.getObs()) {
				isValidEncounter = isValidEncounter(obs);
				
			}
			
			if (isValidEncounter)
				filteredEncounters.add(encounter);
		}
		
		return filteredEncounters;
	}
	
	private boolean isValidEncounter(Obs obs) {
		if(excludedConcepts.size()==0)
		return true;
		for (Concept concept : excludedConcepts) {
			if(obs.getValueCoded().equals(concept))
			return false;
		}
		return true;
	}
	
	private List<Concept> getValueCodedConcepts() {
		
		return Arrays.asList(
			conceptService.getConceptByUuid(RAN_AWAY),
		    conceptService.getConceptByUuid(DECEASED),
		    conceptService.getConceptByUuid(DISCHARGED),
		    conceptService.getConceptByUuid(LOST_TO_FOLLOW_UP)
			);
	}
	
}
