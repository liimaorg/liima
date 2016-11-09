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

import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0.*;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.ApplicationPredecessorRelation;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.UpdateRequest;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTypeEntity;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

import java.util.Arrays;
import java.util.LinkedList;

import static org.mockito.Matchers.argThat;

public class FederationTestHelper {

    public static ApplicationReleaseBinding createAppReleaseBinding(String releaseName, ApplicationPayload applicationPayload){
        ApplicationReleaseBinding appReleaseBinding = new ApplicationReleaseBinding(releaseName,applicationPayload != null ? applicationPayload :  new ApplicationPayload());
        return appReleaseBinding;
    }

    public static Application createApplication(String name, String techStack, String fcKey, String fcLink, ApplicationReleaseBinding...appReleaseBindings){
        Application app = new Application(new ApplicationID(name), techStack, fcKey, fcLink, "", Arrays.asList(appReleaseBindings));
        return app;
    }

    public static UpdateRequest createUpdateRequest(Application...apps){
        UpdateRequest updateRequest = new UpdateRequest(Arrays.asList(apps),new LinkedList<ApplicationPredecessorRelation>(),new LinkedList<ApplicationID>());
        return updateRequest;
    }

    public static ApplicationPayload createApplicationPropertiesPayload(PropertyDeclaration ... properties){
        ApplicationPayload payload = new ApplicationPayload();
        payload.getProperties().addAll(Arrays.asList(properties));
        return payload;
    }

    public static ApplicationPayload createConsumedPortPayload(ConsumedPort ... consumedPorts){
        ApplicationPayload payload = new ApplicationPayload();
        payload.getConsumedPorts().addAll(Arrays.asList(consumedPorts));
        return payload;
    }

    public static ApplicationPayload createProvidedPortPayload(ProvidedPort ... providedPorts){
        ApplicationPayload payload = new ApplicationPayload();
        payload.getProvidedPorts().addAll(Arrays.asList(providedPorts));
        return payload;
    }

    public static ProvidedPort createProvidedPortRelation(String name, String fcKey, String fcLink, String displayName, String localPortID, String providedPortResourceType, PropertyDeclaration ... properties) {
        return new ProvidedPort(new ProvidedPortID(name), fcKey, fcLink, displayName, localPortID, providedPortResourceType, Arrays.asList(properties));
    }

    public static ConsumedPort createConsumedPortRelation( String fcKey, String fcLink, String displayName, String localPortID, String consumedResourceType, String providedPortRefName, PropertyDeclaration ... properties) {
        return new ConsumedPort(fcKey, fcLink, displayName, localPortID, consumedResourceType, new ProvidedPortID(providedPortRefName), Arrays.asList(properties));
    }

    /**
     * Custom matcher for verifying actual PropertyDescriptorEntity against expected PropertyDeclaration and PropertyTypeEntity match.
     */
    private static class PropertyDescriptorEntityMatcher extends ArgumentMatcher<PropertyDescriptorEntity> {

        private PropertyDeclaration expected;
        private PropertyTypeEntity expectedPropertyType;

        public PropertyDescriptorEntityMatcher(PropertyDeclaration expected, PropertyTypeEntity propertyType) {
            this.expected = expected;
            this.expectedPropertyType = propertyType;
        }

        @Override
        public boolean matches(Object actual) {
            if (actual instanceof PropertyDescriptorEntity) {
                PropertyDescriptorEntity propertyDescriptor = (PropertyDescriptorEntity) actual;

                EqualsBuilder eb = new EqualsBuilder();
                eb.append(propertyDescriptor.getDisplayName(), expected.getDisplayName());
                eb.append(propertyDescriptor.getPropertyName(), expected.getTechnicalKey());
                eb.append(propertyDescriptor.getDefaultValue(), expected.getDefaultValue());
                eb.append(propertyDescriptor.getExampleValue(), expected.getExampleValue());
                eb.append(propertyDescriptor.getValidationLogic(), expected.getValidationPattern());
                eb.append(propertyDescriptor.getMachineInterpretationKey(), expected.getMachineInterpretationKey());
                eb.append(propertyDescriptor.isEncrypt(), expected.isEncrypted());
                eb.append(propertyDescriptor.isNullable(), expected.isIsValueOptional());
                eb.append(propertyDescriptor.isOptional(), expected.isIsKeyOptional());
                eb.append(propertyDescriptor.getPropertyTypeEntity(), expectedPropertyType);

                return eb.isEquals();

            } else {
                return false;
            }
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(expected == null ? null : expected.toString());
        }
    }


    /**
     * Convenience factory method for using the custom PropertyDescriptorEntity matcher.
     */
    public static PropertyDescriptorEntity propertyDescriptorEntityEq(PropertyDeclaration expected, PropertyTypeEntity propertyType) {
        return argThat(new PropertyDescriptorEntityMatcher(expected, propertyType));
    }


}
