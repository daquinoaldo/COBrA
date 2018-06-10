const fs = require('fs');
const solc = require('solc');
const Web3 = require('web3');

const provider = "http://localhost:8545";

let web3;

/**
 * Connects to the Ethereum provider specified in the variable "provider".
 * @returns {Promise<Web3>} a Web3 instance.
 */
function connect() {
  return new Promise((resolve, reject) => {
    web3 = new Web3(new Web3.providers.HttpProvider(provider));
    if (!web3.isConnected()) reject("Cannot connect to "+provider+".");
    console.log("Connected to Web3: "+web3.version.node+".\n");
    resolve(web3);
  })
}

/**
 * Compile and deploy on Web3 a contract.
 * @param filename the sol file to be deployed.
 * @returns {Promise<web3.eth.contract>} the contract instance.
 */
function deployContract(filename) {
  return new Promise((resolve, reject) => {
    // Compile the source code
    const name = ":"+filename.replace(".sol", "");
    const input = fs.readFileSync(filename);
    const output = solc.compile(input.toString(), 1);
    const bytecode = output.contracts[name].bytecode;
    const abi = JSON.parse(output.contracts[name].interface);

    const contract = web3.eth.contract(abi);

    const options = {
      data: '0x' + bytecode,
      from: web3.eth.coinbase,
      gas: 4712388
    };

    // Deploy contract instance
    const contractInstance = contract.new(options, (err, res) => {
      if (err) reject(err);
      if (res.address) {
        console.log("Contract address: " + res.address);
        resolve(contractInstance);
      }
    });
  })
}

/**
 * Main function of the program.
 * @returns {Promise<void>} because the function is async.
 */
async function main() {
  await connect();
  const catalogContract = await deployContract('CatalogContract.sol');
  console.log(catalogContract.contentCost());
}


// noinspection JSIgnoredPromiseFromCall
main();

/*main()
  .catch(err => {
    console.error("ERROR: "+err);
    process.exit(1);
  });*/