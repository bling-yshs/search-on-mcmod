package com.yshs.searchonmcmod;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.Util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Optional;

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
     * 物品页面 URL
     */
    private static final String ITEM_PAGE_URL = "https://www.mcmod.cn/item/%s.html";

    /**
     * 获取物品 ID URL
     */
    private static final String FETCH_ITEM_ID_URL = "https://api.mcmod.cn/getItem/?regname=%s";

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
     * 打开物品页面
     *
     * @param id 物品 ID
     */
    public static void openItemPage(@NonNull String id) {
        String url = String.format(ITEM_PAGE_URL, id);
        log.info("打开MC百科物品页面: {}", url);
        Util.getPlatform().openUri(url);
    }

    /**
     * 通过百科 API 获取物品 MCMOD ID
     *
     * @param registryName 物品注册名
     * @return 物品的 MCMOD ID
     */
    @SneakyThrows
    public static Optional<String> fetchItemMCMODID(@NonNull String registryName) {
        String urlStr = String.format(FETCH_ITEM_ID_URL, registryName);
        log.info("通过百科API获取物品 ID: {}", urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            log.error("获取物品 ID 失败: {}", responseCode);
            return Optional.empty();
        }
        @Cleanup BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String mcmodItemID = in.readLine();
        connection.disconnect();
        log.info("获取物品 MCMOD ID 成功: {}", mcmodItemID);
        return Optional.of(mcmodItemID);
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
        if (parts.length >= 2) {
            return parts[1] + ":" + parts[2];
        } else {
            // 如果格式不符合预期，返回空字符串
            return "";
        }
    }
}
