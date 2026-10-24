package com.neoloxal.who_are_they;

import com.mojang.logging.LogUtils;
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
public class WhoAreThey {
    public static final String MODID = "who_are_they";
    private static final Logger LOGGER = LogUtils.getLogger();

    private Set<Item> identityConcealers;

    public WhoAreThey(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, WATConfig.CONFIG_SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener(this::configLoaded);
        modEventBus.addListener(this::onFinishLoad);

        NeoForge.EVENT_BUS.addListener(this::renderNameTag);
        NeoForge.EVENT_BUS.addListener(this::onClientMessageReceived);
    }

    public void onFinishLoad(FMLClientSetupEvent event) {
        event.enqueueWork(WATConfig::registerItems);
    }

    public void configLoaded(ModConfigEvent event) {
        if (event.getConfig().getSpec() == WATConfig.CONFIG_SPEC) {
            this.identityConcealers = WATConfig.CONFIG.identity_concealers.get().stream()
                    .map(ResourceLocation::tryParse)
                    .filter(Objects::nonNull)
                    .map(BuiltInRegistries.ITEM::get)
                    .collect(Collectors.toSet());
        }
    }

    public void renderNameTag(RenderNameTagEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack headItem = player.getItemBySlot(EquipmentSlot.HEAD);

            if (!headItem.isEmpty() && identityConcealers.contains(headItem.getItem())) {
                event.setCanRender(TriState.FALSE);
            }
        }
    }

    public void onClientMessageReceived(ClientChatReceivedEvent.Player event) {
        ClientLevel level = Objects.requireNonNull(Minecraft.getInstance().getConnection()).getLevel();
        Player player = level.getPlayerByUUID(event.getSender());

        if (player == null) return;

        ItemStack headItem = player.getItemBySlot(EquipmentSlot.HEAD);

        if (!headItem.isEmpty() && identityConcealers.contains(headItem.getItem())) {
            if (player != Minecraft.getInstance().player) {
                if (!WATConfig.CONFIG.conceal_chat_messages.getAsBoolean()) return;
                String oldMessage = event.getMessage().getString();

                String replacement = Component.translatable("who_are_they.concealed_name").getString();

                Component newMessage = Component.literal(oldMessage.replace(player.getName().getString(), replacement));

                event.setMessage(newMessage);
            } else {
                if (WATConfig.CONFIG.hide_conceal_notification.getAsBoolean()) return;
                Component message = event.getMessage();

                Component hover = Component.translatable("who_are_they.concealment_notifier.tooltip", Component.translatable("who_are_they.configuration.conceal_chat_messages"));
                Component extra = Component.translatable("who_are_they.concealment_notifier").withStyle(ChatFormatting.GRAY).withStyle(style -> style.withHoverEvent(
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)
                ));

                event.setMessage(message.copy().append(Component.literal(" ")).append(extra));
            }
        }
    }
}
