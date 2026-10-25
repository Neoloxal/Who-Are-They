package com.neoloxal.who_are_they;

import com.mojang.logging.LogUtils;
import com.neoloxal.concealment_api.ConcealmentAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mod(value = WhoAreThey.MODID, dist = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class WhoAreThey {
    public static final String MODID = "who_are_they";
    private static final Logger LOGGER = LogUtils.getLogger();

    public WhoAreThey(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, WATConfig.CONFIG_SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener(this::configLoaded);
        modEventBus.addListener(this::onFinishLoad);

        NeoForge.EVENT_BUS.addListener(this::renderNameTag);
    }

    public void onFinishLoad(FMLClientSetupEvent event) {
        event.enqueueWork(WATConfig::registerItems);
    }

    public void configLoaded(ModConfigEvent event) {
        if (event.getConfig().getSpec() == WATConfig.CONFIG_SPEC) {
            Set<Item> identityConcealers = WATConfig.CONFIG.identity_concealers.get().stream()
                    .map(ResourceLocation::tryParse)
                    .filter(Objects::nonNull)
                    .map(BuiltInRegistries.ITEM::get)
                    .collect(Collectors.toSet());
            ConcealmentAPI.setClientIdentityConcealers(identityConcealers);
        }
    }

    public void renderNameTag(RenderNameTagEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (ConcealmentAPI.isPlayerConcealed(player)) {
                event.setCanRender(TriState.FALSE);
            }
        }
    }
}
