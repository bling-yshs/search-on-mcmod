package com.yshs.searchonmcmod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;

import static com.yshs.searchonmcmod.KeyBindingConstants.CATEGORY;
import static com.yshs.searchonmcmod.KeyBindingConstants.SEARCH_DEFAULT_KEY_CODE;
import static com.yshs.searchonmcmod.KeyBindingConstants.SEARCH_KEY_NAME;
import static com.yshs.searchonmcmod.SearchOnMcmod.MOD_ID;

/**
 * 按键绑定类
 */
public class KeyBindings {

    public static final KeyMapping.Category KEY_CATEGORY =
            new KeyMapping.Category(ResourceLocation.fromNamespaceAndPath(MOD_ID, CATEGORY));

    public static final KeyMapping SEARCH_ON_MCMOD_KEY = new KeyMapping(
            SEARCH_KEY_NAME,
            InputConstants.Type.KEYSYM,
            SEARCH_DEFAULT_KEY_CODE,
            KEY_CATEGORY
    );

}
