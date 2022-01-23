package com.liuyj.secondkill.utils;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author LYJ
 * @create 2022-01-18 17:34
 * MD5工具类
 */


public class MD5Util {

    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }

    private static final String salt = "1a2b3c4d";

    public static String fromInputPassword(String inputPassword){
        String str = String.valueOf("" + salt.charAt(0) + salt.charAt(2) + inputPassword +
                salt.charAt(5) + salt.charAt(4));
        return md5(str);
    }

    public static String fromPasswordToDBPassword(String fromPassword, String salt){
        String str = String.valueOf("" + salt.charAt(0) + salt.charAt(2) + fromPassword +
                salt.charAt(5) + salt.charAt(4));
        return md5(str);
    }

    public static String inputToDBPassword(String inputPassword, String salt){
        String fromPassword = fromInputPassword(inputPassword);
        String DBPassword = fromPasswordToDBPassword(fromPassword, salt);
        return DBPassword;
    }

    public static void main(String[] args) {
        System.out.println(fromInputPassword("123456"));
        System.out.println(fromPasswordToDBPassword("d3b1294a61a07da9b49b6e22b2cbd7f9",
                "1a2b3c4d"));
        System.out.println(inputToDBPassword("123456",
                "1a2b3c4d"));

        /**
         * d3b1294a61a07da9b49b6e22b2cbd7f9
         * b7797cce01b4b131b433b6acf4add449
         * b7797cce01b4b131b433b6acf4add449
         */
    }

}
