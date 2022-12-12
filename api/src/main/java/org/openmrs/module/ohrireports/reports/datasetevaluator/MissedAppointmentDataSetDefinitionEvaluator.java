package org.openmrs.module.ohrireports.reports.datasetevaluator;

import static org.openmrs.module.ohrireports.OHRIReportsConstants.ALIVE;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.PATIENT_STATUS;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.REGIMEN;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.RESTART;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.TREATMENT_END_DATE;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.openmrs.module.ohrireports.reports.datasetdefinition.MissedAppointmentsDataSetDefinition;
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
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.Null;

@Handler(supports = { MissedAppointmentsDataSetDefinition.class })
public class MissedAppointmentDataSetDefinitionEvaluator implements DataSetEvaluator {
	
	@Autowired
	EvaluationService evaluationService;
	
	@Autowired
	ConceptService conceptService;
    HashMap<Integer,Concept> patientStatus = new HashMap<>();
	@Override
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evalContext) throws EvaluationException {
		
		MissedAppointmentsDataSetDefinition mads = (MissedAppointmentsDataSetDefinition) dataSetDefinition;
		
		SimpleDataSet data = new SimpleDataSet(dataSetDefinition, evalContext);
		
		List<Obs> obsList = getTxCurrPatients(mads, evalContext);
		
		DataSetRow row = null;
     
		for (Obs obses : obsList) {
				Person person = obses.getPerson();
				Concept status = patientStatus.get(person.getId());
				Boolean s = true;
				EthiopianDate ethiopianDate = null;
				long diffrence = 0;
				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
				try {
					long numberOfMissed = formatter.parse(current_date()).getTime() - formatter.parse(formatter.format(obses.getValueDate())).getTime();
					TimeUnit time = TimeUnit.DAYS; 
					diffrence = time.convert(numberOfMissed, TimeUnit.MILLISECONDS);
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				row = new DataSetRow();

				
				if (mads.getMissedDateFrom()!=null){
					if (diffrence >= mads.getMissedDateFrom()){	
						s=true;				
					}
					else{
						s=false;
					}
				}

				if (mads.getMissedDateTo()!=null){
					if(diffrence <= mads.getMissedDateTo()){
						s=true;
						}
					else{
						s=false;
					}

				}    
				if(s==true){
					row = createRow(row,person, diffrence, obses);
					data.addRow(row);
				}      
		
		}
		return data;
	}
	private DataSetRow createRow(DataSetRow row,Person person, Long diffrence, Obs obses){
		EthiopianDate ethiopianDate=null;
		try {
			 ethiopianDate =	EthiopianDateConverter.ToEthiopianDate(obses.getValueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Concept status = patientStatus.get(person.getId());
		row.addColumnValue(new DataSetColumn("PersonID", "#", Integer.class), person.getPersonId());
		row.addColumnValue(new DataSetColumn("Name", "Name", String.class), person.getNames());
		row.addColumnValue(new DataSetColumn("Age", "Age", Integer.class), person.getAge());
		row.addColumnValue(new DataSetColumn("Gender", "Gender", Integer.class), person.getGender());
		row.addColumnValue(new DataSetColumn("No_of_missed_days", "No of missed days", 
		Long.class), diffrence);
		row.addColumnValue(new DataSetColumn("TreatmentEndDate", "Treatment End Date", 
		Date.class), obses.getValueDate());	
		row.addColumnValue(new DataSetColumn("TreatmentEndDateETC", "Treatment End Date ETH", 
		String.class),ethiopianDate.equals(null)? "": ethiopianDate.getDay()+"/"+ethiopianDate.getMonth()+"/"+ethiopianDate.getYear());		
		row.addColumnValue(new DataSetColumn("Status", "Status", 
		String.class), status.equals(null)?"":status.getName().getName());
		return row;
	}
	private String current_date()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date date = new Date();
		return formatter.format(date);
		
	}

	private List<Obs> getTxCurrPatients(MissedAppointmentsDataSetDefinition mads, EvaluationContext context) {
		
		List<Integer> patientsId = getListOfALiveorRestartorSTOPPatientObservertions(context, mads);
		
		List<Person> patients = new ArrayList<>();
		List<Obs> obseList = new ArrayList<>();
		
		if (patientsId ==null || patientsId.size()==0) 
		return obseList;
		HqlQueryBuilder queryBuilder = new HqlQueryBuilder();
		queryBuilder.select("obv");
		queryBuilder.from(Obs.class,"obv")
		.whereEqual("obv.encounter.encounterType", mads.getEncounterType())
	  	.and()
		.whereEqual("obv.concept",conceptService.getConceptByUuid(TREATMENT_END_DATE))
		.and()
		.whereLess("obv.valueDatetime", current_date())
		.and();
		if (mads.getEndDate()!=null)
		{
			queryBuilder.whereLess("obv.valueDatetime", mads.getEndDate()).and();
		
		}
		if (mads.getStartDate()!=null){
			queryBuilder.whereGreater("obv.valueDatetime", mads.getStartDate()).and();
		}
		if (mads.getGender()!=null)
		{
			queryBuilder.whereLike("obs.person.gender", mads.getGender()).and();
		}
		if (mads.getAdherence()!=null)
		{
			queryBuilder.whereLike("obs.person.gender", mads.getAdherence()).and();
		}

		queryBuilder.whereIdIn("obv.personId", patientsId)
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
	private List<Integer> getListOfALiveorRestartorSTOPPatientObservertions(EvaluationContext context,MissedAppointmentsDataSetDefinition mads) {
      
        List<Integer> uniqiObs =new ArrayList<>();
        HqlQueryBuilder queryBuilder = new HqlQueryBuilder();
      
        queryBuilder.select("obv")
        .from(Obs.class,"obv")
		.whereEqual("obv.encounter.encounterType", mads.getEncounterType())
		.and()
      	.whereEqual("obv.concept", conceptService.getConceptByUuid(PATIENT_STATUS))
		.and()
		.whereIn("obv.valueCoded", Arrays.
		asList(conceptService.getConceptByUuid(ALIVE),
		conceptService.getConceptByUuid(RESTART),conceptService.getConceptByUuid(STOP)));
		// .and().whereLess("obv.obsDatetime", mads.getEndDate());
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
	
}
