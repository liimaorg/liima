-- this query checks all property values for a specific resource instance and its types (incl. parent types).
   SELECT
   properties.DESCRIPTOR_ID propDescrId,
   properties.VALUE propertyValue,
   propCont.ID propContId,
   propCont.NAME propContName,
   propTypeCont.ID propTypeContId,
   propTypeCont.NAME propTypeContName,
   propTypeRes.NAME resTypeName,
   propTypeRes.ID resTypeId,
   cast(null as int) masterResTypeId,
   propCont.PARENT_ID parentId,
   properties.ID propId,
   -- we mark the result to be originated by the resource instance, since property values might also 
   -- be defined on relations and therefore have to be differentiated
   cast('instance' as VARCHAR(8)) origin,
   propRes.NAME propResName
   FROM TAMW_PROPERTY properties
   LEFT JOIN TAMW_RESOURCECTX_PROP propResCont ON propResCont.PROPERTIES_ID=properties.ID
   LEFT JOIN TAMW_RESOURCECONTEXT propResContext ON propResCont.TAMW_RESOURCECONTEXT_ID=propResContext.ID
   LEFT JOIN TAMW_RESOURCE propRes ON propRes.ID=propResContext.RESOURCE_ID
   LEFT JOIN TAMW_CONTEXT propCont ON propResContext.CONTEXT_ID=propCont.ID
   LEFT JOIN TAMW_RESTYPECTX_PROP propTypeResCont ON propTypeResCont.PROPERTIES_ID=properties.ID
   LEFT JOIN TAMW_RESOURCETYPECONTEXT propTypeResContext ON propTypeResCont.TAMW_RESOURCETYPECONTEXT_ID=propTypeResContext.ID
   LEFT JOIN TAMW_RESOURCETYPE propTypeRes ON propTypeRes.ID=propTypeResContext.RESOURCETYPEENTITY_ID
   LEFT JOIN TAMW_CONTEXT propTypeCont ON propTypeResContext.CONTEXT_ID=propTypeCont.ID
   WHERE (propRes.ID=:resourceId OR propTypeRes.ID IN (:resourceTypeIds) )
   -- Additionally, we're only interested in those results which are defined for the current contexts. 
   -- Please make sure, that you're adding the parent context ids as well: B (5) -> DEV (2) -> GLOBAL (1)   
   AND (propCont.ID IS NULL OR propCont.ID IN (:contextIds))
   AND (propTypeCont.ID IS NULL OR propTypeCont.ID IN (:contextIds))