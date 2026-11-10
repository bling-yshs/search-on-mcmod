package com.yshs.searchonmcmod;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 用系统默认浏览器打开链接
 */
final class BrowserLauncher {

    private static final Logger LOG = LogManager.getLogger(SearchOnMcmod.MODID);

    /**
     * 等待外部命令结束的最长时间，毫秒
     */
    private static final long LAUNCH_TIMEOUT_MS = 10_000L;

    /**
     * 常见浏览器可执行文件名，按优先级排列（gio open 单独优先尝试）
     */
    private static final String[] KNOWN_BROWSERS = { "xdg-open", "sensible-browser", "firefox", "firefox-esr",
        "zen-browser", "zen", "chromium", "chromium-browser", "google-chrome", "google-chrome-stable", "brave-browser",
        "vivaldi", "microsoft-edge", "librewolf", "epiphany" };

    private BrowserLauncher() {}

    /**
     * 打开链接，失败时记录原因
     *
     * @param url 要打开的链接
     * @return 是否至少有一种方式成功
     */
    static boolean open(String url) {
        if (url == null || url.isEmpty()) {
            LOG.warn("要打开的链接为空");
            return false;
        }

        if (tryDesktop(url)) {
            return true;
        }

        if (System.getProperty("os.name", "")
            .toLowerCase(Locale.ROOT)
            .contains("linux")) {
            for (List<String> command : linuxCommands(url)) {
                if (runQuietly(command)) {
                    LOG.info("已通过 {} 打开链接: {}", command.get(0), url);
                    return true;
                }
            }
        }

        LOG.error("无法打开浏览器，请检查系统默认浏览器设置。链接: {}", url);
        return false;
    }

    /**
     * 优先使用 Desktop API
     *
     * <p>
     * Desktop.Action.BROWSE 在 Linux 上依赖 JVM 启动时识别到桌面会话。平铺窗口管理器、Flatpak 沙箱里的
     * 启动器、被判定为 headless 的 JVM、缺少 XDG_CURRENT_DESKTOP 等情况下，它会直接报告不支持，此时
     * 回退到系统命令。
     * </p>
     *
     * @param url 要打开的链接
     * @return Desktop 是否成功打开
     */
    private static boolean tryDesktop(String url) {
        try {
            if (!Desktop.isDesktopSupported()) {
                LOG.info("Desktop API 不可用，改用系统命令打开浏览器");
                return false;
            }
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                LOG.info("Desktop BROWSE 动作不受支持，改用系统命令打开浏览器");
                return false;
            }
            desktop.browse(new URI(url));
            return true;
        } catch (Exception e) {
            LOG.info("Desktop 打开浏览器失败，改用系统命令: {}", e.toString());
            return false;
        }
    }

    /**
     * Linux 下的候选命令，按优先级排列
     *
     * @param url 要打开的链接
     * @return 依次尝试的命令列表
     */
    private static List<List<String>> linuxCommands(String url) {
        List<List<String>> commands = new ArrayList<>();

        // gio open 走 XDG 的默认应用解析，对 Flatpak 版浏览器最友好。
        commands.add(Arrays.asList("gio", "open", url));

        String browserEnv = System.getenv("BROWSER");
        if (browserEnv != null && !browserEnv.trim()
            .isEmpty()) {
            for (String entry : browserEnv.split(":")) {
                String executable = entry.trim();
                if (!executable.isEmpty()) {
                    commands.add(Arrays.asList(executable, url));
                }
            }
        }

        for (String browser : KNOWN_BROWSERS) {
            commands.add(Arrays.asList(browser, url));
        }
        return commands;
    }

    /**
     * 运行命令；能启动并正常退出（或超时后仍在运行）即视为成功
     *
     * @param command 命令与参数
     * @return 命令是否成功启动浏览器
     */
    private static boolean runQuietly(List<String> command) {
        Process process;
        try {
            process = new ProcessBuilder(command).redirectErrorStream(true)
                .start();
        } catch (IOException e) {
            // 命令不存在或无法执行，尝试下一个。
            return false;
        }

        Thread drainer = new Thread(() -> {
            try {
                byte[] buffer = new byte[4096];
                while (process.getInputStream()
                    .read(buffer) >= 0) {
                    // 只用于排空管道。
                }
            } catch (IOException ignored) {
                // 读失败不影响结果。
            }
        }, "searchonmcmod-browser-output");
        drainer.setDaemon(true);
        drainer.start();

        try {
            if (process.waitFor(LAUNCH_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                int exitCode = process.exitValue();
                if (exitCode != 0) {
                    LOG.debug("命令 {} 以退出码 {} 结束", command, exitCode);
                    return false;
                }
                return true;
            }
            // 超时仍在运行：浏览器已经接手（xdg-open 会等待子进程），视为成功。
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread()
                .interrupt();
            return false;
        } finally {
            if (process.isAlive() && !drainer.isAlive()) {
                process.destroy();
            }
        }
    }
}
