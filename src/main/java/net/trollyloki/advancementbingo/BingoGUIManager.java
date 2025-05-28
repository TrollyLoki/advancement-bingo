package net.trollyloki.advancementbingo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BingoGUIManager implements Listener {

    private final @NotNull BingoManager bingoManager;
    private final @NotNull Set<@NotNull Inventory> openGUIs;

    private @Nullable Inventory teamSelectGUI;
    private @Nullable Map<@NotNull Integer, @NotNull BingoTeam> teamSelectTeams;
    private @Nullable Map<@NotNull BingoTeam, @NotNull Integer> teamSelectSlots;

    public BingoGUIManager(@NotNull BingoManager bingoManager) {
        this.bingoManager = bingoManager;
        this.openGUIs = new HashSet<>();
    }

    public @NotNull BingoManager getBingoManager() {
        return bingoManager;
    }

    public boolean isGUI(@NotNull Inventory inventory) {
        return openGUIs.contains(inventory);
    }

    public void openGUI(@NotNull HumanEntity who, @NotNull Inventory inventory) {
        openGUIs.add(inventory);
        who.openInventory(inventory);
    }

    public void updateTeamSelectGUI() {
        List<HumanEntity> viewers = teamSelectGUI != null ? teamSelectGUI.getViewers() : Collections.emptyList();

        teamSelectTeams = new HashMap<>();
        teamSelectSlots = new HashMap<>();
        int slot = 10;
        for (BingoTeam team : bingoManager.getAvailableTeams().values()) {
            teamSelectTeams.put(slot, team);
            teamSelectSlots.put(team, slot);
            slot += 2;
        }

        teamSelectGUI = Bukkit.createInventory(null, 27, Component.text("Choose Team"));
        for (Map.Entry<Integer, BingoTeam> entry : teamSelectTeams.entrySet())
            teamSelectGUI.setItem(entry.getKey(), entry.getValue().getItem());

        updateTeamPlayers();

        for (HumanEntity viewer : viewers)
            openTeamSelectGUI(viewer);
    }

    public void updateTeamPlayers() {
        if (teamSelectGUI == null || teamSelectSlots == null)
            return;

        Map<BingoTeam, Set<UUID>> teamPlayers = new HashMap<>();
        for (Map.Entry<UUID, BingoTeam> entry : bingoManager.getPlayers().entrySet()) {
            teamPlayers.computeIfAbsent(entry.getValue(), k -> new HashSet<>()).add(entry.getKey());
        }

        for (BingoTeam team : bingoManager.getAvailableTeams().values()) {
            ItemStack item = teamSelectGUI.getItem(teamSelectSlots.get(team));
            assert item != null;
            List<Component> lore = teamPlayers.getOrDefault(team, Collections.emptySet()).stream()
                    .map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName).sorted()
                    .map(name -> (Component) Component.text("- " + name, Style.style(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)))
                    .toList();
            item.editMeta(meta -> meta.lore(lore));
        }
    }

    public void openTeamSelectGUI(@NotNull HumanEntity who) {
        if (teamSelectGUI == null)
            updateTeamSelectGUI();
        openGUI(who, teamSelectGUI);
    }

    public void resetTeamSelectGUI() {
        teamSelectGUI = null;
        teamSelectTeams = null;
        teamSelectSlots = null;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (event.getViewers().size() <= 1)
            openGUIs.remove(event.getInventory());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (isGUI(event.getInventory()))
            event.setCancelled(true);

        if (teamSelectGUI != null && teamSelectTeams != null && teamSelectGUI.equals(event.getClickedInventory())) {

            BingoTeam team = teamSelectTeams.get(event.getSlot());
            if (team != null) {
                bingoManager.setTeam(event.getWhoClicked().getUniqueId(), team);
                updateTeamPlayers();
            }

        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        if (isGUI(event.getInventory()))
            event.setCancelled(true);
    }

}
