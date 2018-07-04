package com.aldodaquino.cobra.main;

import org.web3j.protocol.Web3j;

import java.io.IOException;
import java.math.BigInteger;

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

}
