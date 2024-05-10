package org.openmrs.module.ohrireports.datasetdefinition.datim.tb_prev;

import java.util.Date;

import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.springframework.stereotype.Component;

@Component
public class TbPrevNumeratorDataSetDefinition extends BaseDataSetDefinition {
	
	@ConfigurationProperty
	private Date startDate;
	
	@ConfigurationProperty
	private Date endDate;
	
	@ConfigurationProperty
	private boolean aggregateType;
	
	@ConfigurationProperty
	private Boolean header = false;
	
	public boolean getAggregateType() {
		return aggregateType;
	}
	
	public void setAggregateType(boolean _aggregateType) {
		aggregateType = _aggregateType;
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
	
	public Boolean getHeader() {
		return header;
	}
	
	public void setHeader(Boolean header) {
		this.header = header;
	}
	
}
