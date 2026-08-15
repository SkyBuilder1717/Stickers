package ru.skybuilder1717.stickers;

import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Path;

public class FabricPlatform implements Platform {

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
