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

package ch.mobi.itc.mobiliar.rest.resources;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import ch.puzzle.itc.mobiliar.business.server.entity.ServerTuple;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey;

@Provider
@Produces({"text/comma-separated-values", "text/csv"})
public class ServerTupleCSVBodyWriter implements MessageBodyWriter<List<ServerTuple>> {

	private static final String CSV_SEPARATOR = ConfigurationService.getProperty(ConfigKey.CSV_SEPARATOR);

	@Override
	public long getSize(List<ServerTuple> servers, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType) {
		//used for contentLenght header, -1 = calculate it
		return -1;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
	    // Ensure that we're handling only List<ServerTuple> objects.
		boolean isWritable = false;
		if (List.class.isAssignableFrom(type) && genericType instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) genericType;
			Type[] actualTypeArgs = (parameterizedType.getActualTypeArguments());
			isWritable = (actualTypeArgs.length == 1 && actualTypeArgs[0].equals(ServerTuple.class));
		}
		
		return isWritable;
	}

	@Override
	public void writeTo(List<ServerTuple> servers, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
					throws IOException, WebApplicationException {

		PrintWriter pw = new PrintWriter(entityStream);
		
		httpHeaders.add(HttpHeaders.CONTENT_ENCODING, "utf-8");
		httpHeaders.add(HttpHeaders.CACHE_CONTROL, "private, must-revalidate");
		httpHeaders.add("Pragma", "cache");
		httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
		httpHeaders.add("Content-Disposition", "attachment; filename=hostnames_"
				+ new SimpleDateFormat("yyyy-MM-dd_HHmm").format(new Date()) + ".csv");
		
		pw.append("host").append(CSV_SEPARATOR)
			.append("appServer").append(CSV_SEPARATOR)
			.append("appServerRelease").append(CSV_SEPARATOR)
			.append("runtime").append(CSV_SEPARATOR)
			.append("node").append(CSV_SEPARATOR)
			.append("nodeRelease").append(CSV_SEPARATOR)
			.append("environment").append(CSV_SEPARATOR)
			.append("domain").append(CSV_SEPARATOR)
			.append("definedOnNode").append(CSV_SEPARATOR)
			.append('\n');

		for (ServerTuple server : servers) {
			pw.append(server.getHost()).append(CSV_SEPARATOR)
				.append(server.getAppServer()).append(CSV_SEPARATOR)
				.append(server.getAppServerRelease()).append(CSV_SEPARATOR)
				.append(server.getRuntime()).append(CSV_SEPARATOR)
				.append(server.getNode()).append(CSV_SEPARATOR)
				.append(server.getNodeRelease()).append(CSV_SEPARATOR)
				.append(server.getEnvironment()).append(CSV_SEPARATOR)
				.append(server.getDomain()).append(CSV_SEPARATOR)
				.append(Boolean.toString(server.isDefinedOnNode())).append(CSV_SEPARATOR)
				.append('\n');
		}
		pw.flush();
	}
}
