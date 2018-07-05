package com.aldodaquino.cobra.main;

import org.web3j.crypto.Credentials;

import java.math.BigInteger;

public class Status {

    public static final int ROLE_CUSTOMER = 0;
    public static final int ROLE_AUTHOR = 1;

    private Credentials credentials;
    private CatalogManager catalogManager;
    private int role;

    public void login (String privateKey) {
        if (credentials != null)
            throw new UnsupportedOperationException("Already logged in as "+credentials.getAddress()+".");
        credentials = Credentials.create(privateKey);
    }

    public void connectCatalog(String catalogAddress) {
        if (credentials == null)
            throw new UnsupportedOperationException("You must be logged in to connect to a catalog.");
        catalogManager = new CatalogManager(credentials, catalogAddress);
    }

    public void disconnectCatalog() {
        catalogManager = null;
    }

    public void deployCatalog() {
        if (credentials == null)
            throw new UnsupportedOperationException("You must be logged in to deploy a new catalog.");
        catalogManager = new CatalogManager(credentials);
    }

    public String getUserAddress() {
        if (credentials == null)
            throw new UnsupportedOperationException("You must be logged in.");
        return credentials.getAddress();
    }

    public boolean isCatalogOwner() {
        if (catalogManager == null)
            throw new UnsupportedOperationException("Not connected to a catalog.");
        return catalogManager.getOwner().equals(getUserAddress());
    }

    public CatalogManager getCatalogManager() {
        return catalogManager;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getRole() {
        return role;
    }

    public void deployContent(String name, String genre, BigInteger price) {
        if (credentials == null)
            throw new UnsupportedOperationException("You must be logged in to deploy a new catalog.");
        new ContentManager(credentials, catalogManager.getAddress(), name, genre, price);
    }
}
