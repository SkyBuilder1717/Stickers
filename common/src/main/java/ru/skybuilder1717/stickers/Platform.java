package ru.skybuilder1717.stickers;

import java.nio.file.Path;

public interface Platform {
    Path getConfigDir();

    static Platform getInstance() {
        return PlatformHolder.INSTANCE;
    }
}

class PlatformHolder {
    static final Platform INSTANCE = loadPlatform();

    private static Platform loadPlatform() {
        try {
            Class<?> neoforgeClass = Class.forName("ru.skybuilder1717.stickers.NeoForgePlatform");
            return (Platform) neoforgeClass.getDeclaredConstructor().newInstance();
        } catch (Exception _) {}

        try {
            Class<?> fabricClass = Class.forName("ru.skybuilder1717.stickers.FabricPlatform");
            return (Platform) fabricClass.getDeclaredConstructor().newInstance();
        } catch (Exception _) {}

        throw new IllegalStateException("No compatible Platform implementation found!");
    }
}
