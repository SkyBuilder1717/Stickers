package ru.skybuilder1717.stickers.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import ru.skybuilder1717.stickers.Stickers;
import ru.skybuilder1717.stickers.data.StickerManager;
import ru.skybuilder1717.stickers.client.gui.StickerDragScreen;
import ru.skybuilder1717.stickers.client.gui.StickersScreen;

public class StickersClient implements ClientModInitializer {
    public static KeyMapping OPEN_KEY;
    public static KeyMapping DRAG_KEY;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = new KeyMapping.Category(Identifier.fromNamespaceAndPath("stickers", "category"));

        OPEN_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.stickers.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                category
        ));

        DRAG_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.stickers.drag",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                category
        ));

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath("stickers", "before_chat"),
                StickersHud::extract
        );

        ClientPlayConnectionEvents.JOIN.register((_, _, _) -> StickerManager.load(computeKey(isLegacy())));

        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> StickerManager.save());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (OPEN_KEY.consumeClick() && client.gui.screen() == null) {
                client.gui.setScreen(new StickersScreen());
            }
            if (DRAG_KEY.consumeClick() && client.gui.screen() == null && !StickerManager.getAll().isEmpty()) {
                client.gui.setScreen(new StickerDragScreen());
            }
        });
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
                Stickers.LOGGER.info("[Stickers] Found legacy config! Replacing...");
                Path legacyPath = StickerManager.getSaveFile("sp_" + StickerManager.sanitize(levelName));
                try {
                    Files.write(legacyPath, Files.readString(legacyPath).getBytes());
                    try {
                        Files.delete(legacyPath);
                        Stickers.LOGGER.info("[Stickers] Legacy config migrated successfully.");
                    } catch (IOException e) {
                        Stickers.LOGGER.error("[Stickers] Failed to delete legacy config: {}", e.getMessage());
                    }
                } catch (IOException e) {
                    Stickers.LOGGER.error("[Stickers] Failed to migrate legacy config: {}", e.getMessage());
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