package com.neoloxal.concealment_api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class ConcealmentAPI {
    private static final Set<UUID> concealed_players = new HashSet<>();
    private static final Set<Predicate<Player>> concealment_predicates = new HashSet<>();

    private static Set<Item> identity_concealers = new HashSet<>();
    private static Set<Item> server_identity_concealers = new HashSet<>();

    static {
        concealment_predicates.add(player -> concealed_players.contains(player.getUUID()));
        concealment_predicates.add(player -> identity_concealers.contains(player.getItemBySlot(EquipmentSlot.HEAD).getItem()));
        concealment_predicates.add(Entity::isInvisible);
    }

    public static void addConcealmentPredicate(Predicate<Player> predicate) {
        concealment_predicates.add(predicate);
    }

    public static void concealPlayer(UUID player) {
        concealed_players.add(player);
    }

    public static void unconcealPlayer(UUID player) {
        concealed_players.remove(player);
    }

    public static void setClientIdentityConcealers(Set<Item> clientIdentityConcealers) {
        identity_concealers = new HashSet<>(clientIdentityConcealers);
        identity_concealers.addAll(server_identity_concealers);
    }

    public static void addServerIdentityConcealers(Set<Item> serverIdentityConcealers) {
        server_identity_concealers = serverIdentityConcealers;
        identity_concealers.addAll(server_identity_concealers);
    }

    public static boolean isPlayerConcealed(Player player) {
        for (Predicate<Player> predicate : concealment_predicates) {
            if (predicate.test(player)) return true;
        }
        return false;
    }
}
