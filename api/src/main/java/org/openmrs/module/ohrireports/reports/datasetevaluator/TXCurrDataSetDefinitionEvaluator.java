package org.openmrs.module.ohrireports.reports.datasetevaluator;

import static org.openmrs.module.ohrireports.OHRIReportsConstants.ALIVE;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.PATIENT_STATUS;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.REGIMEN;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.RESTART;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.TREATMENT_END_DATE;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ConceptService;
import org.openmrs.module.ohrireports.helper.EthiopianDate;
import org.openmrs.module.ohrireports.helper.EthiopianDateConverter;
import org.openmrs.module.ohrireports.reports.datasetdefinition.TXCurrDataSetDefinition;
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

@Handler(supports = { TXCurrDataSetDefinition.class })
public class TXCurrDataSetDefinitionEvaluator implements DataSetEvaluator {
	
	@Autowired
	EvaluationService evaluationService;
	
	@Autowired
	ConceptService conceptService;
    HashMap<Integer,Concept> patientStatus = new HashMap<>();
	@Override
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evalContext) throws EvaluationException {
		
		TXCurrDataSetDefinition hdsd = (TXCurrDataSetDefinition) dataSetDefinition;
		
		SimpleDataSet data = new SimpleDataSet(dataSetDefinition, evalContext);
		
		List<Obs> obsList = getTxCurrPatients(hdsd, evalContext);
		
		DataSetRow row = null;
     
		for (Obs obses : obsList) {
			
				Person person = obses.getPerson();
				Concept status = patientStatus.get(person.getId());
				EthiopianDate ethiopianDate = null;
				try {
					ethiopianDate=	EthiopianDateConverter.ToEthiopianDate(obses.getValueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                row = new DataSetRow();
                row.addColumnValue(new DataSetColumn("PersonID", "#", Integer.class), person.getPersonId());
                row.addColumnValue(new DataSetColumn("Name", "Name", String.class), person.getNames());
                row.addColumnValue(new DataSetColumn("Age", "Age", Integer.class), person.getAge());
                row.addColumnValue(new DataSetColumn("Gender", "Gender", Integer.class), person.getGender());
                row.addColumnValue(new DataSetColumn("TreatmentEndDate", "Treatment End Date", 
				Date.class), obses.getValueDate());	
				row.addColumnValue(new DataSetColumn("TreatmentEndDateETC", "Treatment End Date ETH", 
				String.class),ethiopianDate.equals(null)? "": ethiopianDate.getDay()+"/"+ethiopianDate.getMonth()+"/"+ethiopianDate.getYear());	
				row.addColumnValue(new DataSetColumn("Regimen","Regmin",String.class), getRegmin(obses,evalContext));	
				row.addColumnValue(new DataSetColumn("Status", "Status", 
				String.class), status.equals(null)?"":status.getName().getName());
				data.addRow(row);
            
		
		}
		return data;
	}

	private List<Obs> getTxCurrPatients(TXCurrDataSetDefinition hdsd, EvaluationContext context) {
		
		List<Integer> patientsId = getListOfALiveORRestartPatientObservertions(context, hdsd);
		
		List<Person> patients = new ArrayList<>();
		List<Obs> obseList = new ArrayList<>();
		
		if (patientsId ==null || patientsId.size()==0) 
		return obseList;
		HqlQueryBuilder queryBuilder = new HqlQueryBuilder();
		queryBuilder.select("obv");
		queryBuilder.from(Obs.class,"obv")
		.whereEqual("obv.encounter.encounterType", hdsd.getEncounterType())
	  	.and()
		.whereEqual("obv.concept",conceptService.getConceptByUuid(TREATMENT_END_DATE))
		.and()
		.whereGreater("obv.valueDatetime", hdsd.getEndDate())
		.and()
		.whereLess("obv.obsDatetime", hdsd.getEndDate())
		.whereIdIn("obv.personId", patientsId)
        .orderDesc("obv.personId,obv.obsDatetime") ;

		for (Obs obs : evaluationService.evaluateToList(queryBuilder, Obs.class, context)) {
			if(!patients.contains(obs.getPerson()))
			  {
				patients.add(obs.getPerson());
				obseList.add(obs);
			  }
		}
		return obseList;
	}
	private List<Integer> getListOfALiveORRestartPatientObservertions(EvaluationContext context,TXCurrDataSetDefinition hdsd) {
      
        List<Integer> uniqiObs =new ArrayList<>();
        HqlQueryBuilder queryBuilder = new HqlQueryBuilder();
      
        queryBuilder.select("obv")
        .from(Obs.class,"obv")
		.whereEqual("obv.encounter.encounterType", hdsd.getEncounterType())
		.and()
      	.whereEqual("obv.concept", conceptService.getConceptByUuid(PATIENT_STATUS))
		.and()
		.whereIn("obv.valueCoded", Arrays.
		asList(conceptService.getConceptByUuid(ALIVE),
		conceptService.getConceptByUuid(RESTART)))
		.and().whereLess("obv.obsDatetime", hdsd.getEndDate());
		queryBuilder.orderDesc("obv.personId,obv.obsDatetime");

		List<Obs> liveObs = evaluationService.evaluateToList(queryBuilder,Obs.class, context);
		

        for (Obs obs :liveObs){
			if(!uniqiObs.contains(obs.getPersonId())){
				uniqiObs.add(obs.getPersonId());
				patientStatus.put(obs.getPersonId(), obs.getValueCoded());
			}	
		}

        return uniqiObs;
    }
	
	private String getRegmin(Obs obs, EvaluationContext context) {
		HqlQueryBuilder queryBuilder = new HqlQueryBuilder();
		
		queryBuilder.select("obv.valueCoded").from(Obs.class, "obv")
		        .whereInAny("obv.concept", conceptService.getConceptByUuid(REGIMEN))
		        .whereEqual("obv.encounter", obs.getEncounter())
				.and().whereEqual("obv.person", obs.getPerson())
		        .orderDesc("obv.obsDatetime").limit(1);
		List<Concept> concepts = evaluationService.evaluateToList(queryBuilder, Concept.class, context);
		
		Concept data = null;
		if (concepts != null && concepts.size() > 0)
			data = concepts.get(0);
		
		return data == null ? "" : data.getName().getName();
	}
}
