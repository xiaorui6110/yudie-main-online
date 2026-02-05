package com.yudie.yudiemainbackend.utils;

/**
 * @description: 颜色转换工具类
 * @author: xiaorui
 * @date: 2025-05-23 20:51
 **/

public class ColorTransformUtils {

    /**
     *  工具类不需要实例化
     */
    private ColorTransformUtils() {}

    /**
     * 获取标准颜色（将数据万象的 5 位色值转为 6 位）
     *
     * @param color 颜色
     * @return 标准颜色
     */
    public static String getStandardColor(String color) {
        /*
            每一种 rgb 色值都有可能只有一个 0，要转换为 00)
            如果是六位，不用转换，如果是五位，要给第三位后面加个 0
            示例：
            0x080e0 => 0x0800e
            如果是 5 位颜色值，添加一个 0 使其成为 6 位
         */
        if (color.length() == 6) {
            StringBuilder sb = new StringBuilder(color);
            sb.insert(3, "0");
            color = sb.toString();
        }
        return color;
    }
}
