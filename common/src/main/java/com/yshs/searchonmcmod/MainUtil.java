package com.yshs.searchonmcmod;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
    public static void openSearchPage(@NonNull String name) {
        String encode = URLEncoder.encode(name, StandardCharsets.UTF_8);
        String url = String.format(SEARCH_PAGE_URL, encode);
        log.info("打开MC百科搜索页面: {}", url);
        Util.getPlatform().openUri(url);
    }

    /**
     * @param id 物品 ID
     * @return 物品页面是否存在 true: 存在 false: 不存在
     * @throws IOException 当 HTTP 请求失败或返回非预期状态码时抛出
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean itemPageExist(@NonNull String id) throws IOException {
        String urlStr = String.format(ITEM_PAGE_URL, id);
        log.info("检查MC百科物品页面是否存在: {}", urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // 设置连接超时为5秒
        connection.setConnectTimeout(5000);
        // 设置读取超时为5秒
        connection.setReadTimeout(5000);
        try {
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                return true;
            }
            if (responseCode == 404) {
                return false;
            }
            throw new IOException("检查物品页面失败，HTTP状态码: " + responseCode);
        } finally {
            connection.disconnect();
        }
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
     * @throws IOException 当 HTTP 请求失败或返回内容无效时抛出
     */
    public static String fetchItemMCMODID(@NonNull String registryName) throws IOException {
        String urlStr = String.format(FETCH_ITEM_ID_URL, registryName);
        log.info("通过百科API获取物品 ID: {}", urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        // 设置连接超时为5秒
        connection.setConnectTimeout(5000);
        // 设置读取超时为5秒
        connection.setReadTimeout(5000);

        try {
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("通过百科 API 获取物品 ID 失败，HTTP状态码: " + responseCode);
            }
            @Cleanup BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String mcmodItemID = in.readLine();
            if (mcmodItemID == null) {
                throw new IOException("通过百科 API 获取物品 ID 失败，返回内容为空");
            }
            log.info("获取物品 MCMOD ID 成功: {}", mcmodItemID);
            return mcmodItemID;
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 将物品描述 ID 转换为注册表名
     *
     * @param descriptionId 物品描述 ID
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
