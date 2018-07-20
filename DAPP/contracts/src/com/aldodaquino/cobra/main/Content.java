package com.aldodaquino.cobra.main;

import java.math.BigInteger;

public class Content {

    public final String address;
    public final String name;
    public final String author;
    public final String genre;
    public final BigInteger price;
    public final BigInteger views;
    public final int averageRating;
    public final int enjoy;
    public final int priceFairness;
    public final int contentMeaning;

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
