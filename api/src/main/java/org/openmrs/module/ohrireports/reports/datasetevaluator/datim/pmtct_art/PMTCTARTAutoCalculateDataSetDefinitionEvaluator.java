package org.openmrs.module.ohrireports.reports.datasetevaluator.datim.pmtct_art;

import static org.openmrs.module.ohrireports.OHRIReportsConstants.ART_START_DATE;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.ALIVE;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.RESTART;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.FOLLOW_UP_STATUS;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.PREGNANT_STATUS;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.YES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ConceptService;
import org.openmrs.module.ohrireports.reports.datasetdefinition.datim.pmtct_art.PMTCTARTAutoCalculateDataSetDefinition;
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

@Handler(supports = { PMTCTARTAutoCalculateDataSetDefinition.class })
public class PMTCTARTAutoCalculateDataSetDefinitionEvaluator implements DataSetEvaluator {

    private EvaluationContext context;
    private int total = 0;
    private int minCount = 0;
    private int maxCount = 4;
    List<Obs> obses = new ArrayList<>();
    private PMTCTARTAutoCalculateDataSetDefinition hdsd;

    private Concept artConcept, pregnantConcept, yesConcept, followUpStatusConcept, aliveConcept, restartConcept;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evalContext)
            throws EvaluationException {

        hdsd = (PMTCTARTAutoCalculateDataSetDefinition) dataSetDefinition;
        context = evalContext;
        setRequiredConcepts();

        DataSetRow dataSet = new DataSetRow();
        dataSet.addColumnValue(new DataSetColumn("auto-calculate", "Numerator", Integer.class),
                getTotalCount());
        SimpleDataSet set = new SimpleDataSet(dataSetDefinition, evalContext);
        set.addRow(dataSet);
        return set;
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
                .and()
                .whereLess("obs.valueDatetime", hdsd.getStartDate())
                .and()
                .whereIdIn("obs.personId", getPregnantPatients());

        obses = evaluationService.evaluateToList(queryBuilder, Obs.class, context);

    }

    private int getTotalCount() {
        setObservations();
        List<Integer> personIds = new ArrayList<>();
        for (Obs obs : obses) {
            if (personIds.contains(obs.getPersonId()))
                continue;
            personIds.add(obs.getPersonId());

        }
        return personIds.size();
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
                        Arrays.asList(conceptService.getConceptByUuid(ALIVE), conceptService.getConceptByUuid(RESTART)))
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
