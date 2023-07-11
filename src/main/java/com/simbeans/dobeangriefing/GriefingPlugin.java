package com.simbeans.dobeangriefing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.simbeans.dobeangriefing.event.MobGriefingListener;

public final class GriefingPlugin extends JavaPlugin implements Listener {

    private static final List<String> CONFIG_KEYS = Arrays.asList("overrideMobGriefing", "enable",
            "allowHostileExplosions", "allowHostileItemPickups", "allowPiglinBartering", "allowHostileBlockPickups",
            "allowHostileBreakDoors", "bossOverride", "friendlyProtectAnimals", "friendlyProtectEntityBlocks",
            "friendlyProtectAgainstProjectiles", "bossOverrideFriendly");
    private static final List<String> COMMAND_KEYS;
    static {
        COMMAND_KEYS = new ArrayList<>(CONFIG_KEYS);
        COMMAND_KEYS.remove("overrideMobGriefing");
    }

    private Set<String> worldsToOverride = Collections.emptySet();
    private boolean overrideAllWorlds = false;
    private MobGriefingListener mobGriefingListener = null;

    public void reloadImpl() {
        if (mobGriefingListener == null) {
            mobGriefingListener = new MobGriefingListener();
        }

        FileConfiguration config = this.getConfig();

        boolean enable = config.getBoolean("enable", true);
        mobGriefingListener.setEnabled(enable);
        if (!enable) {
            return;
        }

        Object overrideMobGriefing = config.get("overrideMobGriefing", null);

        if (overrideMobGriefing != null) {
            if (overrideMobGriefing.equals(Boolean.TRUE)) {
                overrideAllWorlds = true;
            } else if (overrideMobGriefing instanceof String) {
                worldsToOverride = new HashSet<>(Arrays.asList((String) overrideMobGriefing));
            } else if (overrideMobGriefing instanceof List<?>) {
                worldsToOverride = ((List<?>) overrideMobGriefing).stream().map(String::valueOf)
                        .collect(Collectors.toSet());
            }
        }

        List<Class<? extends Entity>> entityExplodeFilter = config.getBoolean("allowHostileExplosions", false)
                ? Collections.emptyList()
                : MobGriefingListener.ENEMIES;
        List<Class<? extends Entity>> itemPickupFilter = config.getBoolean("allowHostileItemPickups", false)
                ? Collections.emptyList()
                : MobGriefingListener.ENEMIES;
        boolean allowPiglinBartering = config.getBoolean("allowPiglinBartering", true);
        List<Class<? extends Entity>> blockPickupFilter = config.getBoolean("allowHostileBlockPickups", false)
                ? Collections.emptyList()
                : MobGriefingListener.ENEMIES;
        List<Class<? extends Entity>> doorBreakFilter = config.getBoolean("allowHostileBreakDoors", false)
                ? Collections.emptyList()
                : MobGriefingListener.ENEMIES;
        boolean bossOverride = config.getBoolean("bossOverride", true);
        boolean friendlyProtectAnimals = config.getBoolean("friendlyProtectAnimals", true);
        boolean friendlyProtectEntityBlocks = config.getBoolean("friendlyProtectEntityBlocks", true);
        boolean friendlyProtectAgainstProjectiles = config.getBoolean("friendlyProtectAgainstProjectiles", true);
        boolean bossOverrideFriendly = config.getBoolean("bossOverrideFriendly", false);

        List<Class<? extends Entity>> friendlyProtectEntities = new ArrayList<>();
        if (friendlyProtectAnimals) {
            friendlyProtectEntities.add(Animals.class);
        }
        if (friendlyProtectEntityBlocks) {
            friendlyProtectEntities.add(ItemFrame.class);
            friendlyProtectEntities.add(ArmorStand.class);
            friendlyProtectEntities.add(Minecart.class);
            friendlyProtectEntities.add(Boat.class);
            friendlyProtectEntities.add(EnderCrystal.class);
        }

        mobGriefingListener.setEntityExplodeFilter(entityExplodeFilter);
        mobGriefingListener.setItemPickupFilter(itemPickupFilter);
        mobGriefingListener.setAllowPiglinBartering(allowPiglinBartering);
        mobGriefingListener.setBlockPickupFilter(blockPickupFilter);
        mobGriefingListener.setDoorBreakFilter(doorBreakFilter);
        mobGriefingListener.setBossOverrideGriefing(bossOverride);
        mobGriefingListener.setFriendlyProtectEntities(friendlyProtectEntities);
        mobGriefingListener.setFriendlyProtectAgainstProjectiles(friendlyProtectAgainstProjectiles);
        mobGriefingListener.setBossOverrideFriendly(bossOverrideFriendly);
    }

    @Override
    public void onEnable() {
        reloadImpl();

        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getPluginManager().registerEvents(mobGriefingListener, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("dobeangriefing")) {
            if (args.length != 2) {
                return false;
            }
            if (!CONFIG_KEYS.contains(args[0])) {
                return false;
            }
            if (!(args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false"))) {
                return false;
            }
            this.getConfig().set(args[0], Boolean.valueOf(args[1]));
            this.saveConfig();
            this.reloadImpl();
            sender.sendMessage("beangrief configuration reloaded");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equals("dobeangriefing")) {
            List<String> list = new ArrayList<>();
            if (args.length == 0) {
                for (String key : COMMAND_KEYS) {
                    list.add(key);
                }
            } else if (args.length == 1) {
                for (String key : COMMAND_KEYS) {
                    if (key.startsWith(args[0])) {
                        list.add(key);
                    }
                }
            } else if (args.length == 2) {
                if (args[1] == null || args[1].isEmpty()) {
                    list.add("true");
                    list.add("false");
                } else if (args[1].startsWith("t")) {
                    list.add("true");
                } else if (args[1].startsWith("f")) {
                    list.add("false");
                }
            }
            return list;
        } else {
            return null;
        }
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        World world = event.getWorld();
        if (overrideAllWorlds || worldsToOverride.contains(world.getName())) {
            if (!world.getGameRuleValue(GameRule.MOB_GRIEFING)) {
                getLogger().info(String.format("Overriding mobGriefing for world %s", world.getName()));
                world.setGameRule(GameRule.MOB_GRIEFING, true);
            }
        }
    }

}