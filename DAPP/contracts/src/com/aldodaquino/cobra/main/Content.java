package com.aldodaquino.cobra.main;

import java.math.BigInteger;

public class Content {

    public final String address;
    public final String name;
    public final String author;
    public final String genre;
    public final BigInteger price;
    public final BigInteger views;
    public final int enjoy;
    public final int priceFairness;
    public final int contentMeaning;

    Content(String address, byte[] name) {
        this.address = address;
        this.name = Utils.bytesToString(name);

        this.views = null;
        this.author = null;
        this.genre = null;
        this.price = null;
        this.enjoy = -1;
        this.priceFairness = -1;
        this.contentMeaning = -1;
    }

    Content(String address, byte[] name, BigInteger views) {
        this.address = address;
        this.name = Utils.bytesToString(name);
        this.views = views;

        this.author = null;
        this.genre = null;
        this.price = null;
        this.enjoy = -1;
        this.priceFairness = -1;
        this.contentMeaning = -1;
    }

    Content(String address, byte[] name, String author, byte[] genre, BigInteger price, BigInteger views,
            BigInteger enjoy, BigInteger priceFairness, BigInteger contentMeaning) {
        this.address = address;
        this.name = Utils.bytesToString(name);
        this.author = author;
        this.genre = Utils.bytesToString(genre);
        this.price = price;
        this.views = views;
        this.enjoy = enjoy.intValue();
        this.priceFairness = priceFairness.intValue();
        this.contentMeaning = contentMeaning.intValue();
    }

    /*public String stringify() {
        return "\"address\n:" + address + "," +
                "\"name\n:" + name + "," +
                "\"author\n:" + author + "," +
                "\"genre\n:" + genre + "," +
                "\"price\n:" + price + "," +
                "\"views\n:" + views + "," +
                "\"enjoy\n:" + enjoy + "," +
                "\"priceFairness\n:" + priceFairness + "," +
                "\"contentMeaning\n:" + contentMeaning;
    }TODO: delete*/

}
