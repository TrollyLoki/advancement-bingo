package net.trollyloki.advancementbingo;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AdvancementBingoPlugin extends JavaPlugin {

    private BingoManager manager;
    private BingoGUIManager guiManager;

    private @NotNull Optional<ItemStack> bingoItem;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        reloadConfig();

        manager = new BingoManager(this, Map.of(
                "red", BingoTeam.red(),
                "blue", BingoTeam.blue(),
                "green", BingoTeam.green(),
                "yellow", BingoTeam.yellow()
        ));
        guiManager = new BingoGUIManager(manager);

        getServer().getPluginManager().registerEvents(manager, this);
        getServer().getPluginManager().registerEvents(guiManager, this);

        //noinspection DataFlowIssue
        getCommand("bingo").setExecutor(new BingoCommand(guiManager));
        //noinspection DataFlowIssue
        getCommand("bingoadmin").setExecutor(new BingoAdminCommand(this));

        World world = getServer().getWorlds().get(0);
        world.getWorldBorder().setCenter(world.getSpawnLocation().add(0.5, 0, 0.5));
        //noinspection DataFlowIssue
        world.getWorldBorder().setSize(world.getGameRuleValue(GameRule.SPAWN_RADIUS) * 2 + 1);

    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        bingoItem = Optional.ofNullable(createItem(getConfig().getConfigurationSection("bingo-item")));
    }

    private @Nullable ItemStack createItem(@Nullable ConfigurationSection section) {
        String material = section.getString("material");
        if (material == null) return null;
        ItemStack item = new ItemStack(Material.valueOf(material));

        item.editMeta(meta -> {

            String name = section.getString("name");
            if (name != null)
                meta.customName(MiniMessage.miniMessage().deserialize(name));

            List<String> lore = section.getStringList("lore");
            meta.lore(lore.stream().map(MiniMessage.miniMessage()::deserialize).toList());

        });

        return item;
    }

    public BingoManager getManager() {
        return manager;
    }

    public BingoGUIManager getGUIManager() {
        return guiManager;
    }

    public int getStartCountdownDuration() {
        return getConfig().getInt("start-countdown-duration");
    }

    public double getBorderSize() {
        return getConfig().getDouble("border-size");
    }

    public int getBoardSize() {
        return getConfig().getInt("board-size");
    }

    public @NotNull Optional<ItemStack> getBingoItem() {
        return bingoItem.map(ItemStack::clone);
    }

    public boolean isBingoItem(@Nullable ItemStack item) {
        return bingoItem.map(b -> b.isSimilar(item)).orElse(false);
    }

    public @NotNull Set<NamespacedKey> getAdvancementOptions() {
        return getConfig().getStringList("advancement-options").stream().map(NamespacedKey::fromString).collect(Collectors.toSet());
    }

}
