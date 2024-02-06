package org.openmrs.module.ohrireports.reports.datim;

import org.openmrs.module.ohrireports.cohorts.util.EthiOhriUtil;
import org.openmrs.module.ohrireports.datasetdefinition.datim.pmtct.EidAgeAndTestDisAggregationDatasetDefinition;
import org.openmrs.module.ohrireports.datasetdefinition.datim.pmtct.EidNumeratorDatasetDefinition;
import org.openmrs.module.ohrireports.datasetdefinition.datim.pmtct_fo.PMTCTFOAutoCalculateDataSetDefinition;
import org.openmrs.module.ohrireports.datasetdefinition.datim.pmtct_fo.PMTCTFODataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManager;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static org.openmrs.module.ohrireports.OHRIReportsConstants.DATIM_REPORT;

@Component
public class PMTCTFOReport implements ReportManager {
	
	@Override
	public String getUuid() {
		return "52a098c1-3d98-4330-9397-2c8dd5f85ceb";
	}
	
	@Override
	public String getName() {
		return DATIM_REPORT + "-PMTCT_FO";
	}
	
	@Override
	public String getDescription() {
		return "Number of HIV-exposed infants who were born 24 months prior to the reporting period and registered in the birth cohort.";
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
		
		PMTCTFOAutoCalculateDataSetDefinition pmtctfoAutoCalculateDataSetDefinition = new PMTCTFOAutoCalculateDataSetDefinition();
		pmtctfoAutoCalculateDataSetDefinition
		        .setDescription("Number of HIV-exposed infants who were born 24 months prior to the reporting period and registered in the birth cohort.");
		pmtctfoAutoCalculateDataSetDefinition.setParameters(getParameters());
		reportDefinition
		        .addDataSetDefinition(
		            "Number of HIV-exposed infants who were born 24 months prior to the reporting period and registered in the birth cohort.",
		            EthiOhriUtil.map(pmtctfoAutoCalculateDataSetDefinition));
		
		PMTCTFODataSetDefinition pmtctfoDataSetDefinition = new PMTCTFODataSetDefinition();
		pmtctfoDataSetDefinition.setDescription("Numerator: Disaggregated by Outcome Type");
		pmtctfoDataSetDefinition.setParameters(getParameters());
		reportDefinition.addDataSetDefinition("Numerator: Disaggregated by Outcome Type",
		    EthiOhriUtil.map(pmtctfoDataSetDefinition));
		
		return reportDefinition;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		ReportDesign design = ReportManagerUtil.createExcelDesign("e6ae727e-8322-4fb4-a174-994b3be939f9", reportDefinition);
		
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
