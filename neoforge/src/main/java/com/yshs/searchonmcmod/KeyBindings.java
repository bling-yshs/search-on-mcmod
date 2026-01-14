package com.yshs.searchonmcmod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import static com.yshs.searchonmcmod.KeyBindingConstants.*;
import static com.yshs.searchonmcmod.SearchOnMcmod.MOD_ID;

public class KeyBindings {

    public static final KeyMapping.Category SEARCHONMCMOD_CATEGORY =
            new KeyMapping.Category(ResourceLocation.fromNamespaceAndPath(MOD_ID, CATEGORY));

    public static final KeyMapping SEARCH_ON_MCMOD_KEY = new KeyMapping(
            KEY_NAME,
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            DEFAULT_KEY_CODE,
            SEARCHONMCMOD_CATEGORY
    );
}
