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

package ch.puzzle.itc.mobiliar.business.domain.commons;

import ch.puzzle.itc.mobiliar.business.deploy.boundary.DeploymentService;
import ch.puzzle.itc.mobiliar.business.utils.JpaWildcardConverter;
import ch.puzzle.itc.mobiliar.common.util.CustomFilter;
import ch.puzzle.itc.mobiliar.common.util.CustomFilter.ComperatorFilterOption;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Stateless
public class CommonFilterService {

	public enum SortingDirectionType {
		ASC, DESC
	}

	@Inject
	private EntityManager em;

	@Inject
	private Logger log;
	
	public void setEm(EntityManager em) {
		this.em = em;
	}
	public void setLog(Logger log) {
		this.log = log;
	}


	public static final String MY_AMW = "myAmw";

	protected static final String SPACE_STRING = " ";

	private static final String WHERE = "where";

	public void appendWhereAndMyAmwParameter(List<Integer> myAmw, StringBuilder stringQuery, String entityDependantMyAmwParameterQl) {
		if (myAmw != null && !myAmw.isEmpty()) {
			appendWhereIfNotAlreadyExists(stringQuery);
			stringQuery.append(entityDependantMyAmwParameterQl);
		}
	}

	/**
	 * 
	 * @param stringQuery
	 * @param filter
	 * @param colToSort
	 * @param sortingDirection
	 * @param uniqueCol Oracle needs to sort by a unique column for pagination to work correctly
	 * @param lowerSortColumn
	 * @param hasSpecialFilter must be set to true if the filters list contains a FilterType.SpecialFilterType
	 * @return
	 */
	public Query addFilterAndCreateQuery(StringBuilder stringQuery, List<CustomFilter> filter, String colToSort, SortingDirectionType sortingDirection, String uniqueCol, boolean lowerSortColumn, boolean hasSpecialFilter) {
		Map<String, CustomFilter> parameterMap = new HashMap<>();
		String uniqueColSortString = SPACE_STRING + uniqueCol + SPACE_STRING + "desc" + SPACE_STRING;
		if (filter != null && !filter.isEmpty()) {
			int[] mutableIndex = {0};

			StringBuilder afterWhere = fixJoinQuery(stringQuery, extractJoiningtableForFilter(filter), hasSpecialFilter);
			StringBuilder filterQuery = new StringBuilder(buildFilterQuery(filter, parameterMap, mutableIndex));

			if (afterWhere.length() > 0) {
				if (filterQuery.length() > 0) {
					filterQuery.append(" and (").append(afterWhere).append("))");
				} else {
					filterQuery.append(afterWhere);
				}
			}

			if (filterQuery.length() > 0) {
				setzeWhereUndAndStatementsZuQuery(stringQuery);
				stringQuery.append('(').append(filterQuery).append(')');
			}
		}
		
		if (lowerSortColumn) {
			colToSort = "LOWER("+colToSort+")";
		}

		if (colToSort != null && sortingDirection != null) {
			stringQuery.append(" order by ").append(colToSort).append(" ").append(sortingDirection.name()).append(",").append(uniqueColSortString);
		}
		else if (uniqueCol != null){
			stringQuery.append(" order by ").append(uniqueColSortString);
		}
		
		log.fine("Query: " + stringQuery);
		Query q = em.createQuery(stringQuery.toString());

		for (String parameter : parameterMap.keySet()) {
			log.fine("Parameter: ");
			CustomFilter f = parameterMap.get(parameter);


			if (f.isBooleanType()) {
				q.setParameter(parameter, f.getBooleanValue());
				log.fine("name: " + parameter + " value: " + f.getBooleanValue());
			} else if (f.isDateType()||f.isLabeledDateType()) {
				q.setParameter(parameter, f.getDateValue());
				log.fine("name: " + parameter + " value: " + f.getDateValue());
			} else if (f.isIntegerType()) {
				q.setParameter(parameter, f.getIntegerValue());
				log.fine("name: " + parameter + " value: " + f.getIntegerValue());
			} else if (f.isStringType()) {
				if(DeploymentService.DeploymentFilterTypes.APPLICATION_NAME.getFilterDisplayName().equals(f.getFilterDisplayName())) {
					//all app names are in a json structure so we have to search with %
					q.setParameter(parameter, "%"+f.getStringValue()+"%");
				}
				else {
					q.setParameter(parameter, JpaWildcardConverter.convertWildCards(f.getStringValue()));
				}
				log.fine("name: " + parameter + " value: " + f.getStringValue());
			}else if(f.isEnumType()){
                q.setParameter(parameter, f.getEnumValue());
                log.fine("name: "+parameter +" value:"+ f.getValue());
            }

		}

		return q;
	}

	private StringBuilder fixJoinQuery(StringBuilder stringQuery, String join, boolean hasSpecialFilter) {
		StringBuilder afterWhere = new StringBuilder();
		if (!join.isEmpty()) {
            if(hasSpecialFilter) {
                String[] parts = stringQuery.toString().split(WHERE);
                stringQuery.setLength(0);
                stringQuery.append(parts[0]).append(join).append(WHERE).append(SPACE_STRING);
                int i = 0;
                while (++i < parts.length) {
                    if (afterWhere.length() > 0) {
                        if (parts[i-1].contains("from")) {
                            afterWhere.append(WHERE);
                        } else {
                            afterWhere.append("and");
                        }
                    }
                    afterWhere.append(parts[i]);
                }
            } else {
                stringQuery.append(join);
            }
        }
		return afterWhere;
	}

	private String extractJoiningtableForFilter(List<CustomFilter> filters) {
		String uniqueJoiningString = "";
		for(CustomFilter filter : filters){
			String joiningTableQuery = filter.getJoiningTableQuery();
			if (!joiningTableQuery.isEmpty() && !uniqueJoiningString.contains(joiningTableQuery)){
				uniqueJoiningString += joiningTableQuery + SPACE_STRING;
			}
		}

		return uniqueJoiningString.isEmpty() ? "" : (SPACE_STRING + uniqueJoiningString);
	}

	private void setzeWhereUndAndStatementsZuQuery(StringBuilder stringQuery) {
		appendWhereIfNotAlreadyExists(stringQuery);
		// Wenn die query bereits mit einer Klammer endet, bedeutet dies,
		// dass ein myAmw-Filter gesetzt ist und die zusätzlichen Filter
		// über ein "and" hinzugefügt werden müssen.
		if (stringQuery.toString().endsWith(") ")) {
			stringQuery.append("and ");
		}
	}
	
	private void appendWhereIfNotAlreadyExists(StringBuilder stringQuery) {
		if (!stringQuery.toString().contains(WHERE)) {
			stringQuery.append(WHERE + SPACE_STRING);
		}
	}

	// Find all filters with the same name and group them with AND or OR
	private String buildFilterQuery(List<CustomFilter> deploymentFilterList, Map<String, CustomFilter> parameterMap, int[] index) {
		HashMap<String, StringBuilder> groupedQueries = new HashMap<>();
		StringBuilder query = new StringBuilder();
		
		for (CustomFilter deploymentFilter : deploymentFilterList) {
			if (deploymentFilter.isValidForSqlQuery()) {			
				if (!groupedQueries.containsKey(deploymentFilter.getFilterDisplayName())) {
					StringBuilder builder = new StringBuilder();
					groupedQueries.put(deploymentFilter.getFilterDisplayName(), builder);
				}
				
				appendFilterToQuery(groupedQueries.get(deploymentFilter.getFilterDisplayName()), deploymentFilter, parameterMap, index);
			}
		}
		
		for (StringBuilder partialQuery : groupedQueries.values()) {
			if (query.length() > 0){
				query.append(" and ");
			}
			query.append('(').append(partialQuery).append(')');
		}
		
		return query.toString();
	}
	
	private void appendFilterToQuery (StringBuilder query, CustomFilter deploymentFilter, Map<String, CustomFilter> parameterMap, int[] index) {
		
		if (query.length() > 0) {
			if (deploymentFilter.isDateType()){
				query.append(" and ");
			}
			else {
				query.append(" or ");
			}
		}
		
		if (deploymentFilter.isBooleanType()) {
			createQueryForBooleanTypeWithoutAddingParameterToParameterMap(query, deploymentFilter);
		} 
		else {
			if (deploymentFilter.hasValidNullValue()) {
				query.append(deploymentFilter.getDeploymentTableColumnName()).append(" is null");
				log.info(deploymentFilter.getFilterDisplayName() + " append value null");
			} 
			else {
				String paramName = deploymentFilter.getParameterName() + index[0];
				query.append(deploymentFilter.getDeploymentTableColumnName()).append(deploymentFilter.getSqlComperator()).append(":").append(paramName).append(SPACE_STRING);
				if(deploymentFilter.getSqlComperator().toLowerCase().contains("like") ) {
					query.append("ESCAPE \'").append(JpaWildcardConverter.ESCAPE_CHARACTER).append("' ");
				}
				parameterMap.put(paramName, deploymentFilter);
			}
		}
		index[0]++;
	}

	protected void createQueryForBooleanTypeWithoutAddingParameterToParameterMap(StringBuilder query, CustomFilter deploymentFilter) {
		if ((deploymentFilter.getComperatorSelection().equals(ComperatorFilterOption.notequal) && !deploymentFilter.getBooleanValue())
				|| (deploymentFilter.getComperatorSelection().equals(ComperatorFilterOption.equals) && deploymentFilter.getBooleanValue())) {
			query.append(deploymentFilter.getDeploymentTableColumnName()).append(" is true ");
		} else {
			query.append("(").append(deploymentFilter.getDeploymentTableColumnName()).append(" is null OR ");
			query.append(deploymentFilter.getDeploymentTableColumnName()).append(" is false) ");
		}
	}

	public Query setParameterToQuery(Integer startIndex, Integer maxResults, List<Integer> myAmw, Query query) {
		if (myAmw != null && !myAmw.isEmpty()) {
			query.setParameter(MY_AMW, myAmw);
		}
		if (startIndex != null) {
			query.setFirstResult(startIndex);
		}
		if (maxResults != null) {
			query.setMaxResults(maxResults);
		}
		return query;
	}
}
