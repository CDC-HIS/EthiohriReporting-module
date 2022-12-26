package org.openmrs.module.ohrireports.reports.hts;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.HTS_FOLLOW_UP_ENCOUNTER_TYPE;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohrireports.cohorts.util.Utilities;
import org.openmrs.module.ohrireports.reports.datasetdefinition.EligibleForCXCADataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManager;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;

public class EligibleForCXCAReport implements ReportManager {

    @Override
    public String getUuid() {
        return "a0e187f0-8971-46a0-9f6d-3129e83f2c40";
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
        return Arrays.asList(
                new Parameter("startDate", "Start Date", Date.class),
                new Parameter("startDateGC", " ", Date.class),
                new Parameter("endDate", "End Date", Date.class),
                new Parameter("endDateGC", " ", Date.class));
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition reportDefinition = new ReportDefinition();
        reportDefinition.setUuid(getUuid());
        reportDefinition.setName(getName());
        reportDefinition.setDescription(getDescription());

        EligibleForCXCADataSetDefinition eCxcaDataSetDefinition = new EligibleForCXCADataSetDefinition();
        eCxcaDataSetDefinition.addParameters(getParameters());
        eCxcaDataSetDefinition.setEncounterType(Context.getEncounterService()
                .getEncounterTypeByUuid(HTS_FOLLOW_UP_ENCOUNTER_TYPE));

        reportDefinition.addDataSetDefinition("Eligible_For_CXCA",
                Utilities.map(eCxcaDataSetDefinition,"endDate=${endDateGC}"));
        return reportDefinition;
    }
   
    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        ReportDesign excelreportDesign = ReportManagerUtil.createExcelDesign("b2190476-18b9-4cdd-b333-2aeb666edab0",
                reportDefinition);
        ReportDesign csvreportDesign = ReportManagerUtil.createCsvReportDesign("b2190476-18b9-4cdd-b333-2aeb666edab0",
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
