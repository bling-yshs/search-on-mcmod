package com.yshs.searchonmcmod;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 通用工具类
 */
public class MainUtil {

    private static final Logger LOG = LogManager.getLogger(SearchOnMcmod.MODID);

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
     * User-Agent 请求头名称
     */
    private static final String USER_AGENT_HEADER = "User-Agent";
    /**
     * 浏览器风格 User-Agent
     */
    private static final String BROWSER_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    /**
     * 打开搜索页面
     *
     * @param name 物品名称
     */
    public static void openSearchPage(String name) {
        if (name == null || name.isEmpty()) {
            LOG.warn("物品名称为空，无法打开搜索页面");
            return;
        }
        try {
            String encode = URLEncoder.encode(name, "UTF-8");
            String url = String.format(SEARCH_PAGE_URL, encode);
            LOG.info("打开MC百科搜索页面: {}", url);
            openUri(url);
        } catch (Exception e) {
            LOG.error("打开搜索页面失败", e);
        }
    }

    /**
     * 打开物品页面
     *
     * @param id 物品 ID
     */
    public static void openItemPage(String id) {
        if (id == null || id.isEmpty()) {
            LOG.warn("物品 ID 为空，无法打开物品页面");
            return;
        }
        try {
            String url = String.format(ITEM_PAGE_URL, id);
            LOG.info("打开MC百科物品页面: {}", url);
            openUri(url);
        } catch (Exception e) {
            LOG.error("打开物品页面失败", e);
        }
    }

    /**
     * 通过百科 API 获取物品 MCMOD ID
     *
     * @param registryName 物品注册名
     * @return 物品的 MCMOD ID
     * @throws IOException 当 HTTP 请求失败或返回内容无效时抛出
     */
    public static String fetchItemMCMODID(String registryName) throws IOException {
        if (registryName == null || registryName.isEmpty()) {
            throw new IOException("物品注册名为空");
        }
        String encode = URLEncoder.encode(registryName, "UTF-8");
        String urlStr = String.format(FETCH_ITEM_ID_URL, encode);
        LOG.info("通过百科API获取物品 ID: {}", urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        setBrowserUserAgent(connection);
        // 设置连接超时为5秒
        connection.setConnectTimeout(5000);
        // 设置读取超时为5秒
        connection.setReadTimeout(5000);

        BufferedReader in = null;
        try {
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("通过百科 API 获取物品 ID 失败，HTTP状态码: " + responseCode);
            }
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String mcmodItemID = in.readLine();
            if (mcmodItemID == null) {
                throw new IOException("通过百科 API 获取物品 ID 失败，返回内容为空");
            }
            mcmodItemID = mcmodItemID.trim();
            if (!isValidMcmodItemID(mcmodItemID)) {
                throw new IOException("通过百科 API 获取物品 ID 失败，返回内容不是有效数字 ID: " + mcmodItemID);
            }
            LOG.info("获取物品 MCMOD ID 成功: {}", mcmodItemID);
            return mcmodItemID;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.warn("关闭输入流失败", e);
                }
            }
            connection.disconnect();
        }
    }

    /**
     * 设置浏览器风格 User-Agent
     *
     * @param connection HTTP 连接
     */
    private static void setBrowserUserAgent(HttpURLConnection connection) {
        if (connection != null) {
            connection.setRequestProperty(USER_AGENT_HEADER, BROWSER_USER_AGENT);
        }
    }

    /**
     * 判断 MC百科物品 ID 是否有效
     *
     * @param mcmodItemID MC百科物品 ID
     * @return 是否为有效数字 ID
     */
    private static boolean isValidMcmodItemID(String mcmodItemID) {
        return mcmodItemID != null && mcmodItemID.matches("\\d+");
    }

    /**
     * 打开 URI（使用 Desktop API）
     *
     * @param uriStr URI 字符串
     */
    private static void openUri(String uriStr) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(uriStr));
                    return;
                }
            }
            LOG.error("当前系统不支持打开浏览器");
        } catch (Exception e) {
            LOG.error("打开浏览器失败", e);
        }
    }
}
