package com.neoloxal.who_are_they;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WATConfig {
    public static final WATConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    public static Set<String> registeredItems;

    public final ModConfigSpec.ConfigValue<List<? extends String>> identity_concealers;
    public final ModConfigSpec.BooleanValue conceal_chat_messages;
    public final ModConfigSpec.BooleanValue hide_conceal_notification;

    public WATConfig(ModConfigSpec.Builder builder) {
        identity_concealers = builder.defineList(
                "identity_concealers",
                List.of(
                        "minecraft:carved_pumpkin",
                        "minecraft:dragon_head",
                        "minecraft:player_head"
                ),
                () -> "",
                object -> object instanceof String string && (registeredItems == null || registeredItems.contains(string))
        );

        conceal_chat_messages = builder.define("conceal_chat_messages", false);
        hide_conceal_notification = builder.define("hide_conceal_notification", false);
    }

    static {
        Pair<WATConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(WATConfig::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    public static void registerItems() {
        registeredItems = BuiltInRegistries.ITEM.keySet().stream()
                .map(BuiltInRegistries.ITEM::get)
                .map(Item::toString)
                .collect(Collectors.toSet());
    }
}
