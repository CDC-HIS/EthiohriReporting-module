package org.openmrs.module.ohrireports.api.impl.query;

import org.hibernate.Query;
import org.jetbrains.annotations.NotNull;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.logic.op.In;
import org.openmrs.module.ohrireports.api.impl.PatientQueryImpDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.openmrs.module.ohrireports.OHRIReportsConstants.*;

@Component
public class TBARTQuery extends PatientQueryImpDao {

    private DbSessionFactory sessionFactory;
    private List<Integer> baseEncounter = new ArrayList<>();

    public List<Integer> getTbTreatmentStartDateEncounter() {
        return tbTreatmentStartDateEncounter;
    }

    private List<Integer> tbTreatmentStartDateEncounter = new ArrayList<>();
    private List<Integer> tbTreatmentCompletedDateEncounter = new ArrayList<>();
    private List<Integer> tbTreatmentDiscontinuedDateEncounter = new ArrayList<>();

    private List<Integer> activeDiagnosticStartDateEncounter = new ArrayList<>();
    @Autowired
    private EncounterQuery encounterQuery;

    public List<Integer> getTbArtEncounter() {
        return tbArtEncounter;
    }

    List<Integer> tbArtEncounter = new ArrayList<>();

    public List<Integer> getBaseEncounter() {
        return baseEncounter;
    }

    @Autowired
    public TBARTQuery(DbSessionFactory _SessionFactory) {

        sessionFactory = _SessionFactory;
        setSessionFactory(sessionFactory);

    }

    public Cohort getCohortByTBTreatmentStartDate(Date start, Date end) {
        //Retrieve latest followup according end date
        List<Integer> latestFollowUp = encounterQuery.getAliveFollowUpEncounters(null, end);
        baseEncounter = latestFollowUp;
        // Check and fetch patient that they are on ART from the latest follow up
        Cohort cohort = getActiveOnArtCohort("", null, end, null, latestFollowUp);
        // Retrieve all patient from active on art of started TB treatment
        cohort = getCurrentOnActiveTB(start, end, latestFollowUp);
        return cohort;
    }

    public Cohort getAlreadyOnArtCohort(Cohort cohort, Date beforeDate) {
        StringBuilder sqlBuilder = getQuery();

        sqlBuilder.append("  and value_datetime < :start");

        Query query = sessionFactory.getCurrentSession().createSQLQuery(sqlBuilder.toString());

        query.setParameterList("encounterIds", baseEncounter);
        query.setParameterList("personId", cohort.getMemberIds());
        query.setDate("start", beforeDate);

        return new Cohort(query.list());
    }

    public Cohort getNewOnArtCohort(Cohort cohort, Date onOrAfter, Date beforeOrOn) {
        StringBuilder sqlBuilder = getQuery();

        sqlBuilder.append(" and value_datetime >= :start");
        sqlBuilder.append(" and value_datetime <= :end");

        Query query = sessionFactory.getCurrentSession().createSQLQuery(sqlBuilder.toString());

        query.setParameterList("encounterIds", baseEncounter);
        query.setParameterList("personId", cohort.getMemberIds());
        query.setDate("start", onOrAfter);
        query.setDate("end", beforeOrOn);

        return new Cohort(query.list());
    }

    @NotNull
    private StringBuilder getQuery() {
        StringBuilder sqlBuilder = new StringBuilder("select distinct (person_id) from obs ");
        sqlBuilder.append(" where encounter_id in (:encounterIds) and person_id in (:personId)");
        sqlBuilder.append(" and concept_id=").append(conceptQuery(ART_START_DATE));
        return sqlBuilder;
    }

    private Cohort getCurrentOnActiveTB(Date start, Date end, List<Integer> encounters) {

        Date startDate = getOneYearBackFromEndDate(end);
        tbTreatmentStartDateEncounter = encounterQuery.getEncounters(Collections.singletonList(TB_TREATMENT_START_DATE), startDate, end,encounters);
        activeDiagnosticStartDateEncounter = encounterQuery.getEncounters(Collections.singletonList(TB_ACTIVE_DATE), startDate, end, encounters);

        // to be excluded encounters from TX_TB counting
        tbTreatmentCompletedDateEncounter = encounterQuery.getEncounters(Collections.singletonList(TB_TREATMENT_COMPLETED_DATE), null, end, encounters);
        tbTreatmentDiscontinuedDateEncounter = encounterQuery.getEncounters(Collections.singletonList(TB_TREATMENT_DISCONTINUED_DATE), null,end, encounters);
        List<Integer> toBeExcludedEncounters = unionTwoMembership(tbTreatmentCompletedDateEncounter, tbTreatmentCompletedDateEncounter);


        List<Integer> tbTreatmentAndDiagnosticStartDateEncounter = unionTwoMembership(tbTreatmentStartDateEncounter, activeDiagnosticStartDateEncounter);


        tbArtEncounter = excludeEncounter(toBeExcludedEncounters, tbTreatmentAndDiagnosticStartDateEncounter);


        Cohort treatmentStartedCohort = getCohort(tbTreatmentStartDateEncounter);
        Cohort activeDiagonosticCohort = getCohort(activeDiagnosticStartDateEncounter);

        for (CohortMembership treatmentMembership : treatmentStartedCohort.getMemberships()) {
            if (!activeDiagonosticCohort.contains(treatmentMembership.getPatientId())) {
                activeDiagonosticCohort.addMembership(treatmentMembership);
            }
        }
        return activeDiagonosticCohort;
    }

    private Date getOneYearBackFromEndDate(Date endDate) {

        // Create a Calendar instance
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);

        // Subtract 1 year from the end date
        calendar.add(Calendar.YEAR, -1);

        return calendar.getTime();

    }

    private List<Integer> excludeEncounter(List<Integer> toBeExcludedEncounters, List<Integer> fromEncounters) {
        List<Integer> result = new ArrayList<>();
        for (Integer encounterId : fromEncounters) {
            if (!toBeExcludedEncounters.contains(encounterId)) {
                result.add(encounterId);
            }
        }
        return result;
    }

    public List<Integer> getActiveDiagnosticStartDateEncounter() {
        return activeDiagnosticStartDateEncounter;
    }

    private List<Integer> unionTwoMembership(List<Integer> firstMembership, List<Integer> secondMembership) {

        // Convert lists to sets
        Set<Integer> firstSet = new HashSet<>(firstMembership);
        Set<Integer> secondSet = new HashSet<>(secondMembership);

        // Find intersection
        Set<Integer> intersection = new HashSet<>(firstSet);
        intersection.retainAll(secondSet);

        // Merge unique elements from both lists and the intersection
        Set<Integer> mergedSet = new HashSet<>(firstSet);
        mergedSet.addAll(secondSet);


        return new ArrayList<>(mergedSet);

    }
}
