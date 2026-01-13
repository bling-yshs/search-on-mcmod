package com.yshs.searchonmcmod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import static com.yshs.searchonmcmod.SearchOnMcmod.MOD_ID;

/**
 * 按键绑定类
 */
public class KeyBindings {

    public static final KeyMapping.Category KEY_CATEGORY =
            new KeyMapping.Category(ResourceLocation.fromNamespaceAndPath(MOD_ID, "categories"));

    public static final KeyMapping SEARCH_ON_MCMOD_KEY = new KeyMapping(
            "key.searchonmcmod.search_on_mcmod",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            KEY_CATEGORY
    );

}
