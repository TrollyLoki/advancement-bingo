package net.trollyloki.advancementbingo;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Jingles {
    private final static float G1 = (float) Math.pow(2, (float) -11/12);
    private final static float E1 = (float) Math.pow(2, (float) -2/12);
    private final static float C1 = (float) Math.pow(2, (float) -6/12);

    private final static float B1 = (float) Math.pow(2, (float) -7/12);
    private final static float D1 = (float) Math.pow(2, (float) -4/12);
    private final static float F1 = (float) Math.pow(2, (float) -1/12);
    private final static float G2 = (float) Math.pow(2, (float) 1/12);
    private final static float A2 = (float) Math.pow(2, (float) 3/12);


    public static void playAdvancementHit(AdvancementBingoPlugin plugin, Player player) {
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (i >= 3) {
                    cancel();
                } else if (i == 0) {
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_COW_BELL, 1, G1);
                } else if (i == 1) {
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_COW_BELL, 1, E1);
                } else if (i == 2) {
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_COW_BELL, 1, C1);
                }
                i++;
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    public static void playGameEnd(AdvancementBingoPlugin plugin, Player player) {
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (i >= 8) {
                    cancel();
                } else if (i == 0 || i == 1 || i == 3 || i == 7) {
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1, C1);
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1, E1);
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1, G2);
                } else if (i == 2) {
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1, D1);
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1, F1);
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1, A2);
                } else if (i == 5) {
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1, B1);
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1, D1);
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1, F1);
                }
                i++;
            }
        }.runTaskTimer(plugin, 0, 3);
    }


}
