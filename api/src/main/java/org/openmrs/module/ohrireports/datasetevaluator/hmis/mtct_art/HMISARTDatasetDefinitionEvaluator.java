package org.openmrs.module.ohrireports.datasetevaluator.hmis.mtct_art;

import org.openmrs.annotation.Handler;
import org.openmrs.module.ohrireports.api.impl.query.pmtct.ARTQuery;
import org.openmrs.module.ohrireports.datasetdefinition.hmis.pmtct.HMISARTDatasetDefinition;
import org.openmrs.module.ohrireports.datasetevaluator.hmis.tx_dsd.RowBuilder;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.springframework.beans.factory.annotation.Autowired;

import static org.openmrs.module.ohrireports.OHRIReportsConstants.*;
import static org.openmrs.module.ohrireports.datasetevaluator.hmis.HMISConstant.COLUMN_1_NAME;
import static org.openmrs.module.ohrireports.datasetevaluator.hmis.HMISConstant.COLUMN_2_NAME;

@Handler(supports = { HMISARTDatasetDefinition.class })
public class HMISARTDatasetDefinitionEvaluator implements DataSetEvaluator {
	
	@Autowired
	private ARTQuery artQuery;
	
	@Override
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evalContext) throws EvaluationException {
		HMISARTDatasetDefinition hmisartDatasetDefinition = (HMISARTDatasetDefinition) dataSetDefinition;
		
		artQuery.setStartDate(hmisartDatasetDefinition.getStartDate());
		artQuery.setEndDate(hmisartDatasetDefinition.getEndDate());
		
		SimpleDataSet data = new SimpleDataSet(hmisartDatasetDefinition, evalContext);
		RowBuilder rowBuilder = new RowBuilder();
		
		data.addRow(rowBuilder.buildDatasetColumn("MTCT_ART",
		    "Percentage of HIV-positive pregnant women who received ART to reduce the risk of mother-to "
		            + "child-transmission (MTCT) during pregnancy, L and D and PNC",
		    String.valueOf(artQuery.getPMTCTARTCohort().size())));
		
		data.addRow(buildColumn("MTCT_ART.1.", "Number of HIV positive women who received ART to "
		        + "reduce the risk of mother to child transmission during ANC for the first time",
		    String.valueOf(artQuery.getCohortByPMTCTEnrollmentStatus(NEW_FROM_ANC).size())));
		data.addRow(buildColumn("MTCT_ART.2.", "Number of HIV positive Pregnant women who received "
		        + "ART to reduce the risk of mother to child transmission during L and D for the first time",
		    String.valueOf(artQuery.getCohortByPMTCTEnrollmentStatus(NEW_FROM_LD).size())));
		data.addRow(buildColumn("MTCT_ART.3.", "Number of HIV positive lactating women who received "
		        + "ART to reduce the risk of mother to child transmission during PNC for the first time",
		    String.valueOf(artQuery.getCohortByPMTCTEnrollmentStatus(NEW_FROM_PNC).size())));
		data.addRow(buildColumn("MTCT_ART.4.", "Number of HIV-positive women who get pregnant while "
		        + "on ART and linked to ANC",
		    String.valueOf(artQuery.getCohortByPMTCTEnrollmentStatus(LINKED_FROM_ART).size())));
		
		return data;
	}
	
	private DataSetRow buildColumn(String col_1_value, String col_2_value, String col_3_value) {
		DataSetRow mtctARTDataSetRow = new DataSetRow();
		mtctARTDataSetRow.addColumnValue(new DataSetColumn(COLUMN_1_NAME, COLUMN_1_NAME, String.class), col_1_value);
		mtctARTDataSetRow.addColumnValue(new DataSetColumn(COLUMN_2_NAME, COLUMN_2_NAME, String.class), col_2_value);
		
		mtctARTDataSetRow.addColumnValue(new DataSetColumn("Number", "Number", Integer.class), col_3_value);
		
		return mtctARTDataSetRow;
	}
}
