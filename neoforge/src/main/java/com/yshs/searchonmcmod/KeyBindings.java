package com.yshs.searchonmcmod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

import static com.yshs.searchonmcmod.KeyBindingConstants.CATEGORY;
import static com.yshs.searchonmcmod.KeyBindingConstants.COPY_ITEM_NAME_DEFAULT_KEY_CODE;
import static com.yshs.searchonmcmod.KeyBindingConstants.COPY_ITEM_NAME_KEY_NAME;
import static com.yshs.searchonmcmod.KeyBindingConstants.SEARCH_DEFAULT_KEY_CODE;
import static com.yshs.searchonmcmod.KeyBindingConstants.SEARCH_KEY_NAME;
import static com.yshs.searchonmcmod.SearchOnMcmod.MOD_ID;

public class KeyBindings {

    public static final KeyMapping.Category SEARCHONMCMOD_CATEGORY =
            new KeyMapping.Category(ResourceLocation.fromNamespaceAndPath(MOD_ID, CATEGORY));

    /**
     * 搜索 MC 百科按键
     */
    public static final KeyMapping SEARCH_ON_MCMOD_KEY = new KeyMapping(
            SEARCH_KEY_NAME,
            KeyConflictContext.GUI,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            SEARCH_DEFAULT_KEY_CODE,
            SEARCHONMCMOD_CATEGORY
    );

    /**
     * 复制鼠标指向物品名称按键
     */
    public static final KeyMapping COPY_ITEM_NAME_KEY = new KeyMapping(
            COPY_ITEM_NAME_KEY_NAME,
            KeyConflictContext.GUI,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            COPY_ITEM_NAME_DEFAULT_KEY_CODE,
            SEARCHONMCMOD_CATEGORY
    );
}
