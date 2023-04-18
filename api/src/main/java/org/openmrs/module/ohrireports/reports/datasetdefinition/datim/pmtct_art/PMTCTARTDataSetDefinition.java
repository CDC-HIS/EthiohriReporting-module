package org.openmrs.module.ohrireports.reports.datasetdefinition.datim.pmtct_art;

import java.util.Date;

import org.openmrs.EncounterType;
import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.springframework.stereotype.Component;

@Component
public class PMTCTARTDataSetDefinition extends BaseDataSetDefinition {
	
	@ConfigurationProperty
	private Date startDate;
	
	@ConfigurationProperty
	private Date endDate;
	
	@ConfigurationProperty
	private EncounterType encounterType;
	
	@ConfigurationProperty
	private Boolean isNewOnArt;
	
	public Boolean getIsNewOnArt() {
		return isNewOnArt;
	}
	
	public void setIsNewOnArt(Boolean isNewOnArt) {
		this.isNewOnArt = isNewOnArt;
	}
	
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
	
}
