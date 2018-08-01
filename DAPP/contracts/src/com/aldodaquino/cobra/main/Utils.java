package com.aldodaquino.cobra.main;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;

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

    private static final String BLOCK_GAS_LIMIT = "5000000";

    /**
     * Build a Web3j protocol using the default provider.
     * @return a Web3j instance.
     */
    static Web3j buildWeb3() {
        Web3j web3 = Web3j.build(new HttpService());    // defaults to http://localhost:8545/
        try {
            System.out.println("Web3 protocol version: " + web3.ethProtocolVersion().send().getProtocolVersion());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return web3;
    }

    /**
     * Return the average gas price.
     * @param web3 a web3j instance.
     * @return a BigInteger of the gas price, 0 in case of error.
     */
    static BigInteger getGasPrice(Web3j web3) {
        BigInteger gasPrice;
        try {
            gasPrice = web3.ethGasPrice().send().getGasPrice();
        } catch (IOException e) {
            System.err.println("Cannot get the gas limit.");
            e.printStackTrace();
            gasPrice = BigInteger.ZERO;
        }
        System.out.println("Gas price: " + gasPrice);
        return gasPrice;
    }

    /**
     * Return the maximum gas limit that we can use in a transaction.
     * @param web3 a web3j instance.
     * @return a BigInteger of the gas limit, 0 in case of error.
     */
    static BigInteger getGasLimit(Web3j web3) {
        BigInteger gasLimit;
        try {
            EthBlock.Block block =
                    web3.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, true).send().getBlock();
            if (block != null) {
                System.out.println("Latest block number: " + block.getNumber());
                gasLimit = block.getGasLimit();
            }
            else gasLimit = new BigInteger(BLOCK_GAS_LIMIT);
        } catch (IOException e) {
            System.err.println("Cannot get the gas limit.");
            e.printStackTrace();
            gasLimit = BigInteger.ZERO;
        }
        System.out.println("Block gas limit: " + gasLimit);
        return gasLimit;
    }

    static BigInteger getBalance(Web3j web3, String address) {
        BigInteger balance;
        try {
             balance = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).send()
                     .getBalance();
        } catch (IOException e) {
            System.err.println("Cannot get the account balance.");
            e.printStackTrace();
            balance = BigInteger.ZERO;
        }
        System.out.println("Account balance: " + balance);
        return balance;
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
