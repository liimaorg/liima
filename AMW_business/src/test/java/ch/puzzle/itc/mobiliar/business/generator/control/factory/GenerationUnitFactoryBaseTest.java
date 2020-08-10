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

package ch.puzzle.itc.mobiliar.business.generator.control.factory;

import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.function.control.FunctionService;
import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratorUtils;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationContext;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationModus;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationOptions;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationPackage;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationUnitFactory;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.test.CustomLogging;
import ch.puzzle.itc.mobiliar.test.EntityBuilder;
import ch.puzzle.itc.mobiliar.test.EntityBuilderType;

import org.junit.Before;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;

public class GenerationUnitFactoryBaseTest<T extends EntityBuilder> {

	@InjectMocks
	GenerationUnitFactory factory;

	@Spy
	Logger log = Logger.getLogger(GenerationUnitFactoryBaseTest.class
			.getSimpleName());

	@Mock
	GeneratorUtils utils;

	T builder;

	@Mock
	ResourceTypeProvider resourceTypeProvider;

    @Mock
    ResourceDependencyResolverService dependencyResolver;
    
    @Mock
	FunctionService FunctionService;
	
	GenerationOptions options;

	GenerationPackage work;

	@Before
	public void before() {
		CustomLogging.setup(Level.OFF);
		MockitoAnnotations.initMocks(this);

		initialize();
	}

	protected void initialize() {
	    Mockito.when(dependencyResolver
			    .getConsumedMasterRelationsForRelease(Mockito.any(ResourceEntity.class),
					    Mockito.<ReleaseEntity>any())).thenAnswer(new Answer<Set<ConsumedResourceRelationEntity>>() {
		   @Override
		   public Set<ConsumedResourceRelationEntity> answer(InvocationOnMock invocation){
			  ResourceEntity r = ((ResourceEntity)invocation.getArguments()[0]);
			  return r!=null ? r.getConsumedMasterRelations() : null;
		   }
	    });
	    Mockito.when(dependencyResolver
			    .getProvidedMasterRelationsForRelease(Mockito.any(ResourceEntity.class),
					    Mockito.<ReleaseEntity>any())).thenAnswer(new Answer<Set<ProvidedResourceRelationEntity>>() {
		   @Override
		   public Set<ProvidedResourceRelationEntity> answer(InvocationOnMock invocation){
			  ResourceEntity r = ((ResourceEntity)invocation.getArguments()[0]);
			  return r!=null ? r.getProvidedMasterRelations() : null;
		   }
	    });
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		DeploymentEntity d = new DeploymentEntity();
		d.setRuntime(builder.platform);
		d.setApplicationsWithVersion(new ArrayList<DeploymentEntity.ApplicationWithVersion>());

	    GenerationContext context = new GenerationContext(builder.context,
			    builder.as, d, null, GenerationModus.SIMULATE, null);
	    context.setNode(builder.resourceFor(EntityBuilderType.NODE1));
	    	options = new GenerationOptions(context);
		work = factory.createWorkForAppServer(options, builder.resourceFor(EntityBuilderType.AS), templateExceptionHandler);
		assertTrue(templateExceptionHandler.isSuccess());
	}

}
