package com.aldodaquino.cobra.main;

import java.math.BigInteger;

/**
 * Defines a content object.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class Content {

    public final String address;        // required
    public final String name;           // required
    public final String author;         // can be null
    public final String genre;          // can be null
    public final BigInteger price;      // can be null
    public final BigInteger views;      // can be null
    public final int averageRating;     // can be -1
    public final int enjoy;             // can be -1
    public final int priceFairness;     // can be -1
    public final int contentMeaning;    // can be -1

    Content(String address, byte[] name) {
        this.address = address;
        this.name = Utils.bytes32ToString(name);

        this.views = null;
        this.author = null;
        this.genre = null;
        this.price = null;
        this.averageRating = -1;
        this.enjoy = -1;
        this.priceFairness = -1;
        this.contentMeaning = -1;
    }

    Content(String address, byte[] name, BigInteger views) {
        this.address = address;
        this.name = Utils.bytes32ToString(name);
        this.views = views;

        this.author = null;
        this.genre = null;
        this.price = null;
        this.averageRating = -1;
        this.enjoy = -1;
        this.priceFairness = -1;
        this.contentMeaning = -1;
    }

    Content(String address, byte[] name, String author, byte[] genre, BigInteger price, BigInteger views,
            BigInteger averageRating, BigInteger enjoy, BigInteger priceFairness, BigInteger contentMeaning) {
        this.address = address;
        this.name = Utils.bytes32ToString(name);
        this.author = author;
        this.genre = Utils.bytes32ToString(genre);
        this.price = price;
        this.views = views;
        this.averageRating = averageRating.intValue();
        this.enjoy = enjoy.intValue();
        this.priceFairness = priceFairness.intValue();
        this.contentMeaning = contentMeaning.intValue();
    }

}
