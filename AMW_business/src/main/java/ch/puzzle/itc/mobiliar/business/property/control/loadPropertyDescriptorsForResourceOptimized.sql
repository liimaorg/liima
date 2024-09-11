-- this is the performance optimized version of loadPropertyDescriptorsForResource.sql for Oracle, not H2 compatible!
WITH descr_with
     AS (SELECT descr.PROPERTYNAME propertyName,
                descr.DISPLAYNAME displayName,
                descr.EXAMPLEVALUE exampleValue,
                descr.DEFAULTVALUE defaultValue,
                descr.PROPERTYCOMMENT propertyComment,
                descr.NULLABLE nullable,
                descr.OPTIONAL optional,
                descr.ENCRYPT encrypt,
                descr.CARDINALITYPROPERTY CARDINALITY,
                descr.TESTING testing,
                descr.VALIDATIONLOGIC validationLogic,
                descr.MACHINEINTERPRETATIONKEY mik,
                descr.ID descId,
                resType.id,
                propType.VALIDATIONREGEX validationRegex,
                :loadedFor loadedFor,
                (CASE
                    WHEN res.ID IS NOT NULL
                    THEN
                       CAST ('instance' AS VARCHAR (8))
                    WHEN resType.ID IS NOT NULL
                    THEN
                       CAST ('type' AS VARCHAR (8))
                    WHEN resRelType.ID IS NOT NULL
                    THEN
                       CAST ('type_rel' AS VARCHAR (8))
                    WHEN resRelation.ID IS NOT NULL
                    THEN
                       CAST ('relation' AS VARCHAR (8))
                 END)
                   propertyDescriptorOrigin,
                descr.FCOWNER fcOwner,
                descr.FCEXTERNALKEY fcExternalKey,
                descr.FCEXTERNALLINK fcExternalLink
           FROM TAMW_PROPERTYDESCRIPTOR descr
                LEFT JOIN TAMW_RESOURCECTX_PROPDESC resCont
                   ON resCont.PROPERTYDESCRIPTORS_ID = descr.ID
                LEFT JOIN TAMW_RESOURCECONTEXT resContext
                   ON resCont.TAMW_RESOURCECONTEXT_ID = resContext.ID
                LEFT JOIN TAMW_RESOURCE res
                   ON resContext.RESOURCE_ID = res.ID
                LEFT JOIN TAMW_RESTYPECTX_PROPDESC resTypeCont
                   ON resTypeCont.PROPERTYDESCRIPTORS_ID = descr.ID
                LEFT JOIN TAMW_RESOURCETYPECONTEXT resTypeContext
                   ON resTypeContext.ID =
                         resTypeCont.TAMW_RESOURCETYPECONTEXT_ID
                LEFT JOIN TAMW_RESOURCETYPE resType
                   ON resType.ID = resTypeContext.RESOURCETYPEENTITY_ID
                LEFT JOIN TAMW_RESRELCTX_PROPDESC resRelCont
                   ON resRelCont.PROPERTYDESCRIPTORS_ID = descr.ID
                LEFT JOIN TAMW_RESRELCONTEXT resRelContext
                   ON resRelContext.ID = resRelCont.TAMW_RESRELCONTEXT_ID
                LEFT JOIN %s resRelation
                   ON resRelation.ID =
                         resRelContext.%s
                LEFT JOIN TAMW_RESRELTCTX_PROPDESC resRelTypeCont
                   ON resRelTypeCont.PROPERTYDESCRIPTORS_ID = descr.ID
                LEFT JOIN TAMW_RESRELTYPECONTEXT resRelTypeContext
                   ON resRelTypeContext.ID =
                         resRelTypeCont.TAMW_RESRELTYPECONTEXT_ID
                LEFT JOIN TAMW_RESOURCERELATIONTYPE resRelType
                   ON resRelType.ID =
                         resRelTypeContext.RESOURCERELATIONTYPE_ID
                LEFT JOIN TAMW_PROPERTYTYPE propType
                   ON descr.PROPERTYTYPEENTITY_ID = propType.ID
          WHERE (   res.ID = :resourceId
                 OR resType.ID IN ( :resourceTypeIds)
                 OR (resRelation.ID = :resourceRelationId)
                 OR (    resRelType.RESOURCETYPEA_ID IN ( :masterResourceTypeIds)
                     AND resRelType.RESOURCETYPEB_ID IN ( :resourceTypeIds)))),
     prop
     AS (
-- this is the place where we want to add the property values. The actual subquery depends on
-- the type (instance only, consumed resource, provided resource) and will therefore be added dynamically
-- this is actually a template: watch out for %s - I'm just the messenger. Have mercy, don't kill me
     %s )
SELECT
--the technicalKey of the property
       descr_with.propertyName,
 --the display name of the property
       descr_with.displayName,
-- the value in the given context
       prop.propertyValue,
-- the example value of the property
       descr_with.exampleValue,
-- the default value of the property
       descr_with.defaultValue,
-- the comment defined for the property description
       descr_with.propertyComment,
-- if the property is nullable
       descr_with.nullable,
-- if the property is optional
       descr_with.optional,
-- if the property is encrypted
       descr_with.encrypt,
-- the cardinality of the property
       descr_with.CARDINALITY,
-- the validation logic a property has to fulfill (if any)
       descr_with.validationLogic,
-- the key which will be used to compute the property's value
       descr_with.mik,
-- the id of the context where the property is defined or null if defined on a resource type
       prop.propContId,
-- the id of the type-context where the property is defined or null if defined on a resource
       prop.propTypeContId,
 -- the id of the property descriptor
       descr_with.descId,
-- the name of the context where the property is defined
       prop.propContName,
       prop.propTypeContName,
       descr_with.id,
-- the id of the resource type on which the property is defined
       prop.resTypeId,
       prop.masterResTypeId,
-- the name of the resource type on which the property is defined or null if the property is defined on the resource
       prop.resTypeName,
-- the validation regex if any
       descr_with.validationRegex,
-- the id of the property value
       prop.propId,
-- constant to define if the property is set on 'instance' or 'relation'
       prop.origin,
-- constant to define if the result is loaded for 'instance' or 'relation'
       :loadedFor loadedFor,
       propResName,
-- constant to define if the property descriptors origin is defined on  resource context -> 'instance' or resource type ->
       descr_with.propertyDescriptorOrigin,
-- foreignable owner
       descr_with.fcOwner,
-- foreignable external key
       descr_with.fcExternalKey,
-- foreignable external link
       descr_with.fcExternalLink
  FROM prop
       FULL OUTER JOIN descr_with
          ON     prop.propDescrId = descr_with.descID
-- we don't want any descriptors without id (although this shouldn't happen - but just to be sure)
             AND descr_with.descID IS NOT NULL
-- IMPORTANT! DON'T CLOSE YOUR SQL STATEMENT WITH SEMICOLON - THIS WILL WORK IN H2 BUT NOT IN ORACLE...
