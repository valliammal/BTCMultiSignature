package com.vosco.bitcoin;


import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

import com.google.common.collect.ImmutableList;
import org.bitcoinj.wallet.Wallet;
import java.util.Arrays;
import java.util.List;

public class MultisigTransaction
{
    private TestNet3Params params = TestNet3Params.get();
    private WalletAppKit appKit;
    private Wallet wallet;
    private Transaction multiSignTx;
    private TransactionInput multiSignTxInput;

    public MultisigTransaction() {
        appKit = new WalletAppKit(params, new File("."), "wallet1"); //Loading existing wallet
        appKit.startAsync();
        appKit.awaitRunning();
        System.out.println("Network connected!");
        wallet = appKit.wallet();
        System.out.println("Wallet's current receive address: " + wallet.currentReceiveAddress());
        System.out.println("Wallet contents: " + wallet);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        MultisigTransaction mt = new MultisigTransaction();
        mt.genLockScript(new ECKey().toString(),new ECKey().toString(),new ECKey().toString());
        mt.genUnlockingScript(new KeyPair().genKeyPair());
    }

    // generate multi sig locking script 2 of 3 , parameters (in/out) in HEX (string)
    public String genLockScript(String pubKey1, String pubKey2, String pubKey3) {

        ECKey key1 = createKeyFromSha256Passphrase(pubKey1);
        ECKey key2 = createKeyFromSha256Passphrase(pubKey2);
        ECKey key3 = createKeyFromSha256Passphrase(pubKey3);

        // Use the redeem script we have saved somewhere to start building the transaction
        Script multiSignatureScript = new
                Script(hexStringToByteArray("524104711acc7644d34e493eba76984c81d99f1233f06b3242d90e6cd082b26fd0c1186f65de8d3378a6630f2285bd17972372685378683b604c68343fa1b532196c4d410476d6ef11a42010a889ee0c3d75f9cac3a51a3e245744fb9bf1bc8c196eb0f6982e39aad753514248966f4d545a5439ece8e27e13764c92f6230e0244cae5bee54104a45f0da4e6501fa781b6534e601f410a59328691d86d034d13362138f7e9a2927451280544e36c88279ee00c7face2fb707d0210842017e3937ae4584faacf6753ae"));

        System.out.println("Lock Script Program " + byteArrayToHex(multiSignatureScript.getProgram()));
        // Start building the transaction by adding the unspent inputs we want to use
        // The data is taken from blockchain.info, and can be found here: https://blockchain.info/rawtx/ca1884b8f2e0ba88249a86ec5ddca04f937f12d4fac299af41a9b51643302077
        multiSignTx = new Transaction(params);
        ScriptBuilder scriptBuilder = new ScriptBuilder();
        scriptBuilder.data(new String("a9144f93910f309e2433c25d1e891e29fd4cec8c5f6187").getBytes()); // Script of this output
        TransactionInput input = multiSignTx.addInput(new Sha256Hash("19f589be5fda5a97b5a26158abd1fa02e68a15e5a6a4d83791935f882dbe0492"), 0, scriptBuilder.build());

        // Add outputs to the person receiving bitcoins
        Address receiverAddress = wallet.currentReceiveAddress();
        Coin charge = Coin.valueOf(10000); // 0.1 mBTC
        Script outputScript = ScriptBuilder.createOutputScript(receiverAddress);
        multiSignTx.addOutput(charge, outputScript);

        // Sign the first part of the transaction using private key #1
        Sha256Hash sighash = multiSignTx.hashForSignature(0, multiSignatureScript, Transaction.SigHash.ALL, false);
        ECKey.ECDSASignature ecdsaSignature1 = key1.sign(sighash);
        TransactionSignature txSignarture1 = new TransactionSignature(ecdsaSignature1, Transaction.SigHash.ALL, false);
        ECKey.ECDSASignature ecdsaSignature2 = key2.sign(sighash);
        TransactionSignature txSignarture2 = new TransactionSignature(ecdsaSignature2, Transaction.SigHash.ALL, false);
        ECKey.ECDSASignature ecdsaSignature3 = key3.sign(sighash);
        TransactionSignature txSignarture3 = new TransactionSignature(ecdsaSignature3, Transaction.SigHash.ALL, false);

        List<TransactionSignature> transactionSignarture = Arrays.asList(txSignarture1,txSignarture2,txSignarture3);
        // Create multisig input script
        Script inputScript = ScriptBuilder.createMultiSigInputScript(transactionSignarture);
        input.setScriptSig(inputScript);
        multiSignTx.addInput(input);
        System.out.println("Multi Signature Transaction " + byteArrayToHex( multiSignTx.bitcoinSerialize()));
        return inputScript.toString();
    };
    /**
     * Method to convert a byte array to human readable hex string
     * @param a
     * @return hex string
     */
    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    // generate unlocking multi sig script - with only 2 provided private keys , parameters (in/out) in HEX (string)
    public String genUnlockingScript(String[] keypairs) {
        ECKey key1 = createKeyFromSha256Passphrase(keypairs[0]);
        ECKey key2 = createKeyFromSha256Passphrase(keypairs[1]);
        ImmutableList<ECKey> keys = ImmutableList.of(
                key1,
                key2);
        Script unlockScript = ScriptBuilder.createMultiSigOutputScript(2,keys);
        System.out.println("Unlock script Program: " + byteArrayToHex(unlockScript.getProgram()));
        return unlockScript.toString();
    }
    public static ECKey createKeyFromSha256Passphrase(String secret) {
        byte[] hash = null;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(secret.getBytes("UTF-8"));
            hash = md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        @SuppressWarnings("deprecation")
        ECKey key = new ECKey(hash, (byte[])null);
        return key;
    }
    /**
     * Method to convert a human readable hex string to a byte array
     * @param s
     * @return byte array
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

}