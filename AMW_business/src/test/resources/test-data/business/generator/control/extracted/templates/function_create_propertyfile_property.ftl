<#function getThisPropertiesFileString >
    <#assign result= "" >
    <#list amwproperties?keys as key >
        <#assign result = result + amwproperties[key]._descriptor.technicalKey + "=" + amwproperties[key] + "\n" >
    </#list>

    <#return result >
</#function>