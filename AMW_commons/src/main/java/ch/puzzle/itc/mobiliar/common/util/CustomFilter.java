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

package ch.puzzle.itc.mobiliar.common.util;

import static java.lang.Enum.valueOf;

import java.util.*;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

public class CustomFilter {

    private Logger log = Logger.getLogger(CustomFilter.class.getSimpleName());

    public enum FilterType {
        booleanType, StringType, IntegerType, DateType, SpecialFilterType, LabeledDateType, ENUM_TYPE
    }

    public enum ComperatorFilterOption {
        smaller("<", " < ", null, null), smallerequals("<=", " <= ", null, null), equals("is", " = ", " like ",
                " is "), greaterequals(">=", " >= ", null,
                        null), greater(">", " > ", null, null), notequal("is not", " != ", null, " is not ");
        private String displayName;
        private String sqlNumComperator;
        private String sqlBoolComperator;
        private String sqlStringComperator;

        private ComperatorFilterOption(String displayName, String sqlNumComperator, String sqlStringComperator,
                String sqlBoolComperator) {
            this.displayName = displayName;
            this.sqlNumComperator = sqlNumComperator;
            this.sqlStringComperator = sqlStringComperator;
            this.sqlBoolComperator = sqlBoolComperator;
        }

        public String getDisplayName() {
            return displayName;
        }

        private String getSqlNumComperator() {
            return sqlNumComperator;
        }

        private String getSqlStringComperator() {
            return sqlStringComperator;
        }

        private String getSqlBoolComperator() {
            return sqlBoolComperator;
        }
    }

    private final Long filterIdentifikationNumber;
    private String filterDisplayName;
    private String deploymentTableColumnName;

    private String joiningTableQuery;
    private Object value = null;
    private FilterType filterType;

    private ComperatorFilterOption comperatorSelection;

    private boolean isSelected;

    private boolean alwaysAutoComplete;

    private List<String> dropDownItems = new ArrayList<>();

    private Map<String, String> dropDownItemsMap = new HashMap<>();

    public CustomFilter(String filterDisplayName, String deploymentTableColumnName, String joiningTableQuery,
            FilterType filterType) {
        this.filterType = filterType;
        this.filterDisplayName = filterDisplayName;
        this.deploymentTableColumnName = deploymentTableColumnName;
        this.isSelected = true;
        this.filterIdentifikationNumber = System.currentTimeMillis();
        this.joiningTableQuery = joiningTableQuery != null ? joiningTableQuery : "";
        if (isIntegerType()) {
            // value muss mit 0 initialisiert werden falls Integer type (sonst
            // fkt rendering nicht!)
            this.value = 0;
        }
        else if (isBooleanType()) {
            // value soll mit true initialisiert werden falls Boolean type
            this.value = true;
            this.dropDownItems.add("true");
            this.dropDownItems.add("false");
        }
        else if (isDateType() || isLabeledDateType()) {
            this.value = new Date();
        }
        else {
            this.value = "";
        }
    }

    public CustomFilter(String filterDisplayName, String deploymentTableColumnName, FilterType filterType) {
        this(filterDisplayName, deploymentTableColumnName, null, filterType);
    }

    public void setAlwaysAutoComplete(boolean alwaysAutoComplete) {
        this.alwaysAutoComplete = alwaysAutoComplete;
    }

    public void setValue(String filterValue) {
        if (StringUtils.isEmpty(filterValue)) {
            this.value = null;
        }
        else {
            if (isBooleanType()) {
                this.value = Boolean.valueOf(filterValue);
            }
            else if (isStringType() || isEnumType()) {
                this.value = filterValue;
            }
            else if (isIntegerType()) {
                try {
                    this.value = Integer.valueOf(filterValue.trim());
                }
                catch (NumberFormatException e) {
                    this.value = null;
                    log.warning(filterValue + " is not a number");
                }
            }
            // Date type is handled differently: The JSF component directly sets its value directly through setDateValue
            else if (isLabeledDateType()) {
                this.value = CustomFilter.convertStringToDate(filterValue);
            }
        }
    }

    public Integer getIntegerValue() {
        if (isIntegerType()) {
            return (Integer) value;
        }
        else {
            return null;
        }
    }

    public boolean isIntegerType() {
        return filterType.equals(FilterType.IntegerType);
    }

    public Boolean getBooleanValue() {
        if (isBooleanType()) {
            return (Boolean) value;
        }
        else {
            return null;
        }
    }

    public boolean isBooleanType() {
        return filterType.equals(FilterType.booleanType);
    }

    public Date getDateValue() {
        if (isDateType() || isLabeledDateType()) {
            return (Date) value;
        }
        else {
            return null;
        }
    }

    public void setDateValue(Date date) {
        if (isDateType()) {
            value = date;
        }
        else {
            value = null;
        }
    }

    public boolean isDateType() {
        return filterType.equals(FilterType.DateType);
    }

    public boolean isLabeledDateType() {
        return filterType.equals(FilterType.LabeledDateType);
    }

    public String getStringValue() {
        if (isStringType()) {
            return (String) value;
        }
        else {
            return null;
        }
    }

    public boolean isStringType() {
        return filterType.equals(FilterType.StringType);
    }

    public boolean isSpecialFilterType() {
        return filterType.equals(FilterType.SpecialFilterType);
    }

    public boolean isAlwaysAutoComplete() {
        return alwaysAutoComplete;
    }

    public String getValue() {
        if (value != null) {
            if (isLabeledDateType()) {
                return convertDateToString(getDateValue());
            }
            return value.toString();
        }
        else {
            return null;
        }
    }

    public boolean isEnumType() {
        return filterType.equals(FilterType.ENUM_TYPE);
    }

    public Enum<?> getEnumValue() {
        return valueOf(enumType, value.toString());
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public String getFilterDisplayName() {
        return filterDisplayName;
    }

    public String getSqlComperator() {
        String result = null;
        if (comperatorSelection != null) {
            if (this.isStringType()) {
                result = comperatorSelection.getSqlStringComperator();
            }
            else if (this.isBooleanType()) {
                result = comperatorSelection.getSqlBoolComperator();
            }
            else {
                result = comperatorSelection.getSqlNumComperator();
            }
        }
        return result;
    }

    public ComperatorFilterOption getComperatorSelection() {
        return comperatorSelection;
    }

    public void setComperatorSelection(ComperatorFilterOption comperatorSelection) {
        this.comperatorSelection = comperatorSelection;
    }

    public String getDeploymentTableColumnName() {
        return deploymentTableColumnName;
    }

    public String getParameterName() {
        return this.filterDisplayName.replaceAll(" ", "");
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    private boolean isNullableFilter = false;

    public boolean hasValidNullValue() {
        return isNullableFilter && value == null;
    }

    public boolean isValidForSqlQuery() {
        return (!filterType.equals(FilterType.SpecialFilterType)
                && (hasValidNullValue() || (value != null && !value.toString().trim().isEmpty()))
                && comperatorSelection != null);
    }

    public List<String> getDropDownItems() {
        return dropDownItems;
    }

    public void setDropDownItems(List<String> dropDownItems) {
        this.dropDownItems = dropDownItems;
    }

    public boolean hasDropDownItems() {
        return !dropDownItems.isEmpty();
    }

    public Map<String, String> getDropDownItemsMap() {
        return dropDownItemsMap;
    }

    public void setDropDownItems(Map<String, String> dropDownItemsMap) {
        this.dropDownItemsMap = dropDownItemsMap;
    }

    public void setEnumType(Class<? extends Enum> enumType) {
        this.enumType = enumType;
    }

    private Class<? extends Enum> enumType;

    public boolean hasDropDownItemsMap() {
        return !dropDownItemsMap.isEmpty();
    }

    public String getJoiningTableQuery() {
        return joiningTableQuery;
    }

    public void setJoiningTableQuery(String joiningTableQuery) {
        this.joiningTableQuery = joiningTableQuery;
    }

    @Override
    public boolean equals(Object obj) {
        CustomFilter filter;

        if (obj instanceof CustomFilter) {
            filter = (CustomFilter) obj;
        }
        else {
            return false;
        }

        if ((this.filterIdentifikationNumber == null && filter.filterIdentifikationNumber != null)
                || (this.filterIdentifikationNumber != null
                        && !this.filterIdentifikationNumber.equals(filter.filterIdentifikationNumber))) {
            return false;
        }

        if ((this.filterType == null && filter.filterType != null)
                || (this.filterType != null && !this.filterType.equals(filter.filterType))) {
            return false;
        }

        if ((this.filterDisplayName == null && filter.filterDisplayName != null)
                || (this.filterDisplayName != null && !this.filterDisplayName.equals(filter.filterDisplayName))) {
            return false;
        }

        if ((this.value == null && filter.value != null) || (this.value != null && !this.value.equals(filter.value))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return this.filterIdentifikationNumber.hashCode();
    }

    public boolean isNullableFilter() {
        return isNullableFilter;
    }

    public void setNullableFilter(boolean isNullableFilter) {
        // only stringtype may have null as value
        if (isStringType()) {
            this.isNullableFilter = isNullableFilter;
        }
    }

    public static String convertDateToString(Date date) {
        return String.valueOf(date.getTime());
    }

    public static Date convertStringToDate(String date) {
        return new Date(Long.valueOf(date));
    }

}
