<html>
<body style="margin:0; padding:0;">
	<div style="margin:0; padding:0;">Item Name: ${ItemName} <span style="color:${ItemTierColor}">[Tier ${ItemTier}]</span></div>
    
    <ul style="margin:0; padding-left: 1em;">
        <li style="margin:0; padding:0;">Cost: ${ItemCost}</li>
        <li style="margin:0; padding:0;">Description:
            <ul style="margin:0; padding-left: 1em;">
            <#list ItemDesc as ItemDescLine>
                <li style="margin:0; padding:0;">${ItemDescLine}</li>
            </#list>
            </ul>
        </li>
        <li style="margin:0; padding:0;">Range: ${ItemRange}</li>
        <li style="margin:0; padding:0;">Type: <span style="color:${ItemTypeColor!"#f9cb9c"}">${ItemType}</span></li>
        <li style="margin:0; padding:0;">Theme:
        <#list 0..(ItemTheme?size - 1) as i>
        <span style="color:${ItemThemeColor[i]}">${ItemTheme[i]}</span><#if i + 1 < ItemTheme?size> / </#if>
        </#list>
        </li>
        <li style="margin:0; padding:0;">Version: ${ItemVersion}</li>
    </ul>
</body>
</html>
