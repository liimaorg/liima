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

package ch.puzzle.itc.mobiliar.maiafederationservice.utils;

import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.UpdateRequest;
import ch.mobi.xml.service.ch.mobi.maia.amw.v1_0.maiaamwfederationservice.datatype.Update;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

public class XMLDataReaderConverter {

    // TODO change UpdateRequestWraperDTO to UpdateRequestDTO as soon the ch.mobi.maia.amw.xml.datatype.tokyo.maiaamwfederationservicetypes.v1_0.xml.MaiaAmwFederationServiceTypesXmlSerializer is within the repository!
    // see ExampleDataReader in AMW_gluecode_export project

	public static final String TEST_DATA_FOLDER = "integration-test";

	private ClassLoader classLoader = getClass().getClassLoader();



	public UpdateRequest getUpdateRequestFromUsecaseFile(String fileName) {
		InputStream fileContent = getFileIoStream(fileName);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Update.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			JAXBElement<Update> userElement = jaxbUnmarshaller.unmarshal(new StreamSource(fileContent), Update.class);
			Update update = userElement.getValue();

			return update.getUpdate();
		}
		catch (JAXBException e) {
			throw new RuntimeException("could not create UpdateRequest");
		}
	}

	private InputStream getFileIoStream(String fileName) {
		try {
			return classLoader.getResourceAsStream(TEST_DATA_FOLDER + "/" + fileName);
		}
		catch (Exception e) {
			throw new RuntimeException("Could not read data from file");
		}
	}
}
