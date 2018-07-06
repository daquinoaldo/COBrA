# COBrA
An Ethereum blockchain university project.

Check the assignments for [COBrA](docs/COBrA_Assignment.pdf) and [COBrA DAPP](docs/COBrA_DAPP_Assignment.pdf) for more
information.

## Requirements
- [Node.js](https://nodejs.org/it/download/)
- [geth](https://geth.ethereum.org/downloads/) (optional)

## Dependency installation
To prepare test suit just run `npm install`.

#### Troubleshooting
In case of problem with node-gyp run `sudo npm install -g node-gyp rebuild` (python 2 is needed).

If you are using MacOS X you may need to download Xcode from the app store, open it and accept the license/terms
agreement.

In case of problems with permissions (EACCES) try to remove the entire `node_modules` folder and run again `npm install`
without `sudo`.

If you got vulnerability alerts with this version of web3 you can use the latest version running
`npm install ethereum/web3.js` instead of `npm install`.

## Run
The test suite requires a web3 interface running at `http://localhost:8545`.
You can use [geth](https://github.com/ethereum/go-ethereum/wiki/geth) or you can run `node node_modules/.bin/testrpc`
to start the RPC testing environment.  

To start the tests just run `npm start` or `node demo.js`.