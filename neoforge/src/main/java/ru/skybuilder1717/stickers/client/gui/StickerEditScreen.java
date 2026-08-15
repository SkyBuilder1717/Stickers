package ru.skybuilder1717.stickers.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import ru.skybuilder1717.stickers.data.StickerData;
import ru.skybuilder1717.stickers.data.StickerManager;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StickerEditScreen extends Screen {
    private static final int PAD = 14;
    private static final int CH = 20;
    private static final int RG = 26;
    private static final int CG = 8;
    private static final int BG = 8;
    private static final int FH = 9;

    private final Screen parent;
    private final StickerData sticker;
    private final boolean isNew;

    private MultiLineEditBox textField;

    private IntSlider xSlider, ySlider, wSlider, hSlider, rotSlider;
    private IntSlider bgR, bgG, bgB, bgA;
    private IntSlider txR, txG, txB, txA;
    private IntSlider brW, brR, brG, brB, brA;
    private Button btn1x, btn2x, btn3x;
    private Button borderTypeBtn, syncBtn;
    private boolean syncBorderToBg = false;

    private int panelX, panelY, panelW, panelH;

    private final List<RightEntry> rightEntries = new ArrayList<>();
    private int rightScrollOff = 0;
    private int rightX, rightW, rightTopY, rightH, rightContentH;
    private int hw;

    private int colorLabelBaseY, bgHdrBaseY, borderLabelBaseY;

    private record RightEntry(AbstractWidget w, int baseY) {}

    public StickerEditScreen(Screen parent, StickerData sticker, boolean isNew) {
        super(Component.translatable("screen.stickers.edit"));
        this.parent = parent;
        this.sticker = sticker;
        this.isNew = isNew;
    }

    @Override
    protected void init() {
        StickerManager.previewSticker = sticker;
        rightEntries.clear();

        panelW = Math.min(this.width - 20, 700);
        panelH = Math.min(this.height - 20, 540);
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        int contentX = panelX + PAD;
        int contentW = panelW - PAD * 2;
        int colGap = 12;
        int leftW = Math.clamp((contentW - colGap) * 2L / 5, 160, 280);
        rightW = contentW - leftW - colGap;
        rightX = contentX + leftW + colGap;
        rightTopY = panelY + 20;
        int actionsY = panelY + panelH - PAD - CH + 10;
        rightH = actionsY - rightTopY - 18;
        hw = (rightW - CG) / 2;

        int fieldH = rightH;
        textField = MultiLineEditBox.builder().build(this.font, leftW, fieldH,
                Component.translatable("screen.stickers.edit"));
        textField.setX(contentX);
        textField.setY(rightTopY);
        textField.setCharacterLimit(2000);
        textField.setValue(sticker.text);
        textField.setValueListener(v -> sticker.text = v);
        this.addRenderableWidget(textField);
        this.setInitialFocus(textField);

        int rx = rightX + hw + CG;
        int ry = rightTopY;

        addRight(xSlider = new IntSlider(rightX, ry, hw, CH, "X", "%",
                0, 100, (int)(sticker.x * 100), v -> sticker.x = v / 100f));
        addRight(ySlider = new IntSlider(rx, ry, hw, CH, "Y", "%",
                0, 100, (int)(sticker.y * 100), v -> sticker.y = v / 100f));
        ry += RG;

        addRight(wSlider = new IntSlider(rightX, ry, hw, CH, tr("label.stickers.width"), tr("label.stickers.px"),
                20, 600, sticker.width,  v -> sticker.width  = v));
        addRight(hSlider = new IntSlider(rx, ry, hw, CH, tr("label.stickers.height"), tr("label.stickers.px"),
                10, 300, sticker.height, v -> sticker.height = v));
        ry += RG;

        addRight(rotSlider = new IntSlider(rightX, ry, rightW, CH, tr("label.stickers.rotation"), "°",
                0, 360, (int)sticker.rotation, v -> sticker.rotation = v));
        ry += RG;

        addRight(brW = new IntSlider(rightX, ry, rightW, CH, tr("label.stickers.border_width"), "px",
                0, 16, sticker.borderWidth, v -> sticker.borderWidth = v));
        ry += RG;

        addRight(borderTypeBtn = Button.builder(
                Component.translatable(sticker.borderOuter ? "btn.stickers.border_outer" : "btn.stickers.border_inner"),
                b -> {
                    sticker.borderOuter = !sticker.borderOuter;
                    b.setMessage(Component.translatable(sticker.borderOuter
                            ? "btn.stickers.border_outer" : "btn.stickers.border_inner"));
                }).bounds(rightX, ry, rightW, CH).build());
        ry += RG;

        int fbw = Math.clamp((rightW - BG * 2) / 3, 40, 60);
        addRight(btn1x = Button.builder(Component.literal("1x"),
                _ -> { sticker.fontScale = 1; refreshFontBtns(); })
                .bounds(rightX, ry, fbw, CH).build());
        addRight(btn2x = Button.builder(Component.literal("2x"),
                _ -> { sticker.fontScale = 2; refreshFontBtns(); })
                .bounds(rightX + fbw + BG, ry, fbw, CH).build());
        addRight(btn3x = Button.builder(Component.literal("3x"),
                _ -> { sticker.fontScale = 3; refreshFontBtns(); })
                .bounds(rightX + (fbw + BG) * 2, ry, fbw, CH).build());
        refreshFontBtns();
        ry += CH + 12;

        colorLabelBaseY = ry - rightTopY;
        ry += FH + 4;
        bgHdrBaseY = ry - rightTopY;
        ry += FH + 5;

        addRight(bgR = new IntSlider(rightX, ry, hw, CH, "R", 0, 255, getR(sticker.bgColor), v -> {
            sticker.bgColor = setR(sticker.bgColor, v);
            if (syncBorderToBg && brR != null) { sticker.borderColor = setR(sticker.borderColor, v); brR.setVal(v); }
        }));
        addRight(txR = new IntSlider(rx, ry, hw, CH, "R", 0, 255, getR(sticker.textColor),
                v -> sticker.textColor = setR(sticker.textColor, v)));
        ry += RG;

        addRight(bgG = new IntSlider(rightX, ry, hw, CH, "G", 0, 255, getG(sticker.bgColor), v -> {
            sticker.bgColor = setG(sticker.bgColor, v);
            if (syncBorderToBg && brG != null) { sticker.borderColor = setG(sticker.borderColor, v); brG.setVal(v); }
        }));
        addRight(txG = new IntSlider(rx, ry, hw, CH, "G", 0, 255, getG(sticker.textColor),
                v -> sticker.textColor = setG(sticker.textColor, v)));
        ry += RG;

        addRight(bgB = new IntSlider(rightX, ry, hw, CH, "B", 0, 255, getB(sticker.bgColor), v -> {
            sticker.bgColor = setB(sticker.bgColor, v);
            if (syncBorderToBg && brB != null) { sticker.borderColor = setB(sticker.borderColor, v); brB.setVal(v); }
        }));
        addRight(txB = new IntSlider(rx, ry, hw, CH, "B", 0, 255, getB(sticker.textColor),
                v -> sticker.textColor = setB(sticker.textColor, v)));
        ry += RG;

        addRight(bgA = new IntSlider(rightX, ry, hw, CH, "A", 0, 255, getA(sticker.bgColor), v -> {
            sticker.bgColor = setA(sticker.bgColor, v);
            if (syncBorderToBg && brA != null) { sticker.borderColor = setA(sticker.borderColor, v); brA.setVal(v); }
        }));
        addRight(txA = new IntSlider(rx, ry, hw, CH, "A", 0, 255, getA(sticker.textColor),
                v -> sticker.textColor = setA(sticker.textColor, v)));
        ry += RG;

        borderLabelBaseY = ry - rightTopY;
        ry += FH + 5;

        addRight(syncBtn = Button.builder(
                Component.translatable("btn.stickers.sync_off"),
                b -> {
                    syncBorderToBg = !syncBorderToBg;
                    b.setMessage(Component.translatable(syncBorderToBg ? "btn.stickers.sync_on" : "btn.stickers.sync_off"));
                    if (syncBorderToBg) syncBorderToBackground();
                }).bounds(rightX, ry, rightW, CH).build());
        ry += RG;

        addRight(brR = new IntSlider(rightX, ry, rightW, CH, "R", 0, 255, getR(sticker.borderColor),
                v -> sticker.borderColor = setR(sticker.borderColor, v)));
        ry += RG;
        addRight(brG = new IntSlider(rightX, ry, rightW, CH, "G", 0, 255, getG(sticker.borderColor),
                v -> sticker.borderColor = setG(sticker.borderColor, v)));
        ry += RG;
        addRight(brB = new IntSlider(rightX, ry, rightW, CH, "B", 0, 255, getB(sticker.borderColor),
                v -> sticker.borderColor = setB(sticker.borderColor, v)));
        ry += RG;
        addRight(brA = new IntSlider(rightX, ry, rightW, CH, "A", 0, 255, getA(sticker.borderColor),
                v -> sticker.borderColor = setA(sticker.borderColor, v)));
        ry += CH;

        rightContentH = ry - rightTopY;

        applyRightScroll();

        int bw = Math.min(160, (contentW - BG) / 2);
        int bx = contentX + (contentW - bw * 2 - BG) / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("btn.stickers.save"),
                _ -> save()).bounds(bx, actionsY, bw, CH).build());
        this.addRenderableWidget(Button.builder(Component.translatable("btn.stickers.cancel"),
                _ -> cancel()).bounds(bx + bw + BG, actionsY, bw, CH).build());
    }

    private void applyRightScroll() {
        int maxScroll = Math.max(0, rightContentH - rightH);
        rightScrollOff = Math.clamp(rightScrollOff, 0, maxScroll);
        for (RightEntry e : rightEntries) {
            int sy = rightTopY + e.baseY() - rightScrollOff;
            e.w().setY(sy);
            e.w().visible = sy + e.w().getHeight() > rightTopY && sy < rightTopY + rightH;
        }
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor ctx, int mouseX, int mouseY, float partialTick) {
        this.extractTransparentBackground(ctx);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xCC111111);
        drawBorder(ctx, panelX, panelY, panelW, panelH, 0xFF676767);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        super.extractRenderState(ctx, mouseX, mouseY, delta);

        ctx.text(this.font, this.title, panelX + panelW / 2, panelY + 6, 0xFFFFFFFF);

        ctx.enableScissor(rightX, rightTopY, rightX + rightW, rightTopY + rightH);

        int colorLabelY = rightTopY + colorLabelBaseY - rightScrollOff;
        int bgHdrY = rightTopY + bgHdrBaseY - rightScrollOff;
        int borderLabelY = rightTopY + borderLabelBaseY - rightScrollOff;

        ctx.text(this.font, Component.translatable("section.stickers.colors"), rightX, colorLabelY, 0xFFE0E0E0, false);

        renderColorHeader(ctx, Component.translatable("label.stickers.bgcolor"), rightX, bgHdrY, sticker.bgColor);
        renderColorHeader(ctx, Component.translatable("label.stickers.txtcolor"), rightX + hw + CG, bgHdrY, sticker.textColor);
        renderColorHeader(ctx, Component.translatable("label.stickers.bordercolor"), rightX, borderLabelY, sticker.borderColor);

        ctx.disableScissor();

        if (rightContentH > rightH) {
            int sbX = rightX + rightW + 4;
            int sbH = Math.max(16, rightH * rightH / rightContentH);
            int sbY = rightTopY + (rightH - sbH) * rightScrollOff / Math.max(1, rightContentH - rightH);
            ctx.fill(sbX, rightTopY, sbX + 3, rightTopY + rightH, 0x44FFFFFF);
            ctx.fill(sbX, sbY, sbX + 3, sbY + sbH, 0xFFAAAAAA);
        }
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (mx >= rightX && mx < rightX + rightW && my >= rightTopY && my < rightTopY + rightH) {
            rightScrollOff -= (int)(scrollY * RG);
            applyRightScroll();
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    @Override public void removed() { StickerManager.previewSticker = null; }
    @Override public boolean isPauseScreen() { return false; }

    private void save() {
        sticker.text = textField.getValue();
        if (isNew) StickerManager.add(sticker);
        else StickerManager.update(sticker);
        if (parent instanceof StickersScreen ss) ss.onStickerSaved(isNew);
        this.minecraft.setScreenAndShow(parent);
    }

    private void cancel() { this.minecraft.setScreenAndShow(parent); }

    private void syncBorderToBackground() {
        sticker.borderColor = sticker.bgColor;
        if (brR != null) brR.setVal(getR(sticker.bgColor));
        if (brG != null) brG.setVal(getG(sticker.bgColor));
        if (brB != null) brB.setVal(getB(sticker.bgColor));
        if (brA != null) brA.setVal(getA(sticker.bgColor));
    }

    private void refreshFontBtns() {
        if (btn1x != null) btn1x.active = sticker.fontScale != 1;
        if (btn2x != null) btn2x.active = sticker.fontScale != 2;
        if (btn3x != null) btn3x.active = sticker.fontScale != 3;
    }

    private <T extends AbstractWidget> T addRight(T w) {
        rightEntries.add(new RightEntry(w, w.getY() - rightTopY));
        return this.addRenderableWidget(w);
    }

    private String tr(String key) { return Component.translatable(key).getString(); }

    private void renderColorHeader(GuiGraphicsExtractor ctx, Component label, int x, int y, int color) {
        ctx.fill(x, y, x + 12, y + 12, opaqueVersion(color));
        drawBorder(ctx, x, y, 12, 12, 0xFF666666);
        ctx.text(this.font, label, x + 16, y + 2, 0xFFCFCFCF, false);
    }

    private static void drawBorder(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int c) {
        ctx.fill(x, y, x + w, y + 1, c);
        ctx.fill(x, y + h - 1, x + w, y + h, c);
        ctx.fill(x, y, x + 1, y + h, c);
        ctx.fill(x + w - 1, y, x + w, y + h, c);
    }

    private static int opaqueVersion(int c) { return (c & 0x00FFFFFF) | 0xFF000000; }
    private static int getA(int c) { return (c >> 24) & 0xFF; }
    private static int getR(int c) { return (c >> 16) & 0xFF; }
    private static int getG(int c) { return (c >> 8) & 0xFF; }
    private static int getB(int c) { return c & 0xFF; }
    private static int setA(int c, int v) { return (c & 0x00FFFFFF) | ((v & 0xFF) << 24); }
    private static int setR(int c, int v) { return (c & 0xFF00FFFF) | ((v & 0xFF) << 16); }
    private static int setG(int c, int v) { return (c & 0xFFFF00FF) | ((v & 0xFF) <<  8); }
    private static int setB(int c, int v) { return (c & 0xFFFFFF00) | (v & 0xFF); }

    private static class IntSlider extends AbstractSliderButton {
        private final String label;
        private final String suffix;
        private final int min, max;
        private final Consumer<Integer> onChange;

        IntSlider(int x, int y, int w, int h, String label, String suffix, int min, int max, int init, Consumer<Integer> onChange) {
            super(x, y, w, h, Component.literal(label + ": " + init + suffix), (double)(init - min) / (max - min));
            this.label = label;
            this.suffix = suffix;
            this.min = min;
            this.max = max;
            this.onChange = onChange;
        }

        IntSlider(int x, int y, int w, int h, String label, int min, int max,
                  int init, Consumer<Integer> onChange) {
            this(x, y, w, h, label, "", min, max, init, onChange);
        }

        private int getVal() { return min + (int)Math.round(value * (max - min)); }

        void setVal(int v) {
            value = (double)(v - min) / (max - min);
            updateMessage();
            applyValue();
        }

        @Override protected void updateMessage() { setMessage(Component.literal(label + ": " + getVal() + suffix)); }
        @Override protected void applyValue() { onChange.accept(getVal()); }
    }
}
