package ru.skybuilder1717.stickers.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.NonNull;
import ru.skybuilder1717.stickers.data.StickerData;
import ru.skybuilder1717.stickers.data.StickerManager;
import java.util.List;

public class StickerDragScreen extends Screen {
    private StickerData dragging = null;
    private float dragOffX = 0f;
    private float dragOffY = 0f;

    public StickerDragScreen() {
        super(Component.empty());
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {}

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        super.extractRenderState(ctx, mouseX, mouseY, delta);

        int sw = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int sh = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        List<StickerData> stickers = StickerManager.getAll();
        for (StickerData s : stickers) {
            if (!s.visible) continue;

            int px = (int)(s.x * sw);
            int py = (int)(s.y * sh);

            float cx = px + s.width / 2f;
            float cy = py + s.height / 2f;

            double dx = mouseX - cx;
            double dy = mouseY - cy;

            double rad = Math.toRadians(-s.rotation);

            double rx = dx * Math.cos(rad) - dy * Math.sin(rad);
            double ry = dx * Math.sin(rad) + dy * Math.cos(rad);

            double localX = rx + s.width / 2f;
            double localY = ry + s.height / 2f;

            boolean isHovered = localX >= 0 && localX < s.width && localY >= 0 && localY < s.height;

            boolean isDragging = dragging != null && dragging.id.equals(s.id);

            Matrix3x2f saved = new Matrix3x2f(ctx.pose());
            ctx.pose().translate(cx, cy);
            if (s.rotation != 0f) {
                ctx.pose().rotate((float)Math.toRadians(s.rotation));
            }
            ctx.pose().translate(-s.width / 2f, -s.height / 2f);

            if (isDragging) {
                drawBorder(ctx,
                        -2, -2,
                        s.width + 4,
                        s.height + 4,
                        0xFFFFFFFF,
                        2);
                ctx.fill(
                        0,
                        0,
                        s.width,
                        s.height,
                        0x33FFFFFF
                );
            } else if (isHovered) {
                drawBorder(ctx,
                        -1, -1,
                        s.width + 2,
                        s.height + 2,
                        0xFFFFEE00,
                        2);

            } else {
                drawBorder(ctx,
                        0,
                        0,
                        s.width,
                        s.height,
                        0x88FFFFFF,
                        1);
            }
            ctx.pose().set(saved);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int sw = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int sh = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            double mx = event.x();
            double my = event.y();

            List<StickerData> stickers = StickerManager.getAll();
            for (int i = stickers.size() - 1; i >= 0; i--) {
                StickerData s = stickers.get(i);
                if (!s.visible) continue;
                int px = (int)(s.x * sw);
                int py = (int)(s.y * sh);
                if (mx >= px && mx < px + s.width && my >= py && my < py + s.height) {
                    dragging = s;
                    dragOffX = (float)(mx - px);
                    dragOffY = (float)(my - py);
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (event.button() == 0 && dragging != null) {

            int sw = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int sh = Minecraft.getInstance().getWindow().getGuiScaledHeight();

            float newX = (float)((mouseX - dragOffX) / sw);
            float newY = (float)((mouseY - dragOffY) / sh);

            newX = Math.clamp(newX, 0f, 1f - (float) dragging.width / sw);
            newY = Math.clamp(newY, 0f, 1f - (float) dragging.height / sh);

            dragging.x = newX;
            dragging.y = newY;

            return true;
        }
        return false;
    }

    private double mouseX, mouseY;
    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && dragging != null) {
            StickerManager.save();
            dragging = null;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static void drawBorder(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int color, int thickness) {
        for (int t = 0; t < thickness; t++) {
            ctx.fill(x - t, y - t, x + w + t, y - t + 1, color);
            ctx.fill(x - t, y + h + t - 1, x + w + t, y + h + t, color);
            ctx.fill(x - t, y - t + 1, x - t + 1, y + h + t - 1, color);
            ctx.fill(x + w + t - 1, y - t + 1, x + w + t, y + h + t - 1, color);
        }
    }
}
