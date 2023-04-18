package org.openmrs.module.ohrireports.reports.datasetevaluator.datim.pmtct_art;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.openmrs.module.ohrireports.OHRIReportsConstants.ART_START_DATE;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.ALIVE;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.RESTART;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.FOLLOW_UP_STATUS;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.PREGNANT_STATUS;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.YES;


import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ConceptService;
import org.openmrs.module.ohrireports.reports.datasetdefinition.datim.pmtct_art.PMTCTARTDataSetDefinition;
import org.openmrs.module.ohrireports.reports.datasetdefinition.datim.tb_art.TBARTDataSetDefinition;
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

@Handler(supports = { PMTCTARTDataSetDefinition.class })
public class PMTCTARTDataSetDefinitionEvaluator implements DataSetEvaluator {

    private EvaluationContext context;
    private int total = 0;
    private int minCount = 0;
    private int maxCount = 4;
    List<Obs> obses = new ArrayList<>();
    private PMTCTARTDataSetDefinition hdsd;

    private Concept artConcept, pregnantConcept, yesConcept, followUpStatusConcept, aliveConcept, restartConcept;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evalContext)
            throws EvaluationException {
        hdsd = (PMTCTARTDataSetDefinition) dataSetDefinition;
        context = evalContext;
        setRequiredConcepts();
        SimpleDataSet simpleDataSet = new SimpleDataSet(dataSetDefinition, evalContext);
        buildDataSet(simpleDataSet);

        return simpleDataSet;

    }

    private void buildDataSet(SimpleDataSet simpleDataSet) {
        setObservations();
        DataSetRow femaleSetRow = new DataSetRow();
        buildDataSet(femaleSetRow);
        simpleDataSet.addRow(femaleSetRow);

    }

    private void buildDataSet(DataSetRow dataSet) {
        total = 0;
        minCount = 10;
        maxCount = 14;

        dataSet.addColumnValue(new DataSetColumn("unknownAge", "Unknown Age", Integer.class),
                getEnrolledByUnknownAge());

        dataSet.addColumnValue(new DataSetColumn("<10", "Below Ten (<10)", Integer.class),
                getCountBelowTenYear());

        while (minCount <= 65) {
            if (minCount == 65) {
                dataSet.addColumnValue(new DataSetColumn("65+", "65+", Integer.class),
                        getCountByAgeRange(65, 200));
            } else {
                dataSet.addColumnValue(
                        new DataSetColumn(minCount + "-" + maxCount, minCount + "-" + maxCount, Integer.class),
                        getCountByAgeRange(minCount, maxCount));
            }
            minCount = maxCount + 1;
            maxCount = minCount + 4;
        }
        dataSet.addColumnValue(new DataSetColumn("Sub-total", "Subtotal", Integer.class),
                total);
    }

    private int getCountByAgeRange(int min, int max) {
        int count = 0;
        List<Integer> personIds = new ArrayList<>();
        for (Obs obs : obses) {

            if (personIds.contains(obs.getPersonId()))
                continue;

            if (obs.getPerson().getAge() >= min && obs.getPerson().getAge() <= max) {
                personIds.add(obs.getPersonId());
                count++;
            }
        }
        incrementTotalCount(count);
        clearCountedPerson(personIds);
        return count;
    }

    private int getEnrolledByUnknownAge() {
        int count = 0;
        List<Integer> personIds = new ArrayList<>();
        for (Obs obs : obses) {
            if (personIds.contains(obs.getPersonId()))
                continue;

            if (Objects.isNull(obs.getPerson().getAge()) ||
                    obs.getPerson().getAge() <= 0) {
                count++;
                personIds.add(obs.getPersonId());
            }

        }
        incrementTotalCount(count);
        clearCountedPerson(personIds);
        return count;
    }

    private int getCountBelowTenYear() {
        int count = 0;
        List<Integer> personIds = new ArrayList<>();
        for (Obs obs : obses) {
            if (personIds.contains(obs.getPersonId()))
                continue;

            if ((obs.getPerson().getAge() < 10)) {
                count++;
                personIds.add(obs.getPersonId());
            }
        }
        incrementTotalCount(count);
        clearCountedPerson(personIds);
        return count;
    }

    private void incrementTotalCount(int count) {
        if (count > 0)
            total = total + count;
    }

    private void clearCountedPerson(List<Integer> personIds) {
        for (int pId : personIds) {
            obses.removeIf(p -> p.getPersonId().equals(pId));
        }
    }

    private void setRequiredConcepts() {
        artConcept = conceptService.getConceptByUuid(ART_START_DATE);
        pregnantConcept = conceptService.getConceptByUuid(PREGNANT_STATUS);
        yesConcept = conceptService.getConceptByUuid(YES);
        followUpStatusConcept = conceptService.getConceptByUuid(FOLLOW_UP_STATUS);
        aliveConcept = conceptService.getConceptByUuid(ALIVE);
        restartConcept = conceptService.getConceptByUuid(RESTART);
    }

    private void setObservations() {
        HqlQueryBuilder queryBuilder = new HqlQueryBuilder();
        queryBuilder.select("obs").from(Obs.class, "obs")
                .whereEqual("obs.encounter.encounterType", hdsd.getEncounterType())
                .and()
                .whereEqual("obs.person.gender", "F")
                .whereEqual("obs.concept", artConcept)
                .and();
        if (hdsd.getIsNewOnArt()) {
            queryBuilder.whereBetweenInclusive("obs.valueDatetime", hdsd.getStartDate(), hdsd.getEndDate())
                    .and();

        } else {
            queryBuilder.whereLess("obs.valueDatetime", hdsd.getStartDate());
        }
        queryBuilder.and()
                .whereIdIn("obs.personId", getPregnantPatients());

        obses = evaluationService.evaluateToList(queryBuilder, Obs.class, context);

    }

    private List<Integer> getAlivePatientsOnFollowUp() {
        HqlQueryBuilder queryBuilder = new HqlQueryBuilder();
        queryBuilder.select("obs").from(Obs.class, "obs")
                .whereEqual("obs.encounter.encounterType", hdsd.getEncounterType()).and()
                .whereEqual("obs.person.gender", "F")
                .and()
                .whereEqual("obs.concept", followUpStatusConcept)
                .and()
                .whereIn("obs.valueCoded",
                        Arrays.asList(aliveConcept, 
                        restartConcept))
                .and()
                .orderDesc("obs.obsDatetime");
        List<Obs> obsList = evaluationService.evaluateToList(queryBuilder, Obs.class, context);
        List<Integer>personId= new ArrayList<>();
        for (Obs obs : obsList) {
            if(!personId.contains(obs.getPersonId()))
            personId.add(obs.getPersonId());
        }
        return personId;
    }

    private List<Integer> getPregnantPatients() {
        HqlQueryBuilder queryBuilder = new HqlQueryBuilder();
        queryBuilder.select("distinct obs.personId").from(Obs.class, "obs")
                .whereEqual("obs.person.gender", "F")
                .and()
                .whereEqual("obs.encounter.encounterType", hdsd.getEncounterType())
                .and().whereEqual("obs.concept", pregnantConcept).and()
                .whereEqual("obs.valueCoded", yesConcept)
                .and().whereIdIn("obs.personId", getAlivePatientsOnFollowUp());
        return evaluationService.evaluateToList(queryBuilder, Integer.class, context);

    }
}
