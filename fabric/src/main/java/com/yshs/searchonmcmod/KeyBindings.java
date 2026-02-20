package com.yshs.searchonmcmod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * 按键绑定类
 */
public class KeyBindings {

    private static final String KEY_CATEGORY = "key.categories.searchonmcmod";

    /**
     * 添加按键绑定
     */
    public static final KeyMapping SEARCH_ON_MCMOD_KEY = new KeyMapping(
            "key.searchonmcmod.search_on_mcmod",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            KEY_CATEGORY
    );

    /**
     * 复制鼠标指向物品名称按键
     */
    public static final KeyMapping COPY_ITEM_NAME_KEY = new KeyMapping(
            "key.searchonmcmod.copy_item_name",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            KEY_CATEGORY
    );

}
