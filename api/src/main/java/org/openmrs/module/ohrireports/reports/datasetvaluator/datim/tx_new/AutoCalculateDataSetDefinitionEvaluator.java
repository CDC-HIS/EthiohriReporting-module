package org.openmrs.module.ohrireports.reports.datasetvaluator.datim.tx_new;

import static org.openmrs.module.ohrireports.OHRIReportsConstants.ART_START_DATE;

import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohrireports.api.query.PatientQuery;
import org.openmrs.module.ohrireports.reports.datasetdefinition.datim.tx_new.AutoCalculateDataSetDefinition;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;

@Handler(supports = { AutoCalculateDataSetDefinition.class })
public class AutoCalculateDataSetDefinitionEvaluator implements DataSetEvaluator {
	
	private AutoCalculateDataSetDefinition hdsd;
	
	private String title = "Number of adults and children newly enrolled on antiretroviral therapy (ART)";
	
	private PatientQuery patientQuery;
	
	@Override
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evalContext) throws EvaluationException {
		
		hdsd = (AutoCalculateDataSetDefinition) dataSetDefinition;
		
		DataSetRow dataSet = new DataSetRow();
		dataSet.addColumnValue(new DataSetColumn("adultAndChildrenEnrolled", "Numerator", Integer.class),
		    getTotalEnrolledPatients());
		SimpleDataSet set = new SimpleDataSet(dataSetDefinition, evalContext);
		set.addRow(dataSet);
		return set;
	}
	
	private int getTotalEnrolledPatients() {
		
		patientQuery = Context.getService(PatientQuery.class);
		Cohort cohort = patientQuery.getActiveOnArtCohort("", hdsd.getStartDate(), hdsd.getEndDate(), null);
		
		return cohort.size();
	}
	
}
