package net.trollyloki.advancementbingo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class CountdownTimer extends BukkitRunnable {

    private final @NotNull BingoManager manager;
    private int remaining;

    public CountdownTimer(@NotNull BingoManager manager, int duration) {
        this.manager = manager;
        this.remaining = duration;
    }

    public @NotNull NamedTextColor getColor() {
        if (remaining == 0)
            return NamedTextColor.GREEN;
        else if (remaining <= 3)
            return NamedTextColor.RED;
        else
            return NamedTextColor.YELLOW;
    }

    public @NotNull String getText() {
        if (remaining == 0)
            return "GO";
        else
            return String.valueOf(remaining);
    }

    @Override
    public void run() {
        Title title = Title.title(Component.text(getText(), getColor()), Component.empty(),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ofSeconds(1))
        );
        for (Player player : Bukkit.getOnlinePlayers())
            player.showTitle(title);

        if (remaining <= 0) {
            cancel();
            manager.startGame();
        }
        remaining--;
    }

}
