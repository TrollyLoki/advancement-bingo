package net.trollyloki.advancementbingo;

import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AdvancementBingoPlugin extends JavaPlugin {

    private BingoManager manager;
    private BingoGUIManager guiManager;

    @Override
    public void onEnable() {

        saveDefaultConfig();

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

    public @NotNull Set<NamespacedKey> getAdvancementOptions() {
        return getConfig().getStringList("advancement-options").stream().map(NamespacedKey::fromString).collect(Collectors.toSet());
    }

}
