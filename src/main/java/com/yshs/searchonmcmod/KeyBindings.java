package com.yshs.searchonmcmod;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

import static net.minecraft.client.util.InputMappings.Type.KEYSYM;

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
            KeyConflictContext.GUI,
            KEYSYM,
            GLFW.GLFW_KEY_B,
            KEY_CATEGORY
    );

}
