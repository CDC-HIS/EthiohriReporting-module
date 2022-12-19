package org.openmrs.module.ohrireports.reports.datasetdefinition;

import java.util.Date;

import org.openmrs.EncounterType;
import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * TXCurrDataSetDefinition
 */
@Component
public class PediatricAgeOutDataSetDefinition extends BaseDataSetDefinition {
	
	@ConfigurationProperty
	private Date ageOutstartDate;
	

	@ConfigurationProperty
	private Date ageOutendDate;
	
	
	@ConfigurationProperty
	private EncounterType encounterType;

	@ConfigurationProperty
	private String patientStatus;

	@ConfigurationProperty
	private String gender;

	@ConfigurationProperty
	private String ageOutStatus;



	public Date getAgeOutstartDate() {
		return ageOutstartDate;
	}

	public void setAgeOutstartDate(Date ageOutstartDate) {
		this.ageOutstartDate = ageOutstartDate;
	}
	 
	public Date getAgeOutendDate() {
		return ageOutendDate;
	}

	public void setAgeOutendDate(Date ageOutendDate) {
		this.ageOutendDate = ageOutendDate;
	}

	
	public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPatientStatus() {
        return patientStatus;
    }

    public void setPatientStatus(String patientStatus) {
        this.patientStatus = patientStatus;
    }

	
	public EncounterType getEncounterType() {
		return encounterType;
	}
	
	public void setEncounterType(EncounterType encounterType) {
		this.encounterType = encounterType;
	}
	
}
