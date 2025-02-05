package com.yshs.searchonmcmod;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Optional;

/**
 * 通用工具类
 */
public class MainUtil {
    private static final Logger log = LogManager.getLogger();
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
    private static final String FETCH_ITEM_ID_URL = "https://api.mcmod.cn/getItem/?regname=%s&metadata=%d";

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
        MainUtil.browse(url);
    }

    /**
     * 打开物品页面
     *
     * @param id 物品 ID
     */
    public static void openItemPage(@NonNull String id) {
        String url = String.format(ITEM_PAGE_URL, id);
        log.info("打开MC百科物品页面: {}", url);
        MainUtil.browse(url);
    }

    /**
     * 通过百科 API 获取物品 MCMOD ID
     *
     * @param registryName 物品注册名
     * @param metadata     物品元数据
     * @return 物品的 MCMOD ID
     */
    @SneakyThrows
    public static Optional<String> fetchItemMCMODID(@NonNull String registryName, int metadata) {
        String urlStr = String.format(FETCH_ITEM_ID_URL, registryName, metadata);
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
                log.error("获取物品 ID 失败: {}", responseCode);
                return Optional.empty();
            }
            @Cleanup BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String mcmodItemID = in.readLine();
            log.info("获取物品 MCMOD ID 成功: {}", mcmodItemID);
            return Optional.of(mcmodItemID);
        } finally {
            connection.disconnect();
        }
    }

    /**
     * @param id 物品 ID
     * @return 物品页面是否存在 true: 存在 false: 不存在
     */
    @SneakyThrows
    public static boolean itemPageExist(@NonNull String id) {
        String urlStr = String.format(ITEM_PAGE_URL, id);
        log.info("检查MC百科物品页面是否存在: {}", urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // 设置连接超时为5秒
        connection.setConnectTimeout(5000);
        // 设置读取超时为5秒
        connection.setReadTimeout(5000);
        try {
            return connection.getResponseCode() == 200;
        } finally {
            connection.disconnect();
        }
    }

    /**
     * copy from <a href="https://github.com/Nova-Committee/McMod-Search-Reborn/blob/1.16-forge/src/main/java/nova/committee/mcmodwiki/core/CoreService.java">...</a>
     *
     * @param url 要打开的url
     */
    @SneakyThrows
    private static void browse(String url) {
        // 获取操作系统的名字
        String osName = System.getProperty("os.name", "");
        if (osName.startsWith("Mac OS")) {
            // 苹果的打开方式
            Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
            Method openURL = fileMgr.getDeclaredMethod("openURL",
                    String.class);
            openURL.invoke(null, url);
        } else if (osName.startsWith("Windows")) {
            // windows的打开方式。
            Runtime.getRuntime().exec(
                    "rundll32 url.dll,FileProtocolHandler " + url);
        } else {
            // Unix or Linux的打开方式
            String[] browsers = {"firefox", "opera", "konqueror", "epiphany",
                    "mozilla", "netscape"};
            String browser = null;
            for (int count = 0; count < browsers.length && browser == null; count++)
                // 执行代码，在browser有值后跳出，
                // 这里是如果进程创建成功了，==0是表示正常结束。
                if (Runtime.getRuntime()
                        .exec(new String[]{"which", browsers[count]})
                        .waitFor() == 0)
                    browser = browsers[count];
            if (browser == null)
                throw new Exception("Could not find web browser");
            else
                // 这个值在上面已经成功的得到了一个进程。
                Runtime.getRuntime().exec(new String[]{browser, url});
        }
    }

}
