package com.aldodaquino.cobra.main;

import org.web3j.protocol.Web3j;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Some utilities for the blockchain.
 * Contains method to get gas information and to convert bytes32 to Strings and vice-versa.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
class Utils {

    /**
     * Return the average gas price.
     * @param web3 a web3j instance.
     * @return a BigInteger of the gas price, 0 in case of error.
     */
    static BigInteger getGasPrice(Web3j web3) {
        try {
            return web3.ethGasPrice().send().getGasPrice();
        } catch (IOException e) {
            System.err.println("Cannot get the gas limit.");
            e.printStackTrace();
            return new BigInteger("0");
        }
    }

    /**
     * Return the maximum gas limit that we can use in a transaction.
     * @param web3 a web3j instance.
     * @return a BigInteger of the gas limit, 0 in case of error.
     */
    static BigInteger getGasLimit(Web3j web3) {
        try {
            return web3.ethGetBlockByHash("latest", true).send().getBlock().getGasLimit();
        } catch (IOException e) {
            System.err.println("Cannot get the gas limit.");
            e.printStackTrace();
            return new BigInteger("0");
        }
    }

    /**
     * Convert a bytes32 in a String.
     * @param bytes32 the byte[].
     * @return the String.
     */
    static String bytes32ToString(byte[] bytes32) {
        int i = bytes32.length - 1;
        while (i >= 0 && bytes32[i] == 0) i--;
        bytes32 = Arrays.copyOf(bytes32, i + 1);
        return new String(bytes32, StandardCharsets.UTF_8);
    }

    /**
     * Convert a String in a bytes32.
     * @param string the String.
     * @return the byte[].
     */
    static byte[] stringToBytes32(String string) {
        byte[] byte32 = new byte[32];
        if (string != null) {
            byte[] bytes = string.getBytes();
            System.arraycopy(bytes, 0, byte32, 0, bytes.length);
        }
        return byte32;
    }

}
