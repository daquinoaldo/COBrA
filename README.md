# COBrA
An Ethereum blockchain university project.

Check the assignments for [COBrA](docs/COBrA_Assignment.pdf) and [COBrA DAPP](docs/COBrA_DAPP_Assignment.pdf) for more
information.

## Requirements
- [geth](https://geth.ethereum.org/downloads/)
- Java 10
    - [JRE](http://www.oracle.com/technetwork/java/javase/downloads/jre10-downloads-4417026.html)
    - [JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk10-downloads-4416644.html)
- [Apache Maven](https://maven.apache.org/install.html/)
    - Ubuntu: `sudo apt-get install maven`
    - MacOS: `sudo brew install maven` (requires [brew](https://docs.brew.sh/Installation))
    - [Binaries](https://maven.apache.org/download.cgi)

## Dependency installation with Maven
- `cd DAPP`
- `mvn install`

## Run
Start and Ethereum node on the testnet Ropsten with [geth](https://github.com/ethereum/go-ethereum/wiki/geth) running
`geth --rpcapi personal,db,eth,net,web3 --rpc --testnet` or start an emulated node with `ganache-cli`.

Start the DAPP with java target/DAPP-1.0.com.aldodaquino.cobra.gui.MainGUI