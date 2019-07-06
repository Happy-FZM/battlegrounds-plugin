package com.matsg.battlegrounds.mode.zombies.handler;

import com.matsg.battlegrounds.api.entity.BattleEntityType;
import com.matsg.battlegrounds.api.entity.GamePlayer;
import com.matsg.battlegrounds.api.entity.Hitbox;
import com.matsg.battlegrounds.api.entity.Mob;
import com.matsg.battlegrounds.api.event.EventHandler;
import com.matsg.battlegrounds.api.event.GamePlayerKillEntityEvent;
import com.matsg.battlegrounds.api.game.Game;
import com.matsg.battlegrounds.mode.zombies.item.PowerUp;
import com.matsg.battlegrounds.mode.zombies.Zombies;
import com.matsg.battlegrounds.mode.zombies.item.factory.PowerUpFactory;
import com.matsg.battlegrounds.mode.zombies.item.powerup.PowerUpEffectType;
import com.matsg.battlegrounds.util.BattleRunnable;

import java.util.List;
import java.util.Random;

public class ZombiesKillEventHandler implements EventHandler<GamePlayerKillEntityEvent> {

    private PowerUpFactory powerUpFactory;
    private Random random;
    private Zombies zombies;

    public ZombiesKillEventHandler(Zombies zombies, PowerUpFactory powerUpFactory) {
        this.zombies = zombies;
        this.powerUpFactory = powerUpFactory;
        this.random = new Random();
    }

    public void handle(GamePlayerKillEntityEvent event) {
        if (!zombies.isActive() || !(event.getEntity() instanceof Mob)) {
            return;
        }

        Game game = event.getGame();
        GamePlayer gamePlayer = event.getGamePlayer();
        Hitbox hitbox = event.getHitbox();
        Mob mob = (Mob) event.getEntity();

        mob.getBukkitEntity().setCustomName(game.getMobManager().getHealthBar(mob));
        mob.setHealth(0);

        gamePlayer.setKills(gamePlayer.getKills() + 1);

        if (hitbox == Hitbox.HEAD) {
            gamePlayer.setHeadshots(gamePlayer.getHeadshots() + 1);
        }

        game.getPlayerManager().givePoints(gamePlayer, zombies.getPowerUpManager().getPowerUpPoints(event.getPoints()));
        game.updateScoreboard();

        List<String> powerUpTypes = zombies.getConfig().getPowerUpTypes();

        if (mob.hasLoot()
                && mob.isHostileTowards(gamePlayer)
                && powerUpTypes.size() > 0
                && zombies.getPowerUpManager().getPowerUpCount() < powerUpTypes.size()
                && random.nextDouble() < zombies.getConfig().getPowerUpChance()) {

            PowerUpEffectType effectType = PowerUpEffectType.values()[random.nextInt(PowerUpEffectType.values().length)];
            PowerUp powerUp = powerUpFactory.make(effectType, zombies.getConfig().getPowerUpDuration());

            zombies.getPowerUpManager().dropPowerUp(powerUp, mob.getBukkitEntity().getLocation());
        }

        long mobKillDelay = 15;

        new BattleRunnable() {
            public void run() {
                game.getMobManager().getMobs().remove(mob);
                game.updateScoreboard();
                mob.remove();

                double maxMovementSpeed = 0.35;

                if (zombies.getConfig().hasRunningMobs()
                        && zombies.getWave().getRound() >= zombies.getConfig().getRunningMobsRound()
                        && game.getMobManager().getMobs().size() == 1
                        && game.getMobManager().getMobs().get(0).getMovementSpeed() <= maxMovementSpeed
                        && !zombies.getWave().isRunning()) {
                    Mob lastMob = game.getMobManager().getMobs().get(0);
                    lastMob.getBukkitEntity().getWorld().strikeLightningEffect(lastMob.getBukkitEntity().getLocation());
                    lastMob.setMovementSpeed(maxMovementSpeed);
                }

                if (game.getMobManager().getMobs().size() > 0 || !game.getState().isInProgress() || zombies.getWave().isRunning()) {
                    return;
                }

                if (mob.getEntityType() == BattleEntityType.HELLHOUND) {
                    PowerUp powerUp = powerUpFactory.make(PowerUpEffectType.MAX_AMMO, 0);

                    zombies.getPowerUpManager().dropPowerUp(powerUp, mob.getBukkitEntity().getLocation());
                }

                zombies.startWave(zombies.getWave().getRound() + 1);
            }
        }.runTaskLater(mobKillDelay);
    }
}
