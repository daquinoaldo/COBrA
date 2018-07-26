package com.aldodaquino.cobra.gui;

import com.aldodaquino.cobra.main.CatalogManager;
import org.web3j.crypto.Credentials;

import javax.naming.OperationNotSupportedException;

/**
 * The Status class. A Status object is generated in the {@link com.aldodaquino.cobra.gui.panels.StarterPanel}.
 * Contains all the information about the user and the catalog and is passed through the classes.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class Status {

    public static final int ROLE_CUSTOMER = 0;
    public static final int ROLE_AUTHOR = 1;

    private String privateKey;
    public Credentials credentials;
    private CatalogManager catalogManager;
    private int role;

    /**
     * Given a private key generates and stores the credentials for this user.
     * @param privateKey the user's private key.
     * @throws OperationNotSupportedException if the user is already logged in.
     */
    public void login (String privateKey) throws OperationNotSupportedException {
        this.privateKey = privateKey;
        if (privateKey == null || privateKey.length() == 0) throw new IllegalArgumentException("Empty private key");
        if (credentials != null)
            throw new OperationNotSupportedException("Already logged in as " + credentials.getAddress() + ".");
        credentials = Credentials.create(privateKey);
    }

    /**
     * Connects to an existent catalog and stores it.
     * @param catalogAddress the catalog address.
     * @throws OperationNotSupportedException if the user is not logged in.
     */
    public void connectCatalog(String catalogAddress) throws OperationNotSupportedException {
        if (catalogAddress == null || catalogAddress.length() == 0)
            throw new IllegalArgumentException("Empty catalog address");
        if (credentials == null)
            throw new OperationNotSupportedException("You must be logged in to connect to a catalog.");
        catalogManager = new CatalogManager(credentials, catalogAddress);
    }

    /**
     * Disconnects from the catalog.
     */
    public void disconnectCatalog() {
        catalogManager = null;
    }

    /**
     * Deploys a new catalog with the user credentials.
     * @throws OperationNotSupportedException if the user is not logged in.
     */
    public void deployCatalog() throws OperationNotSupportedException {
        if (credentials == null)
            throw new OperationNotSupportedException("You must be logged in to deploy a new catalog.");
        catalogManager = new CatalogManager(credentials);
    }

    /**
     * Returns the user address.
     * @return a String with the user address.
     * @throws OperationNotSupportedException if the user is not logged in.
     */
    public String getUserAddress() throws OperationNotSupportedException {
        if (credentials == null)
            throw new OperationNotSupportedException("You must be logged in.");
        return credentials.getAddress();
    }

    /**
     * Returns true if the user that deployed the catalog is the current user, false otherwise.
     * @return true if the current user is the catalog owner, false otherwise.
     * @throws OperationNotSupportedException if the user is not connected to a catalog.
     */
    public boolean isCatalogOwner() throws OperationNotSupportedException {
        if (catalogManager == null)
            throw new OperationNotSupportedException("Not connected to a catalog.");
        return catalogManager.getOwner().equals(getUserAddress());
    }

    /**
     * Returns the catalog manager.
     * @return the CatalogManager.
     */
    public CatalogManager getCatalogManager() {
        return catalogManager;
    }

    /**
     * Returns the user's private key.
     * @return a String with the user's private key.
     */
    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * Set the role for the current user.
     * @param role an int specifying the role.
     */
    public void setRole(int role) {
        this.role = role;
    }

    /**
     * Returns the role for the current user.
     * @return an int specifying the role.
     */
    int getRole() {
        return role;
    }
}
