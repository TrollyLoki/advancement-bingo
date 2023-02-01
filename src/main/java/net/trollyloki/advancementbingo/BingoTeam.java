package net.trollyloki.advancementbingo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BingoTeam {

    private final @NotNull String name, scoreboardName;
    private final @NotNull NamedTextColor textColor;
    private final @NotNull Material itemMaterial;
    private @Nullable BingoBoard board;

    public BingoTeam(@NotNull String name, @NotNull String scoreboardName, @NotNull NamedTextColor textColor, @NotNull Material itemMaterial) {
        this.name = name;
        this.scoreboardName = scoreboardName;
        this.textColor = textColor;
        this.itemMaterial = itemMaterial;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getScoreboardName() {
        return scoreboardName;
    }

    public @NotNull NamedTextColor getTextColor() {
        return textColor;
    }

    public @NotNull Component getDisplayName() {
        return Component.text(getName(), getTextColor());
    }

    public @NotNull Material getItemMaterial() {
        return itemMaterial;
    }

    public @NotNull ItemStack getItem() {
        ItemStack item = new ItemStack(getItemMaterial());
        item.editMeta(meta -> meta.displayName(getDisplayName().decoration(TextDecoration.ITALIC, false)));
        return item;
    }

    public @NotNull Team getScoreboardTeam(@NotNull Scoreboard scoreboard) {
        String scoreboardName = getScoreboardName();
        Team team = scoreboard.getTeam(scoreboardName);
        if (team == null) {
            team = scoreboard.registerNewTeam(scoreboardName);

            team.displayName(getDisplayName());
            team.color(getTextColor());

        }
        return team;
    }

    public @NotNull Team getScoreboardTeam() {
        return getScoreboardTeam(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public @NotNull Optional<BingoBoard> getBoard() {
        return Optional.ofNullable(board);
    }

    public void setBoard(@Nullable BingoBoard board) {
        this.board = board;
    }

    public static @NotNull BingoTeam red() {
        return new BingoTeam("Red Team", "bingo_red", NamedTextColor.RED, Material.RED_CONCRETE);
    }

    public static @NotNull BingoTeam blue() {
        return new BingoTeam("Blue Team", "bingo_blue", NamedTextColor.BLUE, Material.BLUE_CONCRETE);
    }

    public static @NotNull BingoTeam green() {
        return new BingoTeam("Green Team", "bingo_green", NamedTextColor.GREEN, Material.LIME_CONCRETE);
    }

    public static @NotNull BingoTeam yellow() {
        return new BingoTeam("Yellow Team", "bingo_yellow", NamedTextColor.YELLOW, Material.YELLOW_CONCRETE);
    }

}
