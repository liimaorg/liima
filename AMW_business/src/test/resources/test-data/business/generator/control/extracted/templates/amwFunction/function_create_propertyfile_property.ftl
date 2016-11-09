    <#assign result= "" >
    <#list appServer?keys as key >
        <#assign result = result + appServer[key]._descriptor.technicalKey + "=" + appServer[key] + "\n" >
    </#list>

    <#return result >