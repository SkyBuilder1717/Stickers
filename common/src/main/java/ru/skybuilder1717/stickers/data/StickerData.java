package ru.skybuilder1717.stickers.data;

import java.util.UUID;

public class StickerData {
    public String id;
    public String text;
    public float x;
    public float y;
    public int width;
    public int height;
    public int bgColor;
    public int textColor;
    public float rotation;
    public boolean visible;
    public int fontScale;
    public int borderWidth;
    public int borderColor;
    public boolean borderOuter;

    public StickerData() {
        this.id = UUID.randomUUID().toString();
        this.text = "Sticker";
        this.x = 0.05f;
        this.y = 0.05f;
        this.width = 120;
        this.height = 50;
        this.bgColor = 0xCCFFEE00;
        this.textColor = 0xFF000000;
        this.rotation = 0f;
        this.visible = true;
        this.fontScale = 1;
        this.borderWidth = 0;
        this.borderColor = 0xFF000000;
        this.borderOuter = false;
    }

    public StickerData copy() {
        StickerData c = new StickerData();
        c.id = this.id;
        c.text = this.text;
        c.x = this.x;
        c.y = this.y;
        c.width = this.width;
        c.height = this.height;
        c.bgColor = this.bgColor;
        c.textColor = this.textColor;
        c.rotation = this.rotation;
        c.visible = this.visible;
        c.fontScale = this.fontScale;
        c.borderWidth = this.borderWidth;
        c.borderColor = this.borderColor;
        c.borderOuter = this.borderOuter;
        return c;
    }
}
