package com.aldodaquino.cobra.main;

import com.aldodaquino.cobra.contracts.DAPPContentManagementContract;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.DefaultBlockParameterName;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ContentManager extends ContractManager {

    private final DAPPContentManagementContract content;

    private final List<Runnable> contentPublishedRunnables = new ArrayList<>();

    /**
     * Deploy and manage a new content manager contract.
     * @param credentials your account credentials.
     */
    public ContentManager(Credentials credentials, String catalogAddress, String name, String genre, BigInteger price,
                          String hostname, int port)
            throws Exception {
        super(credentials);
        content = (DAPPContentManagementContract) deploy(DAPPContentManagementContract.class);
        content.setName(Utils.stringToBytes32(name)).send();
        content.setGenre(Utils.stringToBytes32(genre)).send();
        content.setPrice(price).send();
        content.setHostname(Utils.stringToBytes32(hostname)).send();
        content.setPort(new BigInteger(Integer.toString(port))).send();
        content.publish(catalogAddress).send();

        content.contentPublishedEventObservable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)
                .subscribe(e -> {
                    for (Runnable runnable : contentPublishedRunnables)
                        runnable.run();
                });
    }

    /**
     * Load and manage an existent content manager contract.
     * @param credentials your account credentials.
     * @param contractAddress the existent contract address on blockchain.
     */
    public ContentManager(Credentials credentials, String contractAddress) {
        super(credentials);
        content = (DAPPContentManagementContract) load(DAPPContentManagementContract.class, contractAddress);
    }

    /**
     * Subscribe a callback for content published events.
     * @param callback a Runnable.
     */
    public void listenContentPublished(Runnable callback) {
        contentPublishedRunnables.add(callback);
    }

    /**
     * Consume a bought content.
     * Note that the content is consumed by the user that owns the credentials passed to the constructor.
     * @return a boolean representing the operation outcome.
     */
    public boolean consumeContent() {
        try {
            return content.consumeContent().send().isStatusOK();
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
            return Utils.bytes32ToString(content.hostname().send());
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
            return content.port().send().intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}
