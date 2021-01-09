package net.shortninja.staffplus.staff.examine;

import net.shortninja.staffplus.IocContainer;
import net.shortninja.staffplus.StaffPlus;
import net.shortninja.staffplus.session.PlayerSession;
import net.shortninja.staffplus.session.SessionManager;
import net.shortninja.staffplus.staff.examine.config.ExamineConfiguration;
import net.shortninja.staffplus.util.PermissionHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public class ExamineInventoryMove implements Listener {
    private final SessionManager sessionManager = IocContainer.getSessionManager();
    private final PermissionHandler permissionHandler = IocContainer.getPermissionHandler();
    private final ExamineConfiguration examineConfiguration = IocContainer.getOptions().examineConfiguration;

    public ExamineInventoryMove() {
        Bukkit.getPluginManager().registerEvents(this, StaffPlus.get());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        PlayerSession playerSession = sessionManager.get(player.getUniqueId());
        if (!playerSession.getCurrentGui().isPresent() || !(playerSession.getCurrentGui().get() instanceof ExamineGui)) {
            return;
        }

        ExamineGui examineGui = (ExamineGui) playerSession.getCurrentGui().get();

        if (event.getClick() != ClickType.LEFT) {
            event.setCancelled(true);
            return;
        }

        if (!isEmptyStack(event.getCursor()) && !isEmptyStack(event.getCurrentItem())) {
            event.setCancelled(true);
            return;
        }

        if (!permissionHandler.has(player, examineConfiguration.getPermissionExamineViewInventory())) {
            return;
        }

        if (event.getClickedInventory() != null && event.getClickedInventory().equals(examineGui.getInventory()) && examineGui.isInventorySlot(event.getSlot())) {
            handleExamineInventoryClick(event, player, examineGui);
        }

        if (event.getClickedInventory() != null && event.getClickedInventory().equals(player.getInventory())) {
            handleStaffInventoryClick(event, player, examineGui);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void dragItem(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        PlayerSession playerSession = sessionManager.get(player.getUniqueId());
        if (!playerSession.getCurrentGui().isPresent() || !(playerSession.getCurrentGui().get() instanceof ExamineGui)) {
            return;
        }
        event.setCancelled(true);
    }

    private void handleExamineInventoryClick(InventoryClickEvent event, Player player, ExamineGui examineGui) {
        if (!permissionHandler.has(player, examineConfiguration.getPermissionExamineInventoryInteraction())) {
            event.setCancelled(true);
            return;
        }

        if (!isEmptyStack(event.getCursor()) && isEmptyStack(event.getCurrentItem())) {
            if ("staff".equalsIgnoreCase(examineGui.getItemSelectedFrom())) {
                int playerSlot;
                if (event.getSlot() < ExamineGui.ARMOR_START) {
                    playerSlot = event.getSlot() - ExamineGui.INVENTORY_START;
                } else {
                    //deduct one to take into account the divider item
                    playerSlot = event.getSlot() - ExamineGui.INVENTORY_START - 1;
                }
                examineGui.getTargetInventory().setItem(playerSlot, event.getCursor());
            }
        }

        examineGui.setItemSelectedFrom(null);

        if (isEmptyStack(event.getCursor()) && !isEmptyStack(event.getCurrentItem())) {
            examineGui.setItemSelectedFrom("player");
            examineGui.setItemSelectedSlot(event.getSlot());
        }
    }

    private void handleStaffInventoryClick(InventoryClickEvent event, Player player, ExamineGui examineGui) {
        if (!permissionHandler.has(player, examineConfiguration.getPermissionExamineInventoryInteraction())) {
            return;
        }

        if (!isEmptyStack(event.getCursor()) && isEmptyStack(event.getCurrentItem())) {
            if ("player".equalsIgnoreCase(examineGui.getItemSelectedFrom())) {
                examineGui.getTargetInventory().setItem(examineGui.getItemSelectedSlot(), null);
            }
        }

        examineGui.setItemSelectedFrom(null);

        if (isEmptyStack(event.getCursor()) && !isEmptyStack(event.getCurrentItem())) {
            examineGui.setItemSelectedFrom("staff");
        }
    }

    private boolean isEmptyStack(ItemStack cursor) {
        return cursor == null || cursor.getType() == Material.AIR;
    }
}