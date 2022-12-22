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
import org.openmrs.module.ohrireports.reports.datasetdefinition.PediatricAgeOutDataSetDefinition;
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
import java.util.Calendar;
import javax.validation.constraints.Null;

@Handler(supports = { PediatricAgeOutDataSetDefinition.class })
public class PediatricAgeOutEvaluator implements DataSetEvaluator {
	
	@Autowired
	EvaluationService evaluationService;
	
	@Autowired
	ConceptService conceptService;
    HashMap<Integer,Concept> patientStatus = new HashMap<>();
	HashMap<Integer,Date> patient15birthday = new HashMap<>();
	HashMap<Integer,String> patientAgeOut= new HashMap<>();
	HashMap<String,String> patientStatusdict= new HashMap<>();
	

	@Override
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evalContext) throws EvaluationException {
		patientStatusdict.put("alive",ALIVE);
		patientStatusdict.put("restart", RESTART);
		
		PediatricAgeOutDataSetDefinition pads = (PediatricAgeOutDataSetDefinition) dataSetDefinition;
		
		SimpleDataSet data = new SimpleDataSet(dataSetDefinition, evalContext);
		
		List<Obs> obsList = getPediatricPatients(pads, evalContext);
		
		DataSetRow row = null;
     
		for (Obs obses : obsList) {
				Person person = obses.getPerson();
				row = new DataSetRow();
				row = createRow(row,person, obses, evalContext);
				data.addRow(row);  
		
		}
		return data;
	}
	private String getMinBirthDate(Date birthdate){
		Calendar curr_date= Calendar.getInstance();
		curr_date.add(Calendar.YEAR, -15);
		if (birthdate.getTime() >= curr_date.getTime().getTime()){
			return "no";
		}
		else{
			return "yes";
		}
		
	}
	private DataSetRow createRow(DataSetRow row,Person person, Obs obses,EvaluationContext evalContext){
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
		row.addColumnValue(new DataSetColumn("TreatmentEndDate", "Treatment End Date", 
		Date.class), obses.getValueDate());	
		row.addColumnValue(new DataSetColumn("TreatmentEndDateETC", "Treatment End Date ETH", 
		String.class),ethiopianDate.equals(null)? "": ethiopianDate.getDay()+"/"+ethiopianDate.getMonth()+"/"+ethiopianDate.getYear());		
		row.addColumnValue(new DataSetColumn("Status", "Status", 
		String.class), status.equals(null)?"":status.getName().getName());
		row.addColumnValue(new DataSetColumn("Regimen","Regmin",String.class), getRegmin(obses,evalContext));
		row.addColumnValue(new DataSetColumn("15thBirthDate","15th BirthDate",Date.class), patient15birthday.get(person.getId()));
		row.addColumnValue(new DataSetColumn("ageOutStatus","Age Out Status",String.class), patientAgeOut.get(person.getId()));

		return row;
	}

	private String current_date()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date date = new Date();
		return formatter.format(date);
		
	}

	private List<Obs> getPediatricPatients(PediatricAgeOutDataSetDefinition pads, EvaluationContext context) {
		
		List<Integer> patientsId = getListOfALiveorRestartorSTOPPatientObservertions(context, pads);
		// List<Integer> dispenceCode= getListOfPatientwithDispenceCode(context, mads,patientsId);
		
		List<Person> patients = new ArrayList<>();
		List<Obs> obseList = new ArrayList<>();
		
		if (patientsId ==null || patientsId.size()==0) 
		return obseList;
		HqlQueryBuilder queryBuilder = new HqlQueryBuilder();
		queryBuilder.select("obv");
		queryBuilder.from(Obs.class,"obv")
		.whereEqual("obv.encounter.encounterType", pads.getEncounterType())
	  	.and();
		if (pads.getAgeOutendDate()!=null)
		{
			queryBuilder.whereLess("obv.valueDatetime", pads.getAgeOutendDate()).and();
		
		}
		if (pads.getAgeOutstartDate()!=null){
			queryBuilder.whereGreater("obv.valueDatetime", pads.getAgeOutstartDate()).and();
		}
		if (pads.getGender()!=null)
		{
			queryBuilder.whereLike("obs.person.gender", pads.getGender()).and();
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
	private List<Integer> getListOfALiveorRestartorSTOPPatientObservertions(EvaluationContext context,PediatricAgeOutDataSetDefinition pads) {
      
        List<Integer> uniqiObs =new ArrayList<>();
		List<Integer> uniqiObs2 =new ArrayList<>();
        HqlQueryBuilder queryBuilder = new HqlQueryBuilder();
      
        queryBuilder.select("obv")
        .from(Obs.class,"obv")
		.whereEqual("obv.encounter.encounterType", pads.getEncounterType())
		.and()
      	.whereEqual("obv.concept", conceptService.getConceptByUuid(PATIENT_STATUS))
		.and();
		if (pads.getPatientStatus()!=null){
		
	
		queryBuilder.whereEqual("obv.valueCoded", conceptService.getConceptByUuid(patientStatusdict.get(pads.getPatientStatus())));
		}
		// .and().whereLess("obv.obsDatetime", mads.getEndDate());
		queryBuilder.orderDesc("obv.personId,obv.obsDatetime");

		List<Obs> liveObs = evaluationService.evaluateToList(queryBuilder,Obs.class, context);
		
        for (Obs obs :liveObs){
			if(!uniqiObs.contains(obs.getPersonId())){
				uniqiObs.add(obs.getPersonId());
				patientStatus.put(obs.getPersonId(), obs.getValueCoded());
			}	
		}
		uniqiObs2=getListOfPatientbelow15atART(context,pads,uniqiObs);

        return uniqiObs2;
    }
	private String getRegmin(Obs obs, EvaluationContext context) {
		HqlQueryBuilder queryBuilder = new HqlQueryBuilder();
		
		queryBuilder.select("obv.valueCoded").from(Obs.class, "obv")
		        .whereInAny("obv.concept", conceptService.getConceptByUuid(REGIMEN))
		        .whereEqual("obv.encounter", obs.getEncounter()).and().whereEqual("obv.person", obs.getPerson())
		        .orderDesc("obv.obsDatetime").limit(1);
		List<Concept> concepts = evaluationService.evaluateToList(queryBuilder, Concept.class, context);
		
		Concept data = null;
		if (concepts != null && concepts.size() > 0)
			data = concepts.get(0);
		
		return data == null ? "" : data.getName().getName();
	}
	private List<Integer> getListOfPatientbelow15atART(EvaluationContext context,PediatricAgeOutDataSetDefinition pads, List <Integer> patientsId) {
      
        List<Integer> uniqiObs =new ArrayList<>();
        HqlQueryBuilder queryBuilder = new HqlQueryBuilder();
      
        queryBuilder.select("obv")
        .from(Obs.class,"obv")
		.whereEqual("obv.encounter.encounterType", pads.getEncounterType())
		.and()
		.whereEqual("obv.concept",conceptService.getConceptByUuid(ART_START_DATE))
		.and()
		.whereLess("obv.valueDatetime", current_date())
		.and()
		.whereIdIn("obv.personId", patientsId);
		// .and().whereLess("obv.obsDatetime", mads.getEndDate());
		queryBuilder.orderDesc("obv.personId,obv.obsDatetime");

		List<Obs> artObs = evaluationService.evaluateToList(queryBuilder,Obs.class, context);
		
        for (Obs obs :artObs){
			// Boolean out=false;
			if(!uniqiObs.contains(obs.getPersonId())){
				Calendar artdate= Calendar.getInstance();
				artdate.setTime(obs.getValueDatetime());
				artdate.add(Calendar.YEAR, -15);
				if (obs.getPerson().getBirthdate() != null){
					if (obs.getPerson().getBirthdate().getTime() > artdate.getTime().getTime()){
					Calendar fiftybirth= Calendar.getInstance();
					fiftybirth.setTime(obs.getPerson().getBirthdate());
					fiftybirth.add(Calendar.YEAR, 15);
					if (pads.getAgeOutStatus() != null){
						if (pads.getAgeOutStatus() == "yes"){
							if(getMinBirthDate(obs.getPerson().getBirthdate()) =="yes"){
								uniqiObs.add(obs.getPersonId());
								patientAgeOut.put(obs.getPersonId(), getMinBirthDate(obs.getPerson().getBirthdate()));

							}

						}
						else{
							if(getMinBirthDate(obs.getPerson().getBirthdate()) =="no"){
								uniqiObs.add(obs.getPersonId());
								patientAgeOut.put(obs.getPersonId(), getMinBirthDate(obs.getPerson().getBirthdate()));

							}

						}
					}
					else{
						uniqiObs.add(obs.getPersonId());					
						patient15birthday.put(obs.getPersonId(), fiftybirth.getTime());
						patientAgeOut.put(obs.getPersonId(), getMinBirthDate(obs.getPerson().getBirthdate()));
					}
					}
				}
			}	
		}

        return uniqiObs;
    }
	
	
}
