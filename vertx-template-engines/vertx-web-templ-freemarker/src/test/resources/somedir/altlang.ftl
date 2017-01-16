Hello ${context.foo}
<#list context.bar as thing>
There is a ${thing}
</#list>
<#list context.baz?keys as k>
${k}
</#list>
<#list context.baz?values as v>
${v}
</#list>
<#list context.team.marseille as peep>
${peep} loves Olympique de Marseille
</#list>
