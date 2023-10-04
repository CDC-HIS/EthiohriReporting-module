package org.openmrs.module.ohrireports.reports.datasetvaluator.datim.tx_new;

import static org.openmrs.module.ohrireports.OHRIReportsConstants.ART_START_DATE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohrireports.api.query.PatientQueryService;
import org.openmrs.module.ohrireports.reports.datasetdefinition.datim.tx_new.FineByAgeAndSexDataSetDefinition;
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

@Handler(supports = { FineByAgeAndSexDataSetDefinition.class })
public class FineByAgeAndSexDataSetDefinitionEvaluator implements DataSetEvaluator {
	
	private EvaluationContext context;
	
	private FineByAgeAndSexDataSetDefinition hdsd;
	
	private Concept artConcept;
	
	private int total = 0;
	
	@Autowired
	private ConceptService conceptService;
	
	private PatientQueryService patientQuery;
	
	@Autowired
	private EvaluationService evaluationService;
	
	private int minCount = 0;
	
	private int maxCount = 4;
	
	@Override
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evalContext) throws EvaluationException {
		total = 0;
		hdsd = (FineByAgeAndSexDataSetDefinition) dataSetDefinition;
		context = evalContext;
		patientQuery = Context.getService(PatientQueryService.class);
		artConcept = conceptService.getConceptByUuid(ART_START_DATE);
		SimpleDataSet set = new SimpleDataSet(dataSetDefinition, evalContext);
		Cohort femaleCohort = patientQuery.getOnArtCohorts("F", hdsd.getStartDate(), hdsd.getEndDate(), null);
		
		// Female aggregation
		List<Person> persons = patientQuery.getPersons(femaleCohort);
		
		DataSetRow femaleDateSet = new DataSetRow();
		buildDataSet(femaleDateSet, "F", persons);
		set.addRow(femaleDateSet);
		persons.clear();
		// Male aggregation
		Cohort maleCohort = patientQuery.getOnArtCohorts("M", hdsd.getStartDate(), hdsd.getEndDate(), null);
		persons = patientQuery.getPersons(maleCohort);
		
		DataSetRow maleDataSet = new DataSetRow();
		buildDataSet(maleDataSet, "M", persons);
		set.addRow(maleDataSet);
		return set;
	}
	
	private void buildDataSet(DataSetRow dataSet, String gender, List<Person> persons) {
		total = 0;
		minCount = 1;
		maxCount = 4;
		dataSet.addColumnValue(new DataSetColumn("FineByAgeAndSexData", "Gender", Integer.class),
		    gender.equals("F") ? "Female" : "Male");
		dataSet.addColumnValue(new DataSetColumn("unknownAge", "Unknown Age", Integer.class),
		    getEnrolledByUnknownAge(persons));
		
		dataSet.addColumnValue(new DataSetColumn("<1", "Below One (<1)", Integer.class), getEnrolledBelowOneYear(persons));
		
		while (minCount <= 65) {
			if (minCount == 65) {
				dataSet.addColumnValue(new DataSetColumn("65+", "65", Integer.class),
				    getEnrolledByAgeAndGender(65, 200, persons));
			} else {
				dataSet.addColumnValue(
				    new DataSetColumn(minCount + "-" + maxCount, minCount + "-" + maxCount, Integer.class),
				    getEnrolledByAgeAndGender(minCount, maxCount, persons));
			}
			minCount = maxCount + 1;
			maxCount = minCount + 4;
		}
		dataSet.addColumnValue(new DataSetColumn("Sub-total", "Subtotal", Integer.class), total);
	}
	
	private int getEnrolledByAgeAndGender(int min, int max, List<Person> persons) {
        int count = 0;
        List<Integer> personIds = new ArrayList<>();
        for (Person person : persons) {

            if (personIds.contains(person.getPersonId()))
                continue;

            if (person.getAge() >= min && person.getAge() <= max ) {
                personIds.add(person.getPersonId());
                count++;
            }
        }
        incrementTotalCount(count);
        clearCountedPerson(personIds, persons);
        return count;
    }
	
	private int getEnrolledByUnknownAge(List<Person> persons) {
        int count = 0;
        List<Integer> personIds = new ArrayList<>();
        for (Person person : persons) {
            if (personIds.contains(person.getPersonId()))
                continue;

            if (Objects.isNull(person.getAge()) ||
                    person.getAge() <= 0) {
                count++;
                personIds.add(person.getPersonId());
            }

        }
        incrementTotalCount(count);
        clearCountedPerson(personIds, persons);
        return count;
    }
	
	private int getEnrolledBelowOneYear(List<Person> persons) {
        int count = 0;
        List<Integer> personIds = new ArrayList<>();
        for (Person person : persons) {
            if (personIds.contains(person.getPersonId()))
                continue;

            if (person.getAge() < 1) {
                count++;
                personIds.add(person.getPersonId());
            }
        }
        incrementTotalCount(count);
        clearCountedPerson(personIds, persons);
        return count;
    }
	
	private void incrementTotalCount(int count) {
		if (count > 0)
			total = total + count;
	}
	
	private void clearCountedPerson(List<Integer> personIds, List<Person> persons) {
        for (int pId : personIds) {
            persons.removeIf(p -> p.getPersonId()==pId);
        }
    }
}
