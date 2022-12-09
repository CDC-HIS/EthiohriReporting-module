package org.openmrs.module.ohrireports.reports.hts;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.openmrs.module.ohrireports.reports.datasetdefinition.MonthlyVisitListDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManager;
import org.springframework.stereotype.Component;

/**
 * MonthlyPatientList
 */
@Component
public class MonthlyVisitList implements ReportManager {
	
	@Override
	public String getUuid() {
		
		return "46323811-7d96-4fa8-9874-179185d2e377";
	}
	
	@Override
	public String getName() {
		
		return "Monthly Visit List";
	}
	
	@Override
	public String getDescription() {
		
		return "List of client visited the facility on the specified month";
	}
	
	@Override
	public List<Parameter> getParameters() {
		Parameter startDate = new Parameter("startDate", "From Date", Date.class);
		startDate.setRequired(false);
		Parameter startDateGC = new Parameter("startDateGC", " ", Date.class);
		startDateGC.setRequired(false);
		Parameter endDate = new Parameter("endDate", "To Date", Date.class);
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
		
		MonthlyVisitListDataSetDefinition mvlDataSetDefinition = new MonthlyVisitListDataSetDefinition();
		mvlDataSetDefinition.addParameters(getParameters());
		reportDefinition.addDataSetDefinition("mvl-P",
        new Mapped<>(mvlDataSetDefinition,
        ParameterizableUtil.createParameterMappings("startDate=${startDateGC},endDate=${endDateGC}")));
		return reportDefinition;
    }
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<ReportRequest> constructScheduledRequests(ReportDefinition reportDefinition) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "1.0.0-SNAPSHOT";
	}
	
}
