package ru.skybuilder1717.stickers.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;
import ru.skybuilder1717.stickers.data.StickerData;
import ru.skybuilder1717.stickers.data.StickerManager;
import java.util.List;

public class StickersHud {
    static void extract(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.keyToggleGui.consumeClick()) return;

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        List<StickerData> list = StickerManager.getAll();
        String previewId = StickerManager.previewSticker != null ? StickerManager.previewSticker.id : null;

        for (StickerData s : list) {
            if (!s.visible) continue;
            if (previewId != null && previewId.equals(s.id)) continue;
            renderOne(ctx, mc, s, sw, sh);
        }

        if (StickerManager.previewSticker != null && StickerManager.previewSticker.visible) {
            renderOne(ctx, mc, StickerManager.previewSticker, sw, sh);
        }
    }

    private static void renderOne(GuiGraphicsExtractor ctx, Minecraft mc, StickerData s, int sw, int sh) {
        int px = (int) (s.x * sw);
        int py = (int) (s.y * sh);

        Matrix3x2f saved = new Matrix3x2f(ctx.pose());

        if (s.rotation != 0f) {
            float cx = px + s.width  / 2f;
            float cy = py + s.height / 2f;
            ctx.pose().translate(cx, cy);
            ctx.pose().rotate((float) Math.toRadians(s.rotation));
            ctx.pose().translate(-s.width / 2f, -s.height / 2f);
        } else {
            ctx.pose().translate(px, py);
        }
        ctx.fill(0, 0, s.width, s.height, s.bgColor);

        drawText(ctx, mc, s);

        if (s.borderWidth > 0) {
            int bw = s.borderWidth;
            int bc = s.borderColor;
            int w = s.width;
            int h = s.height;
            if (s.borderOuter) {
                ctx.fill(-bw, -bw, w + bw, 0, bc);
                ctx.fill(-bw, h, w + bw, h + bw, bc);
                ctx.fill(-bw, 0, 0, h, bc);
                ctx.fill(w, 0, w + bw, h, bc);
            } else {
                ctx.fill(0, 0, w, bw, bc);
                ctx.fill(0, h - bw, w, h, bc);
                ctx.fill(0, bw, bw, h - bw, bc);
                ctx.fill(w - bw, bw, w, h - bw, bc);
            }
        }

        ctx.pose().set(saved);
    }

    private static void drawText(GuiGraphicsExtractor ctx, Minecraft mc, StickerData s) {
        String[] lines = s.text.replace("\\n", "\n").split("\n");

        int fontH = mc.font.lineHeight;
        int lineH = (fontH + 2) * s.fontScale;
        int totalH = lines.length * lineH;
        int startY = (s.height - totalH) / 2;

        for (String line : lines) {
            int tw = mc.font.width(line) * s.fontScale;
            int startX = (s.width - tw) / 2;

            if (s.fontScale != 1) {
                Matrix3x2f savedInner = new Matrix3x2f(ctx.pose());
                ctx.pose().translate(startX, startY);
                ctx.pose().scale(s.fontScale, s.fontScale);
                ctx.text(mc.font, line, 0, 0, s.textColor, false);
                ctx.pose().set(savedInner);
            } else {
                ctx.text(mc.font, line, startX, startY, s.textColor, false);
            }
            startY += lineH;
        }
    }
}
