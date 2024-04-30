package org.openmrs.module.ohrireports.datasetevaluator.datim.tx_ml;

import static org.openmrs.module.ohrireports.OHRIReportsConstants.*;

import java.math.BigDecimal;

import java.util.HashMap;

import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ConceptService;
import org.openmrs.module.ohrireports.api.impl.query.MLQuery;
import org.openmrs.module.ohrireports.api.query.AggregateBuilder;
import org.openmrs.module.ohrireports.datasetdefinition.datim.tx_ml.TxMlInterruption3to5MonthsByAgeAndSexDataSetDefinition;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.springframework.beans.factory.annotation.Autowired;

@Handler(supports = { TxMlInterruption3to5MonthsByAgeAndSexDataSetDefinition.class })
public class TxMlInterruption3to5MonthsByAgeAndSexDataSetDefinitionEvaluator implements DataSetEvaluator {
	
	@Autowired
	private MLQuery mlQuery;
	
	@Autowired
	private AggregateBuilder aggregateBuilder;
	
	@Override
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evalContext) throws EvaluationException {
		
		aggregateBuilder.clearTotal();
		TxMlInterruption3to5MonthsByAgeAndSexDataSetDefinition _datasetDefinition = (TxMlInterruption3to5MonthsByAgeAndSexDataSetDefinition) dataSetDefinition;
		
		SimpleDataSet set = new SimpleDataSet(dataSetDefinition, evalContext);
		
		Cohort cohort = getBetweenThreeToFiveInterruption(mlQuery.getInterruptionMonthList());
		aggregateBuilder.setFollowUpDate(mlQuery.getLastFollowUpDate(cohort));
		aggregateBuilder.setPersonList(mlQuery.getPersons(cohort));
		aggregateBuilder.setLowerBoundAge(0);
		aggregateBuilder.setUpperBoundAge(65);
		DataSetRow femaleDateSet = new DataSetRow();
		aggregateBuilder.buildDataSetColumnWithFollowUpDate(femaleDateSet, "F");
		set.addRow(femaleDateSet);
		
		DataSetRow maleDateSet = new DataSetRow();
		aggregateBuilder.buildDataSetColumnWithFollowUpDate(maleDateSet, "M");
		set.addRow(maleDateSet);
		
		DataSetRow totalSet = new DataSetRow();
		aggregateBuilder.buildDataSetColumnWithFollowUpDate(totalSet, "T");
		set.addRow(totalSet);
		return set;
	}
	
	private Cohort getBetweenThreeToFiveInterruption(HashMap<Integer,BigDecimal> interruptionMonth){
		Cohort cohort = new Cohort();
		interruptionMonth.forEach((k,o)->{
				if( o.intValue() >=3 && o.intValue() <6){
					cohort.addMembership(new CohortMembership(k));

				}

		});
		return cohort;
	}
}
