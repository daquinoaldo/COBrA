package com.aldodaquino.cobra.main;

import com.aldodaquino.cobra.contracts.CatalogContract;
import org.web3j.crypto.Credentials;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tuples.generated.Tuple6;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CatalogManager extends ContractManager {

    private final CatalogContract catalog;

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

    /* Catalog interaction methods */

    /**
     * Buy a content.
     * @param address the content address.
     * @return a boolean representing the operation outcome.
     */
    public boolean buyContent(String address) {
        try {
            BigInteger contentCost = catalog.contentCost().send();
            return catalog.getContent(address, contentCost).send().isStatusOK();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gift a content to another user.
     * @param address the content address.
     * @param user the user address.
     * @return a boolean representing the operation outcome.
     */
    public boolean giftContent(String address, String user) {
        try {
            BigInteger contentCost = catalog.contentCost().send();
            return catalog.giftContent(address, user, contentCost).send().isStatusOK();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Buy a premium subscription.
     * @return a boolean representing the operation outcome.
     */
    public boolean buyPremium() {
        try {
            BigInteger premiumCost = catalog.premiumCost().send();
            return catalog.buyPremium(premiumCost).send().isStatusOK();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gift a premium subscription to another user.
     * @param user the user address.
     * @return a boolean representing the operation outcome.
     */
    public boolean giftPremium(String user) {
        try {
            BigInteger contentCost = catalog.premiumCost().send();
            return catalog.giftPremium(user, contentCost).send().isStatusOK();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Return true if the user has an active premium subscription.
     * @param user the user address.
     * @return a boolean representing the operation outcome.
     */
    public boolean isPremium(String user) {
        try {
            return catalog.isPremium(user).send();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* Getters for lists, statistics and charts */

    /**
     * Returns a list of all contents in the Catalog and its views.
     * @return a list of Content objects.
     */
    public List<Content> getContentsList() {
        try {
            Tuple2<List<byte[]>, List<String>> statistics = catalog.getContentsList().send();
            List<byte[]> names = statistics.getValue1();
            List<String> addresses = statistics.getValue2();

            List<Content> contents = new ArrayList<>();
            for (int i = 0; i < names.size(); i++)
                contents.add(new Content(addresses.get(i), names.get(i)));

            return contents;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns a list of all contents in the Catalog and its views.
     * @return a list of Content objects.
     */
    public List<Content> getContentsListWithViews() {
        try {
            Tuple3<List<byte[]>, List<String>, List<BigInteger>> statistics = catalog.getStatistics().send();
            List<byte[]> names = statistics.getValue1();
            List<String> addresses = statistics.getValue2();
            List<BigInteger> views = statistics.getValue3();

            List<Content> contents = new ArrayList<>();
            for (int i = 0; i < names.size(); i++)
                contents.add(new Content(addresses.get(i), names.get(i), views.get(i)));

            return contents;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns a list of all contents in the Catalog with all the data available.
     * @return a list of Content objects.
     */
    public List<Content> getFullContentsList() {
        ContentList contentList = new ContentList();
        return contentList.getFilteredContentsList(null, null);
    }

    /**
     * Return the list of all the content of a given author.
     * @param author the authors address.
     * @return a list of Content objects.
     */
    public List<Content> getAuthorContents(String author) {
        ContentList contentList = new ContentList();
        return contentList.getFilteredContentsList(contentList.authors, author);
    }

    /**
     * Return the n latest releases.
     * @param n the number of item that you want in the list.
     * @return Map<name of the content, address of the content>.
     */
    public String[][] getNewContentsList(int n) {
        try {
            // get the list
            Tuple2<List<byte[]>, List<String>> res =
                    catalog.getNewContentsList(new BigInteger(Integer.toString(n))).send();
            List<byte[]> names = res.getValue1();
            List<String> addresses = res.getValue2();

            // parse the list in a String matrix
            String[][] rows = new String[names.size()][2];
            for (int i = 0; i < names.size(); i++) {
                rows[i][0] = Utils.bytesToString(names.get(i));
                rows[i][1] = addresses.get(i);
            }
            return rows;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the latest release.
     * @return String[] where the first element is the name of the content and the second is the address.
     */
    public String[] getLatest() {
        String[][] rows = getNewContentsList(1);
        if (rows == null || rows.length == 0) return null;
        return rows[0];
    }

    /**
     * Return the latest release for a genre.
     * @param genre the chosen genre.
     * @return String[] where the first element is the name of the content and the second is the address.
     */
    public String[] getLatestByGenre(String genre) {
        try {
            Tuple2<byte[], String> res = catalog.getLatestByGenre(Utils.stringToBytes(genre)).send();
            return new String[] {Utils.bytesToString(res.getValue1()), res.getValue2()};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the latest release of an author.
     * @param author the author address.
     * @return String[] where the first element is the name of the content and the second is the address.
     */
    public String[] getLatestByAuthor(String author) {
        try {
            Tuple2<byte[], String> res = catalog.getLatestByAuthor(author).send();
            return new String[] {Utils.bytesToString(res.getValue1()), res.getValue2()};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the most popular content.
     * @return String[] where the first element is the name of the content and the second is the address.
     */
    public String[] getMostPopular() {
        try {
            Tuple2<byte[], String> res = catalog.getMostPopular().send();
            return new String[] {Utils.bytesToString(res.getValue1()), res.getValue2()};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the most popular content for a genre.
     * @param genre the chosen genre.
     * @return String[] where the first element is the name of the content and the second is the address.
     */
    public String[] getMostPopularByGenre(String genre) {
        try {
            Tuple2<byte[], String> res = catalog.getMostPopularByGenre(Utils.stringToBytes(genre)).send();
            return new String[] {Utils.bytesToString(res.getValue1()), res.getValue2()};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the most popular content of an author.
     * @param author the author address.
     * @return String[] where the first element is the name of the content and the second is the address.
     */
    public String[] getMostPopularByAuthor(String author) {
        try {
            Tuple2<byte[], String> res = catalog.getMostPopularByAuthor(author).send();
            return new String[] {Utils.bytesToString(res.getValue1()), res.getValue2()};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the highest rated content.
     * @return String[] where the first element is the name of the content and the second is the address.
     */
    public String[] getMostRated(String category) {
        try {
            Tuple2<byte[], String> res = catalog.getMostRated(Utils.stringToBytes(category)).send();
            return new String[] {Utils.bytesToString(res.getValue1()), res.getValue2()};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the highest rated content for a genre.
     * @param genre the chosen genre.
     * @return String[] where the first element is the name of the content and the second is the address.
     */
    public String[] getMostRatedByGenre(String genre, String category) {
        try {
            Tuple2<byte[], String> res = catalog.getMostRatedByGenre(Utils.stringToBytes(genre),
                    Utils.stringToBytes(category)).send();
            return new String[] {Utils.bytesToString(res.getValue1()), res.getValue2()};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the highest rated content of an author.
     * @param author the author address.
     * @return String[] where the first element is the name of the content and the second is the address.
     */
    public String[] getMostRatedByAuthor(String author, String category) {
        try {
            Tuple2<byte[], String> res = catalog.getMostRatedByAuthor(author, Utils.stringToBytes(category)).send();
            return new String[] {Utils.bytesToString(res.getValue1()), res.getValue2()};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* Authors method */

    public BigInteger withdraw(String address) {
        try {
            BigInteger amount = catalog.payoutAvailable(address).send();
            if (!amount.equals(BigInteger.ZERO))
                catalog.collectPayout(address).send();
            return amount;
        } catch (Exception e) {
            e.printStackTrace();
            return BigInteger.ZERO;
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
        List<BigInteger> enjoyRatings;
        List<BigInteger> priceFairnessRatings;
        List<BigInteger> contentMeaningRatings;

        ContentList() {
            try {
                // Query the CatalogContract for the list
                Tuple6<List<String>, List<byte[]>, List<String>, List<byte[]>, List<BigInteger>, List<BigInteger>>
                        fullContentList = catalog.getFullContentsList().send();
                Tuple4<List<String>, List<BigInteger>, List<BigInteger>, List<BigInteger>>
                        ratingsList = catalog.getRatingsList().send();

                // Parse parameters
                addresses = fullContentList.getValue1();
                names = fullContentList.getValue2();
                authors = fullContentList.getValue3();
                genres = fullContentList.getValue4();
                prices = fullContentList.getValue5();
                views = fullContentList.getValue6();
                enjoyRatings = ratingsList.getValue2();
                priceFairnessRatings = ratingsList.getValue3();
                contentMeaningRatings = ratingsList.getValue4();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                    contentsList.add(new Content(
                            addresses.get(i),
                            names.get(i),
                            authors.get(i),
                            genres.get(i),
                            prices.get(i), views.get(i),
                            enjoyRatings.get(i),
                            priceFairnessRatings.get(i),
                            contentMeaningRatings.get(i)
                            ));
            return contentsList;
        }
    }

}
