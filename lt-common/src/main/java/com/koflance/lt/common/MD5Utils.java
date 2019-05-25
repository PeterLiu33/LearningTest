package com.koflance.lt.common;


import org.apache.commons.lang.StringUtils;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Optional;

/**
 * Created by liujun on 2019/5/24.
 */
public class MD5Utils {

    private static final Charset charset = Charset.forName("utf-8");

    private static final String[] strDigits = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    // 返回形式为数字跟字符串
    private static String byteToArrayString(byte bByte) {
        int iRet = bByte;
        if (iRet < 0) {
            iRet += 256;
        }
        int iD1 = iRet / 16;
        int iD2 = iRet % 16;
        return strDigits[iD1] + strDigits[iD2];
    }

    // 转换字节数组为16进制字串
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sBuffer = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            sBuffer.append(byteToArrayString(bytes[i]));
        }
        return sBuffer.toString();
    }

    public static byte[] hexToBytes(String hexStr) {
        if(StringUtils.isBlank(hexStr)){
            return null;
        }
        hexStr = hexStr.toUpperCase();
        int length = hexStr.length() / 2;
        byte[] re = new byte[length];
        for (int i = 0; i < length; i++) {
            int poi = i * 2;
            re[i] = (byte) (char2Byte(hexStr.charAt(poi)) << 4 | char2Byte(hexStr.charAt(poi + 1)));
        }
        return re;
    }

    public static boolean isHex(String str) {
        boolean isHexFlg = true;
        char c;
        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);
            if (!(((c >= '0') && (c <= '9')) ||
                    ((c >= 'A') && (c <= 'F')) ||
                    (c >= 'a') && (c <= 'f'))) {
                isHexFlg = false;
                break;
            }
        }
        return isHexFlg;
    }

    private static byte char2Byte(char a) {
        return (byte) "0123456789ABCDEF".indexOf(a);
    }

    public static Optional<String> getSignature(String value, String salt) {
        String content = salt + ":" + value;
        String byteToString = bytesToHex(content.getBytes(charset));
        return getMD5(byteToString.getBytes(charset));
    }

    private static Optional<String> getMD5(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return Optional.of(bytesToHex(md.digest(bytes)));
        } catch (Exception ex) {
            ;
        }
        return Optional.empty();
    }

    public static void main(String[] args) {
        System.out.println(getSignature("test", "mySalt"));
        System.out.println(bytesToHex("test".getBytes(charset)));
        System.out.println(new String(hexToBytes("74657374"), charset));
        System.out.println(new String(hexToBytes("7465737"), charset));
        System.out.println(isHex("74657374"));
        System.out.println(isHex("74657374&"));
        System.out.println(isHex("7465737"));
    }
}