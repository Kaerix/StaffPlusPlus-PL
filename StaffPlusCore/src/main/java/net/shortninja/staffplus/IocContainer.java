package net.shortninja.staffplus;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.shortninja.staffplus.actions.database.DelayedActionsRepository;
import net.shortninja.staffplus.actions.database.MysqlDelayedActionsRepository;
import net.shortninja.staffplus.actions.database.SqliteDelayedActionsRepository;
import net.shortninja.staffplus.player.UserManager;
import net.shortninja.staffplus.reporting.ReportService;
import net.shortninja.staffplus.reporting.database.MysqlReportRepository;
import net.shortninja.staffplus.reporting.database.ReportRepository;
import net.shortninja.staffplus.reporting.database.SqliteReportRepository;
import net.shortninja.staffplus.server.data.config.Messages;
import net.shortninja.staffplus.server.data.config.Options;
import net.shortninja.staffplus.server.data.storage.IStorage;
import net.shortninja.staffplus.server.data.storage.MemoryStorage;
import net.shortninja.staffplus.server.data.storage.MySQLStorage;
import net.shortninja.staffplus.server.data.storage.SqliteStorage;
import net.shortninja.staffplus.staffchat.StaffChatService;
import net.shortninja.staffplus.util.MessageCoordinator;
import net.shortninja.staffplus.util.PermissionHandler;
import net.shortninja.staffplus.util.database.DatabaseType;
import net.shortninja.staffplus.util.database.DatabaseUtil;
import net.shortninja.staffplus.warn.WarnService;
import net.shortninja.staffplus.warn.database.MysqlWarnRepository;
import net.shortninja.staffplus.warn.database.SqliteWarnRepository;
import net.shortninja.staffplus.warn.database.WarnRepository;

public class IocContainer {

    private static ReportService reportService;
    private static ReportRepository reportRepository;
    private static WarnRepository warnRepository;
    private static DelayedActionsRepository delayedActionsRepository;
    private static UserManager userManager;
    private static StaffPlus staffPlus;
    private static Messages messages;
    private static PermissionHandler permissionHandler;
    private static Options options;
    private static IStorage storage;
    private static WarnService warnService;
    private static MessageCoordinator message;
    private static StaffChatService staffChatService;

    public static void init(StaffPlus staffPlus) {
        IocContainer.staffPlus = staffPlus;
    }

    public static ReportRepository getReportRepository() {
        if (reportRepository == null) {
            reportRepository = RepositoryFactory.get("REPORT");

//            if (DatabaseUtil.database().getType() == DatabaseType.MYSQL) {
//                reportRepository = new MysqlReportRepository(getUserManager());
//            } else if (DatabaseUtil.database().getType() == DatabaseType.SQLITE) {
//                reportRepository = new SqliteReportRepository(getUserManager());
//            } else {
//                throw new RuntimeException("Unsupported database type");
//            }
        }
        return reportRepository;
    }

    public static WarnRepository getWarnRepository() {
        if (warnRepository == null) {
            warnRepository = RepositoryFactory.get("WARN");

//            if (DatabaseUtil.database().getType() == DatabaseType.MYSQL) {
//                warnRepository = new MysqlWarnRepository(getUserManager());
//            } else if (DatabaseUtil.database().getType() == DatabaseType.SQLITE) {
//                warnRepository = new SqliteWarnRepository(getUserManager());
//            } else {
//                throw new RuntimeException("Unsupported database type");
//            }
        }
        return warnRepository;
    }

    public static DelayedActionsRepository getDelayedActionsRepository() {
        if (delayedActionsRepository == null) {
            delayedActionsRepository = RepositoryFactory.get("DELAYED_ACTIONS");

//            if (DatabaseUtil.database().getType() == DatabaseType.MYSQL) {
//                delayedActionsRepository = new MysqlDelayedActionsRepository();
//            } else if (DatabaseUtil.database().getType() == DatabaseType.SQLITE) {
//                delayedActionsRepository = new SqliteDelayedActionsRepository();
//            } else {
//                throw new RuntimeException("Unsupported database type");
//            }
        }
        return delayedActionsRepository;
    }

    public static ReportService getReportService() {
        if (reportService == null) {
            reportService = new ReportService(getReportRepository(), getUserManager(), getMessages());
        }
        return reportService;
    }

    public static WarnService getWarnService() {
        if (warnService == null) {
            warnService = new WarnService(
                getPermissionHandler(),
                getMessage(),
                getOptions(),
                getMessages(),
                getUserManager(),
                getWarnRepository(),
                getDelayedActionsRepository());
        }
        return warnService;
    }

    // Could use a factory but for sake of simplicity of the factory demonstration, not going to do.
    public static IStorage getStorage() {
        if (storage == null) {
            if (DatabaseUtil.database().getType() == DatabaseType.MYSQL) {
                storage = new MySQLStorage();
            } else if (DatabaseUtil.database().getType() == DatabaseType.SQLITE) {
                storage = new SqliteStorage();
            } else {
                storage = new MemoryStorage();
            }
        }
        return storage;
    }

    public static UserManager getUserManager() {
        if (userManager == null) {
            userManager = new UserManager(staffPlus, getOptions());
        }
        return userManager;
    }

    public static Messages getMessages() {
        if (messages == null) {
            messages = new Messages();
        }
        return messages;
    }

    public static PermissionHandler getPermissionHandler() {
        if (permissionHandler == null) {
            permissionHandler = new PermissionHandler(getOptions());
        }
        return permissionHandler;
    }

    public static MessageCoordinator getMessage() {
        if (message == null) {
            message = new MessageCoordinator(staffPlus, getPermissionHandler());
        }
        return message;
    }

    public static Options getOptions() {
        if (options == null) {
            options = new Options();
        }
        return options;
    }

    public static StaffChatService getStaffChatService() {
        if (staffChatService == null) {
            staffChatService = new StaffChatService(getMessages(), getOptions());
        }
        return staffChatService;
    }

    public static interface Repository {
    }

    private static final class RepositoryFactory {

        private static final Table<String, DatabaseType, Repository> MAP = HashBasedTable.create();

        static {
            MAP.put("WARN", DatabaseType.MYSQL, new MysqlWarnRepository(userManager));
            MAP.put("WARN", DatabaseType.SQLITE, new SqliteWarnRepository(userManager));
            MAP.put("REPORT", DatabaseType.MYSQL, new MysqlReportRepository(userManager));
            MAP.put("REPORT", DatabaseType.SQLITE, new SqliteReportRepository(userManager));
            MAP.put("DELAYED_ACTIONS", DatabaseType.MYSQL, new MysqlDelayedActionsRepository());
            MAP.put("DELAYED_ACTIONS", DatabaseType.SQLITE, new SqliteDelayedActionsRepository());
        }

        // Could retrieve information from caller method but for sake of demonstration and ease, let's try this way.
        @SuppressWarnings("unchecked")
        public static <T extends Repository> T get(String type) {
            if (type == null) {
                throw new IllegalArgumentException("Type may not be null.");
            }

            final DatabaseType dbType = DatabaseUtil.database().getType();

            if (!MAP.contains(type, dbType)) {
                throw new IllegalStateException("No repository registered for type.");
            }

            return (T) MAP.get(type, dbType);
        }
    }
}
