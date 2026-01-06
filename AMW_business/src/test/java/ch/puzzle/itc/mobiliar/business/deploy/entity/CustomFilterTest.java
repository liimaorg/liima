/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2016 by Puzzle ITC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.puzzle.itc.mobiliar.business.deploy.entity;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class CustomFilterTest {

    DeploymentFilterTypes integerType = DeploymentFilterTypes.TRACKING_ID;
    DeploymentFilterTypes booleanType = DeploymentFilterTypes.DEPLOYMENT_CONFIRMED;
    DeploymentFilterTypes dateType = DeploymentFilterTypes.JOB_CREATION_DATE;
    DeploymentFilterTypes stringType = DeploymentFilterTypes.CONFIRMATION_USER;
    DeploymentFilterTypes specialFilterType = DeploymentFilterTypes.LASTDEPLOYJOBFORASENV;
    DeploymentFilterTypes labeledDateType = DeploymentFilterTypes.RELEASE;

    @Test
    public void test_CustomFilter_Build_Integer() {
        // given
        CustomFilter c = CustomFilter.builder(integerType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();

        // when

        // then
        assertEquals(FilterType.IntegerType, c.getFilterType());
        assertTrue(c.isIntegerType());
        assertEquals("0", c.getValue());
        assertEquals(Integer.valueOf(0), c.getIntegerValue());
        assertEquals("filterDisplayName", c.getFilterDisplayName());
        assertEquals("deploymentTableColumnName", c.getDeploymentTableColumnName());

        assertEquals(0, c.getDropDownItems().size());

        assertEquals(true, c.isSelected());

    }

    @Test
    public void test_CustomFilter_Build_Boolean() {
        // given
        CustomFilter c = CustomFilter.builder(booleanType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();

        // when

        // then
        assertEquals(FilterType.booleanType, c.getFilterType());
        assertTrue(c.isBooleanType());
        assertEquals("true", c.getValue());
        assertEquals(true, c.getBooleanValue());
        assertEquals(2, c.getDropDownItems().size());
        assertEquals("true", c.getDropDownItems().get(0));
        assertEquals("false", c.getDropDownItems().get(1));
        assertEquals("filterDisplayName", c.getFilterDisplayName());
        assertEquals("deploymentTableColumnName", c.getDeploymentTableColumnName());

        assertEquals(true, c.isSelected());

    }

    @Test
    public void test_CustomFilter_Build_Date() {
        // given
        CustomFilter c = CustomFilter.builder(dateType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when

        // then
        assertEquals(FilterType.DateType, c.getFilterType());
        assertTrue(c.isDateType());
        assertTrue(c.getDateValue() instanceof Date);
        assertEquals(0, c.getDropDownItems().size());
        assertEquals("filterDisplayName", c.getFilterDisplayName());
        assertEquals("deploymentTableColumnName", c.getDeploymentTableColumnName());

        assertEquals(true, c.isSelected());

    }

    @Test
    public void test_CustomFilter_Build_String() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when

        // then
        assertEquals(FilterType.StringType, c.getFilterType());
        assertTrue(c.isStringType());
        assertEquals("", c.getStringValue());
        assertEquals("", c.getValue());
        assertEquals(0, c.getDropDownItems().size());
        assertEquals("filterDisplayName", c.getFilterDisplayName());
        assertEquals("deploymentTableColumnName", c.getDeploymentTableColumnName());

        assertEquals(true, c.isSelected());

    }

    @Test
    public void test_CustomFilter_Build_SpecialFiler() {
        // given
        CustomFilter c = CustomFilter.builder(specialFilterType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when

        // then
        assertEquals(FilterType.SpecialFilterType, c.getFilterType());
        assertTrue(c.isSpecialFilterType());
        assertEquals("", c.getValue());

        assertEquals(0, c.getDropDownItems().size());
        assertEquals("filterDisplayName", c.getFilterDisplayName());
        assertEquals("deploymentTableColumnName", c.getDeploymentTableColumnName());

        assertEquals(true, c.isSelected());

    }

    @Test
    public void test_setValue_boolean_true() {
        // given
        CustomFilter c = CustomFilter.builder(booleanType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValue("true");
        // then
        assertEquals(FilterType.booleanType, c.getFilterType());
        assertEquals(true, c.getBooleanValue());
        assertEquals("true", c.getValue());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValue_boolean_false() {
        // given
        CustomFilter c = CustomFilter.builder(booleanType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValue("false");
        // then
        assertEquals(FilterType.booleanType, c.getFilterType());
        assertEquals(false, c.getBooleanValue());
        assertEquals("false", c.getValue());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValue_boolean_null() {
        // given
        CustomFilter c = CustomFilter.builder(booleanType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValue(null);
        // then
        assertEquals(FilterType.booleanType, c.getFilterType());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getValue());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValue_boolean_notBoolean() {
        // given
        CustomFilter c = CustomFilter.builder(booleanType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValue("test");
        // then
        assertEquals(FilterType.booleanType, c.getFilterType());
        assertEquals(false, c.getBooleanValue());
        assertEquals("false", c.getValue());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValue_boolean_notBoolean2() {
        // given
        CustomFilter c = CustomFilter.builder(booleanType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValue("1");
        // then
        assertEquals(FilterType.booleanType, c.getFilterType());
        assertEquals(false, c.getBooleanValue());
        assertEquals("false", c.getValue());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValue_String() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValue("str");
        // then
        assertEquals(FilterType.StringType, c.getFilterType());
        assertEquals("str", c.getStringValue());
        assertEquals("str", c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValue_String_empty() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValue("");
        // then
        assertEquals(FilterType.StringType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValue_String_null() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValue(null);
        // then
        assertEquals(FilterType.StringType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValue_Integer() {
        // given
        CustomFilter c = CustomFilter.builder(integerType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValue("2");
        // then
        assertEquals(FilterType.IntegerType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        assertEquals("2", c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getDateValue());
        assertEquals(Integer.valueOf(2), c.getIntegerValue());
    }

    @Test
    public void test_setValue_Integer_0() {
        // given
        CustomFilter c = CustomFilter.builder(integerType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValue("0");
        // then
        assertEquals(FilterType.IntegerType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        assertEquals("0", c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getDateValue());
        assertEquals(Integer.valueOf(0), c.getIntegerValue());
    }

    @Test
    public void test_setValue_Integer_trailing_spaces() {
        // given
        CustomFilter c = CustomFilter.builder(integerType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValue(" 3 ");
        // then
        assertEquals(FilterType.IntegerType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        assertEquals("3", c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getDateValue());
        assertEquals(Integer.valueOf(3), c.getIntegerValue());
    }

    @Test
    public void test_setValue_Integer_null() {
        // given
        CustomFilter c = CustomFilter.builder(integerType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValue(null);
        // then
        assertEquals(FilterType.IntegerType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValue_Integer_empty() {
        // given
        CustomFilter c = CustomFilter.builder(integerType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValue("");
        // then
        assertEquals(FilterType.IntegerType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValue_Integer_noInt() {
        // given
        CustomFilter c = CustomFilter.builder(integerType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValue("asdf");
        // then
        assertEquals(FilterType.IntegerType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValue_Date() {
        // given
        CustomFilter c = CustomFilter.builder(dateType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        Date d = new Date();
        String date = "" + d.toGMTString();
        // when
        c.setValue(date);
        // then
        assertEquals(FilterType.DateType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        // FIXME Due to wrong usage of Date needs to be refactored
        // assertEquals(d.toLocaleString(), c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(date, c.getDateValue().toGMTString());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValue_LabeledDate() {
        // given
        CustomFilter c = CustomFilter.builder(labeledDateType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        Date d = new Date();
        String date = CustomFilter.convertDateToString(d);
        // when
        c.setValue(date);
        // then
        assertEquals(FilterType.LabeledDateType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        // FIXME Due to wrong usage of Date needs to be refactored
        // assertEquals(d.toLocaleString(), c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(d.getTime(), c.getDateValue().getTime());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValue_SpecialFilter() {
        // given
        CustomFilter c = CustomFilter.builder(specialFilterType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();

        // when
        // does nothing still is ""
        c.setValue("spez");
        // then
        assertEquals(FilterType.SpecialFilterType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        assertEquals("", c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_isNullableFilter() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();

        // when
        boolean nullableFilter = c.isNullableFilter();
        // then Default value is false
        assertFalse(nullableFilter);
    }

    @Test
    public void test_isNullableFilter_true() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();

        c.setNullableFilter(true);

        // when
        boolean nullableFilter = c.isNullableFilter();
        // then
        assertTrue(nullableFilter);
    }

    @Test
    public void test_isNullableFilter_Integer() {
        // given
        CustomFilter c = CustomFilter.builder(integerType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();

        c.setNullableFilter(true);

        // when
        boolean nullableFilter = c.isNullableFilter();
        // then only for StringType
        assertFalse(nullableFilter);
    }

    @Test
    public void test_getSqlComperator_default_comperatorSelection() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();

        // when
        String sqlComperator = c.getSqlComperator();
        // then
        assertThat(sqlComperator, is(ComparatorFilterOption.eq.getSqlStringComperator()));
    }

    @Test
    public void test_getSqlComperator_String() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        c.setComparatorSelection(ComparatorFilterOption.eq);

        // when
        String sqlComperator = c.getSqlComperator();
        // then
        assertEquals(" like ", sqlComperator);
    }

    @Test
    public void test_getSqlComperator_Integer() {
        // given
        CustomFilter c = CustomFilter.builder(integerType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        c.setComparatorSelection(ComparatorFilterOption.eq);

        // when
        String sqlComperator = c.getSqlComperator();
        // then
        assertEquals(" = ", sqlComperator);
    }

    @Test
    public void test_getSqlComperator_Boolean() {
        // given
        CustomFilter c = CustomFilter.builder(booleanType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        c.setComparatorSelection(ComparatorFilterOption.eq);

        // when
        String sqlComperator = c.getSqlComperator();
        // then
        assertEquals(" is ", sqlComperator);
    }

    @Test
    public void test_getSqlComperator_Special() {
        // given
        CustomFilter c = CustomFilter.builder(specialFilterType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        c.setComparatorSelection(ComparatorFilterOption.eq);

        // when
        String sqlComperator = c.getSqlComperator();
        // then
        assertEquals(" = ", sqlComperator);
    }

    @Test
    public void test_getParameterName() {
        // given
        CustomFilter c = CustomFilter.builder(specialFilterType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        c.setComparatorSelection(ComparatorFilterOption.eq);

        // when
        String parameterName = c.getParameterName();
        // then
        assertEquals("filterDisplayName", parameterName);
    }

    @Test
    public void test_getParameterName_empty() {
        // given
        CustomFilter c = CustomFilter.builder(specialFilterType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        c.setComparatorSelection(ComparatorFilterOption.eq);

        // when
        String parameterName = c.getParameterName();
        // then
        assertEquals("filterDisplayName", parameterName);
    }

    @Test
    public void test_isValidForSqlQuery_SpecialFilter() {
        // given
        CustomFilter c = CustomFilter.builder(specialFilterType)
                .filterDisplayName("filter DisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();

        // then
        assertFalse(c.isValidForSqlQuery());
    }

    @Test
    public void test_isValidForSqlQuery_String() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filter DisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();

        // then
        assertFalse(c.isValidForSqlQuery());
    }

    @Test
    public void test_isValidForSqlQuery_String_ok() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filter DisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .comparatorSelection(ComparatorFilterOption.eq)
                .build();
        c.setValue("test");
        // then
        assertTrue(c.isValidForSqlQuery());
    }

    @Test
    public void test_isValidForSqlQuery_String_empty_value() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filter DisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        c.setComparatorSelection(ComparatorFilterOption.eq);
        c.setValue("");

        // then
        assertFalse(c.isValidForSqlQuery());
    }

    @Test
    public void test_isValidForSqlQuery_String_null() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filter DisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        c.setComparatorSelection(ComparatorFilterOption.eq);
        c.setValue(null);

        // then
        assertFalse(c.isValidForSqlQuery());
    }

    @Test
    public void test_isValidForSqlQuery_String_null_andNullable() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filter DisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        c.setComparatorSelection(ComparatorFilterOption.eq);
        c.setNullableFilter(true);
        c.setValue(null);

        // then
        assertTrue(c.isValidForSqlQuery());
    }

    @Test
    public void test_isValidForSqlQuery_String_val_andNullable() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filter DisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        c.setComparatorSelection(ComparatorFilterOption.eq);
        c.setNullableFilter(true);
        c.setValue("test");

        // then
        assertTrue(c.isValidForSqlQuery());
    }

    // ---

    @Test
    public void test_isValidForSqlQuery_String_empty_valueForRest() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filter DisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        c.setComparatorSelection(ComparatorFilterOption.eq);
        c.setValueFromRest("");

        // then
        assertFalse(c.isValidForSqlQuery());
    }

    @Test
    public void test_isValidForSqlQuery_String_null_andNullable_forRest() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filter DisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        c.setComparatorSelection(ComparatorFilterOption.eq);
        c.setNullableFilter(true);
        c.setValueFromRest(null);

        // then
        assertTrue(c.isValidForSqlQuery());
    }

    @Test
    public void test_isValidForSqlQuery_String_ok_forRest() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filter DisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .comparatorSelection(ComparatorFilterOption.eq)
                .build();
        c.setValueFromRest("test");
        // then
        assertTrue(c.isValidForSqlQuery());
    }

    @Test
    public void test_isValidForSqlQuery_String_val_andNullable_forRest() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filter DisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        c.setComparatorSelection(ComparatorFilterOption.eq);
        c.setNullableFilter(true);
        c.setValueFromRest("test");

        // then
        assertTrue(c.isValidForSqlQuery());
    }

    @Test
    public void test_setValueFromRest_boolean_false() {
        // given
        CustomFilter c = CustomFilter.builder(booleanType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValueFromRest("false");
        // then
        assertEquals(FilterType.booleanType, c.getFilterType());
        assertEquals(false, c.getBooleanValue());
        assertEquals("false", c.getValue());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValueFromRest_boolean_notBoolean() {
        // given
        CustomFilter c = CustomFilter.builder(booleanType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValueFromRest("test");
        // then
        assertEquals(FilterType.booleanType, c.getFilterType());
        assertEquals(false, c.getBooleanValue());
        assertEquals("false", c.getValue());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValueFromRest_boolean_notBoolean2() {
        // given
        CustomFilter c = CustomFilter.builder(booleanType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValueFromRest("1");
        // then
        assertEquals(FilterType.booleanType, c.getFilterType());
        assertEquals(false, c.getBooleanValue());
        assertEquals("false", c.getValue());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValueFromRest_boolean_null() {
        // given
        CustomFilter c = CustomFilter.builder(booleanType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValueFromRest(null);
        // then
        assertEquals(FilterType.booleanType, c.getFilterType());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getValue());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }


    @Test
    public void test_setValueFromRest_boolean_true() {
        // given
        CustomFilter c = CustomFilter.builder(booleanType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValueFromRest("true");
        // then
        assertEquals(FilterType.booleanType, c.getFilterType());
        assertEquals(true, c.getBooleanValue());
        assertEquals("true", c.getValue());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValueFromRest_Date() {
        // given
        CustomFilter c = CustomFilter.builder(dateType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        long timeInMillis = System.currentTimeMillis();
        Date date = new Date(timeInMillis);
        // when
        c.setValueFromRest(String.valueOf(timeInMillis));
        // then
        assertEquals(FilterType.DateType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(date, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValueFromRest_Integer() {
        // given
        CustomFilter c = CustomFilter.builder(integerType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValueFromRest("2");
        // then
        assertEquals(FilterType.IntegerType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        assertEquals("2", c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getDateValue());
        assertEquals(Integer.valueOf(2), c.getIntegerValue());
    }

    @Test
    public void test_setValueFromRest_Integer_0() {
        // given
        CustomFilter c = CustomFilter.builder(integerType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValueFromRest("0");
        // then
        assertEquals(FilterType.IntegerType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        assertEquals("0", c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getDateValue());
        assertEquals(Integer.valueOf(0), c.getIntegerValue());
    }

    @Test
    public void test_setValueFromRest_Integer_empty() {
        // given
        CustomFilter c = CustomFilter.builder(integerType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValueFromRest("");
        // then
        assertEquals(FilterType.IntegerType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValueFromRest_Integer_noInt() {
        // given
        CustomFilter c = CustomFilter.builder(integerType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValueFromRest("asdf");
        // then
        assertEquals(FilterType.IntegerType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValueFromRest_Integer_null() {
        // given
        CustomFilter c = CustomFilter.builder(integerType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValueFromRest(null);
        // then
        assertEquals(FilterType.IntegerType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValueFromRest_Integer_trailing_spaces() {
        // given
        CustomFilter c = CustomFilter.builder(integerType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValueFromRest(" 3 ");
        // then
        assertEquals(FilterType.IntegerType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        assertEquals("3", c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getDateValue());
        assertEquals(Integer.valueOf(3), c.getIntegerValue());
    }

    @Test
    public void test_setValueFromRest_LabeledDate() {
        // given
        CustomFilter c = CustomFilter.builder(labeledDateType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        Date d = new Date();
        String date = CustomFilter.convertDateToString(d);
        // when
        c.setValueFromRest(date);
        // then
        assertEquals(FilterType.LabeledDateType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(d.getTime(), c.getDateValue().getTime());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValueFromRest_SpecialFilter() {
        // given
        CustomFilter c = CustomFilter.builder(specialFilterType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();

        // when
        // does nothing still is ""
        c.setValueFromRest("spez");
        // then
        assertEquals(FilterType.SpecialFilterType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        assertEquals("", c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValueFromRest_String() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValueFromRest("str");
        // then
        assertEquals(FilterType.StringType, c.getFilterType());
        assertEquals("str", c.getStringValue());
        assertEquals("str", c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValueFromRest_String_empty() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValueFromRest("");
        // then
        assertEquals(FilterType.StringType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

    @Test
    public void test_setValueFromRest_String_null() {
        // given
        CustomFilter c = CustomFilter.builder(stringType)
                .filterDisplayName("filterDisplayName")
                .deploymentTableColumnName("deploymentTableColumnName")
                .build();
        // when
        c.setValueFromRest(null);
        // then
        assertEquals(FilterType.StringType, c.getFilterType());
        assertEquals(null, c.getStringValue());
        assertEquals(null, c.getValue());
        assertEquals(null, c.getBooleanValue());
        assertEquals(null, c.getDateValue());
        assertEquals(null, c.getIntegerValue());
    }

}
