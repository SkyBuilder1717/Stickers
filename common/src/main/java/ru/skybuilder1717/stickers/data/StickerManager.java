package ru.skybuilder1717.stickers.data;

import com.google.gson.*;
import ru.skybuilder1717.stickers.Platform;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StickerManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static List<StickerData> stickers  = new ArrayList<>();
    private static String currentKey = "default";
    public static StickerData previewSticker = null;

    public static Path getSaveFile(String key) {
        Path dir = Platform.getInstance().getConfigDir().resolve("stickers");
        try { Files.createDirectories(dir); } catch (IOException ignored) {}
        return dir.resolve(key + ".json");
    }

    public static String sanitize(String s) {
        if (s == null) return "unknown";
        return s.replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }

    public static void load(String key) {
        currentKey = key;
        Path file  = getSaveFile(key);
        if (Files.exists(file)) {
            try (FileReader r = new FileReader(file.toFile())) {
                JsonArray arr = GSON.fromJson(r, JsonArray.class);
                stickers = new ArrayList<>();
                if (arr != null) {
                    for (JsonElement el : arr) {
                        StickerData sd = GSON.fromJson(el, StickerData.class);
                        if (sd != null) stickers.add(sd);
                    }
                }
            } catch (Exception e) {
                stickers = new ArrayList<>();
            }
        } else {
            stickers = new ArrayList<>();
        }
    }

    public static void save() {
        Path file = getSaveFile(currentKey);
        try (FileWriter w = new FileWriter(file.toFile())) {
            GSON.toJson(stickers, w);
        } catch (IOException ignored) {}
    }

    public static List<StickerData> getAll() { return stickers; }

    public static boolean isEmpty() { return stickers.isEmpty(); }

    public static void add(StickerData s) {
        stickers.add(s);
        save();
    }

    public static void remove(String id) {
        stickers.removeIf(s -> id.equals(s.id));
        save();
    }

    public static void update(StickerData updated) {
        for (int i = 0; i < stickers.size(); i++) {
            if (updated.id.equals(stickers.get(i).id)) {
                stickers.set(i, updated);
                break;
            }
        }
        save();
    }
}
