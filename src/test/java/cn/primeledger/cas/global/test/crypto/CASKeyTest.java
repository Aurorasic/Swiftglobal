package cn.primeledger.cas.global.test.crypto;

import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import com.alibaba.fastjson.JSON;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.jar.JarFile;

/**
 * @author yangyi
 * @deta 2018/2/26
 * @description
 */
public class CASKeyTest {

    private static volatile Instrumentation instru;

    public static void premain(String args, Instrumentation inst) {
        instru = inst;
        System.out.println(instru);
    }

    public static void main(String[] args){
        ECKey casKey = new ECKey();
        KeyPair keyPair = casKey.getKeyPair();
        long objectSize = instru.getObjectSize(keyPair);
        System.out.println("大小"+objectSize);
    }

}
