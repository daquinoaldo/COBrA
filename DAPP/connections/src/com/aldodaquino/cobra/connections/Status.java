package com.aldodaquino.cobra.connections;

import com.aldodaquino.cobra.main.CatalogManager;
import org.web3j.crypto.Credentials;

/**
 * Defines the status for the author-server.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class Status {
    public String privateKey;
    public Credentials credentials;
    public CatalogManager catalogManager;
    public String hostname;
    public int port;
}