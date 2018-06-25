import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class Wallet {

    private static final String WALLET_PATH = "wallet";
    private static final String WALLET_PW = "password";

    private Credentials credentials;

    public Wallet() {
        try {
            // load the wallet folder
            File folder = new File(WALLET_PATH);
            // create the folder  if not exist
            folder.mkdirs();
            // list files on folder
            File[] files = folder.listFiles();

            String walletPath;

            if (files == null || files.length == 0) {  // no wallets
                String fileName = null;
                fileName = WalletUtils.generateFullNewWalletFile(WALLET_PW, folder);
                walletPath = WALLET_PATH + "/" + fileName;
                System.out.println("Wallet created in " + walletPath);
            }
            else walletPath = files[0].getName();

            // load credentials
            credentials = WalletUtils.loadCredentials(WALLET_PW, walletPath);
        } catch (IOException | NoSuchAlgorithmException | CipherException | NoSuchProviderException |
                InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot load or create credentials.");
        }
    }

    public Credentials getCredentials() {
        return credentials;
    }
}
