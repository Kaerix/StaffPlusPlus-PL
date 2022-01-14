package net.shortninja.staffplus.core.domain.staff.reporting.chatchannels;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.IocListener;
import be.garagepoort.mcioc.configuration.ConfigProperty;
import net.shortninja.staffplus.core.common.utils.BukkitUtils;
import net.shortninja.staffplus.core.domain.chatchannels.ChatChannelService;
import net.shortninja.staffplus.core.domain.synchronization.ServerSyncConfiguration;
import net.shortninja.staffplusplus.chatchannels.ChatChannelType;
import net.shortninja.staffplusplus.reports.AcceptReportEvent;
import net.shortninja.staffplusplus.reports.IReport;
import net.shortninja.staffplusplus.reports.RejectReportEvent;
import net.shortninja.staffplusplus.reports.ResolveReportEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@IocBean
@IocListener
public class ReportChatChannelListener implements Listener {

    @ConfigProperty("%lang%:reports.chatchannel.prefix")
    public String chatChannelPrefix;
    @ConfigProperty("%lang%:reports.chatchannel.openingmessage")
    public String chatChannelOpeningMessage;

    private final ChatChannelService chatChannelService;
    private final ServerSyncConfiguration serverSyncConfiguration;
    private final BukkitUtils bukkitUtils;

    public ReportChatChannelListener(ChatChannelService chatChannelService,
                                     ServerSyncConfiguration serverSyncConfiguration,
                                     BukkitUtils bukkitUtils) {
        this.chatChannelService = chatChannelService;
        this.serverSyncConfiguration = serverSyncConfiguration;
        this.bukkitUtils = bukkitUtils;
    }

    @EventHandler
    public void onReportAccepted(AcceptReportEvent reportEvent) {
        bukkitUtils.runTaskAsync(() -> {
            IReport report = reportEvent.getReport();
            List<UUID> members = Arrays.asList(report.getReporterUuid(), report.getStaffUuid());

            chatChannelService.create(
                String.valueOf(report.getId()),
                chatChannelPrefix,
                chatChannelOpeningMessage,
                members,
                ChatChannelType.REPORT);
        });
    }

    @EventHandler
    public void onReportClosed(ResolveReportEvent reportEvent) {
        bukkitUtils.runTaskAsync(() -> chatChannelService.closeChannel(
            String.valueOf(reportEvent.getReport().getId()),
            chatChannelPrefix,
            ChatChannelType.REPORT,
            serverSyncConfiguration.reportSyncServers));
    }

    @EventHandler
    public void onReportClosed(RejectReportEvent reportEvent) {
        bukkitUtils.runTaskAsync(() -> chatChannelService.closeChannel(
            String.valueOf(reportEvent.getReport().getId()),
            chatChannelPrefix,
            ChatChannelType.REPORT,
            serverSyncConfiguration.reportSyncServers));
    }
}
