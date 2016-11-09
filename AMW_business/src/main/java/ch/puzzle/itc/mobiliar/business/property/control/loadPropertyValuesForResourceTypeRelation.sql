   SELECT
   properties.DESCRIPTOR_ID  propDescrId,
   properties.VALUE  propertyValue,
   cast(null as int)  propContId,
   cast(null as VARCHAR(5))  propContName,
   resTypeContext.ID  propTypeContId,
   resTypeContext.NAME propTypeContName,
   -- the resource type relation doesn't carry a name. We therefore concatenate the names of the involved types
   (CASE WHEN resTypeA.NAME IS NOT NULL THEN concat(concat(resTypeA.NAME, ' 2 '), resTypeB.NAME) ELSE NULL END)  resTypeName,
   resTypeB.ID resTypeId,
   resTypeA.ID masterResTypeId,
   cast(null as int) parentId,
   properties.ID  propId,
   -- we mark the results of this sub query to be originated within a relation to differentiate them from the instance property values
   cast('type_rel' as VARCHAR(8)) origin,
   cast(null as VARCHAR(5))  propResName
   FROM TAMW_PROPERTY properties
   LEFT JOIN TAMW_RESRELTCTX_PROP propResRelTypeCont ON propResRelTypeCont.PROPERTIES_ID=properties.ID
   LEFT JOIN TAMW_RESRELTYPECONTEXT resRelTypeContext ON resRelTypeContext.ID=propResRelTypeCont.TAMW_RESRELTYPECONTEXT_ID
   LEFT JOIN TAMW_RESOURCERELATIONTYPE resRelType ON resRelType.ID=resRelTypeContext.RESOURCERELATIONTYPE_ID
   LEFT JOIN TAMW_CONTEXT resTypeContext ON resTypeContext.ID=resRelTypeContext.CONTEXT_ID
   LEFT JOIN TAMW_RESOURCETYPE resTypeA ON resRelType.RESOURCETYPEA_ID=restypeA.ID
   LEFT JOIN TAMW_RESOURCETYPE resTypeB ON resRelType.RESOURCETYPEB_ID=restypeB.ID
   -- we only want properties related to the resource relation context or the resource type relation contexts. 
   WHERE resRelType.ID IN (SELECT ID FROM TAMW_RESOURCERELATIONTYPE RT WHERE RT.RESOURCETYPEA_ID IN(:masterResourceTypeIds) AND RT.RESOURCETYPEB_ID IN (:resourceTypeIds))
   -- Additionally, we're only interested in those results which are defined for the current contexts. 
   -- Please make sure, that you're adding the parent context ids as well: B (5) -> DEV (2) -> GLOBAL (1)   
   AND (resTypeContext.ID IS NULL OR resTypeContext.ID IN (:contextIds))
