package com.aldodaquino.cobra.main;

import com.aldodaquino.cobra.contracts.DAPPContentManagementContract;
import org.web3j.crypto.Credentials;

import java.math.BigInteger;

public class ContentManager extends ContractManager {

    private final DAPPContentManagementContract contentManager;

    /**
     * Deploy and manage a new content manager contract.
     * @param credentials your account credentials.
     */
    public ContentManager(Credentials credentials, String catalogAddress, String name, String genre, BigInteger price,
                          String hostname, int port)
            throws Exception {
        super(credentials);
        contentManager = (DAPPContentManagementContract) deploy(DAPPContentManagementContract.class);
        contentManager.setName(Utils.stringToBytes32(name)).send();
        contentManager.setGenre(Utils.stringToBytes32(genre)).send();
        contentManager.setPrice(price).send();
        contentManager.setHostname(Utils.stringToBytes32(hostname)).send();
        contentManager.setPort(new BigInteger(Integer.toString(port))).send();
        contentManager.publish(catalogAddress).send();
    }

    /**
     * Load and manage an existent content manager contract.
     * @param credentials your account credentials.
     * @param contractAddress the existent contract address on blockchain.
     */
    public ContentManager(Credentials credentials, String contractAddress) {
        super(credentials);
        contentManager = (DAPPContentManagementContract) load(DAPPContentManagementContract.class, contractAddress);
    }

    /**
     * Consume a bought content.
     * Note that the content is consumed by the user that owns the credentials passed to the constructor.
     * @return a boolean representing the operation outcome.
     */
    public boolean consumeContent() {
        try {
            return contentManager.consumeContent().send().isStatusOK();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns the hostname of the author-server.
     * @return String the hostname.
     */
    public String getHostname() {
        try {
            return Utils.bytes32ToString(contentManager.hostname().send());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the port number of the author-server.
     * @return the int number of the port.
     */
    public int getPort() {
        try {
            return contentManager.port().send().intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}
