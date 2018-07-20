package com.aldodaquino.cobra.main;

import com.aldodaquino.cobra.contracts.CatalogContract;
import org.web3j.crypto.Credentials;
import org.web3j.tuples.generated.*;

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
     * Check if the user has access to a content.
     * @param address the content address.
     * @return boolean if has access, false otherwise.
     */
    public boolean hasAccess(String address) {
        return hasAccess(address, credentials.getAddress());
    }

    /**
     * Check if the specified user has access to a content.
     * @param address the content address.
     * @param user the user address.
     * @return boolean if has access, false otherwise.
     */
    public boolean hasAccess(String address, String user) {
        try {
            return catalog.hasAccess(user, address).send();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Buy a content.
     * @param address the content address.
     * @param price the content price.
     * @return a boolean representing the operation outcome.
     */
    public boolean buyContent(String address, BigInteger price) {
        try {
            return catalog.getContent(address, price).send().isStatusOK();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gift a content to another user.
     * @param address the content address.
     * @param user the user address.
     * @param price the content price.
     * @return a boolean representing the operation outcome.
     */
    public boolean giftContent(String address, String user, BigInteger price) {
        try {
            return catalog.giftContent(address, user, price).send().isStatusOK();
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
     * @return a boolean representing the operation outcome.
     */
    public boolean isPremium() {
        try {
            return catalog.isPremium(credentials.getAddress()).send();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* Getters for lists, statistics and charts */

    /**
     * Returns all the info and ratings of a content.
     * @return a list of Content objects.
     */
    public Content getContentInfo(String address) {
        try {
            Tuple5<byte[], String, byte[], BigInteger, BigInteger> info = catalog.getContentInfo(address).send();
            Tuple4<BigInteger, BigInteger, BigInteger, BigInteger> ratings = catalog.getContentRatings(address).send();

            return new Content(address, info.getValue1(), info.getValue2(), info.getValue3(), info.getValue4(),
                    info.getValue5(), ratings.getValue1(), ratings.getValue2(), ratings.getValue3(), ratings.getValue4());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns a list of all contents in the Catalog.
     * @return a list of Content objects.
     */
    public List<Content> getContentList() {
        try {
            Tuple2<List<byte[]>, List<String>> statistics = catalog.getContentList().send();
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
    public List<Content> getContentListWithViews() {
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
    public List<Content> getFullContentList() {
        ContentList contentList = new ContentList();
        return contentList.getFilteredContentList(null, null);
    }

    /**
     * Return the list of all the content of a given author.
     * @param author the authors address.
     * @return a list of Content objects.
     */
    public List<Content> getAuthorContents(String author) {
        ContentList contentList = new ContentList();
        return contentList.getFilteredContentList(contentList.authors, author);
    }

    /**
     * Return the n latest releases.
     * @param n the number of item that you want in the list.
     * @return List<Content> with the latest n contents.
     */
    public List<Content> getNewContentList(int n) {
        try {
            // get the list
            Tuple2<List<byte[]>, List<String>> res =
                    catalog.getNewContentList(new BigInteger(Integer.toString(n))).send();
            List<byte[]> names = res.getValue1();
            List<String> addresses = res.getValue2();

            // parse the list in a String matrix
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
     * Return the latest release.
     * @return String[] where the first element is the name of the content and the second is the address.
     */
    public Content getLatest() {
        List<Content> contents = getNewContentList(1);
        if (contents == null || contents.size() == 0) return null;
        return contents.get(0);
    }

    /**
     * Return the latest release for a genre.
     * @param genre the chosen genre.
     * @return String[] where the first element is the name of the content and the second is the address.
     */
    public Content getLatestByGenre(String genre) {
        try {
            Tuple2<byte[], String> res = catalog.getLatestByGenre(Utils.stringToBytes32(genre)).send();
            return new Content(res.getValue2(), res.getValue1());
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
    public Content getLatestByAuthor(String author) {
        try {
            Tuple2<byte[], String> res = catalog.getLatestByAuthor(author).send();
            return new Content(res.getValue2(), res.getValue1());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the most popular content.
     * @return Content.
     */
    public Content getMostPopular() {
        try {
            Tuple2<byte[], String> res = catalog.getMostPopular().send();
            return new Content(res.getValue2(), res.getValue1());
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
    public Content getMostPopularByGenre(String genre) {
        try {
            Tuple2<byte[], String> res = catalog.getMostPopularByGenre(Utils.stringToBytes32(genre)).send();
            return new Content(res.getValue2(), res.getValue1());
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
    public Content getMostPopularByAuthor(String author) {
        try {
            Tuple2<byte[], String> res = catalog.getMostPopularByAuthor(author).send();
            return new Content(res.getValue2(), res.getValue1());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the highest rated content.
     * @return Content.
     */
    public Content getMostRated(String category) {
        try {
            Tuple2<byte[], String> res = catalog.getMostRated(Utils.stringToBytes32(category)).send();
            return new Content(res.getValue2(), res.getValue1());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the highest rated content for a genre.
     * @param genre the chosen genre.
     * @return Content.
     */
    public Content getMostRatedByGenre(String genre, String category) {
        try {
            Tuple2<byte[], String> res = catalog.getMostRatedByGenre(Utils.stringToBytes32(genre),
                    Utils.stringToBytes32(category)).send();
            return new Content(res.getValue2(), res.getValue1());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the highest rated content of an author.
     * @param author the author address.
     * @return Content.
     */
    public Content getMostRatedByAuthor(String author, String category) {
        try {
            Tuple2<byte[], String> res = catalog.getMostRatedByAuthor(author, Utils.stringToBytes32(category)).send();
            return new Content(res.getValue2(), res.getValue1());
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
        List<BigInteger> averageRatings;
        List<BigInteger> enjoyRatings;
        List<BigInteger> priceFairnessRatings;
        List<BigInteger> contentMeaningRatings;

        ContentList() {
            try {
                // Query the CatalogContract for the list
                Tuple6<List<String>, List<byte[]>, List<String>, List<byte[]>, List<BigInteger>, List<BigInteger>>
                        fullContentList = catalog.getFullContentList().send();
                Tuple5<List<String>, List<BigInteger>, List<BigInteger>, List<BigInteger>, List<BigInteger>>
                        ratingsList = catalog.getRatingsList().send();

                // Parse parameters
                addresses = fullContentList.getValue1();
                names = fullContentList.getValue2();
                authors = fullContentList.getValue3();
                genres = fullContentList.getValue4();
                prices = fullContentList.getValue5();
                views = fullContentList.getValue6();
                averageRatings = ratingsList.getValue2();
                enjoyRatings = ratingsList.getValue3();
                priceFairnessRatings = ratingsList.getValue4();
                contentMeaningRatings = ratingsList.getValue5();
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
        <T> List<Content> getFilteredContentList(List<T> filterBy, T filterValue) {
            // Build an usable list
            List<Content> contentList = new ArrayList<>();
            for (int i = 0; i < addresses.size(); i++)
                // if the where list is null do not filter
                if (filterBy == null || filterBy.get(i).equals(filterValue))
                    contentList.add(new Content(
                            addresses.get(i),
                            names.get(i),
                            authors.get(i),
                            genres.get(i),
                            prices.get(i),
                            views.get(i),
                            averageRatings.get(i),
                            enjoyRatings.get(i),
                            priceFairnessRatings.get(i),
                            contentMeaningRatings.get(i)
                            ));
            return contentList;
        }
    }

}
