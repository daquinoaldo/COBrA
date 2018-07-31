package com.aldodaquino.cobra.main;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;

/**
 * An higher level Contract Manager.
 * Contains basic methods of all the contracts, such as the deploy, load and suicide function, but also the get owner
 * and get address functions.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
class ContractManager {

    private Web3j web3;
    private BigInteger gasPrice;
    private BigInteger gasLimit;
    Credentials credentials;

    private Contract contract;
    private Class<? extends Contract> contractClass;
    private String owner;

    /*
     * CONSTRUCTORS
     */

    /**
     * Save credentials, connect to web3 and save the gas information.
     * @param credentials your account credentials.
     */
    ContractManager(Credentials credentials) {
        if (credentials == null) throw new IllegalArgumentException("Credentials cannot be null.");
        // save credentials
        this.credentials = credentials;
        // connect to web3
        web3 = Web3j.build(new HttpService());    // defaults to http://localhost:8545/
        web3 = Web3j.build(new HttpService("http://localhost:8546/"));
        // get gas information
        gasPrice = Utils.getGasPrice(web3);
        gasLimit = Utils.getGasLimit(web3);
        Utils.getBalance(web3, credentials.getAddress());
    }

    /**
     * Deploy a new contract of class contractClass and return the contractClass instance.
     * @param contractClass the class of the contract that you want deploy.
     * @return the deployed contract as contractClass instance.
     */
    Contract deploy(Class<? extends Contract> contractClass) {
        this.contractClass = contractClass;
        try {
            Object[] params = {web3, credentials, gasPrice, gasLimit};
            Class[] paramsTypes = {Web3j.class, Credentials.class, BigInteger.class, BigInteger.class};
            Method deploy = contractClass.getMethod("deploy", paramsTypes);
            contract = (Contract) ((RemoteCall) deploy.invoke(null, params)).send();
            owner = credentials.getAddress();   // who deploy the contract is the owner
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            System.err.println("ERROR while deploying " + contractClass + ".");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Got Web3j error while deploying " + contractClass + ".");
            // I want to end the program if the exception occur,
            // but I don't want to have to manage this exception that should not be thrown
            throw new RuntimeException(e);
        }
        return contract;
    }

    /**
     * Load an existent contract of class contractClass and return the contractClass instance.
     * @param contractClass the class of the contract that you want to load.
     * @param contractAddress the address of the contract.
     * @return the loaded contract as contractClass instance.
     */
    Contract load(Class<? extends Contract> contractClass, String contractAddress) {
        this.contractClass = contractClass;
        Object[] params = {contractAddress, web3, credentials, gasPrice, gasLimit};
        Class[] paramsTypes = {String.class, Web3j.class, Credentials.class, BigInteger.class, BigInteger.class};
        try {
            // Load contract
            Method load = contractClass.getMethod("load", paramsTypes);
            contract = (Contract) load.invoke(null, params);
            // Get the contract owner
            Method owner = contractClass.getMethod("owner");
            try {
                this.owner = (String) ((RemoteCall) owner.invoke(contract)).send();
            } catch (NullPointerException e) {
                System.err.println("ERROR while loading " + contractClass +
                        ". Contract " + contractAddress + " may not exists.");
                e.printStackTrace();
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            System.err.println("ERROR while loading " + contractClass + ".");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Got Web3j error while trying to get the contract owner.");
            e.printStackTrace();
        }
        return contract;
    }

    /**
     * Returns the contract address.
     * @return a string containing the contract address.
     */
    public String getAddress() {
        return contract.getContractAddress();
    }

    /**
     * Returns the contract owner.
     * @return a string containing the contract owner.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Call the suicide function on the contract.
     * @return true if the contract suicide has been committed, false in case of errors.
     */
    public boolean suicide() {
        try {
            Method suicide = contractClass.getMethod("_suicide");
            contract = (Contract) ((RemoteCall) suicide.invoke(null)).send();
            return true;
        } catch (Exception e) {
            System.err.println("Got Web3j error while try to get the contract owner.");
            e.printStackTrace();
            return false;
        }
    }

}
