<#function getThisPropertiesFileString_with_Tag_tag1 >
    <#assign result= "" >
    <#list amwproperties?keys as key >
        <#if amwproperties[key]._descriptor.hasTag("tag1")>
            <#assign result = result + amwproperties[key]._descriptor.technicalKey + "=" + amwproperties[key] + "\n" >
        </#if>
    </#list>

    <#return result >
</#function>