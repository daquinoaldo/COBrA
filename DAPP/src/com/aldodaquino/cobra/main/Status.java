package com.aldodaquino.cobra.main;

import org.web3j.crypto.Credentials;

public class Status {

    private Credentials credentials;
    private Catalog catalog;

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

    public void deploy() {
        if (credentials == null)
            throw new UnsupportedOperationException("You must be logged in to deploy a new catalog.");
        catalog = new Catalog(credentials);
    }

}
