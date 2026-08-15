package ru.skybuilder1717.stickers;

import net.neoforged.fml.loading.FMLPaths;
import java.nio.file.Path;

public class NeoForgePlatform implements Platform {

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
