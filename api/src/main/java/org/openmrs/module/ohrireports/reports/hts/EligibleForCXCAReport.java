package org.openmrs.module.ohrireports.reports.hts;

import static org.openmrs.module.ohrireports.OHRIReportsConstants.HTS_FOLLOW_UP_ENCOUNTER_TYPE;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.ohrireports.cohorts.util.Utilities;
import org.openmrs.module.ohrireports.reports.datasetdefinition.EligibleForCXCADataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManager;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.springframework.stereotype.Component;

@Component
public class EligibleForCXCAReport implements ReportManager {
	
	@Override
	public String getUuid() {
		return "8c4753ae-21ad-4153-8f32-1aacc4da2643";
	}
	
	@Override
	public String getName() {
		return "Eligible For CXCA";
	}
	
	@Override
	public String getDescription() {
		return "Eligible For Cervical cancer test";
	}
	
	@Override
	public List<Parameter> getParameters() {
		Parameter startDate = new Parameter("startDate", "Start Date", Date.class);
		startDate.setRequired(false);
		Parameter startDateGC = new Parameter("startDateGC", " ", Date.class);
		startDateGC.setRequired(false);
		Parameter endDate = new Parameter("endDate", "End Date", Date.class);
		endDate.setRequired(false);
		Parameter endDateGC = new Parameter("endDateGC", " ", Date.class);
		endDateGC.setRequired(false);
		
		return Arrays.asList(startDate, startDateGC, endDate, endDateGC);
	}
	
	@Override
	public ReportDefinition constructReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setUuid(getUuid());
		reportDefinition.setName(getName());
		reportDefinition.setDescription(getDescription());
		reportDefinition.setParameters(getParameters());
		
		EligibleForCXCADataSetDefinition eCxcaDataSetDefinition = new EligibleForCXCADataSetDefinition();
		eCxcaDataSetDefinition.addParameters(getParameters());
		eCxcaDataSetDefinition.setEncounterType(Context.getEncounterService().getEncounterTypeByUuid(
		    HTS_FOLLOW_UP_ENCOUNTER_TYPE));
		
		reportDefinition.addDataSetDefinition("Eligible_For_CXCA",
		    Utilities.map(eCxcaDataSetDefinition, "endDate=${endDateGC},startDate=${startDateGC}"));
		return reportDefinition;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		ReportDesign excelreportDesign = ReportManagerUtil.createExcelDesign("503a5764-feaa-43d5-ad7e-f523091fbd8f",
		    reportDefinition);
		ReportDesign csvreportDesign = ReportManagerUtil.createCsvReportDesign("ca52a95c-8bb4-4a9f-a0cf-f0df437592da",
		    reportDefinition);
		
		return Arrays.asList(excelreportDesign, csvreportDesign);
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
