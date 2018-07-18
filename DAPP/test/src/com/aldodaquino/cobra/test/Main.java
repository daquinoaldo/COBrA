package com.aldodaquino.cobra.test;

import com.aldodaquino.cobra.authorserver.CliHelper;
import com.aldodaquino.cobra.gui.HttpHelper;
import com.aldodaquino.cobra.main.CatalogManager;
import org.web3j.crypto.Credentials;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Prepares an environment for tests:
 *  - deploys a CatalogContract;
 *  - starts an author-server on the default port 8080;
 *  - deploy some contents;
 *  - start two instances of the GUI, one for the Customer and one for the Author.
 *  You have to login with the rights private key on the GUIs.
 */
public class Main {

    private static final int NUMBER_OF_CONTENTS = 15;
    private static final String[] genres = {"Comedy", "Romance", "Thriller"};

    // Change this value with your private keys.
    private static final String CATALOG_OWNER_DEFAULT_KEY = "f22196df42e288dc5c94efe33f19808c12e5af29b667c2e354cb92d1a444c527";
    private static final String[] AUTHOR_DEFAULT_KEYS = {"c148832fde8619543c6573f3da7241ef5d9f0be12fbc9caf3606f1aaa0a47690",
            "68630d4d647d14d93b6954fec63094ef6b18ea0a6cd372d1489716b894014354"};
    //TODO reset
    //private static final String CATALOG_OWNER_DEFAULT_KEY = "";
    //private static final String[] AUTHOR_DEFAULT_KEYS = {""};

    /**
     * Starts the tests.
     * @param args a String[], optionally containing 2 options:
     *             -p --private-key    Catalog owner private key.
     *             -a --author         Author's private key.
     *             If {@param args} is null or an option is missing the default option will be used.
     */
    public static void main(String[] args) {

        // Parse cmd options
        CliHelper cliHelper = new CliHelper();
        cliHelper.addOption("h", "help", false, "Print this help message.");
        cliHelper.addOption("k", "private-key", true, "Catalog owner private key.");
        cliHelper.addOption("a", "author", true, "Author's private key.");
        cliHelper.parse(args);

        String catalogOwnerKeyOpt = cliHelper.getValue("private-key");
        String catalogOwnerKey = catalogOwnerKeyOpt.length() > 0 ? catalogOwnerKeyOpt : CATALOG_OWNER_DEFAULT_KEY;

        String[] authorKeysOpt = cliHelper.getValues("author");
        String[] authorKeys = authorKeysOpt.length > 0 ? authorKeysOpt : AUTHOR_DEFAULT_KEYS;


        // Create credentials for catalog owner's private key
        Credentials catalogOwnerCredentials = Credentials.create(catalogOwnerKey);
        System.out.println("Created credentials for catalog owner. Account address: "
                + catalogOwnerCredentials.getAddress() + ".\n");


        // Deploy a new Catalog and retrieve the address
        CatalogManager catalogManager = new CatalogManager(catalogOwnerCredentials);
        String catalogAddress = catalogManager.getAddress();
        System.out.println("Deployed a new CatalogContract. Catalog address: " + catalogAddress + ".\n");


        // Start n author servers on ports 8000-(8000+n-1),
        // bound with the catalog and associated to the author's private key
        System.out.println("Starting "+ authorKeys.length + " author servers, please wait...");
        int[] port = {8000};  // "Clickety-click... Barba-trick!": in lambda expressions we can only use final
                                    // (or effectively final) variables.
        for (String authorKey : authorKeys) {
            new Thread(() -> {
                try {
                    com.aldodaquino.cobra.authorserver.Main.main(
                            new String[]{"-k", authorKey, "-c", catalogAddress, "-p", Integer.toString(port[0]++)});
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        // Wait 3 seconds for the server to become online.
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            System.err.println("Interrupted during while waiting the author server becomes online.");
            e.printStackTrace();
        }


        // Deploying contents
        for (int i = 0; i < NUMBER_OF_CONTENTS; i++) {
            int authorIndex = rand(authorKeys.length);
            deploy(8000 + authorIndex, authorKeys[authorIndex], "Content " + (i+1),
                    genres[rand(genres.length)], Integer.toString(rand(5) * 5000));
        }


        // Start the GUI
        System.out.print("Starting two GUI windows...");
        //new Thread(() -> com.aldodaquino.cobra.gui.Main.main(null)).start();
        //new Thread(() -> com.aldodaquino.cobra.gui.Main.main(null)).start();

        Process GUI1 = newGUIProcess();
        Process GUI2 = newGUIProcess();

        // Wait for the GUIs to end
        try {
            assert GUI1 != null && GUI2 != null;
            GUI1.waitFor();
            GUI2.waitFor();
            System.out.println("    GUI 1 exit with value " + GUI1.exitValue()
                    + ".\n    GUI 2 exit with value " + GUI2.exitValue() + ".\n\n");
            System.exit(GUI1.exitValue() + GUI2.exitValue()); // 0 if none fails, 1 if one fails, 2 if both fail.
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    private static int rand(int lessThan) {
        return ThreadLocalRandom.current().nextInt(0, lessThan);
    }

    private static void deploy(int port, String authorKey, String name, String genre, String price) {
        // get input data
        String url = "http://localhost:" + port + "/deploy";

        // make the request
        Map<String, String> parameters = new HashMap<>();
        parameters.put("privateKey", authorKey);
        parameters.put("name", name);
        parameters.put("genre", genre);
        parameters.put("price", price);

        System.out.println("Deploying a content..." +
                "\n    Url: " + url +
                "\n    Author key: " + authorKey +
                "\n    Name: " + name +
                "\n    Genre: " + genre +
                "\n    Price: " + price);

        HttpHelper.Response response = HttpHelper.makePost(url, parameters);
        if (response.code != 200) System.err.println("Something went wrong. Response" + response.toString());
        else System.out.println("Deployed successfully.\n");
    }

    private static Process newGUIProcess() {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = com.aldodaquino.cobra.gui.Main.class.getCanonicalName();
        ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className);
        try {
            return builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
