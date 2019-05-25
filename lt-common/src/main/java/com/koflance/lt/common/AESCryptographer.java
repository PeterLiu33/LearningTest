package com.koflance.lt.common;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;

/**
 * JAVA默认不支持AES256位，默认支持AES126
 * bcprov-ext
 * Created by liujun on 2019/5/24.
 */
public class AESCryptographer {

    private static final String EncryptAlg = "AES";

    // AES-128位-无向量-ECB/PKCS7Padding
    // AES分组密码有五种工作体制：https://www.cnblogs.com/starwolf/p/3365834.html
    // 1.电码本模式（Electronic Codebook Book (ECB)）；
    // 2.密码分组链接模式（Cipher Block Chaining (CBC)）；
    // 3.计算器模式（Counter (CTR)）；
    // 4.密码反馈模式（Cipher FeedBack (CFB)）；
    // 5.输出反馈模式（Output FeedBack (OFB)）
    private static final String Cipher_Mode = "AES/%s/PKCS5Padding"; //"算法/模式/补码方式"

    private final String password;

    private final String salt;

    private SecretKeySpec key;

    private Charset charset = Charset.forName("utf-8");

    private final EncryptModel encryptModel;

    private String cipherMode;

    // key值大小
    private static final int Secret_Key_Size = 16;

    public static final String SPLIT_CHAR = "#";

    public AESCryptographer(String password, String salt, EncryptModel encryptModel) throws NoSuchAlgorithmException {
        this.password = password;
        this.salt = salt;
        if(encryptModel == null){
            throw new NoSuchAlgorithmException("加密模式不能为空!");
        }
        this.encryptModel = encryptModel;
        this.cipherMode = getCipherMode();
        // 初始化AES专用秘钥
        initNewKeyForAES();
    }

    /**
     * JAVA中有效密码为16位/24位/32位，
     * 其中24位/32位需要JCE（Java 密码扩展无限制权限策略文件
     *
     * @throws NoSuchAlgorithmException
     */
    private void initKeyForAES() throws NoSuchAlgorithmException {
        if (StringUtils.isBlank(password)) {
            throw new NullPointerException("密码不能为空");
        }
        try {
            // 这个可能存在不同JDK实现不一致问题
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(password.getBytes());
            // 创建AES的Key生产者
            KeyGenerator kgen = KeyGenerator.getInstance(EncryptAlg);
            // 利用用户密码作为随机数初始化出128位的key生产者
            // 与加密没关系，SecureRandom是生成安全随机数序列，password.getBytes()是种子，只要种子相同，序列就一样，所以解密只要有password就行
            kgen.init(128, random);
//            kgen.init(128, new SecureRandom(password.getBytes()));
            // 根据用户密码，生成一个密钥
            SecretKey secretKey = kgen.generateKey();
            // 返回基本编码格式的密钥，如果此密钥不支持编码，则返回
            byte[] enCodeFormat = secretKey.getEncoded();
            // 转换为AES专用密钥
            key = new SecretKeySpec(enCodeFormat, EncryptAlg);
        } catch (NoSuchAlgorithmException ex) {
            throw new NoSuchAlgorithmException();
        }
    }

    private void initNewKeyForAES() {
        this.key = new SecretKeySpec(getSecretKey(password), EncryptAlg);
    }

    /**
     * 对密钥key进行处理：如密钥长度不够位数的则 以指定paddingChar 进行填充；
     * 此处用空格字符填充，也可以 0 填充，具体可根据实际项目需求做变更
     *
     * @param key
     * @return
     * @throws Exception
     */
    private byte[] getSecretKey(String key) {
        final byte paddingChar = 'p';
        byte[] realKey = new byte[Secret_Key_Size];
        byte[] byteKey = Base64.encodeBase64(key.getBytes(charset));
        for (int i = 0; i < realKey.length; i++) {
            if (i < byteKey.length) {
                realKey[i] = byteKey[i];
            } else {
                realKey[i] = paddingChar;
            }
        }

        return realKey;
    }

    private String getCipherMode(){
        return String.format(Cipher_Mode, encryptModel.name());
    }

    public Optional<String> encrypt(String rawContent, OutputFormat model) {
        Optional<String> signatureOpt = MD5Utils.getSignature(rawContent, salt);
        if (!signatureOpt.isPresent()) {
            return Optional.empty();
        }
        String value = rawContent + SPLIT_CHAR + signatureOpt.get();
        return doAesEncrypt(value, model);
    }

    public Optional<String> decrypt(String encryptedContent, OutputFormat model) {
        if (StringUtils.isBlank(encryptedContent)) {
            return Optional.empty();
        }
        Optional<String> optional = doAesDecrypt(encryptedContent, model);
        if (!optional.isPresent()) {
            return Optional.empty();
        }
        String value = optional.get();
        String signature = StringUtils.substringAfterLast(value, SPLIT_CHAR);
        if (StringUtils.isBlank(signature)) {
            // 没有签名, 验证失败
            return Optional.empty();
        }
        String rawContent = StringUtils.substringBeforeLast(value, SPLIT_CHAR);
        Optional<String> signatureOpt = MD5Utils.getSignature(rawContent, salt);
        if (!signatureOpt.isPresent()) {
            // 签名失败
            return Optional.empty();
        }
        if (StringUtils.equals(signature, signatureOpt.get())) {
            // 验签成功
            return Optional.of(rawContent);
        }
        return Optional.empty();
    }

    private Optional<String> doAesEncrypt(String rawContent, OutputFormat model) {
        try {
            if (model == null) {
                return Optional.empty();
            }
            // 创建密码器
            Cipher cipher = Cipher.getInstance(cipherMode);
            // 初始化为加密模式的密码器
            switch (encryptModel){
                case CBC:
                    //使用CBC模式，需要一个向量iv，可增加加密算法的强度
                    IvParameterSpec iv = new IvParameterSpec(key.getEncoded());
                    cipher.init(Cipher.ENCRYPT_MODE, key, iv);
                    break;
                case ECB:
                    cipher.init(Cipher.ENCRYPT_MODE, key);
                    break;
            }
            // 加密
            byte[] bytes = cipher.doFinal(rawContent.getBytes(charset));
            switch (model) {
                case HEX:
                    return Optional.of(MD5Utils.bytesToHex(bytes));
                case BASE64:
                    return Optional.of(Base64.encodeBase64String(bytes));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private Optional<String> doAesDecrypt(String encryptedContent, OutputFormat model) {
        try {
            if (model == null) {
                return Optional.empty();
            }
            if (model == OutputFormat.HEX && !MD5Utils.isHex(encryptedContent)) {
                return Optional.empty();
            }
            if (model == OutputFormat.BASE64 && !Base64.isBase64(encryptedContent)) {
                return Optional.empty();
            }
            // 创建密码器
            Cipher cipher = Cipher.getInstance(cipherMode);
            // 初始化为解密模式的密码器
            switch (encryptModel){
                case CBC:
                    //使用CBC模式，需要一个向量iv，可增加加密算法的强度
                    IvParameterSpec iv = new IvParameterSpec(key.getEncoded());
                    cipher.init(Cipher.DECRYPT_MODE, key, iv);
                    break;
                case ECB:
                    cipher.init(Cipher.DECRYPT_MODE, key);
                    break;
            }
            // 明文
            switch (model) {
                case HEX:
                    return Optional.of(new String(cipher.doFinal(MD5Utils.hexToBytes(encryptedContent)), charset));
                case BASE64:
                    return Optional.of(new String(cipher.doFinal(Base64.decodeBase64(encryptedContent)), charset));
            }
        } catch (Exception e) {
            ;
        }
        return Optional.empty();
    }

    /**
     * 密文输出格式
     */
    public enum OutputFormat {
        BASE64, HEX
    }

    /**
     * 加密模式
     */
    public enum EncryptModel {
        ECB, CBC
    }

}
