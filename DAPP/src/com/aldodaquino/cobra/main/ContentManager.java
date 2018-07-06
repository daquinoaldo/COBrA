package com.aldodaquino.cobra.main;

import com.aldodaquino.cobra.contracts.GenericContentManagementContract;
import org.web3j.crypto.Credentials;

import java.math.BigInteger;

class ContentManager extends ContractManager {

    private final GenericContentManagementContract contentManager;

    /**
     * Deploy and manage a new content manager contract.
     * @param credentials your account credentials.
     */
    ContentManager(Credentials credentials, String catalogAddress, String name, String genre, BigInteger price) {
        super(credentials);
        contentManager = (GenericContentManagementContract) deploy(GenericContentManagementContract.class);
        try {
            contentManager.setName(Utils.stringToBytes(name)).send();
            contentManager.setGenre(Utils.stringToBytes(genre)).send();
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

}
