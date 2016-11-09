//package ch.puzzle.itc.mobiliar.maiafederationservice.usecasetests;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//
//import java.util.*;
//
//import javax.inject.Inject;
//import javax.persistence.TypedQuery;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import ch.mobi.common.common.dto.pepper.commons.v3_0.MessageSeverity;
//import ch.mobi.common.common.exception.pepper.commons.v3_0.BusinessException;
//import ch.mobi.common.common.exception.pepper.commons.v3_0.TechnicalException;
//import ch.mobi.common.common.exception.pepper.commons.v3_0.ValidationException;
//import ch.mobi.maia.amw.common.dto.pepper.maiaamwfederationservicetypes.v1_0.UpdateResponse;
//import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
//import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
//import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
//import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
//import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
//import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
//import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
//import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
//
//public class MaiaAmwFederationServiceUseCaseTEMPIntegrationTest extends BaseUseCaseIntegrationTest {
//
////    private static final String MAIN_RELEASE_14_10 = "RL-14.10";
////    private static final String MAIN_RELEASE_PAST = "Past";
////
////    private static final String MINOR_RELEASE_16_05 = "ZW-16.05";
////    private static final String MINOR_RELEASE_16_06 = "ZW-16.06";
////
////    private static final String PUZZLE_SHOP_V_1 = "ch_mobi_testing_PuzzleShop_v1_default";
////    private static final String PUZZLE_BANK_V_1 = "ch_mobi_testing_PuzzleBank_v1_default";
//
//    private static final String RELATED_RESOURCE_NAME = "adExtern";
//
//    // usecase files
//    private static final String REQUEST_1 = "usecase4/request1.xml";
//    private static final String REQUEST_2 = "usecase4/request2.xml";
//
//    @Inject
//    private ContextDomainService contextDomainService;
//
//    @Inject
//    private ResourceRelationService resourceRelationService;
//
//
//
//    // Use
//    // 1      ./generateModel.sh model_small uc_dev_iterations/model_02_add_puzzleshop/
//    // 2      ./generateModel.sh uc_dev_iterations/model_02_add_puzzleshop/ uc_dev_iterations/model_03_puzzleshop_add_customer_cpi/
//    // to generate the Requests
//
//
////    Use Case:
////    1. small -->02: [Grundzustand]
////    2. AMW: Information auf Puzzleshop inkl. CPI aufdekorieren. Sowohl Werte für MAIA-Props als auch eigene Propertydeskriptoren (Relations, Templates und Funktionen hinzufügen) Owner der neuen Element muss AMW sein
////    3. AMW: 2 Zwischenrelease für Puzzleshop inkl CPI erstellenAlle 3 Releases sind identisch, Owner der Elemente bleibt erhalten
////    4. 02 -->03: puzzlebank konsumiert neu auch den customerserviceErstellt 3 neue CPI (pro Release 1), alle Releases erhalten einen neuen Relation, AMW Änderungen bleiben erhalten, Softlink ist auflösbar
//
//
//    @Before
//    public void setUp(){
//        super.setUp();
//
//        createAndAddMainRelease16_04();
//        createAndAddReleaseIfNotYetExist(MINOR_RELEASE_16_05, new Date(2016, Calendar.MAY, 1), false);
//        createAndAddReleaseIfNotYetExist(MINOR_RELEASE_16_06, new Date(2016, Calendar.JUNE, 1), false);
//
//        addResourceTypes();
//    }
//
//    @After
//    public void tearDown(){
//        super.tearDown();
//    }
//
//    @Test
//    public void useCase1shouldNotFail() throws Exception {
//        executeRequest1();
////        decoratePuzzleShopWithAmwRelation();
//        // TODO set values to maia properties
//        // TODO add AMW properties
//        // TODO add AMW templates
//        // TODO add AMW functions
////        createMinorRelease1ForPuzzleShop();
//
//        decorateAMWTemplateOnResourceForRelease(PUZZLE_SHOP_V_1, addedReleaseEntitiesCache.get(MAIN_RELEASE_16_04));
//        decorateAMWFunctionOnResourceForRelease(PUZZLE_SHOP_V_1, addedReleaseEntitiesCache.get(MAIN_RELEASE_16_04));
//
//        decorateConsumedRelation(PUZZLE_SHOP_V_1, addedReleaseEntitiesCache.get(MAIN_RELEASE_16_04), RELATED_RESOURCE_NAME, findReleaseByName(MAIN_RELEASE_PAST));
//
//        System.out.println();
//    }
//
//
//
//
//
//    /**
//     * small -->02: [Grundzustand]
//     * Create PuzzleShop (CPI) and PuzzleBank (PPI)
//     */
//    private void executeRequest1() throws BusinessException, TechnicalException, ValidationException {
//        // when
//        UpdateResponse updateResponse = doUpdate(REQUEST_1);
//
//        // then
//        assertNotNull(updateResponse);
//        assertEquals(2, updateResponse.getProcessedApplications().size());
//        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(0).getMessages().get(0).getSeverity());
//        assertEquals(MessageSeverity.INFO, updateResponse.getProcessedApplications().get(1).getMessages().get(0).getSeverity());
//
//        // verify puzzlebank created
//        List<ResourceEntity> apps = getResourceByName("ch_mobi_testing_PuzzleBank_v1_default");
//        assertNotNull(apps);
//        assertEquals(1, apps.size());
//
//        // verify puzzlebank created
//        apps = getResourceByName("ch_mobi_testing_PuzzleShop_v1_default");
//        assertNotNull(apps);
//        assertEquals(1, apps.size());
//    }
//
//
//    private TemplateDescriptorEntity createTemplate(String name, String path, String content) {
//        TemplateDescriptorEntity template = new TemplateDescriptorEntity();
//        template.setFileContent(content);
//        template.setName(name);
//        template.setTargetPath(path);
//        template.setTesting(false);
//
//        TypedQuery<ResourceGroupEntity> createQuery = entityManager.createNamedQuery(ResourceGroupEntity.ALLRESOURCESBYTYPE_QUERY, ResourceGroupEntity.class).setParameter("restype", ResourceTypeEntity.RUNTIME);
//        Set<ResourceGroupEntity> platforms = new HashSet<>(createQuery.getResultList());
//        template.setTargetPlatforms(platforms);
//        return template;
//    }
//
//    private void addTemplate(ResourceEntity resource, TemplateDescriptorEntity template) {
//        ContextEntity context = contextDomainService.getGlobalResourceContextEntity();
//        ResourceContextEntity resourceContextEntity = resource.getOrCreateContext(context);
//        resourceContextEntity.addTemplate(template);
//
//        entityManager.persist(resourceContextEntity);
//        entityManager.persist(resource);
//    }
//
//
//
////    public void addTemplate(ResourceTypeEntity resourceType, TemplateDescriptorEntity template) {
////        ContextEntity context = contextDomainService.getGlobalResourceContextEntity();
////        ResourceTypeContextEntity resourceTypeContext = resourceType.getOrCreateContext(context);
////        resourceTypeContext.addTemplate(template);
////
////        entityManager.persist(resourceTypeContext);
////        entityManager.persist(resourceType);
////    }
//
//
//}
