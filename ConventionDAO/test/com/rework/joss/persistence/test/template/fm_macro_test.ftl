<#macro isNotNull property prepend>
  <#if property??>
    ${prepend!}<#nested>
  </#if>
</#macro>

<#macro isNotEmpty property prepend>
  <#assign x=${property?eval}>
  <#if x?? && x != ''>
    ${prepend!}<#nested/>
  </#if>
</#macro>

<@isNotEmpty property="test" prepend="and">
  œ‘ æ: ${test}
</@isNotEmpty>

<@isNotEmpty property="test0" prepend="and">
  ≤ªœ‘ æ: ${test}
</@isNotEmpty>

<isNotEmpty property="test0">
<#if test0?? && test0 != ''>

<isNotNull property="test0" prentend="and">
<#if test0??> and 

like #xgGoodsKeeper.id#
like #${xgGoodsKeeper.id}#