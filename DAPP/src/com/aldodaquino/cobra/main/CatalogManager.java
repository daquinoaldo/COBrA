package com.aldodaquino.cobra.main;

import com.aldodaquino.cobra.contracts.CatalogContract;
import org.web3j.crypto.Credentials;
import org.web3j.tuples.generated.Tuple6;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CatalogManager extends ContractManager {

    private CatalogContract catalog;

    /*
     * CONSTRUCTORS
     */

    /**
     * Deploy and manage a new catalog contract.
     * @param credentials your account credentials.
     */
    public CatalogManager(Credentials credentials) {
        super(credentials);
        catalog = (CatalogContract) deploy(CatalogContract.class);
    }

    /**
     * Load and manage an existent catalog contract.
     * @param credentials your account credentials.
     * @param contractAddress the existent contract address on blockchain.
     */
    public CatalogManager(Credentials credentials, String contractAddress) {
        super(credentials);
        catalog = (CatalogContract) load(CatalogContract.class, contractAddress);
    }

    /*
     * CATALOG CONTRACT SPECIFIC METHODS
     */

    /**
     * Return the list of all the content of a given author.
     * @param author the authors address.
     * @return a list of Content objects.
     */
    public List<Content> getAuthorContents(String author) {
        try {
            ContentList contentList = new ContentList();
            return contentList.getFilteredContentsList(contentList.authors, author);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* Authors method */

    public boolean withdraw(String address) {
        try {
            catalog.collectPayout(address).send();
            // TODO: how much I have withdraw?
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Auxiliary class
     */
    private class ContentList {

        List<String> addresses;
        List<byte[]> names;
        List<String> authors;
        List<byte[]> genres;
        List<BigInteger> prices;
        List<BigInteger> views;

        ContentList() throws Exception {
            // Query the CatalogContract for the list
            Tuple6<List<String>, List<byte[]>, List<String>, List<byte[]>, List<BigInteger>, List<BigInteger>> list;
            list = catalog.getFullContentsList().send();

            // Parse parameters
            addresses = list.getValue1();
            names = list.getValue2();
            authors = list.getValue3();
            genres = list.getValue4();
            prices = list.getValue5();
            views = list.getValue6();
        }

        /**
         * Returns a list of all contents that has the parameter where equals to value.
         * The list is not filtered if where is null.
         * @param filterBy a list of this class that can be addresses, names, authors, genres, prices or views.
         * @param filterValue the value that filterBy must have.
         * @return a list of Content objects.
         */
        <T> List<Content> getFilteredContentsList(List<T> filterBy, T filterValue) {
            // Build an usable list
            List<Content> contentsList = new ArrayList<>();
            for (int i = 0; i < addresses.size(); i++)
                // if the where list is null do not filter
                if (filterBy == null || filterBy.get(i).equals(filterValue))
                    contentsList.add(new Content(addresses.get(i), names.get(i), authors.get(i), genres.get(i),
                            prices.get(i), views.get(i)));
            return contentsList;
        }

        /**
         * Returns a list of all contents in the Catalog.
         * @return a list of Content objects.
         */
        List<Content> getFullContentsList() {
            return getFilteredContentsList(null, null);
        }
    }

}
