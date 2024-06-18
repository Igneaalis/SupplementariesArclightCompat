package net.mehvahdjukaar.supplementaries.common.events.forge;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.hud.SelectableContainerItemHud;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.client.hud.forge.SelectableContainerItemHudImpl;
import net.mehvahdjukaar.supplementaries.client.hud.forge.SlimedOverlayHudImpl;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.funny.JarredHeadLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.QuiverLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.SlimedLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.forge.CannonChargeOverlayImpl;
import net.mehvahdjukaar.supplementaries.client.renderers.items.AltimeterItemRenderer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.EndermanSkullBlock;
import net.mehvahdjukaar.supplementaries.common.events.ClientEvents;
import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.SherdTooltip;
import net.mehvahdjukaar.supplementaries.common.misc.songs.SongsManager;
import net.mehvahdjukaar.supplementaries.common.utils.IQuiverPlayer;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.sound.SoundEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.util.stream.Collectors;

public class ClientEventsForge {

    public static void init() {
        MinecraftForge.EVENT_BUS.register(ClientEventsForge.class);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(ClientEventsForge::onAddLayers);
        bus.addListener(ClientEventsForge::onPackReload);
        bus.addListener(ClientEventsForge::onAddGuiLayers);
        bus.addListener(ClientEventsForge::onRegisterSkullModels);
    }

    public static void onRegisterSkullModels(EntityRenderersEvent.CreateSkullModels event) {
        event.registerSkullModel(EndermanSkullBlock.TYPE,
                new SkullModel(event.getEntityModelSet().bakeLayer(ModelLayers.SKELETON_SKULL)));
        SkullBlockRenderer.SKIN_BY_TYPE.put(EndermanSkullBlock.TYPE,
                Supplementaries.res("textures/entity/enderman_head.png"));
    }


    @SuppressWarnings("unchecked")
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (String skinType : event.getSkins()) {
            var renderer = event.getSkin(skinType);
            if (renderer != null) {
                renderer.addLayer(new QuiverLayer(renderer, false));
                RenderLayerParent model = renderer;
                renderer.addLayer(new JarredHeadLayer<>(model, event.getEntityModels()));
            }
        }
        var skeletonRenderer = event.getRenderer(EntityType.SKELETON);
        if (skeletonRenderer != null) {
            skeletonRenderer.addLayer(new QuiverLayer(skeletonRenderer, true));
        }
        var strayRenderer = event.getRenderer(EntityType.STRAY);
        if (strayRenderer != null) {
            strayRenderer.addLayer(new QuiverLayer(strayRenderer, true));
        }


        //adds to all entities
        var entityTypes = ImmutableList.copyOf(
                ForgeRegistries.ENTITY_TYPES.getValues().stream()
                        .filter(DefaultAttributes::hasSupplier)
                        .filter(e -> (e != EntityType.ENDER_DRAGON))
                        .map(entityType -> (EntityType<LivingEntity>) entityType)
                        .collect(Collectors.toList()));

        entityTypes.forEach((entityType -> {
            var r = event.getRenderer(entityType);
            if (r != null &&  !((Object)r instanceof NoopRenderer<?>)) r.addLayer(new SlimedLayer(r));
        }));

        //player skins
        for (String skinType : event.getSkins()) {
            var r = event.getSkin(skinType);
            if (r != null) r.addLayer(new SlimedLayer(r));
        }
    }


    public static void onAddGuiLayers(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "quiver_overlay",
                new SelectableContainerItemHudImpl());

        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "cannon_charge_overlay",
                new CannonChargeOverlayImpl());

        event.registerBelow(VanillaGuiOverlay.FROSTBITE.id(), "slimed_overlay",
                new SlimedOverlayHudImpl());
    }

    @SubscribeEvent
    public static void itemTooltip(ItemTooltipEvent event) {
        if (event.getEntity() != null) {
            ClientEvents.onItemTooltip(event.getItemStack(), event.getFlags(), event.getToolTip());
        }
    }


    @SubscribeEvent
    public static void screenInit(ScreenEvent.Init.Post event) {
        if (CompatHandler.CONFIGURED) {
            ClientEvents.addConfigButton(event.getScreen(), event.getListenersList(), event::addListener);
        }
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ClientEvents.onClientTick(Minecraft.getInstance());
        }
    }

    @SubscribeEvent
    public static void onKeyPress(InputEvent.Key event) {
        if (Minecraft.getInstance().screen == null &&
                ClientRegistry.QUIVER_KEYBIND.matches(event.getKey(), event.getScanCode())
                && Minecraft.getInstance().player instanceof IQuiverPlayer qe) {
            int a = event.getAction();
            if (a == InputConstants.REPEAT || a == InputConstants.PRESS) {
                SelectableContainerItemHud.INSTANCE.setUsingKeybind(qe.supplementaries$getQuiverSlot());
            } else if (a == InputConstants.RELEASE) {
                SelectableContainerItemHud.INSTANCE.setUsingKeybind(SlotReference.EMPTY);
            }
        }

        if (CannonController.isActive()) {
            CannonController.onKeyPressed(event.getKey(), event.getAction(), event.getModifiers());
            //event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseScrolled(InputEvent.MouseScrollingEvent event) {
        if (SelectableContainerItemHud.INSTANCE.onMouseScrolled(event.getScrollDelta())) {
            event.setCanceled(true);
        }
        if (CannonController.isActive()) {
            CannonController.onMouseScrolled(event.getScrollDelta());
            event.setCanceled(true);
        }
    }


    //forge only below

    //TODO: add to fabric

    private static double wobble; // from 0 to 1

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        Player p = Minecraft.getInstance().player;
        if (p != null && !Minecraft.getInstance().isPaused()) {
            boolean isOnRope = ClientEvents.isIsOnRope();
            if (isOnRope || wobble != 0) {
                double period = ClientConfigs.Blocks.ROPE_WOBBLE_PERIOD.get();
                double newWobble = (((p.tickCount + event.getPartialTick()) / period) % 1);
                if (!isOnRope && newWobble < wobble) {
                    wobble = 0;
                } else {
                    wobble = newWobble;
                }
                event.setRoll((float) (event.getRoll() + Mth.sin((float) (wobble * 2 * Math.PI)) * ClientConfigs.Blocks.ROPE_WOBBLE_AMPLITUDE.get()));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof DeathScreen && event.getCurrentScreen() instanceof ChatScreen cs
                && ClientConfigs.Tweaks.DEATH_CHAT.get()) {
            cs.charTyped((char) GLFW.GLFW_KEY_MINUS, 0);
            cs.keyPressed(GLFW.GLFW_KEY_ENTER, 0, 0);
        }
    }

    @SubscribeEvent
    public static void onSoundPlay(SoundEvent.SoundSourceEvent event) {
        SongsManager.recordNoteFromSound(event.getSound(), event.getName());
    }

    @SubscribeEvent
    public static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        Item i = stack.getItem();
        var pattern = DecoratedPotPatterns.getResourceKey(i);
        if (pattern != null && i != Items.BRICK) {
            event.getTooltipElements().add(Either.right(new SherdTooltip(pattern)));
        }
    }


    public static void onPackReload(TextureStitchEvent.Post event) {
        if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            AltimeterItemRenderer.onReload();
        }
    }

    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (CannonController.isActive()) {
            event.setCanceled(true);
            event.setSwingHand(false);
            if (event.isAttack()) {
                CannonController.onPlayerAttack();
            } else if (event.isUseItem()) {
                CannonController.onPlayerUse();
            }
        }
    }

    @SubscribeEvent
    public static void renderHandEvent(RenderHandEvent event) {
        if (CannonController.isActive())
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        if (CannonController.isActive()) {
            var overlay = event.getOverlay();
            if (overlay == VanillaGuiOverlay.EXPERIENCE_BAR.type() || overlay == VanillaGuiOverlay.HOTBAR.type()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderOutline(RenderHighlightEvent.Block event) {
        if (CannonController.isActive()) {
            event.setCanceled(true);
        }
    }


}
