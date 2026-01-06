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

package ch.puzzle.itc.mobiliar.business.property.control;

import ch.puzzle.itc.mobiliar.builders.PropertyDescriptorEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceTypeEntityBuilder;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextDependency;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextTypeEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.AppServerRelationsTemplateProcessor;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.common.util.ContextNames;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertyValidationServiceTest {


    private PropertyDescriptorEntity hostNameProperty;
    private PropertyDescriptorEntity otherThanHostNameProperty;
    private ResourceTypeEntity nodeResourceType;
    private ResourceEntity nodeResource;
    private ResourceTypeEntity otherThanNodeResourceType;
    private ResourceEntity otherThanNodeResource;

    private PropertyValidationService propertyValidationService;

    @BeforeEach
    public void setUp() {
        propertyValidationService = new PropertyValidationService();

        hostNameProperty = new PropertyDescriptorEntityBuilder().withPropertyName(AppServerRelationsTemplateProcessor.HOST_NAME).build();
        otherThanHostNameProperty = new PropertyDescriptorEntityBuilder().withPropertyName("anyPropertyName").build();
        nodeResourceType = new ResourceTypeEntityBuilder().buildResourceTypeEntity(DefaultResourceTypeDefinition.NODE.name(), null, false);
        nodeResource = new ResourceEntityBuilder().withType(nodeResourceType).build();

        otherThanNodeResourceType = new ResourceTypeEntityBuilder().buildResourceTypeEntity("other", null, false);
        otherThanNodeResource = new ResourceEntityBuilder().withType(otherThanNodeResourceType).build();

        assertThat(hostNameProperty.getPropertyName()).isEqualTo(AppServerRelationsTemplateProcessor.HOST_NAME);
        assertThat(otherThanHostNameProperty.getPropertyName()).isNotEqualTo(AppServerRelationsTemplateProcessor.HOST_NAME);
    }

    @Test
    public void testIsValidTechnicalKey() throws Exception {
        assertThat(propertyValidationService.isValidTechnicalKey("test")).isTrue();
        assertThat(propertyValidationService.isValidTechnicalKey("test.test")).isTrue();
        assertThat(propertyValidationService.isValidTechnicalKey("test_test")).isTrue();
        assertThat(propertyValidationService.isValidTechnicalKey("test-test")).isTrue();
        assertThat(propertyValidationService.isValidTechnicalKey("test/test")).isTrue();
        assertThat(propertyValidationService.isValidTechnicalKey("<test>")).isTrue();
        assertThat(propertyValidationService.isValidTechnicalKey("${test}")).isTrue();
        assertThat(propertyValidationService.isValidTechnicalKey("<test> </test>")).isTrue();
        assertThat(propertyValidationService.isValidTechnicalKey("/")).isTrue();
        assertThat(propertyValidationService.isValidTechnicalKey(".")).isTrue();
        assertThat(propertyValidationService.isValidTechnicalKey("")).isFalse();
        assertThat(propertyValidationService.isValidTechnicalKey(null)).isFalse();
    }

    @Test
    public void canPropertyValueBeSetOnContextForHostNamePropertyAndNodeResourceOnGlobalContextShouldReturnFalse() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(nodeResource, ContextNames.GLOBAL.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isFalse();
    }

    @Test
    public void canPropertyValueBeSetOnContextForOtherThanHostNamePropertyAndNodeResourceOnGlobalContextShouldReturnFalse() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(nodeResource, ContextNames.GLOBAL.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isFalse();
    }

    @Test
    public void canPropertyValueBeSetOnContextForHostNamePropertyAndOtherThanNodeResourceOnGlobalContextShouldReturnTrue() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(otherThanNodeResource, ContextNames.GLOBAL.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isTrue();
    }

    @Test
    public void canPropertyValueBeSetOnContextForOtherThanHostNamePropertyAndOtherThanNodeResourceOnGlobalContextShouldReturnTrue() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(otherThanNodeResource, ContextNames.GLOBAL.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isTrue();
    }

    @Test
    public void canPropertyValueBeSetOnContextForHostNamePropertyAndNodeResourceTypeOnGlobalContextShouldReturnFalse() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(nodeResourceType, ContextNames.GLOBAL.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isFalse();
    }

    @Test
    public void canPropertyValueBeSetOnContextForHostNamePropertyAndOtherThanNodeResourceTypeOnGlobalContextShouldReturnTrue() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(otherThanNodeResourceType, ContextNames.GLOBAL.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isTrue();
    }


    @Test
    public void canPropertyValueBeSetOnContextForHostNamePropertyAndNodeResourceOnDomainContextShouldReturnFalse() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(nodeResource, ContextNames.DOMAIN.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isFalse();
    }

    @Test
    public void canPropertyValueBeSetOnContextForOtherThanHostNamePropertyAndNodeResourceOnDomainContextShouldReturnFalse() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(nodeResource, ContextNames.DOMAIN.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isFalse();
    }

    @Test
    public void canPropertyValueBeSetOnContextForHostNamePropertyAndOtherThanNodeResourceOnDomainContextShouldReturnTrue() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(otherThanNodeResource, ContextNames.DOMAIN.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isTrue();
    }

    @Test
    public void canPropertyValueBeSetOnContextForOtherThanHostNamePropertyAndOtherThanNodeResourceOnDomainContextShouldReturnTrue() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(otherThanNodeResource, ContextNames.DOMAIN.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isTrue();
    }

    @Test
    public void canPropertyValueBeSetOnContextForHostNamePropertyAndNodeResourceTypeOnDomainContextShouldReturnFalse() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(nodeResourceType, ContextNames.DOMAIN.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isFalse();
    }

    @Test
    public void canPropertyValueBeSetOnContextForHostNamePropertyAndOtherThanNodeResourceTypeOnDomainContextShouldReturnTrue() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(otherThanNodeResourceType, ContextNames.DOMAIN.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isTrue();
    }


    @Test
    public void canPropertyValueBeSetOnContextForHostNamePropertyAndNodeResourceOnEnvContextShouldReturnTrue() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(nodeResource, ContextNames.ENV.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isTrue();
    }

    @Test
    public void canPropertyValueBeSetOnContextForOtherThanHostNamePropertyAndNodeResourceOnEnvContextShouldReturnTrue() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(nodeResource, ContextNames.ENV.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isTrue();
    }

    @Test
    public void canPropertyValueBeSetOnContextForHostNamePropertyAndOtherThanNodeResourceOnEnvContextShouldReturnTrue() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(otherThanNodeResource, ContextNames.ENV.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isTrue();
    }

    @Test
    public void canPropertyValueBeSetOnContextForOtherThanHostNamePropertyAndOtherThanNodeResourceOnEnvContextShouldReturnTrue() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(otherThanNodeResource, ContextNames.ENV.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isTrue();
    }

    @Test
    public void canPropertyValueBeSetOnContextForHostNamePropertyAndNodeResourceTypeOnEnvContextShouldReturnTrue() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(nodeResourceType, ContextNames.ENV.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isTrue();
    }

    @Test
    public void canPropertyValueBeSetOnContextForHostNamePropertyAndOtherThanNodeResourceTypeOnEnvContextShouldReturnTrue() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(otherThanNodeResourceType, ContextNames.ENV.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isTrue();
    }

    @Test
    public void canPropertyValueBeSetOnContextForHostNamePropertyAndOtherContextDependencyResourceOnGlobalContextShouldReturnTrue() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(new ResourceRelationContextEntity(), ContextNames.GLOBAL.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isTrue();
    }

    @Test
    public void canPropertyValueBeSetOnContextForHostNamePropertyAndOtherContextDependencyResourceOnDomainContextShouldReturnTrue() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(new ResourceRelationContextEntity(), ContextNames.DOMAIN.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isTrue();
    }

    @Test
    public void canPropertyValueBeSetOnContextForHostNamePropertyAndOtherContextDependencyResourceOnEnvContextShouldReturnTrue() {
        // given
        ContextDependency<ResourceEntity> nodeResourceDependency = createFor(new ResourceRelationContextEntity(), ContextNames.ENV.name());

        // when
        boolean b = propertyValidationService.canPropertyValueBeSetOnContext(hostNameProperty, nodeResourceDependency);

        // then
        assertThat(b).isTrue();
    }

    private <T> ContextDependency createFor(final T contextualizedObject, String contextTypeName) {
        ContextDependency cd = new ContextDependency<T>() {
            @Override
            public T getContextualizedObject() {
                return contextualizedObject;
            }

            @Override
            public void setContextualizedObject(T contextualizedObject) {
            }
        };
        ContextTypeEntity ct = new ContextTypeEntity();
        ct.setName(contextTypeName);
        ContextEntity contextEntity = new ContextEntity();
        contextEntity.setContextType(ct);

        cd.setContext(contextEntity);
        return cd;
    }
}