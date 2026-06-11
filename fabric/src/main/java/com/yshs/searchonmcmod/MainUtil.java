package com.yshs.searchonmcmod;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.Util;

import java.net.URLEncoder;

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
    @SneakyThrows
    public static void openSearchPage(@NonNull String name) {
        String encode = URLEncoder.encode(name, "UTF-8");
        String url = String.format(SEARCH_PAGE_URL, encode);
        log.info("打开MC百科搜索页面: {}", url);
        Util.getPlatform().openUri(url);
    }

    /**
     * 将物品描述ID转换为注册表名
     *
     * @param descriptionId 物品描述ID
     * @return 注册表名
     */
    public static String convertDescriptionIdToRegistryName(@NonNull String descriptionId) {
        // 将输入字符串按"."分割
        String[] parts = descriptionId.split("\\.");

        // 返回格式化后的字符串
        if (parts.length >= 3) {
            return parts[1] + ":" + parts[2];
        } else {
            // 如果格式不符合预期，返回空字符串
            return "";
        }
    }
}
