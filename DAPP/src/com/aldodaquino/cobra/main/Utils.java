package com.aldodaquino.cobra.main;

import org.web3j.protocol.Web3j;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

class Utils {

    static BigInteger getGasPrice(Web3j web3) {
        try {
            return web3.ethGasPrice().send().getGasPrice();
        } catch (IOException e) {
            System.err.println("Cannot get the gas limit.");
            e.printStackTrace();
            return new BigInteger("0");
        }
    }

    static BigInteger getGasLimit(Web3j web3) {
        try {
            return web3.ethGetBlockByHash("latest", true).send().getBlock().getGasLimit();
        } catch (IOException e) {
            System.err.println("Cannot get the gas limit.");
            e.printStackTrace();
            return new BigInteger("0");
        }
    }

    static String bytesToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    static byte[] stringToBytes(String string) {
        byte[] byte32 = new byte[32];
        byte[] bytes = string.getBytes();
        System.arraycopy(bytes, 0, byte32, 0, bytes.length);
        return byte32;
    }

}
