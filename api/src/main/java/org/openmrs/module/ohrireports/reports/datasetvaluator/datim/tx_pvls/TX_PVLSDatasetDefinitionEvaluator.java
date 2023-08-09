package org.openmrs.module.ohrireports.reports.datasetvaluator.datim.tx_pvls;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.openmrs.Cohort;
import org.openmrs.Person;
import org.openmrs.annotation.Handler;
import org.openmrs.module.ohrireports.api.impl.query.HivPvlsQuery;
import org.openmrs.module.ohrireports.reports.datasetdefinition.datim.tx_pvls.TX_PVLSDatasetDefinition;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.springframework.beans.factory.annotation.Autowired;

@Handler(supports = { TX_PVLSDatasetDefinition.class })
public class TX_PVLSDatasetDefinitionEvaluator implements DataSetEvaluator {



    private TX_PVLSDatasetDefinition txDatasetDefinition;
    private int minCount = 0;
    private int maxCount = 4;
    private int total = 0;
    private String routineDesc = "ROUTINE: Disaggregated by Age / Sex / Testing Indication (Fine Disaggregated). Must complete finer disaggregated unless permitted by program";
    private String targetDesc = "Targeted: Disaggregated by Age / Sex / Testing Indication (Fine Disaggregated). Must complete finer disaggregated unless permitted by program";

    List<Integer> countedPatientId = new ArrayList<>();
    Set<Integer> patientIdList = new HashSet<>();
    List<Person> personList = new ArrayList<>();

    @Autowired
    private HivPvlsQuery qHivPvlsQuery;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evalContext)
            throws EvaluationException {

        txDatasetDefinition = (TX_PVLSDatasetDefinition) dataSetDefinition;

        patientIdList = txDatasetDefinition.getIncludeUnSuppressed() ? getAllPatientWithViralLoadCount()
                : getAllPatientListWithSuppressedViral();
        countedPatientId.clear();
       
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, evalContext);
       
        DataSetRow routineDataSetRow = new DataSetRow();
       
        routineDataSetRow.addColumnValue(new DataSetColumn("label", "", String.class), routineDesc);
        dataSet.addRow(routineDataSetRow);
       
        buildDataSet(dataSet, true);

        countedPatientId.clear();

        DataSetRow targetDataSetRow = new DataSetRow();
      
        targetDataSetRow.addColumnValue(new DataSetColumn("label", "", String.class), targetDesc);
        dataSet.addRow(targetDataSetRow);
       
        buildDataSet(dataSet, false);
       
        return dataSet;
    }

    /*
     * Load all patient with viral load count has done in the last 12 month
     */

    private void buildDataSet(SimpleDataSet simpleDataSet, boolean isRouteType) {

        DataSetRow femaleSetRow = new DataSetRow();
        buildDataSetColumn(femaleSetRow, "F", isRouteType);
        simpleDataSet.addRow(femaleSetRow);

        DataSetRow maleSetRow = new DataSetRow();
        buildDataSetColumn(maleSetRow, "M", isRouteType);
        simpleDataSet.addRow(maleSetRow);
    }

    private Set<Integer> getAllPatientWithViralLoadCount() {

        Cohort cohort = qHivPvlsQuery.getPatientWithViralLoadCount("", txDatasetDefinition.getStartDate(),
                txDatasetDefinition.getEndDate());
        return cohort.getMemberIds();
    }

    private Set<Integer> getAllPatientListWithSuppressedViral() {
        Cohort suppressedCohort = qHivPvlsQuery.getPatientsWithViralLoadSuppressed("",
                txDatasetDefinition.getStartDate(), txDatasetDefinition.getEndDate());
        Cohort ViremiaCohort = qHivPvlsQuery.getPatientWithViralLoadCountLowLevelViremia(" ",
                txDatasetDefinition.getStartDate(), txDatasetDefinition.getEndDate());
        Set<Integer> patientIds = new HashSet<>();
        patientIds.addAll(suppressedCohort.getMemberIds());
        patientIds.addAll(ViremiaCohort.getMemberIds());

        return patientIds;
    }

    private void buildDataSetColumn(DataSetRow dataSet, String gender, boolean isRouteType) {
        total = 0;
        minCount = 15;
        maxCount = 19;
        personList.clear();
        Cohort cohort = qHivPvlsQuery.getPatientVLByTestReason(gender, txDatasetDefinition.getStartDate(),
                txDatasetDefinition.getEndDate(), isRouteType, new Cohort(patientIdList));
        personList = qHivPvlsQuery.getPersons(cohort);
        dataSet.addColumnValue(new DataSetColumn("ByAgeAndSexData", "Gender", Integer.class),
                gender.equals("F") ? "Female"
                        : "Male");
        dataSet.addColumnValue(new DataSetColumn("unknownAge", "Unknown Age", Integer.class),
                getEnrolledByUnknownAge());

        while (minCount <= 65) {
            if (minCount == 65) {
                dataSet.addColumnValue(new DataSetColumn("65+", "65+", Integer.class),
                        getEnrolledByAgeAndGender(65, 200, gender));
            } else {
                dataSet.addColumnValue(
                        new DataSetColumn(minCount + "-" + maxCount, minCount + "-" + maxCount, Integer.class),
                        getEnrolledByAgeAndGender(minCount, maxCount, gender));
            }
            minCount = maxCount + 1;
            maxCount = minCount + 4;
        }
        dataSet.addColumnValue(new DataSetColumn("Sub-total", "Subtotal", Integer.class), total);
    }

    private int getEnrolledByAgeAndGender(int min, int max, String gender) {
        int count = 0;

        for (Person person : personList) {

            if (countedPatientId.contains(person.getPersonId()))
                continue;

            if (person.getAge() >= min && person.getAge() <= max) {

                countedPatientId.add(person.getPersonId());
                count++;
            }
        }
        incrementTotalCount(count);
        clearCountedObs(countedPatientId);
        return count;
    }

    private int getEnrolledByUnknownAge() {
        int count = 0;

        for (Person person : personList) {

            if (countedPatientId.contains(person.getPersonId()))
                continue;

            if (Objects.isNull(person.getAge()) ||
                    person.getAge() <= 0) {
                count++;

                countedPatientId.add(person.getPersonId());
            }

        }
        incrementTotalCount(count);
        clearCountedObs(countedPatientId);
        return count;
    }

    private void incrementTotalCount(int count) {
        if (count > 0)
            total = total + count;
    }

    private void clearCountedObs(List<Integer> obsList) {
        for (Integer patientId : countedPatientId) {
            personList.removeIf(p -> p.getPersonId().equals(patientId));
        }
    }

}
