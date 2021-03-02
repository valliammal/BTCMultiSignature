package com.vosco.bitcoin;

import org.bitcoinj.core.ECKey;

import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;

public class KeyPair {
    public String[] genKeyPair() {
        // generate one priv/pub key pair in HEX ( string) - ret [0] is private, ret[0] is private
        String[] keys = new String[2];
        ECKey key = new ECKey();
        keys[0]  =  key.getPrivateKeyAsHex();
        keys[1] = key.getPublicKeyAsHex();
        System.out.println(" Keys [0] " + keys[0]);
        System.out.println(" Keys [1] " + keys[1]);

        return keys;
    }
    public static void main(String[] args) {
        new KeyPair().genKeyPair();
    }
}

