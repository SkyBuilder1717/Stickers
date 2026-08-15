package ru.skybuilder1717.stickers.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NonNull;
import ru.skybuilder1717.stickers.data.StickerData;
import ru.skybuilder1717.stickers.data.StickerManager;

import java.util.List;

public class StickersScreen extends Screen {
    private static final int ITEM_H = 22;
    private static final int LIST_PAD = 4;
    private static final int LIST_W = 310;
    private static final int LIST_SWATCH_SIZE = 14;

    private int selectedIndex = -1;
    private int scrollOffset  = 0;

    private Button editBtn;
    private Button deleteBtn;
    private Button toggleBtn;

    public StickersScreen() {
        super(Component.translatable("screen.stickers.title"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int listX = cx - LIST_W / 2;
        int row1Y = this.height - 50;
        int row2Y = this.height - 26;
        int btnW = 58;

        this.addRenderableWidget(
            Button.builder(Component.translatable("btn.stickers.new"), _ -> {
                StickerData s = new StickerData();
                s.x = 0.4f; s.y = 0.4f;
                this.minecraft.setScreenAndShow(new StickerEditScreen(this, s, true));
            }).bounds(listX, row1Y, btnW, 20).build()
        );

        editBtn = this.addRenderableWidget(
            Button.builder(Component.translatable("btn.stickers.edit"), _ -> openEdit())
                .bounds(listX + 62, row1Y, btnW, 20).build()
        );

        toggleBtn = this.addRenderableWidget(
            Button.builder(Component.translatable("btn.stickers.toggle"), _ -> toggleVisible())
                .bounds(listX + 124, row1Y, btnW, 20).build()
        );

        deleteBtn = this.addRenderableWidget(
            Button.builder(Component.translatable("btn.stickers.delete"), _ -> deleteSelected())
                .bounds(listX + 186, row1Y, btnW, 20).build()
        );

        this.addRenderableWidget(
            Button.builder(Component.translatable("btn.stickers.close"), _ -> this.onClose())
                .bounds(listX + 248, row1Y, 60, 20).build()
        );

        Button dragMode = Button.builder(Component.translatable("btn.stickers.move_mode"), _ ->
                this.minecraft.setScreenAndShow(new StickerDragScreen())
        ).bounds(listX, row2Y, LIST_W, 20).build();

        if (StickerManager.isEmpty()) {
            dragMode.active = false;
        }

        this.addRenderableWidget(dragMode);

        refreshButtons();
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        super.extractRenderState(ctx, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int listX = cx - LIST_W / 2;
        int listY = 30;
        int listH = this.height - 88;

        ctx.centeredText(this.font, this.title, cx, 10, 0xFFFFFFFF);

        ctx.fill(listX, listY, listX + LIST_W, listY + listH, 0x88000000);
        drawBorder(ctx, listX, listY, LIST_W, listH, 0xFF888888);

        List<StickerData> stickers = StickerManager.getAll();
        int visCount = (listH - LIST_PAD * 2) / ITEM_H;

        if (selectedIndex >= 0 && selectedIndex < stickers.size()) {
            if (selectedIndex < scrollOffset) {
                scrollOffset = selectedIndex;
            } else if (selectedIndex >= scrollOffset + visCount) {
                scrollOffset = Math.max(0, selectedIndex - visCount + 1);
            }
        }

        int endIdx = Math.min(scrollOffset + visCount + 1, stickers.size());

        ctx.enableScissor(listX + 1, listY + 1, listX + LIST_W - 1, listY + listH - 1);

        for (int i = scrollOffset; i < endIdx; i++) {
            StickerData s = stickers.get(i);
            int itemY = listY + LIST_PAD + (i - scrollOffset) * ITEM_H;

            if (i == selectedIndex)
                ctx.fill(listX + 2, itemY, listX + LIST_W - 2, itemY + ITEM_H, 0x55AADDFF);

            int swatchX = listX + 4;
            int swatchY = itemY + (ITEM_H - LIST_SWATCH_SIZE) / 2;
            ctx.fill(swatchX, swatchY, swatchX + LIST_SWATCH_SIZE, swatchY + LIST_SWATCH_SIZE, opaqueVersion(s.bgColor));
            drawBorder(ctx, swatchX, swatchY, LIST_SWATCH_SIZE, LIST_SWATCH_SIZE, 0xFF666666);

            int visColor = s.visible ? 0xFF55FF55 : 0xFF888888;
            ctx.text(this.font, s.visible ? "+" : "-", listX + 23, itemY + (ITEM_H - 8) / 2, visColor, false);

            String preview = s.text.replace("\\n", " ").replace("\n", " ");
            if (preview.length() > 36) preview = preview.substring(0, 33) + "...";
            ctx.text(this.font, preview, listX + 34, itemY + (ITEM_H - 8) / 2, 0xFFFFFFFF, false);

            String sizeStr = s.width + "x" + s.height;
            int sizeW = this.font.width(sizeStr);
            ctx.text(this.font, sizeStr, listX + LIST_W - sizeW - 6, itemY + (ITEM_H - 8) / 2, 0xFFAAAAAA, false);
        }

        ctx.disableScissor();

        if (stickers.isEmpty())
            ctx.centeredText(this.font, Component.translatable("screen.stickers.empty"), cx, listY + listH / 2 - 4, 0xFF888888);

        if (stickers.size() > visCount) {
            int sbH = Math.max(20, listH * visCount / stickers.size());
            int sbY = listY + (listH - sbH) * scrollOffset / Math.max(1, stickers.size() - visCount);
            ctx.fill(listX + LIST_W - 4, sbY, listX + LIST_W - 1, sbY + sbH, 0xFFAAAAAA);
        }

        refreshButtons();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent p_446542_, boolean p_434187_) {
        int mouseX = (int) p_446542_.x();
        int mouseY = (int) p_446542_.y();

        int cx = this.width / 2;
        int listX = cx - LIST_W / 2;
        int listY = 30;
        int listH = this.height - 88;

        if (mouseX >= listX && mouseX < listX + LIST_W && mouseY >= listY && mouseY < listY + listH) {
            int relY = mouseY - listY - LIST_PAD;
            int clicked = scrollOffset + relY / ITEM_H;
            List<StickerData> stickers = StickerManager.getAll();
            if (clicked >= 0 && clicked < stickers.size()) {
                if (clicked == selectedIndex && p_446542_.button() == InputConstants.MOUSE_BUTTON_LEFT) openEdit();
                else selectedIndex = clicked;
                refreshButtons();
                return true;
            }
        }
        return super.mouseClicked(p_446542_, p_434187_);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int cx = this.width / 2;
        int listX = cx - LIST_W / 2;
        int listY = 30;
        int listH = this.height - 88;

        if (mouseX >= listX && mouseX < listX + LIST_W && mouseY >= listY && mouseY < listY + listH) {
            int visCount = (listH - LIST_PAD * 2) / ITEM_H;
            int maxScroll = Math.max(0, StickerManager.getAll().size() - visCount);
            scrollOffset = Math.clamp(maxScroll, 0, scrollOffset - (int) scrollY);
            return true;
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    public void onStickerSaved(boolean wasNew) {
        List<StickerData> list = StickerManager.getAll();
        if (wasNew && !list.isEmpty()) {
            selectedIndex = list.size() - 1;
        }
    }

    private void openEdit() {
        List<StickerData> list = StickerManager.getAll();
        if (selectedIndex >= 0 && selectedIndex < list.size()) {
            StickerData copy = list.get(selectedIndex).copy();
            this.minecraft.setScreenAndShow(new StickerEditScreen(this, copy, false));
        }
    }

    private void toggleVisible() {
        List<StickerData> list = StickerManager.getAll();
        if (selectedIndex >= 0 && selectedIndex < list.size()) {
            list.get(selectedIndex).visible = !list.get(selectedIndex).visible;
            StickerManager.save();
        }
    }

    private void deleteSelected() {
        List<StickerData> list = StickerManager.getAll();
        if (selectedIndex >= 0 && selectedIndex < list.size()) {
            StickerManager.remove(list.get(selectedIndex).id);
            int newSize = StickerManager.getAll().size();
            if (selectedIndex >= newSize) selectedIndex = newSize - 1;
            refreshButtons();
        }
    }

    private void refreshButtons() {
        boolean has = selectedIndex >= 0 && selectedIndex < StickerManager.getAll().size();
        if (editBtn != null) editBtn.active = has;
        if (deleteBtn != null) deleteBtn.active = has;
        if (toggleBtn != null) toggleBtn.active = has;
    }

    private static int opaqueVersion(int argb) { return (argb & 0x00FFFFFF) | 0xFF000000; }

    private static void drawBorder(@UnknownNullability GuiGraphicsExtractor ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }
}
