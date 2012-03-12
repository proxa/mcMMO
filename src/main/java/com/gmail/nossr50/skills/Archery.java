package com.gmail.nossr50.skills;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.nossr50.Users;
import com.gmail.nossr50.m;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.datatypes.SkillType;
import com.gmail.nossr50.locale.mcLocale;
import com.gmail.nossr50.party.Party;

public class Archery {

    /**
     * Track arrows fired for later retrieval.
     *
     * @param plugin mcMMO plugin instance
     * @param entity Entity damaged by the arrow
     * @param PPa PlayerProfile of the player firing the arrow
     */
    public static void trackArrows(mcMMO plugin, Entity entity, PlayerProfile PPa) {
        final int MAX_BONUS_LEVEL = 1000;
        int skillLevel = PPa.getSkillLevel(SkillType.ARCHERY);

        if (!plugin.misc.arrowTracker.containsKey(entity)) {
            plugin.misc.arrowTracker.put(entity, 0);
        }

        if (skillLevel > MAX_BONUS_LEVEL || (Math.random() * 1000 <= skillLevel)) {
            plugin.misc.arrowTracker.put(entity, 1);
        }
    }

    /**
     * Check for ignition on arrow hit.
     *
     * @param entity Entity damaged by the arrow
     * @param attacker Player who fired the arrow
     */
    public static void ignitionCheck(Entity entity, Player attacker) {

        //Check to see if PVP for this world is disabled before executing
        if (!entity.getWorld().getPVP()) {
            return;
        }

        final int IGNITION_CHANCE = 25;
        final int MAX_IGNITION_TICKS = 120;

        PlayerProfile PPa = Users.getProfile(attacker);

        if (Math.random() * 100 <= IGNITION_CHANCE) {
            int ignition = 20;
            ignition += (PPa.getSkillLevel(SkillType.ARCHERY) / 200) * 20;

            if (ignition > MAX_IGNITION_TICKS) {
                ignition = MAX_IGNITION_TICKS;
            }

            if (entity instanceof Player) {
                Player defender = (Player) entity;

                if (!Party.getInstance().inSameParty(attacker, defender)) {
                    defender.setFireTicks(defender.getFireTicks() + ignition);
                    attacker.sendMessage(mcLocale.getString("Combat.Ignition"));
                    defender.sendMessage(mcLocale.getString("Combat.BurningArrowHit"));
                }
            }
            else {
                entity.setFireTicks(entity.getFireTicks() + ignition);
                attacker.sendMessage(mcLocale.getString("Combat.Ignition"));
            }
        }
    }

    /**
     * Check for Daze.
     *
     * @param defender Defending player
     * @param attacker Attacking player
     */
    public static void dazeCheck(Player defender, Player attacker) {
        final int MAX_BONUS_LEVEL = 1000;

        int skillLevel = Users.getProfile(attacker).getSkillLevel(SkillType.ARCHERY);
        Location loc = defender.getLocation();
        int skillCheck = skillLevel;

        if (Math.random() * 10 > 5) {
            loc.setPitch(90);
        }
        else {
            loc.setPitch(-90);
        }

        if (skillLevel > MAX_BONUS_LEVEL) {
            skillCheck = MAX_BONUS_LEVEL;
        }

        if (Math.random() * 2000 <= skillCheck) {
            defender.teleport(loc);
            defender.sendMessage(mcLocale.getString("Combat.TouchedFuzzy"));
            attacker.sendMessage(mcLocale.getString("Combat.TargetDazed"));
        }
    }

    /**
     * Check for arrow retrieval.
     *
     * @param entity The entity hit by the arrows
     * @param plugin mcMMO plugin instance
     */
    public static void arrowRetrievalCheck(Entity entity, mcMMO plugin) {
        if (plugin.misc.arrowTracker.containsKey(entity)) {
            m.mcDropItems(entity.getLocation(), new ItemStack(Material.ARROW), plugin.misc.arrowTracker.get(entity));
        }

        plugin.misc.arrowTracker.remove(entity);
    }
}
