package com.yshs.searchonmcmod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

/**
 * 按键绑定
 */
public class KeyBindings {

    private static final String KEY_CATEGORY = "key.categories.searchonmcmod";

    /**
     * 搜索MC百科按键
     */
    public static final KeyMapping SEARCH_ON_MCMOD_KEY = new KeyMapping(
            "key.searchonmcmod.search_on_mcmod",
            KeyConflictContext.GUI,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            KEY_CATEGORY
    );

    /**
     * 复制鼠标指向物品名称按键
     */
    public static final KeyMapping COPY_ITEM_NAME_KEY = new KeyMapping(
            "key.searchonmcmod.copy_item_name",
            KeyConflictContext.GUI,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            KEY_CATEGORY
    );

}
