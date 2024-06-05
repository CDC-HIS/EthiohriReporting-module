package org.openmrs.module.ohrireports.datasetevaluator.hmis.pr_ep.hiv_prep_curr;

import static org.openmrs.module.ohrireports.OHRIReportsConstants.*;
import static org.openmrs.module.ohrireports.datasetevaluator.hmis.HMISConstant.COLUMN_1_NAME;
import static org.openmrs.module.ohrireports.datasetevaluator.hmis.HMISConstant.COLUMN_2_NAME;

import java.util.*;

import org.openmrs.Cohort;
import org.openmrs.Person;
import org.openmrs.module.ohrireports.datasetevaluator.hmis.Gender;
import org.openmrs.module.ohrireports.datasetevaluator.hmis.pr_ep.HivPrEpQuery;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class HivPrEpCurrEvaluator  {
    
    @Autowired
    private HivPrEpQuery hivPrEPQuery;
    
	private final String COLUMN_3_NAME = "Number";
    List<Person> personList = new ArrayList<>();
	private Date end;
    
    public void buildDataset(Date start,Date end, SimpleDataSet dataset) {
	    
	    this.end = end;

        hivPrEPQuery.setStartDate(start);
        hivPrEPQuery.setEndDate(end, FOLLOW_UP_DATE, PREP_FOLLOW_UP_ENCOUNTER_TYPE);

        Cohort prepFollowupCohort = hivPrEPQuery.getAllPrEPCurr();


        dataset.addRow(buildColumn("HIV_PrEP_CURR.1", "PrEP Curr (Number of individuals that received oral PrEP during the reporting period)", 0));

        personList = hivPrEPQuery.getPersons(prepFollowupCohort);
        dataset.addRow(buildColumn("1", "By Age and Sex",prepFollowupCohort.size()));
        dataset.addRow(buildColumn("1 .1", "15 - 19 years, Male", getCohortSizeByAgeAndGender(15, 19, Gender.Male)));
        dataset.addRow(buildColumn("1 .2", "15 - 19 years, Female", getCohortSizeByAgeAndGender(15, 19, Gender.Female)));
        dataset.addRow(buildColumn("1 .3", "20 - 24 years, Male", getCohortSizeByAgeAndGender(20, 24, Gender.Male)));
        dataset.addRow(buildColumn("1 .4", "20 - 24 years, Female", getCohortSizeByAgeAndGender(20, 24, Gender.Female)));
        dataset.addRow(buildColumn("1 .5", "25 - 29 years, Male", getCohortSizeByAgeAndGender(25, 29, Gender.Male)));
        dataset.addRow(buildColumn("1 .6", "25 - 29 years, Female", getCohortSizeByAgeAndGender(25, 29, Gender.Female)));
        dataset.addRow(buildColumn("1 .7", "30 - 34 years, Male", getCohortSizeByAgeAndGender(30, 34, Gender.Male)));
        dataset.addRow(buildColumn("1 .8", "30 - 34 years, Female", getCohortSizeByAgeAndGender(30, 34, Gender.Female)));
        dataset.addRow(buildColumn("1 .9", "35 - 39 years, Male", getCohortSizeByAgeAndGender(35, 39, Gender.Male)));
        dataset.addRow(buildColumn("1 .10", "35 - 39 years, Female", getCohortSizeByAgeAndGender(35, 39, Gender.Female)));
        dataset.addRow(buildColumn("1 .11", "40 - 44 years, Male", getCohortSizeByAgeAndGender(40, 44, Gender.Male)));
        dataset.addRow(buildColumn("1 .12", "40 - 44 years, Female", getCohortSizeByAgeAndGender(40, 44, Gender.Female)));
        dataset.addRow(buildColumn("1 .13", "45 - 49 years, Male", getCohortSizeByAgeAndGender(45, 49, Gender.Male)));
        dataset.addRow(buildColumn("1 .14", "45 - 49 years, Female", getCohortSizeByAgeAndGender(45, 49, Gender.Female)));
        dataset.addRow(buildColumn("1 .15", ">=50 years, Male", getCohortSizeByAgeAndGender(50, 150, Gender.Male)));
        dataset.addRow(buildColumn("1 .16", ">=50 years, Female", getCohortSizeByAgeAndGender(50, 150, Gender.Female)));

        int total = 0;
        Cohort fsw = hivPrEPQuery.getCategoryOnPrep(SELF_IDENTIFYING_FSW, prepFollowupCohort);
        Cohort discordantCouple = hivPrEPQuery.getCategoryOnPrep(DISCORDANT_COUPLE, prepFollowupCohort);
        total = fsw.size() + discordantCouple.size();

        DataSetRow clientCategoryRow = new DataSetRow();

        clientCategoryRow.addColumnValue(new DataSetColumn(COLUMN_1_NAME, COLUMN_1_NAME, String.class), "HIV_PrEP_CURR.2");
        clientCategoryRow.addColumnValue(new DataSetColumn(COLUMN_2_NAME, COLUMN_2_NAME, String.class),
                "By Client Category");
        clientCategoryRow.addColumnValue(new DataSetColumn(COLUMN_3_NAME, COLUMN_3_NAME, Integer.class), total);

        dataset.addRow(clientCategoryRow);

        DataSetRow discordantCoupleCategoryRow = new DataSetRow();

        discordantCoupleCategoryRow.addColumnValue(new DataSetColumn(COLUMN_1_NAME, COLUMN_1_NAME, String.class),
                "HIV_PrEP_CURR.2 .1");
        discordantCoupleCategoryRow.addColumnValue(new DataSetColumn(COLUMN_2_NAME, COLUMN_2_NAME, String.class),
                "Discordant Couple");
        discordantCoupleCategoryRow.addColumnValue(new DataSetColumn(COLUMN_3_NAME, COLUMN_3_NAME, Integer.class),
                discordantCouple.size());

        dataset.addRow(discordantCoupleCategoryRow);

        DataSetRow fwCategoryRow = new DataSetRow();

        fwCategoryRow.addColumnValue(new DataSetColumn(COLUMN_1_NAME, COLUMN_1_NAME, String.class), "HIV_PrEP_CURR.2 .2");
        fwCategoryRow.addColumnValue(new DataSetColumn(COLUMN_2_NAME, COLUMN_2_NAME, String.class),
                "Female sex worker[FSW]");
        fwCategoryRow.addColumnValue(new DataSetColumn(COLUMN_3_NAME, COLUMN_3_NAME, Integer.class), fsw.size());

        dataset.addRow(fwCategoryRow);


    }

    private DataSetRow buildColumn(String col_1_value, String col_2_value, Integer col_3_value) {
        DataSetRow prepDataSetRow = new DataSetRow();
	    String baseName = "HIV_PrEP_CURR. ";
	    prepDataSetRow.addColumnValue(
                new DataSetColumn(COLUMN_1_NAME, COLUMN_1_NAME, String.class),
                baseName + col_1_value);
        prepDataSetRow.addColumnValue(
                new DataSetColumn(COLUMN_2_NAME, COLUMN_2_NAME, String.class), col_2_value);

        prepDataSetRow.addColumnValue(new DataSetColumn(COLUMN_3_NAME, COLUMN_3_NAME, Integer.class),
                col_3_value);

        return prepDataSetRow;
    }

    private Integer getCohortSizeByAgeAndGender(int minAge, int maxAge, Gender gender) {
        int _age = 0;
        List<Integer> patients = new ArrayList<>();
        String _gender = gender.equals(Gender.Female) ? "f" : "m";
        if (maxAge > 1) {
            maxAge = maxAge + 1;
        }
        for (Person person : personList) {

            _age = person.getAge(end);

            if (!patients.contains(person.getPersonId())
                    && (_age >= minAge && _age < maxAge)
                    && (person.getGender().toLowerCase().equals(_gender))) {

                patients.add(person.getPersonId());

            }
        }
        return patients.size();
    }

}
