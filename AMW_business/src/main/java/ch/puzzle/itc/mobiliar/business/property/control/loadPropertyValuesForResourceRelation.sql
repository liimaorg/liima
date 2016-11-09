-- this query checks all property values for a specific resource relation and its relation types
-- 1st param: the table name of the resource relation (either "TAMW_CONSUMEDRESREL" or "TAMW_PROVIDEDRESREL")
-- 2nd param: the column name of the foreign key connecting TAMW_RESRELCONTEXT to the resource relation table (either "CONSUMEDRESOURCERELATION_ID" or "PROVIDEDRESOURCERELATION_ID")
-- 3rd param: resource relation id (either of consumedresrel or providedresrel)
-- 4th param: a comma-separated list of the resource type ids of the MASTER resource of this relation
-- 5th param: a comma-separated list of the resource type ids of the SLAVE resource of this relation
-- 6th & 7th param: a comma-separated list of context-ids 
   SELECT
   properties.DESCRIPTOR_ID  propDescrId,
   properties.VALUE  propertyValue,
   resContext.ID  propContId,
   resContext.NAME  propContName,
   resTypeContext.ID  propTypeContId,
   resTypeContext.NAME propTypeContName,
   -- the resource type relation doesn't carry a name. We therefore concatenate the names of the involved types
   (CASE WHEN resTypeA.NAME IS NOT NULL THEN concat(concat(resTypeA.NAME, ' 2 '), resTypeB.NAME) ELSE NULL END)  resTypeName,
   restypeB.ID resTypeId,
   resTypeA.ID masterResTypeId,
   resContext.PARENT_ID  parentId,
   properties.ID  propId,
   -- 'relation' we mark the results of this  sub query to be originated within a relation to differentiate them from the instance property values
   cast('relation' as VARCHAR(8)) origin,
   cast(null as VARCHAR(5))  propResName
   FROM TAMW_PROPERTY properties
   LEFT JOIN TAMW_RESRELCTX_PROP propResRelCont ON propResRelCont.PROPERTIES_ID=properties.ID
   LEFT JOIN TAMW_RESRELCONTEXT resRelContext ON resRelContext.ID=propResRelCont.TAMW_RESRELCONTEXT_ID
   LEFT JOIN %s resRelation ON resRelation.ID=resRelContext.%s
   LEFT JOIN TAMW_CONTEXT resContext ON resContext.ID=resRelContext.CONTEXT_ID
   LEFT JOIN TAMW_RESRELTCTX_PROP propResRelTypeCont ON propResRelTypeCont.PROPERTIES_ID=properties.ID
   LEFT JOIN TAMW_RESRELTYPECONTEXT resRelTypeContext ON resRelTypeContext.ID=propResRelTypeCont.TAMW_RESRELTYPECONTEXT_ID
   LEFT JOIN TAMW_RESOURCERELATIONTYPE resRelType ON resRelType.ID=resRelTypeContext.RESOURCERELATIONTYPE_ID
   LEFT JOIN TAMW_CONTEXT resTypeContext ON resTypeContext.ID=resRelTypeContext.CONTEXT_ID
   LEFT JOIN TAMW_RESOURCETYPE restypeA ON resRelType.RESOURCETYPEA_ID=restypeA.ID
   LEFT JOIN TAMW_RESOURCETYPE restypeB ON resRelType.RESOURCETYPEB_ID=restypeB.ID
   -- we only want properties related to the resource relation context or the resource type relation contexts. 
   WHERE (resRelation.ID=:resourceRelationId OR resRelType.ID IN (SELECT ID FROM TAMW_RESOURCERELATIONTYPE RT WHERE RT.RESOURCETYPEA_ID IN(:masterResourceTypeIds) AND RT.RESOURCETYPEB_ID IN (:resourceTypeIds)))
   -- Additionally, we're only interested in those results which are defined for the current contexts. 
   -- Please make sure, that you're adding the parent context ids as well: B (5) -> DEV (2) -> GLOBAL (1)   
   AND (resContext.ID IS NULL OR resContext.ID IN (:contextIds))
   AND (resTypeContext.ID IS NULL OR resTypeContext.ID IN (:contextIds))
