package com.aldodaquino.cobra.main;

import org.web3j.crypto.Credentials;

public class Status {

    public static final int ROLE_CUSTOMER = 0;
    public static final int ROLE_AUTHOR = 1;

    private Credentials credentials;
    private Catalog catalog;
    private int role;

    public void login (String privateKey) {
        if (credentials != null)
            throw new UnsupportedOperationException("Already logged in as "+credentials.getAddress()+".");
        credentials = Credentials.create(privateKey);
    }

    public void connect(String catalogAddress) {
        if (credentials == null)
            throw new UnsupportedOperationException("You must be logged in to connect to a catalog.");
        catalog = new Catalog(credentials, catalogAddress);
    }

    public void disconnect() {
        catalog = null;
    }

    public void deploy() {
        if (credentials == null)
            throw new UnsupportedOperationException("You must be logged in to deploy a new catalog.");
        catalog = new Catalog(credentials);
    }

    public boolean isCatalogOwner() {
        if (credentials == null)
            throw new UnsupportedOperationException("You must be logged in.");
        if (catalog == null)
            throw new UnsupportedOperationException("Not connected to a catalog.");
        return catalog.getOwner().equals(credentials.getAddress());
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getRole() {
        return role;
    }
}
