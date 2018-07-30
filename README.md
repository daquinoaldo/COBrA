# COBrA
An Ethereum blockchain university project.

Check the assignments for [COBrA](docs/assignment/COBrA_Assignment.pdf) and [COBrA DAPP](docs/assignment/COBrA_DAPP_Assignment.pdf) and the
[COBrA DAPP Relationship](docs/COBrA_DAPP_Relationship.pdf) for more information.

## Requirements
- [Ethereum (geth)](https://geth.ethereum.org/downloads/)
    - Ubuntu:
        - `sudo add-apt-repository -y ppa:ethereum/ethereum`
        - `sudo apt-get update`
        - `sudo apt-get install ethereum`
    - MacOS:
        - `brew tap ethereum/ethereum`
        - `brew install ethereum`
    - Compile sources (requires [Go](https://golang.org/dl/)):
        - `go install github.com/ethereum/go-ethereum/cmd/geth`
- [Solidity compiler](https://github.com/ethereum/solidity)
    - Ubuntu:
        - `sudo add-apt-repository ppa:ethereum/ethereum`
        - `sudo apt-get update`
        - `sudo apt-get install solc`
    - MacOS:
        - `brew tap ethereum/ethereum`
        - `brew install solidity`
    - [Build from sources](http://solidity.readthedocs.io/en/v0.4.24/installing-solidity.html#building-from-source)
- Java 10
    - [JRE](http://www.oracle.com/technetwork/java/javase/downloads/jre10-downloads-4417026.html)
    - [JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk10-downloads-4416644.html)
- [Apache Maven](https://maven.apache.org/install.html/)
    - Ubuntu: `sudo apt-get install maven`
    - MacOS: `sudo brew install maven` (requires [brew](https://docs.brew.sh/Installation))
    - [Binaries](https://maven.apache.org/download.cgi)

## Run
### Compile sources
To compile solidity contracts, generate Java contract with web3j and build JARs run `bash install.sh`.

### Ethereum client
Start and Ethereum node on the testnet Ropsten with [geth](https://github.com/ethereum/go-ethereum/wiki/geth) running
`geth --rpcapi personal,db,eth,net,web3 --rpc --testnet` or start an emulated node with `ganache-cli`.

### App
Run the GUI with `java -jar DAPP/jar/gui-1.0-jar-with-dependencies.jar`. It starts a wizard that allows you to create
credentials from your private key, deploy a new catalog or connect to an existent one and choose if you want the
author's or the customer's view.

Authors also need a running author-server in which store their content in order to deploy a ContentManagementContract.
You can run an author-server with `java -jar DAPP/jar/author-server-1.0-jar-with-dependencies.jar -k <your-private-key>
-c <existent-catalog-contract-address>`.  
The contract deploy can be done from the author's GUI, then the GUI can be stopped and the content will remain available
as far as the author-server remain online.