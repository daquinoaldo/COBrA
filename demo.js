const fs = require('fs');
const solc = require('solc');
const Web3 = require('web3');

const provider = "http://localhost:8545";
const genres = ["adventure", "fantasy", "romance", "horror"];
const contentsNumber = 1;
const gas = 4712388;

let web3;
let catalogContract;

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
    const outputContractName = filename+":"+filename.replace(".sol", "");
    const input = fs.readFileSync(filename);
    function findImports(path) {
      return { contents: fs.readFileSync(path).toString() }
    }
    const source = { };
    source[filename] = input.toString();
    const output = solc.compile({ sources: source }, 1, findImports);
    const bytecode = output.contracts[outputContractName].bytecode;
    const abi = JSON.parse(output.contracts[outputContractName].interface);

    const contract = web3.eth.contract(abi);

    const options = {
      data: '0x' + bytecode,
      from: address,
      gas: gas
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
  const contents = [];
  for (let i = 0; i < num; i++)
    contents[i] = {
      name: web3.fromAscii("title"+i),
      content: web3.fromAscii("This is content "+i),
      genre: web3.fromAscii(genres[rand(genres.length-1)])
    };
  return contents;
}

/**
 * Auxiliary function: deploy num content contracts on the blockchain and returns it in an array.
 * @param num the number of contents that you want to deploy.
 * returns {Promise<contract[]>} an array contract instances.
 */
async function deployContentsContract(num) {
  function getParams(owner = web3.eth.coinbase) { return { from: owner, gas: gas } }
  // deploy num empty Contents
  const promises = [];
  const contentContracts = [];
  for (let i = 0; i < num; i++)
    promises[i] = deployContract('GenericContentManagementContract.sol',
      web3.eth.accounts[rand(web3.eth.accounts.length)])
      .then(contractInstance => contentContracts.push(contractInstance));
  await Promise.all(promises);
  // set the name, content and genre of each content
  const contents = generateContents(num);
  for (let i = 0; i < num; i++) {
    const owner = contentContracts[i].author();
    contentContracts[i].setName(contents[i].name, getParams(owner));
    contentContracts[i].setContent(contents[i].content, getParams(owner));
    contentContracts[i].setGenre(contents[i].genre, getParams(owner));
    console.log(catalogContract.address);
    contentContracts[i].publish(catalogContract.address, getParams(owner));
  }
  return contentContracts;
}

/**
 * Auxiliary function: generates a random number.
 * @param to, optional, the last number of the range.
 * @param from, optional, the starting number of the range.
 * returns the random number.
 */
function rand(to = 1, from = 0) {
  if (from > to) return -1;
  if (from === to) return from;
  return from + Math.floor(Math.random() * to+1);
}

/**
 * Main function of the program.
 * @returns {Promise<void>} because the function is async.
 */
async function main() {
  await connect();
  // deploy the Catalog
  console.log("Deploying catalog...");
  catalogContract = await deployContract('CatalogContract.sol');
  console.log("\nDeploying "+contentsNumber+" contents...");
  const contentContracts = await deployContentsContract(contentsNumber);

  // TEST
  for (let i = 0; i < contentsNumber; i++)
    console.log(contentContracts[i].name());
  //console.log(catalogContract.contentCost());
}


// noinspection JSIgnoredPromiseFromCall
main();

/*main()
  .catch(err => {
    console.error("ERROR: "+err);
    process.exit(1);
  });*/
