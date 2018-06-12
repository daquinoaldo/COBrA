const fs = require('fs');
const solc = require('solc');
const Web3 = require('web3');

const provider = "http://localhost:8545";
const genres = ["adventure", "fantasy", "romance", "horror"];
const contentsNum = 30;

let web3;
const contentContracts[];

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
 * @param address, optional, the address with which deploy the contract. If not specify is the coinbase.
 * @returns {Promise<web3.eth.contract>} the contract instance.
 */
function deployContract(filename = "Contract.sol", address = web3.eth.coinbase) {
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
      from: address,
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
 * Auxiliary function: generates num contracts, that are object with 3 parameters: name, content and genre.
 * @param num the number of contents that you want to generate.
 * returns an array of contents object.
 */
function generateContents(num = 0) {
  const contents[];
  for (int i = 0; i < num; i++)
    contents[i] = {
      name: "title"+i,
      content: "This is content "+i,
      genre: genres[rand(0, genres.length-1)]
    };
  return contents;
}

/**
 * Auxiliary function: generates a random number.
 * @param to, optional, the last number of the range.
 * @param from, optional, the starting number of the range.
 * returns the random number.
 */
function rand(to = 1, from = 0) {
  if (from > to) return -1;
  if (from == to) return from;
  return from + Math.floor(Math.random() * to+1);
}

/**
 * Main function of the program.
 * @returns {Promise<void>} because the function is async.
 */
async function main() {
  await connect();
  // deploy the Catalog
  const catalogContract = await deployContract('CatalogContract.sol');
  // deploy contentsNum empty Contents
  const promises[];
  for (i = 0; i < contentsNum, i++)
    promises[i] = deployContract('BaseContentManagementContract.sol')
      .then(contractInstance => contentContracts.push(contractInstance);
  await Promise.all(promises);
  // set the name, content and genre of each content
  const contents = generateContents(contentsNum);
  for (i = 0; i < contentsNum, i++) {
    contentContracts[i].setName(contents[i].name);
    contentContracts[i].setContent(contents[i].content);
    contentContracts[i].setGenre(contents[i].genre);
    contentContracts[i].publish(catalogContract.address);
  }
  //console.log(catalogContract.contentCost());
}


// noinspection JSIgnoredPromiseFromCall
main();

/*main()
  .catch(err => {
    console.error("ERROR: "+err);
    process.exit(1);
  });*/
