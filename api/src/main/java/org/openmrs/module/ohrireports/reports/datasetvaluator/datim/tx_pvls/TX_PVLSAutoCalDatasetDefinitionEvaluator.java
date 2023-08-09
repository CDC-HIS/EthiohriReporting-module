package org.openmrs.module.ohrireports.reports.datasetvaluator.datim.tx_pvls;

import static org.openmrs.module.ohrireports.OHRIReportsConstants.HIV_ROUTINE_VIRAL_LOAD_COUNT;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.HIV_VIRAL_LOAD_UNSUPPRESSED;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.HIV_VIRAL_LOAD_STATUS;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.ART_START_DATE;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.FOLLOW_UP_STATUS;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.HIV_VIRAL_LOAD_SUPPRESSED;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.ALIVE;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.RESTART;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohrireports.api.impl.query.HivPvlsQuery;
import org.openmrs.module.ohrireports.api.query.PatientQuery;
import org.openmrs.module.ohrireports.reports.datasetdefinition.datim.tx_pvls.TX_PVLSAutoCalcDatasetDefinition;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.springframework.beans.factory.annotation.Autowired;

@Handler(supports = { TX_PVLSAutoCalcDatasetDefinition.class })
public class TX_PVLSAutoCalDatasetDefinitionEvaluator implements DataSetEvaluator {
	
	@Autowired
	private HivPvlsQuery qHivPvlsQuery;
	
	private TX_PVLSAutoCalcDatasetDefinition txDatasetDefinition;
	
	@Override
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evalContext) throws EvaluationException {
		
		txDatasetDefinition = (TX_PVLSAutoCalcDatasetDefinition) dataSetDefinition;
		
		SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, evalContext);
		DataSetRow setRow = new DataSetRow();
		setRow.addColumnValue(new DataSetColumn("Numerator", "Numerator", String.class), txDatasetDefinition
		        .getIncludeUnSuppressed() ? getAllPatientWithViralLoadCount() : getAllPatientListWithSuppressedViral());
		dataSet.addRow(setRow);
		return dataSet;
	}
	
	private int getAllPatientWithViralLoadCount() {
		
		Cohort cohort = qHivPvlsQuery.getPatientWithViralLoadCount("", txDatasetDefinition.getStartDate(),
		    txDatasetDefinition.getEndDate());
		return cohort.size();
	}
	
	private int getAllPatientListWithSuppressedViral() {
		Cohort suppressedCohort = qHivPvlsQuery.getPatientsWithViralLoadSuppressed("", txDatasetDefinition.getStartDate(),
		    txDatasetDefinition.getEndDate());
		Cohort ViremiaCohort = qHivPvlsQuery.getPatientWithViralLoadCountLowLevelViremia(" ",
		    txDatasetDefinition.getStartDate(), txDatasetDefinition.getEndDate());
		
		return suppressedCohort.size() + ViremiaCohort.size();
	}
	
}
