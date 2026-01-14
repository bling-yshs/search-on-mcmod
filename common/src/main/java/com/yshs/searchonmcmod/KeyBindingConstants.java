package com.yshs.searchonmcmod;

import org.lwjgl.glfw.GLFW;

/**
 * 按键绑定常量类
 * 用于在 Fabric 和 NeoForge 之间共享按键配置
 */
public class KeyBindingConstants {

    /**
     * 名称
     */
    public static final String KEY_NAME = "key.searchonmcmod.search_on_mcmod";

    /**
     * 分类
     */
    public static final String CATEGORY = "categories";

    /**
     * 默认按键码 (B 键)
     */
    public static final int DEFAULT_KEY_CODE = GLFW.GLFW_KEY_B;

    private KeyBindingConstants() {
        // 工具类，禁止实例化
    }
}
