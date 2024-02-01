package org.openmrs.module.ohrireports.api.impl.query.pmtct;

import org.hibernate.Query;
import org.openmrs.Cohort;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.ohrireports.api.impl.PatientQueryImpDao;
import org.openmrs.module.ohrireports.api.impl.query.EncounterQuery;
import org.openmrs.module.ohrireports.datasetevaluator.hmis.HMISUtilies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.openmrs.module.ohrireports.OHRIReportsConstants.*;

@Component
public class ARTQuery extends PatientQueryImpDao {
	
	private DbSessionFactory sessionFactory;
	
	private Cohort baseCohort;
	
	@Autowired
	private EncounterQuery encounterQuery;
	
	public Cohort getBaseCohort() {
		return baseCohort;
	}
	
	List<Integer> latestFollowUpEncounter;
	
	public List<Integer> getLatestFollowUpEncounter() {
		return latestFollowUpEncounter;
	}
	
	public Cohort pmtctARTCohort;
	
	public Cohort newOnARTPMTCTARTCohort;
	
	public Cohort getNewOnARTPMTCTARTCohort() {
		return newOnARTPMTCTARTCohort;
	}
	
	public void setNewOnARTPMTCTARTCohort(Cohort newOnARTPMTCTARTCohort) {
		this.newOnARTPMTCTARTCohort = newOnARTPMTCTARTCohort;
	}
	
	public Cohort alreadyOnARTPMTCTARTCohort;
	
	public Cohort getAlreadyOnARTPMTCTARTCohort() {
		return alreadyOnARTPMTCTARTCohort;
	}
	
	public Cohort getPmtctARTCohort() {
		return pmtctARTCohort;
	}
	
	public List<Integer> getBaseEncounter() {
		return baseEncounter;
	}
	
	private List<Integer> baseEncounter;
	
	private Date startDate;
	
	private Date endDate;
	
	public ARTQuery(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
		super.setSessionFactory(sessionFactory);
	}
	
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
		latestFollowUpEncounter = encounterQuery.getAliveFollowUpEncounters(null, endDate);
		baseCohort = getByPregnantStatus();
		pmtctARTCohort = getPMTCTARTCohort();
		newOnARTPMTCTARTCohort = getNewOnArtCohort("F", startDate, endDate, pmtctARTCohort, latestFollowUpEncounter);
		alreadyOnARTPMTCTARTCohort = HMISUtilies.getOuterUnion(pmtctARTCohort, newOnARTPMTCTARTCohort);
	}
	
	public Cohort getByPregnantStatus() {
		StringBuilder sql = baseConceptQuery(PREGNANCY_STATUS);
		sql.append(" and " + CONCEPT_BASE_ALIAS_OBS + " value_coded = " + conceptQuery(YES));
		sql.append(" and " + CONCEPT_BASE_ALIAS_OBS + "encounter_id in (:latestEncounter) ");
		
		Query query = sessionFactory.getCurrentSession().createSQLQuery(sql.toString());
		query.setParameterList("latestEncounter", latestFollowUpEncounter);
		
		return new Cohort(query.list());
	}
	
	public Cohort getPMTCTARTCohort() {
		String stringQuery = "select distinct person_id\n" + "from obs\n" + "where concept_id = "
		        + conceptQuery(PMTCT_BOOKING_DATE) + "and value_datetime >= :start " + " and value_datetime <= :end "
		        + " and person_id in (:personIdList)";
		
		Query query = sessionFactory.getCurrentSession().createSQLQuery(stringQuery);
		query.setDate("start", startDate);
		query.setDate("end", endDate);
		query.setParameterList("personIdList", baseCohort.getMemberIds());
		
		return new Cohort(query.list());
	}
	
	protected HashMap<Integer, Object> getDictionary(Query query) {
        List list = query.list();
        HashMap<Integer, Object> dictionary = new HashMap<>();
        int personId = 0;
        Object[] objects;
        for (Object object : list) {

            objects = (Object[]) object;
            personId = (Integer) objects[0];

            if (dictionary.get((Integer) personId) == null) {
                dictionary.put(personId, objects[1]);
            }

        }

        return dictionary;
    }
}
