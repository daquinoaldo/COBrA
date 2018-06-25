import com.aldodaquino.cobra.CatalogContract;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import static org.web3j.tx.Transfer.GAS_LIMIT;  // transaction gas limit
import static org.web3j.tx.gas.DefaultGasProvider.GAS_PRICE;    // average gas price

public class Catalog {

    private Credentials credentials;
    private CatalogContract contract;

    public Catalog() {
        // connect to web3
        Web3j web3 = Web3j.build(new HttpService());    // defaults to http://localhost:8545/

        // load credentials
        credentials = new Wallet().getCredentials();

        // deploy contract
        try {
            contract = CatalogContract.deploy(web3, credentials, GAS_PRICE, GAS_LIMIT).send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
