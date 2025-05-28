package net.trollyloki.advancementbingo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class BingoAdminCommand implements CommandExecutor, TabCompleter {

    private final @NotNull AdvancementBingoPlugin plugin;

    public BingoAdminCommand(@NotNull AdvancementBingoPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) {

            if (args[0].equalsIgnoreCase("reload")) {

                plugin.reloadConfig();
                sender.sendMessage(Component.text("Configuration reloaded", NamedTextColor.GREEN));
                return true;

            } else if (args[0].equalsIgnoreCase("generate")) {

                boolean sharedBoard;
                if (args.length > 1 && args[1].equalsIgnoreCase("same"))
                    sharedBoard = true;
                else if (args.length > 1 && args[1].equalsIgnoreCase("different"))
                    sharedBoard = false;
                else {
                    sender.sendMessage(Component.text("Usage: /" + label + " generate <same|different> [required-rows]", NamedTextColor.RED));
                    return false;
                }

                int rows = 1;
                if (args.length > 2) {

                    try {
                        rows = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Component.text(args[2] + " is not a valid integer", NamedTextColor.RED));
                        return false;
                    }

                    if (rows < 1) {
                        sender.sendMessage(Component.text("Number of rows must be positive", NamedTextColor.RED));
                        return false;
                    }

                }

                BingoBoard board = null;
                if (sharedBoard)
                    board = BingoBoard.generateRandom(Component.text("Bingo Board"), plugin.getBoardSize(), plugin.getAdvancementOptions(), rows);

                for (BingoTeam team : plugin.getManager().getAvailableTeams().values()) {
                    Component title = team.getDisplayName().append(Component.text(" Bingo Board", NamedTextColor.BLACK));
                    BingoBoard teamBoard = board != null ? board.clone() : null;
                    if (teamBoard == null) {
                        teamBoard = BingoBoard.generateRandom(title, plugin.getBoardSize(), plugin.getAdvancementOptions(), rows);
                    } else {
                        teamBoard.setTitle(title);
                    }
                    team.setBoard(teamBoard);
                }

                for (Map.Entry<UUID, BingoTeam> entry : plugin.getManager().getPlayers().entrySet()) {
                    Player player = plugin.getServer().getPlayer(entry.getKey());
                    if (player != null && entry.getValue().getBoard().isPresent())
                        plugin.getGUIManager().openGUI(player, entry.getValue().getBoard().get().getGUI());
                }

                sender.sendMessage(Component.text("Bingo board generated", NamedTextColor.GREEN));
                return true;

            } else if (args[0].equalsIgnoreCase("start")) {

                plugin.getManager().startCountdown();
                sender.sendMessage(Component.text("Starting countdown...", NamedTextColor.GREEN));
                return true;

            } else if (args[0].equalsIgnoreCase("board")) {

                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
                    return false;
                }

                if (args.length == 1) {
                    sender.sendMessage(Component.text("Usage: /" + label + " board <player>", NamedTextColor.RED));
                    return false;
                }

                OfflinePlayer bingoPlayer = plugin.getServer().getOfflinePlayerIfCached(args[1]);
                if (bingoPlayer == null) {
                    sender.sendMessage(Component.text("Unknown player " + args[1], NamedTextColor.RED));
                    return false;
                }

                Optional<BingoTeam> team = plugin.getManager().getTeam(bingoPlayer.getUniqueId());
                if (team.isEmpty()) {
                    sender.sendMessage(Component.text(bingoPlayer.getName() + " is not on a bingo team", NamedTextColor.RED));
                    return false;
                }

                Optional<BingoBoard> board = team.get().getBoard();
                if (board.isEmpty()) {
                    sender.sendMessage(team.get().getDisplayName().append(Component.text(" currently does not have a bingo board", NamedTextColor.RED)));
                    return false;
                }

                plugin.getGUIManager().openGUI(player, board.get().getGUI());
                return true;

            } else if (args[0].equalsIgnoreCase("team")) {

                if (args.length <= 2) {
                    sender.sendMessage(Component.text("Usage: /" + label + " team <player> <team>", NamedTextColor.RED));
                    return false;
                }

                OfflinePlayer bingoPlayer = plugin.getServer().getOfflinePlayerIfCached(args[1]);
                if (bingoPlayer == null) {
                    sender.sendMessage(Component.text("Unknown player " + args[1], NamedTextColor.RED));
                    return false;
                }

                BingoTeam team = plugin.getManager().getAvailableTeams().get(args[2].toLowerCase());
                if (team == null) {
                    sender.sendMessage(Component.text("Unknown team " + args[2], NamedTextColor.RED));
                    return false;
                }

                plugin.getManager().setTeam(bingoPlayer.getUniqueId(), team);
                plugin.getGUIManager().updateTeamPlayers();

                sender.sendMessage(Component.text(bingoPlayer.getName() + " is now on ").append(team.getDisplayName()));
                return true;

            } else if (args[0].equalsIgnoreCase("reset")) {

                plugin.getManager().clear();
                for (BingoTeam team : plugin.getManager().getAvailableTeams().values())
                    team.setBoard(null);

                sender.sendMessage(Component.text("Bingo teams reset", NamedTextColor.GREEN));
                return true;

            }

        }

        sender.sendMessage(Component.text("Usage: /" + label + " <reload|generate|start|board|team|reset>", NamedTextColor.RED));
        return false;

    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        String prefix = args[args.length - 1].toLowerCase();

        if (args.length == 1) {

            return Stream.of("reload", "generate", "start", "board", "team", "reset").filter(option -> option.startsWith(prefix)).toList();

        } else if (args[0].equalsIgnoreCase("board")) {

            if (args.length == 2) {
                return sender.getServer().getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().startsWith(prefix)).toList();
            }

        } else if (args[0].equalsIgnoreCase("generate")) {

            if (args.length == 2) {
                return Stream.of("same", "different").filter(option -> option.startsWith(prefix)).toList();
            }

        } else if (args[0].equalsIgnoreCase("team")) {

            if (args.length == 2) {
                return sender.getServer().getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().startsWith(prefix)).toList();
            } else if (args.length == 3) {
                return plugin.getManager().getAvailableTeams().keySet().stream().filter(id -> id.startsWith(prefix)).toList();
            }

        }

        return Collections.emptyList();
    }

}
