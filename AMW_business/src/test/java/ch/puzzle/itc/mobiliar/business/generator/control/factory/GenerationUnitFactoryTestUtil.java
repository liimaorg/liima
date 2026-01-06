package ch.puzzle.itc.mobiliar.business.generator.control.factory;

import java.util.ArrayList;
import java.util.Set;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationContext;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.GenerationModus;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationOptions;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationPackage;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.templates.GenerationUnitFactory;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.test.EntityBuilder;
import ch.puzzle.itc.mobiliar.test.EntityBuilderType;

public final class GenerationUnitFactoryTestUtil {

    public static GenerationPackage createWorkForBuilder(GenerationUnitFactory factory,
            ResourceDependencyResolverService dependencyResolver,
            EntityBuilder builder) {

        Mockito.when(dependencyResolver
                .getConsumedMasterRelationsForRelease(Mockito.any(ResourceEntity.class), Mockito.<ReleaseEntity>any()))
                .thenAnswer(new Answer<Set<ConsumedResourceRelationEntity>>() {
                    @Override
                    public Set<ConsumedResourceRelationEntity> answer(InvocationOnMock invocation) {
                        ResourceEntity r = ((ResourceEntity) invocation.getArguments()[0]);
                        return r != null ? r.getConsumedMasterRelations() : null;
                    }
                });

        Mockito.when(dependencyResolver
                .getProvidedMasterRelationsForRelease(Mockito.any(ResourceEntity.class), Mockito.<ReleaseEntity>any()))
                .thenAnswer(new Answer<Set<ProvidedResourceRelationEntity>>() {
                    @Override
                    public Set<ProvidedResourceRelationEntity> answer(InvocationOnMock invocation) {
                        ResourceEntity r = ((ResourceEntity) invocation.getArguments()[0]);
                        return r != null ? r.getProvidedMasterRelations() : null;
                    }
                });

        AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
        DeploymentEntity d = new DeploymentEntity();
        d.setRuntime(builder.platform);
        d.setApplicationsWithVersion(new ArrayList<DeploymentEntity.ApplicationWithVersion>());

        GenerationContext context = new GenerationContext(builder.context,
                builder.as, d, null, GenerationModus.SIMULATE, null);
        context.setNode(builder.resourceFor(EntityBuilderType.NODE1));
        GenerationOptions options = new GenerationOptions(context);
        GenerationPackage work = factory.createWorkForAppServer(options, builder.resourceFor(EntityBuilderType.AS),
                templateExceptionHandler);
        if (!templateExceptionHandler.isSuccess()) {
            throw new IllegalStateException("Template initialization failed in test util");
        }
        return work;
    }

}
