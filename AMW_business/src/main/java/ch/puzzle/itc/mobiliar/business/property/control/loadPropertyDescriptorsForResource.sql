SELECT
--the technicalKey of the property
descr.PROPERTYNAME propertyName,
 --the display name of the property
descr.DISPLAYNAME displayName,
-- the value in the given context
prop.propertyValue,
-- the example value of the property
descr.EXAMPLEVALUE exampleValue,
-- the default value of the property
descr.DEFAULTVALUE defaultValue,
-- the comment defined for the property description
descr.PROPERTYCOMMENT propertyComment,
-- if the property is nullable
descr.NULLABLE nullable,
-- if the property is optional
descr.OPTIONAL optional,
-- if the property is encrypted
descr.ENCRYPT encrypt,
-- the cardinality of the property 
descr.CARDINALITYPROPERTY cardinality,
-- if the property is for testing purposes only (shakedown testing)
descr.TESTING testing,
-- the validation logic a property has to fulfill (if any)
descr.VALIDATIONLOGIC validationLogic,
-- the key which will be used to compute the property's value
descr.MACHINEINTERPRETATIONKEY mik,
-- the id of the context where the property is defined or null if defined on a resource type
prop.propContId,
-- the id of the type-context where the property is defined or null if defined on a resource
prop.propTypeContId,
 -- the id of the property descriptor
descr.ID descId,
-- the name of the context where the property is defined
prop.propContName,
prop.propTypeContName,
-- the id of the resource type on which the property is defined
resType.id,
-- the id of the resource type on which the property is defined
prop.resTypeId,
prop.masterResTypeId,
-- the name of the resource type on which the property is defined or null if the property is defined on the resource
prop.resTypeName,
-- the validation regex if any
propType.VALIDATIONREGEX validationRegex,
-- the id of the property value
prop.propId,
-- constant to define if the property is set on 'instance' or 'relation'
prop.origin,
-- constant to define if the result is loaded for 'instance' or 'relation'
:loadedFor loadedFor,
(CASE WHEN propResName IS NOT NULL THEN propResName ELSE res.NAME END) resourceName,
-- constant to define if the property descriptors origin is defined on  resource context -> 'instance' or resource type -> 'type'
(CASE WHEN res.ID IS NOT NULL THEN cast('instance' as VARCHAR(8)) WHEN resType.ID IS NOT NULL THEN cast('type' as VARCHAR(8)) WHEN resRelType.ID IS NOT NULL THEN cast('type_rel' as VARCHAR(8)) WHEN resRelation.ID IS NOT NULL THEN cast('relation' as VARCHAR(8)) END) propertyDescriptorOrigin,
-- foreignable owner
descr.FCOWNER fcOwner,
-- foreignable external key
descr.FCEXTERNALKEY fcExternalKey,
-- foreignable external link
descr.FCEXTERNALLINK fcExternalLink
--we select all property descriptors and join them with their assigned contexts
FROM TAMW_PROPERTYDESCRIPTOR descr

--either on the resource...
LEFT JOIN TAMW_RESOURCECTX_PROPDESC resCont ON resCont.PROPERTYDESCRIPTORS_ID=descr.ID
LEFT JOIN TAMW_RESOURCECONTEXT resContext ON resCont.TAMW_RESOURCECONTEXT_ID=resContext.ID
LEFT JOIN TAMW_RESOURCE res ON resContext.RESOURCE_ID=res.ID

-- on the resource type...
LEFT JOIN TAMW_RESTYPECTX_PROPDESC resTypeCont ON resTypeCont.PROPERTYDESCRIPTORS_ID=descr.ID
LEFT JOIN TAMW_RESOURCETYPECONTEXT resTypeContext ON resTypeContext.ID=resTypeCont.TAMW_RESOURCETYPECONTEXT_ID
LEFT JOIN TAMW_RESOURCETYPE resType ON resType.ID=resTypeContext.RESOURCETYPEENTITY_ID

-- on the resource relation...
LEFT JOIN TAMW_RESRELCTX_PROPDESC resRelCont ON resRelCont.PROPERTYDESCRIPTORS_ID=descr.ID
LEFT JOIN TAMW_RESRELCONTEXT resRelContext ON resRelContext.ID = resRelCont.TAMW_RESRELCONTEXT_ID
LEFT JOIN %s resRelation ON resRelation.ID=resRelContext.%s

-- and on the resource type relation
LEFT JOIN TAMW_RESRELTCTX_PROPDESC resRelTypeCont ON resRelTypeCont.PROPERTYDESCRIPTORS_ID=descr.ID
LEFT JOIN TAMW_RESRELTYPECONTEXT resRelTypeContext ON resRelTypeContext.ID=resRelTypeCont.TAMW_RESRELTYPECONTEXT_ID
LEFT JOIN TAMW_RESOURCERELATIONTYPE resRelType ON resRelType.ID=resRelTypeContext.RESOURCERELATIONTYPE_ID


LEFT JOIN TAMW_PROPERTYTYPE propType ON descr.PROPERTYTYPEENTITY_ID=propType.ID
-- additionally, we join them with their values according to the given parametrization.
LEFT JOIN
( 
   -- this is the place where we want to add the property values. The actual subquery depends on 
   -- the type (instance only, consumed resource, provided resource) and will therefore be added dynamically
   %s
)
 prop ON propDescrId=descr.ID
-- we restrict the result set: We are only interested in property descriptors which either are assigned 
-- to a context related to the given resource or it's resource type. Please notice, that there might be 
-- multiple resource types because a resource type might have a parent type. In this case, both have 
-- to be considered.
WHERE (res.ID=:resourceId OR resType.ID IN (:resourceTypeIds) OR (resRelation.ID=:resourceRelationId) OR
(resRelType.RESOURCETYPEA_ID IN (:masterResourceTypeIds) AND resRelType.RESOURCETYPEB_ID IN (:resourceTypeIds)))

-- we don't want any descriptors without id (although this shouldn't happen - but just to be sure)
AND descr.ID IS NOT NULL
-- IMPORTANT! DON'T CLOSE YOUR SQL STATEMENT WITH SEMICOLON - THIS WILL WORK IN H2 BUT NOT IN ORACLE...