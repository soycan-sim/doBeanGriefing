package com.simbeans.dobeangriefing.event;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Boss;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.PolarBear;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class MobGriefingListener implements Listener {

    public final static List<Class<? extends Entity>> ENEMIES = Collections
            .unmodifiableList(Arrays.asList(Enemy.class));

    private boolean enabled = true;
    private List<Class<? extends Entity>> entityExplodeFilter = ENEMIES;
    private List<Class<? extends Entity>> itemPickupFilter = ENEMIES;
    private boolean allowPiglinBartering = true;
    private List<Class<? extends Entity>> blockPickupFilter = ENEMIES;
    private List<Class<? extends Entity>> doorBreakFilter = ENEMIES;
    private boolean bossOverrideGriefing = true;
    private List<Class<? extends Entity>> friendlyProtectEntities = Arrays.asList(Animals.class, ItemFrame.class,
            ArmorStand.class, Minecart.class, Boat.class, EnderCrystal.class);
    private List<Class<? extends Entity>> friendlyProtectExcludeEntities = Arrays.asList(Tameable.class, Enemy.class,
            Ocelot.class, PolarBear.class);
    private List<Class<? extends Entity>> friendlyProtectAgainst = ENEMIES;
    private boolean friendlyProtectAgainstProjectiles = true;
    private boolean bossOverrideFriendly = false;

    public MobGriefingListener() {

    }

    public MobGriefingListener(List<Class<? extends Entity>> entityExplodeFilter,
            List<Class<? extends Entity>> itemPickupFilter, boolean allowPiglinBartering,
            List<Class<? extends Entity>> blockPickupFilter, List<Class<? extends Entity>> doorBreakFilter,
            boolean bossOverrideGriefing, List<Class<? extends Entity>> friendlyProtectEntities,
            /* List<Class<? extends Entity>> friendlyProtectAgainst, */ boolean friendlyProtectAgainstProjectiles,
            boolean bossOverrideFriendly) {
        this.entityExplodeFilter = entityExplodeFilter;
        this.itemPickupFilter = itemPickupFilter;
        this.allowPiglinBartering = allowPiglinBartering;
        this.blockPickupFilter = blockPickupFilter;
        this.doorBreakFilter = doorBreakFilter;
        this.bossOverrideGriefing = bossOverrideGriefing;
        this.friendlyProtectEntities = friendlyProtectEntities;
        // this.friendlyProtectAgainst = friendlyProtectAgainst;
        this.friendlyProtectAgainstProjectiles = friendlyProtectAgainstProjectiles;
        this.bossOverrideFriendly = bossOverrideFriendly;
    }

    private boolean isInstanceOrShooter(Entity e, Class<? extends Entity> c, boolean cond) {
        return c.isInstance(e) || cond && e instanceof Projectile && c.isInstance(((Projectile) e).getShooter());
    }

    private boolean isInstanceOrShooter(Entity e, Class<? extends Entity> c) {
        return isInstanceOrShooter(e, c, true);
    }

    private boolean isInstanceOrShooter(Entity e, List<Class<? extends Entity>> cs, boolean cond) {
        return cs.stream().anyMatch(c -> isInstanceOrShooter(e, c, cond));
    }

    private boolean isInstanceOrShooter(Entity e, List<Class<? extends Entity>> cs) {
        return cs.stream().anyMatch(c -> isInstanceOrShooter(e, c));
    }

    private boolean checkEntityFilter(EntityEvent event, List<Class<? extends Entity>> filter) {
        if (bossOverrideGriefing && isInstanceOrShooter(event.getEntity(), Boss.class)) {
            return true;
        }

        return isInstanceOrShooter(event.getEntity(), filter);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!enabled) {
            return;
        }

        if (checkEntityFilter(event, entityExplodeFilter)) {
            event.blockList().clear();
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!enabled) {
            return;
        }

        if (allowPiglinBartering) {
            if (event.getEntity() instanceof Piglin
                    && event.getItem().getItemStack().getType() == Material.GOLD_INGOT) {
                return;
            }
        }
        if (checkEntityFilter(event, itemPickupFilter)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDoorBreak(EntityBreakDoorEvent event) {
        if (!enabled) {
            return;
        }

        if (checkEntityFilter(event, doorBreakFilter)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockChange(EntityChangeBlockEvent event) {
        if (!enabled) {
            return;
        }

        if (event.getTo().isAir()) {
            if (checkEntityFilter(event, blockPickupFilter)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!enabled) {
            return;
        }

        if (bossOverrideFriendly && isInstanceOrShooter(event.getDamager(), Boss.class)) {
            return;
        }

        if (friendlyProtectEntities.stream().anyMatch(c -> c.isInstance(event.getEntity()))
                && !friendlyProtectExcludeEntities.stream().anyMatch(c -> c.isInstance(event.getEntity()))) {
            if (isInstanceOrShooter(event.getDamager(), friendlyProtectAgainst, friendlyProtectAgainstProjectiles)) {
                event.setCancelled(true);
            }
        }
    }

    public void setEntityExplodeFilter(List<Class<? extends Entity>> entityExplodeFilter) {
        this.entityExplodeFilter = entityExplodeFilter;
    }

    public void setItemPickupFilter(List<Class<? extends Entity>> itemPickupFilter) {
        this.itemPickupFilter = itemPickupFilter;
    }

    public void setAllowPiglinBartering(boolean allowPiglinBartering) {
        this.allowPiglinBartering = allowPiglinBartering;
    }

    public void setBlockPickupFilter(List<Class<? extends Entity>> blockPickupFilter) {
        this.blockPickupFilter = blockPickupFilter;
    }

    public void setDoorBreakFilter(List<Class<? extends Entity>> doorBreakFilter) {
        this.doorBreakFilter = doorBreakFilter;
    }

    public void setBossOverrideGriefing(boolean bossOverride) {
        this.bossOverrideGriefing = bossOverride;
    }

    public void setFriendlyProtectEntities(List<Class<? extends Entity>> friendlyProtectEntities) {
        this.friendlyProtectEntities = friendlyProtectEntities;
    }

    public void setFriendlyProtectAgainst(List<Class<? extends Entity>> friendlyProtectAgainst) {
        this.friendlyProtectAgainst = friendlyProtectAgainst;
    }

    public void setFriendlyProtectAgainstProjectiles(boolean friendlyProtectAgainstProjectiles) {
        this.friendlyProtectAgainstProjectiles = friendlyProtectAgainstProjectiles;
    }

    public void setBossOverrideFriendly(boolean bossOverrideFriendly) {
        this.bossOverrideFriendly = bossOverrideFriendly;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
