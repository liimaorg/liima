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

package ch.puzzle.itc.mobiliar.business.database.control;

import ch.puzzle.itc.mobiliar.common.exception.AMWRuntimeException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;


public class JpaSqlResultMapper {

	public static <T> List<T> list(Query q, Class<T> clazz) throws IllegalArgumentException {
		Constructor<?> ctor = null;
		for (Constructor<?> constructor : clazz.getConstructors()) {
			if(Modifier.isPublic(constructor.getModifiers())){
				ctor = constructor;
			}
		}
		List<T> result = new ArrayList<T>();
		List<Object[]> list = q.getResultList();

		for (Object obj : list) {
			if (ctor.getParameterTypes().length == 1) {
				obj = new Object[] { obj };
			}
			createAndAddBean(ctor, (Object[]) obj, result);
		}
		return result;
	}

	private static Object[] normalizeArgs(Object[] args, Class<?>[] expected) {
		Object[] result = new Object[args.length];
		int i = 0;
		for (Object a : args) {
			if (a instanceof Number) {
				if (expected[i] == Integer.class) {
					result[i] = ((Number) a).intValue();
				}
				else if (expected[i] == Short.class) {
					result[i] = ((Number) a).shortValue();
				}
				else if (expected[i] == boolean.class) {
					Number s = (Number) a;
					boolean resultNum = false;
					if (s != null && s.intValue() > 0) {
						resultNum = true;
					}
					result[i] = resultNum;
				}
			}else if(a instanceof Clob){
				try {
					result[i] = QueryUtils.clobToString((Clob)a);
				} catch (SQLException | IOException e) {
					throw new AMWRuntimeException("An Error occured converting a Clob into a String", e);
				}
			}
			else {
					result[i] = a;
			}
			i++;
		}
		return result;

	}

	private static <T> void createAndAddBean(Constructor<?> ctor, Object[] args, List<T> result) {
		try {
			T obj = (T) ctor.newInstance(normalizeArgs(args, ctor.getParameterTypes()));
			result.add(obj);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}