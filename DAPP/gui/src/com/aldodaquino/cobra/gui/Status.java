package com.aldodaquino.cobra.gui;

import com.aldodaquino.cobra.main.CatalogManager;
import org.web3j.crypto.Credentials;

import javax.naming.OperationNotSupportedException;

public class Status {

    public static final int ROLE_CUSTOMER = 0;
    public static final int ROLE_AUTHOR = 1;

    private String privateKey;
    private Credentials credentials;
    private CatalogManager catalogManager;
    private int role;

    public void login (String privateKey) throws OperationNotSupportedException {
        this.privateKey = privateKey;
        if (privateKey == null || privateKey.length() == 0) throw new IllegalArgumentException("Empty private key");
        if (credentials != null)
            throw new OperationNotSupportedException("Already logged in as "+credentials.getAddress()+".");
        credentials = Credentials.create(privateKey);
    }

    public void connectCatalog(String catalogAddress) throws OperationNotSupportedException {
        if (catalogAddress == null || catalogAddress.length() == 0)
            throw new IllegalArgumentException("Empty catalog address");
        if (credentials == null)
            throw new OperationNotSupportedException("You must be logged in to connect to a catalog.");
        catalogManager = new CatalogManager(credentials, catalogAddress);
    }

    public void disconnectCatalog() {
        catalogManager = null;
    }

    public void deployCatalog() throws OperationNotSupportedException {
        if (credentials == null)
            throw new OperationNotSupportedException("You must be logged in to deploy a new catalog.");
        catalogManager = new CatalogManager(credentials);
    }

    public String getUserAddress() throws OperationNotSupportedException {
        if (credentials == null)
            throw new OperationNotSupportedException("You must be logged in.");
        return credentials.getAddress();
    }

    public boolean isCatalogOwner() throws OperationNotSupportedException {
        if (catalogManager == null)
            throw new OperationNotSupportedException("Not connected to a catalog.");
        return catalogManager.getOwner().equals(getUserAddress());
    }

    public CatalogManager getCatalogManager() {
        return catalogManager;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getRole() {
        return role;
    }
}
