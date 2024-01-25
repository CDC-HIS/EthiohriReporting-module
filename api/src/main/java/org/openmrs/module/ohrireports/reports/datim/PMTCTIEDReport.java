package org.openmrs.module.ohrireports.reports.datim;

import org.openmrs.api.context.Context;
import org.openmrs.module.ohrireports.cohorts.util.EthiOhriUtil;
import org.openmrs.module.ohrireports.datasetdefinition.datim.pmtct.EidAgeAndTestDisAggregationDatasetDefinition;
import org.openmrs.module.ohrireports.datasetdefinition.datim.pmtct.EidNumeratorDatasetDefinition;
import org.openmrs.module.ohrireports.datasetdefinition.datim.tx_curr.*;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManager;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.openmrs.module.ohrireports.OHRIReportsConstants.DATIM_REPORT;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.HTS_FOLLOW_UP_ENCOUNTER_TYPE;

@Component
public class PMTCTIEDReport implements ReportManager {
	
	@Override
	public String getUuid() {
		return "ccca77d6-521c-43f6-811d-3d268877fe70";
	}
	
	@Override
	public String getName() {
		return DATIM_REPORT + "-PMTCT_EID";
	}
	
	@Override
	public String getDescription() {
		return "Percentage of infants born to women living with HIV who received a sample collected for  Virologic HIV test by 12 months of age.";
	}
	
	@Override
	public List<Parameter> getParameters() {
		
		return EthiOhriUtil.getDateRangeParameters();
	}
	
	@Override
	public ReportDefinition constructReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setUuid(getUuid());
		reportDefinition.setName(getName());
		reportDefinition.setDescription(getDescription());
		reportDefinition.setParameters(getParameters());
		
		EidNumeratorDatasetDefinition numeratorDatasetDefinition = new EidNumeratorDatasetDefinition();
		numeratorDatasetDefinition
		        .setDescription("Number of infants who had a virologic HIV test (sample collected) by 12 months of age during the reporting period");
		numeratorDatasetDefinition.setParameters(getParameters());
		reportDefinition
		        .addDataSetDefinition(
		            "Number of infants who had a virologic HIV test (sample collected) by 12 months of age during the reporting period",
		            EthiOhriUtil.map(numeratorDatasetDefinition));
		
		EidAgeAndTestDisAggregationDatasetDefinition ageAndTestDisAggregationDatasetDefinition = new EidAgeAndTestDisAggregationDatasetDefinition();
		ageAndTestDisAggregationDatasetDefinition
		        .setDescription("PMTCT STAT POS + HTS TST POS from the [PostANC1:Pregnancy/L&D] + [PostANC1:BF] MODALITIES (SEE PMTCT_STAT & HTS_TST reference sheets)");
		ageAndTestDisAggregationDatasetDefinition.setParameters(getParameters());
		reportDefinition
		        .addDataSetDefinition(
		            "PMTCT STAT POS + HTS TST POS from the [PostANC1:Pregnancy/L&D] + [PostANC1:BF] MODALITIES (SEE PMTCT_STAT & HTS_TST reference sheets)",
		            EthiOhriUtil.map(ageAndTestDisAggregationDatasetDefinition));
		
		return reportDefinition;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		ReportDesign design = ReportManagerUtil.createExcelDesign("9d4dabdb-f300-4b34-a126-aa0e77baabd5", reportDefinition);
		
		return Collections.singletonList(design);
		
	}
	
	@Override
	public List<ReportRequest> constructScheduledRequests(ReportDefinition reportDefinition) {
		return null;
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
		
	}
	
}
