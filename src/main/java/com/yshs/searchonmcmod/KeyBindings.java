package com.yshs.searchonmcmod;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

/**
 * 按键绑定
 */
public class KeyBindings {

    private static final String KEY_CATEGORY = "key.categories.searchonmcmod";

    /**
     * 搜索MC百科按键
     */
    public static final KeyBinding SEARCH_ON_MCMOD_KEY = new KeyBinding(
        "key.searchonmcmod.search_on_mcmod",
        Keyboard.KEY_B,
        KEY_CATEGORY);

    /**
     * 复制鼠标指向物品名称按键
     */
    public static final KeyBinding COPY_ITEM_NAME_KEY = new KeyBinding(
        "key.searchonmcmod.copy_item_name",
        Keyboard.KEY_NONE,
        KEY_CATEGORY);
}
