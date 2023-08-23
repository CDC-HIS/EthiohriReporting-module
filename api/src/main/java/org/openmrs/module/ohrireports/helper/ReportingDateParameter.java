package org.openmrs.module.ohrireports.helper;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.openmrs.module.reporting.evaluation.parameter.Parameter;

public class ReportingDateParameter {
    public static List<Parameter> getEthiopianDateRange(){
        
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
}
    
