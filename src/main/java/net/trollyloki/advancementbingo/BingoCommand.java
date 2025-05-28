package net.trollyloki.advancementbingo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BingoCommand implements CommandExecutor, TabCompleter {

    private final @NotNull BingoGUIManager manager;

    public BingoCommand(@NotNull BingoGUIManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
            return false;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("book")) {
            manager.getBingoManager().giveBingoBook(player);
            return true;
        }

        Optional<BingoTeam> team = manager.getBingoManager().getTeam(player.getUniqueId());
        if (team.isPresent() && team.get().getBoard().isPresent()) {
            manager.openGUI(player, team.get().getBoard().get().getGUI());
        } else {
            manager.openTeamSelectGUI(player);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }

}
