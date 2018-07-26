package com.aldodaquino.cobra.test;

import com.aldodaquino.javautils.CliHelper;
import com.aldodaquino.javautils.FileExchange;
import com.aldodaquino.cobra.connections.API;
import com.aldodaquino.cobra.connections.CobraHttpHelper;
import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.main.CatalogManager;
import org.web3j.crypto.Credentials;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.ServerSocketChannel;
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
 *  @author Aldo D'Aquino.
 *  @version 1.0.
 */
public class Main {

    private static final boolean START_GUIS = false;

    private static final int NUMBER_OF_CONTENTS = 15;
    private static final String[] genres = {"Comedy", "Romance", "Thriller"};
    private static URI FILENAME;

    // static constructor
    static {
        try {
            FILENAME = Main.class.getResource("/test_file.png").toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    // Change this value with your private keys.
    private static final String CATALOG_OWNER_DEFAULT_KEY = "be35bfe5c861f3478feccc3dd647f5a27791260a41e8e77bdf5d156f95200525";
    private static final String[] AUTHOR_DEFAULT_KEYS = {"5a17260c7c368910b242bda4138df4e5390700b59524f148f7ee038fa429e69c",
            "9717a1dbb7710336f4d5d753f4b9b8359083779830f317641f17e1ba52f13a06"};
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
                            new String[]{"-k", authorKey, "-c", catalogAddress, "-n", "localhost",
                                    "-p", Integer.toString(port[0]++)});
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
        if (START_GUIS) {
            System.out.print("Starting two GUI windows...");
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

    }

    /* Auxiliary functions */

    private static int rand(int lessThan) {
        return ThreadLocalRandom.current().nextInt(0, lessThan);
    }

    private static void deploy(int serverPort, String authorKey, String name, String genre, String price) {
        // assemble the url
        String url = "http://localhost:" + serverPort + API.DEPLOY_API_PATH;

        // prepare the file
        ServerSocketChannel serverSocketChannel = FileExchange.openFileSocket();
        if (serverSocketChannel == null) {
            Utils.newErrorDialog("Error while opening server socket.");
            return;
        }

        int port = serverSocketChannel.socket().getLocalPort();
        FileExchange.startFileSender(serverSocketChannel, new File(FILENAME),
                () -> System.out.println("File uploaded successfully."));

        // make the request
        Map<String, String> parameters = new HashMap<>();
        parameters.put("privateKey", authorKey);
        parameters.put("name", name);
        parameters.put("genre", genre);
        parameters.put("price", price);
        parameters.put("port", Integer.toString(port));

        System.out.println("Deploying a content..." +
                "\n    Url: " + url +
                "\n    Author key: " + authorKey +
                "\n    Name: " + name +
                "\n    Genre: " + genre +
                "\n    Price: " + price);

        CobraHttpHelper.Response response = CobraHttpHelper.makePost(url, parameters);
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
