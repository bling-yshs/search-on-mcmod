package com.yshs.searchonmcmod;

import net.minecraft.util.Util;

public class MainUtil {
    public static final String SEARCH_PAGE_URL = "https://search.mcmod.cn/s?key=%s";

    public static void openSearchPage(String name) {
        Util.getPlatform().openUri(String.format(SEARCH_PAGE_URL, name));
    }
}
