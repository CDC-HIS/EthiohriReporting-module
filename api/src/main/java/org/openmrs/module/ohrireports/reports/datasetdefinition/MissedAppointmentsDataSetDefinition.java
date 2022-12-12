package org.openmrs.module.ohrireports.reports.datasetdefinition;

import java.util.Date;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * HtsNewDataSetDefinition
 */
@Component
public class MissedAppointmentsDataSetDefinition extends BaseDataSetDefinition {
	
	@ConfigurationProperty
	private Date startDate;
	
	@ConfigurationProperty
	private Date endDate;
	
	@ConfigurationProperty
	private EncounterType encounterType;

	@ConfigurationProperty
	private String gender;

	@ConfigurationProperty
	private String adherence;

	@ConfigurationProperty
	private Integer missedDateFrom;
	

	@ConfigurationProperty
	private Integer missedDateTo;

	public Integer getMissedDateFrom() {
		return missedDateFrom;
	}

	public void setMissedDateFrom(Integer missedDateFrom) {
		this.missedDateFrom = missedDateFrom;
	}
	
	
	public Integer getMissedDateTo() {
		return missedDateTo;
	}

	public void setMissedDateTo(Integer missedDateTo) {
		this.missedDateTo = missedDateTo;
	}

	public String getAdherence() {
		return adherence;
	}

	public void setAdherence(String adherence) {
		this.adherence = adherence;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
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
