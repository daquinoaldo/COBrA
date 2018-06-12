const fs = require('fs');
const solc = require('solc');
const Web3 = require('web3');

const provider = "http://localhost:8545";
const genres = ["adventure", "fantasy", "romance", "horror"];
const contentsNumber = 3;
const gas = 4712388;

let web3;
let catalogContract;
let ContentContract;

/**
 * Connects to the Ethereum provider specified in the variable "provider".
 * @returns {Promise<Web3>} a Web3 instance.
 */
function connect() {
  return new Promise((resolve, reject) => {
    web3 = new Web3(new Web3.providers.HttpProvider(provider));
    if (!web3.isConnected()) reject("Cannot connect to "+provider+".");
    console.log("\nConnected to Web3: "+web3.version.node+".\n");
    resolve(web3);
  })
}

/**
 * Compile a contract.
 * @param filename the sol file to be deployed.
 * @returns {Promise<solc.output>} the compiled contract.
 */
function compileContract(filename = "Contract.sol") {
  return new Promise(resolve => {
    // Compile the source code
    const outputContractName = filename+":"+filename.replace(".sol", "");
    const input = fs.readFileSync(filename);
    function findImports(path) {
      return { contents: fs.readFileSync(path).toString() }
    }
    const source = { };
    source[filename] = input.toString();
    const output = solc.compile({ sources: source }, 1, findImports);
    resolve(output.contracts[outputContractName]);
  })
}

/**
 * Deploy on Web3 a contract.
 * @param compiledContract the compiled contract, give in output by the compileContract.
 * @param address, optional, the address with which deploy the contract. If not specify is the first account.
 * @returns {Promise<web3.eth.contract>} the contract instance.
 */
function deployContract(compiledContract, address = web3.eth.accounts[0]) {
  return new Promise((resolve, reject) => {
    const contract = web3.eth.contract(JSON.parse(compiledContract.interface));
    const options = {
      data: '0x' + compiledContract.bytecode,
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
 * Compiles and Deploy on Web3 a contract.
 * @param filename the sol file to be deployed.
 * @param address, optional, the address with which deploy the contract. If not specify is the first account.
 * @returns {Promise<web3.eth.contract>} the contract instance.
 */
function compileAndDeployContract(filename = "Contract.sol", address = web3.eth.accounts[0]) {
  return compileContract(filename).then(compiledContract => deployContract(compiledContract, address));
}

/**
 * Auxiliary function: generates num contracts, that are object with 3 parameters: name, content and genre.
 * @param num the number of contents that you want to generate.
 * @returns Array of contents object.
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
 * Auxiliary function: returns the object needed to call contract functions that modifies the state.
 * @param from, the person that do the transaction.
 * @param value of the transaction, optional. Default is 0.
 * @returns object, the object needed for the function call.
 */
function getParams(from = web3.eth.accounts[0], value = 0) {
  return {
    from: from,
    gas: gas,
    value: value
  }
}

/**
 * Auxiliary function: deploy num content contracts on the blockchain and returns it in an array.
 * @param num the number of contents that you want to deploy.
 * @returns {Promise<contract[]>} an array contract instances.
 */
async function deployContentsContract(num) {
  // compile the contract
  const compiledContract = await compileContract('GenericContentManagementContract.sol');
  ContentContract = web3.eth.contract(JSON.parse(compiledContract.interface));
  // deploy num empty Contents
  const contentContracts = [];
  for (let i = 0; i < num; i++)
    await deployContract(compiledContract, web3.eth.accounts[rand(web3.eth.accounts.length - 1)])
      .then(contractInstance => contentContracts.push(contractInstance));
  // set the name, content and genre of each content
  const contents = generateContents(num);
  for (let i = 0; i < num; i++) {
    const owner = contentContracts[i].author();
    contentContracts[i].setName(contents[i].name, getParams(owner));
    contentContracts[i].setContent(contents[i].content, getParams(owner));
    contentContracts[i].setGenre(contents[i].genre, getParams(owner));
    contentContracts[i].publish(catalogContract.address, getParams(owner));
  }
  return contentContracts;
}

/**
 * Auxiliary function: generates a random number.
 * @param to, optional, the last number of the range.
 * @param from, optional, the starting number of the range.
 * @returns number (random).
 */
function rand(to = 1, from = 0) {
  if (from > to) return -1;
  if (from === to) return from;
  return from + Math.floor(Math.random() * to+1);
}

/**
 * Auxiliary function: parse the content list returned by catalogContract.getContentsList().
 * @param contentsList, the content list.
 * @returns Array of object with 2 field: name and address.
 */
function parseContentsList(contentsList = ["", ""]) {
  const list = [];
  for (let i = 0; i < contentsList[0].length; i++) {
    list[i] = {
      name: web3.toUtf8(contentsList[0][i]),
      address: contentsList[1][i]
    }
  }
  return list;
}

/**
 * Auxiliary function: print the content list.
 * @param contentsList, the content list.
 */
function printContentsList(contentsList = []) {
  for (let i = 0; i < contentsList.length; i++)
    console.log(" - "+contentsList[i].name+": "+contentsList[i].address);
}

/**
 * Small test of the functions to buy and gift content.
 * Are needed at least 3 accounts in web3.eth.accounts and at least 3 contents.
 * The first account (web3.eth.accounts[0]) buys the first two contents.
 * Then the second account (web3.eth.accounts[1]) gifts to the first one the third content.
 * It is also tested the purchase of an already purchased content (title3) that should raise an error.
 * @param contentsList, the list of all available contents.
 * @returns Array of the contents on which the first account has access.
 */
function grantAccessTest(contentsList = []) {
  const value = catalogContract.contentCost();
  console.log("The first account ("+web3.eth.accounts[0]+") buys the first two contents:");
  catalogContract.getContent(contentsList[0].address, getParams(web3.eth.accounts[0], value));
  console.log(" - "+contentsList[0].name);
  catalogContract.getContent(contentsList[1].address, getParams(web3.eth.accounts[0], value));
  console.log(" - "+contentsList[1].name);
  console.log("The second account ("+web3.eth.accounts[1]+") gifts to the first one the third content:");
  catalogContract.giftContent(contentsList[2].address, web3.eth.accounts[0], getParams(web3.eth.accounts[1], value));
  console.log(" - "+contentsList[2].name);
  console.log("Is now tested the purchase of an already purchased content ("+contentsList[2].name+") " +
    "that should raise an error:");
  try {
    catalogContract.getContent(contentsList[2].address, getParams(web3.eth.accounts[0], value));
  } catch(e) {
    console.log(" - ERROR: "+e.message);
  }
  if (!catalogContract.hasAccess(web3.eth.accounts[0], contentsList[0].address)
    || !catalogContract.hasAccess(web3.eth.accounts[0], contentsList[1].address)
    || !catalogContract.hasAccess(web3.eth.accounts[0], contentsList[2].address))
    throw "The user doesn't have the access right: something went wrong.";
  console.log("Access rights verified: OK.");
  return [contentsList[0], contentsList[1], contentsList[2]];
}

/**
 * Small test of the consumeContent function. Consume a content.
 * @param contentAddress, the consumable content.
 * @param account, the account with which consume the content.
 */
function consumeContentTest(contentAddress, account = web3.eth.accounts[0]) {
  if (!contentAddress) throw "You must specify the content.";
  return ContentContract.at(contentAddress).consumeContent(getParams(account));
}

/**
 * Small test about the grantAccess (getContent and giftContent),
 * both the consumeContent functions (Premium and Standard),
 * and the Premium subscription.
 * Produces verbose logs to better understand the behaviour.
 * Are needed at least 3 accounts in web3.eth.accounts and at least 3 contents.
 * @param contentsList, the list of all available contents.
 */
function smallTests(contentsList) {
  console.log("\n\n --- Small tests ---");
  // grant access to web3.eth.accounts[0] on the first 3 contents in contentsList
  const accessibleContents = grantAccessTest(contentsList);
  // consume the first content and check that is no more consumable
  console.log("\nConsuming the first content: "+accessibleContents[0].name);
  console.log(" - "+consumeContentTest(accessibleContents[0].address, web3.eth.accounts[0]));
  if (catalogContract.hasAccess(web3.eth.accounts[0], accessibleContents[0].address))
    throw "The content still consumable: something went wrong.";
  else console.log("The content is no more consumable: OK.");
  // apply for a premium account
  //TODO
  // consume the second content and check that it still consumable
  // (should be, because Premium account should not consume previously bought content)
  console.log("\nConsuming the first content: "+accessibleContents[0].name);
  console.log(" - "+consumeContentTest(accessibleContents[1].address, web3.eth.accounts[0]));
  if (catalogContract.hasAccess(web3.eth.accounts[0], accessibleContents[1].address))
    throw "The content is no more consumable: something went wrong.";
  else console.log("The content still consumable: OK.");
}

/**
 * Main function of the program.
 * @returns {Promise<void>} because the function is async.
 */
async function main() {
  await connect();
  // deploy the Catalog
  console.log("Deploying catalog...");
  catalogContract = await compileAndDeployContract('CatalogContract.sol');

  // deploy contentsNumber contract from different accounts
  console.log("\nDeploying "+contentsNumber+" contents...");
  const contentContracts = await deployContentsContract(contentsNumber);

  // retrieve contents list
  const contentsList = parseContentsList(catalogContract.getContentsList());
  console.log("\nContents in the catalog:");
  printContentsList(contentsList);

  // check the getNewContentsList
  //console.log("\ngetNewContentsList: you should see the last 10 element of the previous list in the opposite order.");
  //printContentsList(parseContentsList(catalogContract.getNewContentsList()));

  // Small test about the grantAccess (getContent and giftContent),
  // both the consumeContent functions (Premium and Standard),
  // and the Premium subscription.
  // Produces verbose logs to better understand the behaviour.
  smallTests(contentsList);
}


// noinspection JSIgnoredPromiseFromCall
main();

/*main()
  .catch(err => {
    console.error("ERROR: "+err);
    process.exit(1);
  });*/
