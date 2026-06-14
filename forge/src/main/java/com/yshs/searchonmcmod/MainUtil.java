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
     * 浏览器 User-Agent
     */
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

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
     * @return 物品的 MCMOD ID，如果返回 0 表示物品不存在
     */
    @SneakyThrows
    public static String fetchItemMCMODID(@NonNull String registryName) {
        String urlStr = String.format(FETCH_ITEM_ID_URL, registryName);
        log.info("通过百科API获取物品 ID: {}", urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        // 设置连接超时为5秒
        connection.setConnectTimeout(5000);
        // 设置读取超时为5秒
        connection.setReadTimeout(5000);

        try {
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new Exception("获取物品 ID 失败，HTTP 状态码: " + responseCode);
            }
            @Cleanup BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = in.readLine();
            String mcmodItemID = response == null ? "" : response.trim();
            if (!mcmodItemID.matches("\\d+")) {
                throw new Exception("获取物品 ID 失败，返回内容不是纯数字: " + mcmodItemID);
            }
            log.info("获取物品 MCMOD ID 成功: {}", mcmodItemID);
            return mcmodItemID;
        } finally {
            connection.disconnect();
        }
    }
}
