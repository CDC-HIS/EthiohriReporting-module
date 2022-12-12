/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ohrireports.reports.hts;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.ohrireports.reports.datasetdefinition.MissedAppointmentsDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManager;
import org.springframework.stereotype.Component;
import java.util.Collection;
import static org.openmrs.module.ohrireports.OHRIReportsConstants.HTS_FOLLOW_UP_ENCOUNTER_TYPE;

@Component
public class Missed_Appointment_Report implements ReportManager {
	
	@Override
	public String getUuid() {
		return "4e29b77f-eedd-4486-9f08-be039a894ff7";
	}
	
	@Override
	public String getName() {
		return "TX New";
	}
	
	@Override
	public String getDescription() {
		return null;
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
		Parameter missedDateTo = new Parameter("missedDateTo", " missed Date To", Integer.class);
		missedDateTo.setRequired(false);
		Parameter missedDateFrom = new Parameter("missedDateFrom", " missed Date From", Integer.class);
		missedDateFrom.setRequired(false);
		Parameter gender = new Parameter("gender", "gender", String.class);
		gender.addToWidgetConfiguration("optionValues","male, female, all");
		Parameter adherence = new Parameter("adherence","adherence",String.class);
		adherence.addToWidgetConfiguration("optionValues","All,fair,good,poor");
		return Arrays.asList(startDate, startDateGC, endDate, endDateGC,gender,adherence,missedDateFrom,missedDateTo);
		
	}
	
	@Override
	public ReportDefinition constructReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setUuid(getUuid());
		reportDefinition.setName(getName());
		reportDefinition.setDescription(getDescription());
		reportDefinition.setParameters(getParameters());
		
		MissedAppointmentsDataSetDefinition MissedAppointmentsDataSetDefinition  = new MissedAppointmentsDataSetDefinition();
		MissedAppointmentsDataSetDefinition.addParameters(getParameters());
		MissedAppointmentsDataSetDefinition.setEncounterType(Context.getEncounterService().getEncounterTypeByUuid(
		    HTS_FOLLOW_UP_ENCOUNTER_TYPE));
		reportDefinition.addDataSetDefinition("TX-New",
		    map(MissedAppointmentsDataSetDefinition, "startDate=${startDateGC},endDate=${endDateGC},gender=${gender}, adherence=${adherence}, missedDateFrom=${missedDateFrom}, missedDateTo=${missedDateTo}"));
		return reportDefinition;
	}
	
	public static <T extends Parameterizable> Mapped<T> map(T parameterizable, String mappings) {
		if (parameterizable == null) {
			throw new IllegalArgumentException("Parameterizable cannot be null");
		}
		if (mappings == null) {
			mappings = ""; // probably not necessary, just to be safe
		}
		return new Mapped<T>(parameterizable, ParameterizableUtil.createParameterMappings(mappings));
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		// ReportDesign design =
		// ReportManagerUtil.createCsvReportDesign(HTS_REPORT_DESIGN_UUID,
		// reportDefinition);
		
		return null;
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
