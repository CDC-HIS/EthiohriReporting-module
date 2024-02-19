package org.openmrs.module.ohrireports.datasetdefinition.linelist;

import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MissedAppointmentDatasetDefinition extends BaseDataSetDefinition {
	
	@ConfigurationProperty
	private Date endDate;
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
