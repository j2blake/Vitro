<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Default object property statement template -->

<a href="${profileUrl(statement.object)}" >
    <span about="${individual.uri}" rel="${property.curie}">
        <#if statement.label?has_content>            
            <span class="link" about="${statement.object}" property="rdfs:label">${statement.label}</span><#t>           
        <#else>
            <span about="${statement.object}">${statement.localname}</span><#t>
        </#if>
    </span><#t>
</a> ${statement.moniker!} 