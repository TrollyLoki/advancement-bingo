package net.trollyloki.advancementbingo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;

public class BingoManager implements Listener {

    private final @NotNull AdvancementBingoPlugin plugin;

    private final @NotNull List<@NotNull BingoTeam> availableTeams;
    private final @NotNull Map<@NotNull UUID, @NotNull BingoTeam> players;

    public BingoManager(@NotNull AdvancementBingoPlugin plugin, @NotNull List<@NotNull BingoTeam> availableTeams) {
        this.plugin = plugin;
        this.availableTeams = availableTeams;
        this.players = new HashMap<>();
    }

    public @NotNull List<@NotNull BingoTeam> getAvailableTeams() {
        return Collections.unmodifiableList(availableTeams);
    }

    public @NotNull Map<@NotNull UUID, @NotNull BingoTeam> getPlayers() {
        return Collections.unmodifiableMap(players);
    }

    public @NotNull Optional<BingoTeam> setTeam(@NotNull UUID player, @NotNull BingoTeam team) {
        team.getScoreboardTeam().addPlayer(Bukkit.getOfflinePlayer(player));
        return Optional.ofNullable(players.put(player, team));
    }

    public @NotNull Optional<BingoTeam> remove(@NotNull UUID player) {
        Optional<BingoTeam> team = Optional.ofNullable(players.remove(player));
        team.ifPresent(t -> t.getScoreboardTeam().removePlayer(Bukkit.getOfflinePlayer(player)));
        return team;
    }

    public void clear() {
        for (BingoTeam team : availableTeams) {
            Team scoreboardTeam = team.getScoreboardTeam();
            scoreboardTeam.removeEntries(scoreboardTeam.getEntries());
        }
        players.clear();
    }

    public @NotNull Optional<BingoTeam> getTeam(@NotNull UUID player) {
        return Optional.ofNullable(players.get(player));
    }

    public @NotNull Set<BingoTeam> getActiveTeams() {
        return new HashSet<>(players.values());
    }

    public void startCountdown() {
        new CountdownTimer(this, plugin.getStartCountdownDuration()).runTaskTimer(plugin, 0, 20);
    }

    public void startGame() {

        for (UUID uuid : new HashSet<>(players.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                remove(uuid);
                continue;
            }

            player.spigot().respawn();
            player.setGameMode(GameMode.SURVIVAL);
            //noinspection DataFlowIssue
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            player.setSaturation(5);
            player.setFoodLevel(20);
            player.setLevel(0);
            player.setExp(0);
        }

        double borderSize = plugin.getBorderSize();
        for (World world : plugin.getServer().getWorlds()) {
            world.setTime(0);
            world.getWorldBorder().setSize(borderSize);
        }

    }

    @EventHandler
    public void onPlayerAdvancementDone(@NotNull PlayerAdvancementDoneEvent event) {
        getTeam(event.getPlayer().getUniqueId()).ifPresent(team -> team.getBoard().ifPresent(board -> {
            if (board.complete(event.getAdvancement().getKey())) {
                if (board.isWinning()) {
                    onWin(team);
                } else {
                    onBingoHit(team, event.getAdvancement().getKey());
                }
            }

        }));
    }

    public void onWin(@NotNull BingoTeam team) {
        Title title = Title.title(team.getDisplayName().append(Component.text(" wins!", NamedTextColor.WHITE)),
                Component.empty(),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofSeconds(1))
        );
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(title);
            Jingles.playGameEnd(plugin, player);
        }
    }

    private void onBingoHit(BingoTeam team, NamespacedKey advancement) {
        if (team.getBoard().isEmpty()) return;
        @NotNull BingoBoard board = team.getBoard().get();

        int[] advancementLocation = board.findAdvancement(advancement);
        if (advancementLocation == null) return;
        int row = advancementLocation[0];
        int col = advancementLocation[1];

        // Hit for {Team name}
        // Progress on rows/columns/diagonals
        // Total bingos scored

        Component hitMessage = hitMessage(team);
        Component lineMessage = progressMessage(board, row, col);
        Component completedMessage = Component.text("Completed " + board.getCompletedRows() + "/" + board.getRequiredRows() + " bingos to win")
                .color(team.getTextColor());

        // Send message next tick so it's displayed after vanilla advancement message
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(hitMessage);
                player.sendMessage(lineMessage);
                if (board.getCompletedRows() > 0 && board.getRequiredRows() > 1) player.sendMessage(completedMessage);
                Jingles.playAdvancementHit(plugin, player);
            }
        });

    }

    private Component hitMessage(BingoTeam team) {
        return Component.text().content("Hit for ")
                .append(Component.text("&")
                        .color(team.getTextColor())
                        .decoration(TextDecoration.OBFUSCATED, true))
                .append(team.getDisplayName().color(team.getTextColor()))
                .append(Component.text("&")
                        .color(team.getTextColor())
                        .decoration(TextDecoration.OBFUSCATED, true))
                .decoration(TextDecoration.BOLD, true)
                .build();
    }

    public void mergeLineProgress(Map<Integer, String> map, int key, String value) {
        map.merge(key, value, (existing, append) -> existing + ", " + append);
    }

    private Component progressMessage(BingoBoard board, int row, int col) {
        HashMap<Integer, String> lineProgress = new HashMap<>();

        mergeLineProgress(lineProgress, board.getRowProgress(row), "Row " + (row+1));
        mergeLineProgress(lineProgress, board.getColumnProgress(col), "Column " + (col+1));
        if (row == col)
            mergeLineProgress(lineProgress, board.getTopLeftDiagonalProgress(), "Left Diagonal");
        if (row == board.getLength() - 1 - col)
            mergeLineProgress(lineProgress, board.getTopRightDiagonalProgress(), "Right Diagonal");

        List<TextComponent> components =
        lineProgress.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .map((entry) -> {
                    TextComponent.Builder builder = Component.text().content(entry.getKey() + "/" + board.getLength());
                    if (entry.getKey() == board.getLength()) builder.color(NamedTextColor.GREEN);
                    else builder.color(NamedTextColor.GRAY);
                    builder.append(Component.text(" for " + entry.getValue()).color(NamedTextColor.GRAY));
                    return builder.build();
                })
                .toList();

        JoinConfiguration joinConfig = JoinConfiguration.separator(Component.text(" | ").color(NamedTextColor.GRAY));
        return Component.join(joinConfig, components);
    }

}
