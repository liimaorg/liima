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

package ch.puzzle.itc.mobiliar.business.template.entity;

import ch.puzzle.itc.mobiliar.builders.ReleaseEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.TemplateDescriptorEntityBuilder;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratedTemplate;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainServiceTestHelper;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyUnit;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.utils.CopyHelper;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import org.junit.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Tests {@link TemplateDescriptorEntity}
 */
public class TemplateDescriptorEntityTest {

    private ReleaseEntityBuilder releaseEntityBuilder = new ReleaseEntityBuilder();

    @Test
    public void test_getCopy() throws AMWException {
        Map<CopyResourceDomainService.CopyMode, Set<ForeignableOwner>> validModeOwnerCombinationsMap = CopyHelper.getValidModeOwnerCombinationsMap();
        for (CopyResourceDomainService.CopyMode copyMode : validModeOwnerCombinationsMap.keySet()) {
            for (ForeignableOwner actingOwner : validModeOwnerCombinationsMap.get(copyMode)) {
                shouldCopyTemplateDescriptor_emptyTarget(copyMode, actingOwner);
                shouldCopyTemplateDescriptor(copyMode, actingOwner);
            }
        }
    }

    private void shouldCopyTemplateDescriptor_emptyTarget(CopyResourceDomainService.CopyMode mode, ForeignableOwner actingOwner) throws AMWException {
        // given
        ResourceEntity originResource = CopyResourceDomainServiceTestHelper.mockOriginResource();
        ResourceEntity targetResource = new ResourceEntityBuilder().buildAppServerEntity("targetResource", null, null, true);

        ReleaseEntity pastRelease = releaseEntityBuilder.mockReleaseEntity("Past", new Date());
        ResourceGroupEntity targetPlatform1 = new ResourceEntityBuilder().mockRuntimeEntity("EAP6", null,
                pastRelease).getResourceGroup();
        ResourceGroupEntity targetPlatform2 = new ResourceEntityBuilder().mockRuntimeEntity("Tomcat", null,
                pastRelease).getResourceGroup();
        ResourceGroupEntity targetPlatform3 = new ResourceEntityBuilder().mockRuntimeEntity("JBoss7", null,
                pastRelease).getResourceGroup();

        Set<ResourceGroupEntity> targetPlatforms_target = new HashSet<>();
        targetPlatforms_target.add(targetPlatform1);
        targetPlatforms_target.add(targetPlatform2);

        String fileContent = "foo=bar";
        String targetPath = "foo/bar";
        String name = "bar";
        Set<ResourceGroupEntity> targetPlatforms = new HashSet<>();
        targetPlatforms.add(targetPlatform1);
        targetPlatforms.add(targetPlatform3);
        TemplateDescriptorEntity origin = new TemplateDescriptorEntityBuilder()
                .withFileContent(fileContent)
                .withTargetPath(targetPath)
                .withName(name)
                .withTargetPlatforms(targetPlatforms)
                .build();

        CopyUnit copyUnit = new CopyUnit(originResource, targetResource, mode, actingOwner);

        // when
        TemplateDescriptorEntity copy = origin.getCopy(null, copyUnit);

        // then
        assertNotNull(copy);
        assertEquals(fileContent, copy.getFileContent());
        assertEquals(targetPath, copy.getTargetPath());
        assertEquals(name, copy.getName());
        assertEquals(2, copy.getTargetPlatforms().size());
        assertTrue(copy.getTargetPlatforms().containsAll(targetPlatforms));
    }

    private void shouldCopyTemplateDescriptor(CopyResourceDomainService.CopyMode mode, ForeignableOwner actingOwner) throws AMWException {
        // given
        ResourceEntity originResource = CopyResourceDomainServiceTestHelper.mockOriginResource();
        ResourceEntity targetResource = new ResourceEntityBuilder().buildAppServerEntity("targetResource", null, null, true);

        ReleaseEntity pastRelease = releaseEntityBuilder.mockReleaseEntity("Past", new Date());
        ResourceGroupEntity targetPlatform1 = new ResourceEntityBuilder().mockRuntimeEntity("EAP6", null,
                pastRelease).getResourceGroup();
        ResourceGroupEntity targetPlatform2 = new ResourceEntityBuilder().mockRuntimeEntity("Tomcat", null,
                pastRelease).getResourceGroup();
        ResourceGroupEntity targetPlatform3 = new ResourceEntityBuilder().mockRuntimeEntity("JBoss7", null,
                pastRelease).getResourceGroup();

        Set<ResourceGroupEntity> targetPlatforms_target = new HashSet<>();
        targetPlatforms_target.add(targetPlatform1);
        targetPlatforms_target.add(targetPlatform2);
        TemplateDescriptorEntity target = new TemplateDescriptorEntityBuilder()
                .withFileContent("a=b")
                .withTargetPath("a/b")
                .withName("abc")
                .withTargetPlatforms(targetPlatforms_target)
                .build();

        String fileContent = "foo=bar";
        String targetPath = "foo/bar";
        String name = "bar";
        Set<ResourceGroupEntity> targetPlatforms = new HashSet<>();
        targetPlatforms.add(targetPlatform1);
        targetPlatforms.add(targetPlatform3);
        TemplateDescriptorEntity origin = new TemplateDescriptorEntityBuilder()
                .withFileContent(fileContent)
                .withTargetPath(targetPath)
                .withName(name)
                .withTargetPlatforms(targetPlatforms)
                .build();

        CopyUnit copyUnit = new CopyUnit(originResource, targetResource, mode, actingOwner);

        // when
        TemplateDescriptorEntity copy = origin.getCopy(target, copyUnit);

        // then
        assertNotNull(copy);
        assertEquals(fileContent, copy.getFileContent());
        assertEquals(targetPath, copy.getTargetPath());
        assertEquals(name, copy.getName());
        assertEquals(3, copy.getTargetPlatforms().size());
        assertTrue(copy.getTargetPlatforms().containsAll(targetPlatforms));
        assertTrue(copy.getTargetPlatforms().containsAll(target.getTargetPlatforms()));
    }

    @Test
    public void shouldCreateHashWithCorrectValues() {
        // given
        String fileContent = "foo=bar";
        String targetPath = "foo/bar";
        String name = "bar";
        boolean relationTemplate = true;
        TemplateDescriptorEntity templateDescriptor = new TemplateDescriptorEntityBuilder()
                .withTargetPath(targetPath)
                .withFileContent(fileContent)
                .withName(name)
                .withRelationTemplate(relationTemplate)
                .build();

        // when
        Map<String, String> hash = templateDescriptor.toHash();

        // then
        assertThat(hash.get(GeneratedTemplate.RESERVED_PROPERTY_PATH), is(targetPath));
        assertThat(hash.get(GeneratedTemplate.RESERVED_PROPERTY_CONTENT), is(fileContent));
        assertThat(hash.get(GeneratedTemplate.RESERVED_PROPERTY_NAME), is(String.valueOf(name)));
        assertThat(hash.get(GeneratedTemplate.RESERVED_PROPERTY_IS_RELATION_TEMPLATE), is(String.valueOf(relationTemplate)));
    }

}