# COBrA
An Ethereum blockchain university project

## Installation
Just run `npm install`.

#### Troubleshooting
In case of problem with node-gyp run `sudo npm install -g node-gyp rebuild` (python 2 is needed).

If you are using MacOS X you may need to download Xcode from the app store, open it and accept the license/terms
agreement.

In case of problems with permissions (EACCES) try to remove the entire `node_modules` folder and run again `npm install`
without `sudo`.

If you got vulnerability alerts with this version of web3 you can use the latest version running
`npm install ethereum/web3.js` instead of `npm install`.