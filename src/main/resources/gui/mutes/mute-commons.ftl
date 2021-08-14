<#assign GuiUtils=statics['net.shortninja.staffplus.core.common.gui.GuiUtils']>
<#assign DateTimeFormatter=statics['java.time.format.DateTimeFormatter']>
<#assign JavaUtils=statics['net.shortninja.staffplus.core.common.JavaUtils']>

<#macro mutelore mute actions=[]>
    <Lore>
        <LoreLine>
            &bId: &6${mute.id}
        </LoreLine>

        <LoreLine if="${.data_model["server-sync-module.mute-sync"]?c}">
            &bServer: &6${mute.serverName}
        </LoreLine>

        <LoreLine>
            &bMuted player: &6${mute.targetName}
        </LoreLine>

        <LoreLine>
            &bIssuer: &6${mute.issuerName}
        </LoreLine>

        <LoreLine>
            &bIssued on: &6${GuiUtils.parseTimestamp(mute.creationTimestamp, .data_model["timestamp-format"])}
        </LoreLine>

        <LoreLine>
            &bReason: &6${mute.reason}
        </LoreLine>

        <#if mute.endTimestamp??>
            <LoreLine>&bTime Left: &6${mute.humanReadableDuration}</LoreLine>
        </#if>

        <LoreLine></LoreLine>
        <LoreLine>
            <#if mute.endTimestamp??>&6TEMPORARY<#else>&CPERMANENT</#if>
        </LoreLine>
    </Lore>
</#macro>