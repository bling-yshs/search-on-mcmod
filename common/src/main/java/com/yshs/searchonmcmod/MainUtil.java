package com.yshs.searchonmcmod;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.Util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 通用工具类
 */
@Slf4j
public class MainUtil {
    /**
     * 搜索页面 URL
     */
    private static final String SEARCH_PAGE_URL = "https://search.mcmod.cn/s?key=%s";

    /**
     * 打开搜索页面
     *
     * @param name 物品名称
     */
    public static void openSearchPage(@NonNull String name) {
        String encode = URLEncoder.encode(name, StandardCharsets.UTF_8);
        String url = String.format(SEARCH_PAGE_URL, encode);
        log.info("打开MC百科搜索页面: {}", url);
        Util.getPlatform().openUri(url);
    }
}
