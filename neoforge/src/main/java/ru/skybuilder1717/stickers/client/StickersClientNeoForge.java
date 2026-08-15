package ru.skybuilder1717.stickers.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;
import ru.skybuilder1717.stickers.data.StickerManager;
import ru.skybuilder1717.stickers.client.gui.StickerDragScreen;
import ru.skybuilder1717.stickers.client.gui.StickersScreen;
import com.mojang.blaze3d.platform.InputConstants;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

@Mod(value = "stickers", dist = Dist.CLIENT)
public class StickersClientNeoForge {
    public static KeyMapping OPEN_KEY;
    public static KeyMapping DRAG_KEY;

    public StickersClientNeoForge(IEventBus modBus) {
        modBus.addListener(StickersClientNeoForge::onRegisterKeyMappings);
        modBus.addListener(StickersClientNeoForge::onRegisterGuiLayers);

        NeoForge.EVENT_BUS.addListener(StickersClientNeoForge::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(StickersClientNeoForge::onPlayerLogout);
        NeoForge.EVENT_BUS.addListener(StickersClientNeoForge::onClientTick);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        KeyMapping.Category cat = new KeyMapping.Category(Identifier.parse("key.categories.stickers"));
        event.registerCategory(cat);
        OPEN_KEY = new KeyMapping(
                "key.stickers.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                cat
        );
        DRAG_KEY = new KeyMapping(
                "key.stickers.drag",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                cat
        );
        event.register(OPEN_KEY);
        event.register(DRAG_KEY);
    }

    private static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                Identifier.parse("hud.stickers"),
                StickersHudNeoForge::render
        );
    }

    public static void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        StickerManager.load(computeKey(isLegacy()));
    }

    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        StickerManager.save();
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (OPEN_KEY != null && OPEN_KEY.consumeClick() && mc.canInterruptScreen())
            mc.setScreenAndShow(new StickersScreen());
        if (DRAG_KEY != null && DRAG_KEY.consumeClick() && mc.canInterruptScreen() && !StickerManager.getAll().isEmpty())
            mc.setScreenAndShow(new StickerDragScreen());
    }

    private static boolean isLegacy() {
        Minecraft client = Minecraft.getInstance();
        if (client.getSingleplayerServer() != null && !client.isMultiplayerServer()) {
            String levelName = client.getSingleplayerServer().getWorldData().getLevelName();
            Path path = StickerManager.getSaveFile("sp_" + StickerManager.sanitize(levelName));
            return Files.exists(path);
        }
        return false;
    }

    private static String computeKey(boolean legacy) {
        Minecraft client = Minecraft.getInstance();
        if (client.getSingleplayerServer() != null && !client.isMultiplayerServer()) {
            String levelName = client.getSingleplayerServer().getWorldData().getLevelName();
            if (levelName.isEmpty()) return "sp_default";
            if (legacy) {
                System.out.println("[Stickers] Found legacy config! Replacing...");
                Path legacyPath = StickerManager.getSaveFile("sp_" + StickerManager.sanitize(levelName));
                try {
                    Files.write(legacyPath, Files.readString(legacyPath).getBytes());
                    try {
                        Files.delete(legacyPath);
                        System.out.println("[Stickers] Legacy config migrated successfully.");
                    } catch (IOException e) {
                        System.out.println("[Stickers] Failed to delete legacy config: " + e.getMessage());
                    }
                } catch (IOException e) {
                    System.out.println("[Stickers] Failed to migrate legacy config: " + e.getMessage());
                }
            }
            String seeded = UUID.nameUUIDFromBytes(levelName.getBytes(StandardCharsets.UTF_8)).toString();
            return "sp_" + StickerManager.sanitize(seeded);
        }
        ServerData entry = client.getCurrentServer();
        if (entry != null) {
            return "mp_" + StickerManager.sanitize(entry.ip);
        }
        return "default";
    }
}
