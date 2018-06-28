package com.aldodaquino.cobra.main;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class Content implements Stringifiable {

    private String address;
    private String name;
    private String author;
    private String genre;
    private BigInteger price;
    private BigInteger views;
    private int enjoy;
    private int priceFairness;
    private int contentMeaning;

    public Content (String address, byte[] name, String author) {
        this.address = address;
        this.name = bytesToString(name);
        this.author = author;
    }

    public Content (String address, byte[] name, String author, byte[] genre, BigInteger price, BigInteger views) {
        this(address, name, author);
        this.genre = bytesToString(genre);
        this.price = price;
        this.views = views;
    }

    public Content (String address, byte[] name, String author, byte[] genre, BigInteger price, BigInteger views,
             BigInteger enjoy, BigInteger priceFairness, BigInteger contentMeaning) {
        new Content(address, name, author, genre, price, views);
        setEnjoy(enjoy);
        setPriceFairness(priceFairness);
        setContentMeaning(contentMeaning);
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public BigInteger getPrice() {
        return price;
    }

    public BigInteger getViews() {
        return views;
    }

    public void setViews(BigInteger views) {
        this.views = views;
    }

    public int getEnjoy() {
        return enjoy;
    }

    public void setEnjoy(BigInteger enjoy) {
        this.enjoy = enjoy.intValue();
    }

    public int getPriceFairness() {
        return priceFairness;
    }

    public void setPriceFairness(BigInteger priceFairness) {
        this.priceFairness = priceFairness.intValue();
    }

    public int getContentMeaning() {
        return contentMeaning;
    }

    public void setContentMeaning(BigInteger contentMeaning) {
        this.contentMeaning = contentMeaning.intValue();
    }

    public String bytesToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public String stringify() {
        return "\"address\n:" + address +
                "\"name\n:" + name +
                "\"author\n:" + author +
                "\"genre\n:" + genre +
                "\"price\n:" + price +
                "\"views\n:" + views +
                "\"enjoy\n:" + enjoy +
                "\"priceFairness\n:" + priceFairness +
                "\"contentMeaning\n:" + contentMeaning;
    }

}
