package com.yshs.searchonmcmod;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class KeyBindings {

    private static final String KEY_CATEGORY = "key.categories.searchonmcmod";

    public static final KeyBinding SEARCH_ON_MCMOD_KEY = new KeyBinding(
            "key.searchonmcmod.search_on_mcmod",
            Keyboard.KEY_B,
            KEY_CATEGORY
    );

}
