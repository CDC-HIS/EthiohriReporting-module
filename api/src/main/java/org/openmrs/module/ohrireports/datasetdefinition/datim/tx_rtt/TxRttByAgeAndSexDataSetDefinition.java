package org.openmrs.module.ohrireports.datasetdefinition.datim.tx_rtt;

import java.util.Date;

import org.openmrs.EncounterType;
import org.openmrs.module.ohrireports.datasetevaluator.datim.tx_new.CD4Status;
import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.springframework.stereotype.Component;

@Component
public class TxRttByAgeAndSexDataSetDefinition extends BaseDataSetDefinition {
	
	@ConfigurationProperty
	private Date startDate;
	
	@ConfigurationProperty
	private Date endDate;
	
	@ConfigurationProperty
	private EncounterType encounterType;
	
	@ConfigurationProperty
	private CD4Status countCD4GreaterThan200 = CD4Status.CD4Unknown;
	
	@ConfigurationProperty
	private Boolean header = false;
	
	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public EncounterType getEncounterType() {
		return encounterType;
	}
	
	public void setEncounterType(EncounterType encounterType) {
		this.encounterType = encounterType;
	}
	
	public CD4Status getCountCD4GreaterThan200() {
		return countCD4GreaterThan200;
	}
	
	public void setCountCD4GreaterThan200(CD4Status countCD4GreaterThan200) {
		this.countCD4GreaterThan200 = countCD4GreaterThan200;
	}
	
	public Boolean getHeader() {
		return header;
	}
	
	public void setHeader(Boolean header) {
		this.header = header;
	}
}
