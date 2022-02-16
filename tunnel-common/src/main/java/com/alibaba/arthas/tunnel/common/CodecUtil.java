package com.alibaba.arthas.tunnel.common;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 编码工具类
 *
 * @author zzj
 * @version 2019/11/6
 */
public class CodecUtil {

    private static final String DEFAULT_URL_ENCODING = "UTF-8";
    private static final char[] BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    /**
     * Hex编码
     * 参考 org.apache.commons.codec.binary.Hex#encode
     */
    public static String encodeHex(byte[] input) {
        final char[] toDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        final int l = input.length;
        final char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & input[i]) >>> 4];
            out[j++] = toDigits[0x0F & input[i]];
        }
        return new String(out);
    }

    /**
     * Converts a hexadecimal character to an integer.
     *
     * @param ch
     *            A character to convert to an integer digit
     * @param index
     *            The index of the character in the source
     * @return An integer
     */
    private static int toDigit(final char ch, final int index) {
        final int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }

    /**
     * Hex解码
     * 参考 org.apache.commons.codec.binary.Hex#decode
     */
    public static byte[] decodeHex(String input) {
        char[] data = input.toCharArray();

        final int len = data.length;

        if ((len & 0x01) != 0) {
            throw new RuntimeException("Odd number of characters.");
        }

        final byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    /**
     * Base64编码.
     */
    public static String encodeBase64(byte[] input) {
        return new String(Base64.getUrlEncoder().encode(input));
    }

    /**
     * Base64编码.
     */
    public static String encodeBase64(String input) {
        return new String(Base64.getUrlEncoder().encode(input.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Base64解码.
     */
    public static byte[] decodeBase64(String input) {
        return Base64.getUrlDecoder().decode(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64解码.
     */
    public static String decodeBase64String(String input) {
        return new String(Base64.getUrlDecoder().decode(input.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    /**
     * Base62编码。
     */
    public static String encodeBase62(byte[] input) {
        char[] chars = new char[input.length];
        for (int i = 0; i < input.length; i++) {
            chars[i] = BASE62[((input[i] & 0xFF) % BASE62.length)];
        }
        return new String(chars);
    }

    /**
     * URL 编码, Encode默认为UTF-8.
     */
    public static String encodeUrl(String part) {
        return encodeUrl(part, DEFAULT_URL_ENCODING);
    }

    /**
     * URL 编码, Encode默认为UTF-8.
     */
    public static String encodeUrl(String part, String encoding) {
        if (part == null){
            return null;
        }
        try {
            return URLEncoder.encode(part, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * URL 解码, Encode默认为UTF-8.
     */
    public static String decodeUrl(String part) {
        return decodeUrl(part, DEFAULT_URL_ENCODING);
    }

    /**
     * URL 解码, Encode默认为UTF-8.
     */
    public static String decodeUrl(String part, String encoding) {
        if (part == null){
            return null;
        }
        try {
            return URLDecoder.decode(part, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * URL 解码（两次）, Encode默认为UTF-8.
     */
    public static String decodeUrlTwice(String part) {
        return decodeUrl(decodeUrl(part));
    }

}
