package com.koflance.lt.common.task;

import com.koflance.lt.common.AESCryptographer;
import org.junit.Assert;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Created by liujun on 2019/5/25.
 */
public class AESCryptographerTest {

    @Test
    public void test() throws NoSuchAlgorithmException {
        AESCryptographer aesCryptographer = new AESCryptographer("password", "salt", AESCryptographer.EncryptModel.CBC);
        AESCryptographer aesCryptographer2 = new AESCryptographer("password", "salt", AESCryptographer.EncryptModel.CBC);
        String rawContent = "我是123456.12121abc";
        long l = System.currentTimeMillis();
        IntStream.range(0, 1000000).forEach(x -> {
            Optional<String> encrypt = aesCryptographer.encrypt(rawContent, AESCryptographer.OutputFormat.BASE64);
            if (!encrypt.isPresent()) {
                System.out.println("error!");
                return;
            }
            String encryptedContent = encrypt.get();
            Optional<String> decrypt = aesCryptographer2.decrypt(encryptedContent, AESCryptographer.OutputFormat.BASE64);
            if (!decrypt.isPresent()) {
                System.out.println("error2!");
                return;
            }
        });
        System.out.println(System.currentTimeMillis() - l);
        l = System.currentTimeMillis();
        IntStream.range(0, 1000000).forEach(x -> {
            Optional<String> encrypt = aesCryptographer.encrypt(rawContent, AESCryptographer.OutputFormat.HEX);
            if (!encrypt.isPresent()) {
                System.out.println("error!");
                return;
            }
            String encryptedContent = encrypt.get();
            Optional<String> decrypt = aesCryptographer2.decrypt(encryptedContent, AESCryptographer.OutputFormat.HEX);
            if (!decrypt.isPresent()) {
                System.out.println("error2!");
                return;
            }
        });
        System.out.println(System.currentTimeMillis() - l);
    }

    @Test
    public void test2() throws NoSuchAlgorithmException {
        // CBC: f4cPF8FIam37FcYP5jCs6Z0+3BquCEZZGIiTLHxJf/+f1/fUD3LFlwnFgZV/0Dt10d91Khn3w73yKdyNfSXCRA==
        // ECB: b8x+9db768MEke5BjtU70H/Y/rIkqRjZmRUXW+iCsC9MIx6hhoWSkFZsxtkT/kl7f7/26AsZbL6tvbM8WcM+DQ==
        AESCryptographer encoder = new AESCryptographer("password", "salt", AESCryptographer.EncryptModel.CBC);
        AESCryptographer decoder = new AESCryptographer("password", "salt", AESCryptographer.EncryptModel.CBC);
        String rawContent = "我是123456.12121abc";
        Optional<String> encrypt = encoder.encrypt(rawContent, AESCryptographer.OutputFormat.BASE64);
        if (encrypt.isPresent()) {
            String encryptedContent = encrypt.get();
            System.out.println(encryptedContent);
            Optional<String> decrypt = decoder.decrypt(encryptedContent, AESCryptographer.OutputFormat.BASE64);
            if (decrypt.isPresent()) {
                System.out.println(decrypt.get());
                Assert.assertEquals(decrypt.get(), rawContent);
            } else {
                Assert.fail();
            }

        }

        encrypt = encoder.encrypt(rawContent, AESCryptographer.OutputFormat.HEX);
        if (encrypt.isPresent()) {
            String encryptedContent = encrypt.get();
            System.out.println(encryptedContent);
            Optional<String> decrypt = decoder.decrypt(encryptedContent, AESCryptographer.OutputFormat.HEX);
            if (decrypt.isPresent()) {
                System.out.println(decrypt.get());
                Assert.assertEquals(decrypt.get(), rawContent);
            } else {
                Assert.fail();
            }
        }
    }

    @Test
    public void test3() throws NoSuchAlgorithmException {
        AESCryptographer aesCryptographer = new AESCryptographer("password", "salt", AESCryptographer.EncryptModel.CBC);
        String hexString = "fa4639de31bc8a838eb3e3a0cde79481bd5e8e388702ec86edcdaa9958d1d3f0a44990b62d282a844bb85517dfcc58b3a0d9a3a59b3dd46fad2502b1fc37b1cb";
        Optional<String> decrypt = aesCryptographer.decrypt(hexString, AESCryptographer.OutputFormat.BASE64);
        if (decrypt.isPresent()) {
            System.out.println(decrypt.get());
        } else {
            Assert.fail();
        }
    }

    @Test
    public void test4() throws NoSuchAlgorithmException {
        AESCryptographer fake = new AESCryptographer("password", "fake", AESCryptographer.EncryptModel.CBC);
        AESCryptographer aesCryptographer = new AESCryptographer("password", "salt", AESCryptographer.EncryptModel.CBC);
        String fakeValue = "我是伪造的";
        Optional<String> encrypt = fake.encrypt(fakeValue, AESCryptographer.OutputFormat.BASE64);
        if(encrypt.isPresent()){
            Optional<String> decrypt = aesCryptographer.decrypt(encrypt.get(), AESCryptographer.OutputFormat.BASE64);
            if(decrypt.isPresent()){
                Assert.assertEquals(decrypt.get(), fakeValue);
            } else {
                Assert.fail();
            }
        } else {
            Assert.fail();
        }
    }

    @Test
    public void test5() throws NoSuchAlgorithmException, InterruptedException {
        final AESCryptographer encoder = new AESCryptographer("password", "salt", AESCryptographer.EncryptModel.CBC);
        final AESCryptographer decoder = new AESCryptographer("password", "salt", AESCryptographer.EncryptModel.CBC);
        long l = System.currentTimeMillis();
        List<Thread> collect = IntStream.range(0, 2000).mapToObj(x -> new Thread(() -> {
            String rawContent = "我是123456.12121abc";
            Optional<String> encrypt = encoder.encrypt(rawContent, AESCryptographer.OutputFormat.BASE64);
            if (encrypt.isPresent()) {
                String encryptedContent = encrypt.get();
//                System.out.println(encryptedContent);
                Optional<String> decrypt = decoder.decrypt(encryptedContent, AESCryptographer.OutputFormat.BASE64);
                if (decrypt.isPresent()) {
//                    System.out.println(decrypt.get());
                    Assert.assertEquals(decrypt.get(), rawContent);
                } else {
                    Assert.fail();
                }
            }
        })).collect(toList());
        for (Thread thread : collect) {
            thread.start();
        }
        for (Thread thread : collect) {
            thread.join();
        }
        System.out.println(System.currentTimeMillis() - l);
    }

}
