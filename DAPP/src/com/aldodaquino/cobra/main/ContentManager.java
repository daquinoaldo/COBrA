package com.aldodaquino.cobra.main;

import com.aldodaquino.cobra.contracts.GenericContentManagementContract;
import org.web3j.crypto.Credentials;

import java.math.BigInteger;

public class ContentManager extends ContractManager {

    private GenericContentManagementContract contentManager;

    /*
     * CONSTRUCTORS
     */

    /**
     * Deploy and manage a new content manager contract.
     * @param credentials your account credentials.
     */
    public ContentManager(Credentials credentials, String catalogAddress, String name, String genre, BigInteger price) {
        super(credentials);
        contentManager = (GenericContentManagementContract) deploy(GenericContentManagementContract.class);
        try {
            contentManager.setName(stringToBytes32(name)).send();
            contentManager.setGenre(stringToBytes32(genre)).send();
            contentManager.setPrice(price).send();
            contentManager.publish(catalogAddress).send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load and manage an existent content manager contract.
     * @param credentials your account credentials.
     * @param contractAddress the existent contract address on blockchain.
     */
    public ContentManager(Credentials credentials, String contractAddress) {
        super(credentials);
        contentManager = (GenericContentManagementContract) load(GenericContentManagementContract.class, contractAddress);
    }

    private byte[] stringToBytes32(String string) {
        byte[] byte32 = new byte[32];
        byte[] bytes = string.getBytes();
        System.arraycopy(bytes, 0, byte32, 0, bytes.length);
        return byte32;
    }

}
