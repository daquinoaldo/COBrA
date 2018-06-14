const fs = require('fs');
const solc = require('solc');
const Web3 = require('web3');

const provider = "http://localhost:8545";
const genres = ["adventure", "fantasy", "romance", "horror"];
const contentsNumber = 20;

let web3;
let catalogContract;
let ContentContract;
let latestByAuthor0;
let latestByGenre0;
let contentCost;
let gasLimit;

/**
 * Connects to the Ethereum provider specified in the variable "provider".
 * @returns {Promise<Web3>} a Web3 instance.
 */
function connect() {
  return new Promise((resolve, reject) => {
    web3 = new Web3(new Web3.providers.HttpProvider(provider));
    if (!web3.isConnected()) reject("Cannot connect to "+provider+".");
    console.log("\nConnected to Web3: "+web3.version.node+".\n");
    gasLimit = web3.eth.getBlock("latest").gasLimit;
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
      gas: gasLimit
    };
    // Deploy contract instance
    const contractInstance = contract.new(options, (err, res) => {
      if (err) reject(err);
      if (res.address) {
        console.log(" - contract address: " + res.address);
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
      name: web3.fromUtf8("title"+i),
      genre: web3.fromUtf8(genres[rand(genres.length-1)])
    };
  return contents;
}

/**
 * Auxiliary function: returns the object needed to call contract functions that modifies the state.
 * @param from, the person that do the transaction.
 * @param value of the transaction, optional. Default is 0.
 * @param gas, max gas that the transaction can use.
 * @returns object, the object needed for the function call.
 */
function getParams(from = web3.eth.accounts[0], value = 0, gas = gasLimit) {
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
  for (let i = 0; i < num; i++) {
    const authorIndex = rand(web3.eth.accounts.length - 1);
    await deployContract(compiledContract, web3.eth.accounts[authorIndex])
      .then(contractInstance => {
        contentContracts.push(contractInstance);
        // exclude the last one because it will deleted
        if (i < num - 1 && authorIndex === 0) latestByAuthor0 = contractInstance;
      });
  }
  // set the name, content and genre of each content
  const contents = generateContents(num);
  for (let i = 0; i < num; i++) {
    const owner = contentContracts[i].author();
    contentContracts[i].setName(contents[i].name, getParams(owner));
    contentContracts[i].setGenre(contents[i].genre, getParams(owner));
    contentContracts[i].publish(catalogContract.address, getParams(owner));
    // exclude the last one because it will deleted
    if (i < num - 1 && contents[i].genre === web3.fromUtf8(genres[0])) latestByGenre0 = contentContracts[i];
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
  return from + Math.floor(Math.random() * (to - from + 1));
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
 * Auxiliary function: parse the statistics list returned by catalogContract.getStatistics().
 * @param contentsList, the content list.
 * @returns Array of object with 3 field: name, address and views.
 */
function parseStatistics(contentsList = ["", ""]) {
  const list = [];
  for (let i = 0; i < contentsList[0].length; i++) {
    list[i] = {
      name: web3.toUtf8(contentsList[0][i]),
      address: contentsList[1][i],
      views: contentsList[2][i]
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
 * Auxiliary function: print the statistics.
 * @param contentsList, the content list.
 */
function printStatistics(contentsList = []) {
  for (let i = 0; i < contentsList.length; i++)
    console.log(" - "+contentsList[i].name+": "+contentsList[i].address+" - "+contentsList[i].views+" views");
}

/**
 * testing the getLatestByGenre function.
 */
function latestByGenreTest() {
  console.log("\nTesting the getLatestByGenre function on the genre "+genres[0]+".");
  if(!latestByGenre0) throw "There is no content of genre "+genres[0]+". Try again with more contents.";
  console.log(" - expected: "+web3.toUtf8(latestByGenre0.name())+": "+latestByGenre0.address);
  const latest = catalogContract.getLatestByGenre(web3.fromUtf8(genres[0]));
  console.log(" - got (should be the same): "+web3.toUtf8(latest[0])+": "+latest[1]);
}

/**
 * testing the getLatestByAuthor function.
 */
function latestByAuthorTest() {
  console.log("\nTesting the getLatestByAuthor function on the author "+web3.eth.accounts[0]+".");
  if(!latestByAuthor0) throw "There is no content of author "+web3.eth.accounts[0]+". Try again with more contents.";
  console.log(" - expected: "+web3.toUtf8(latestByAuthor0.name())+": "+latestByAuthor0.address);
  const latest = catalogContract.getLatestByAuthor(web3.eth.accounts[0]);
  console.log(" - got (should be the same): "+web3.toUtf8(latest[0])+": "+latest[1]);
}

/**
 * testing the getMostPopularByGenre function.
 */
function mostPopularByGenreTest() {
  console.log("\nTesting the getMostPopularByGenre function on the genre "+genres[0]+".");
  const before = catalogContract.getMostPopularByGenre(web3.fromUtf8(genres[0]));
  console.log(" - before: "+web3.toUtf8(before[0])+": "+before[1]);
  console.log(" - generating 10 views on the "+web3.toUtf8(latestByGenre0.name())+" content. " +
    "After that this content should be the most popular.");
  const account = web3.eth.accounts[web3.eth.accounts.length - 1];
  for (let i = 0; i < 10; i++) {
    if (!catalogContract.hasAccess(account, latestByGenre0.address))
      grantAccess(latestByGenre0.address, account);
    consumeContent(latestByGenre0.address, account);
  }
  const after = catalogContract.getMostPopularByGenre(web3.fromUtf8(genres[0]));
  console.log(" - after: "+web3.toUtf8(after[0])+": "+after[1]);
}

/**
 * testing the getMostPopularByAuthor function.
 */
function mostPopularByAuthorTest() {
  console.log("\nTesting the getMostPopularByAuthor function on the author "+web3.eth.accounts[0]+".");
  const before = catalogContract.getMostPopularByAuthor(web3.eth.accounts[0]);
  console.log(" - before: "+web3.toUtf8(before[0])+": "+before[1]);
  console.log(" - generating 10 views on the "+web3.toUtf8(latestByAuthor0.name())+" content. " +
    "After that this content should be the most popular.");
  const account = web3.eth.accounts[web3.eth.accounts.length - 1];
  for (let i = 0; i < 10; i++) {
    if (!catalogContract.hasAccess(account, latestByAuthor0.address))
      grantAccess(latestByAuthor0.address, account);
    consumeContent(latestByAuthor0.address, account);
  }
  const after = catalogContract.getMostPopularByAuthor(web3.eth.accounts[0]);
  console.log(" - after: "+web3.toUtf8(after[0])+": "+after[1]);
}

/**
 * Auxiliary function: grant to an user the access to a content.
 * @param contentAddress the content.
 * @param user the user.
 */
function grantAccess(contentAddress, user = web3.eth.accounts[0]) {
  if (!contentCost) contentCost = catalogContract.contentCost();
  catalogContract.getContent(contentAddress, getParams(user, contentCost));
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
  console.log("The first account ("+web3.eth.accounts[0]+") buys the first two contents:");
  grantAccess(contentsList[0].address);
  //catalogContract.getContent(contentsList[0].address, getParams(web3.eth.accounts[0], value));
  console.log(" - "+contentsList[0].name);
  grantAccess(contentsList[1].address);
  //catalogContract.getContent(contentsList[1].address, getParams(web3.eth.accounts[0], value));
  console.log(" - "+contentsList[1].name);
  console.log("The second account ("+web3.eth.accounts[1]+") gifts to the first one the third content:");
  catalogContract.giftContent(contentsList[2].address, web3.eth.accounts[0], getParams(web3.eth.accounts[1], contentCost));
  console.log(" - "+contentsList[2].name);
  console.log("Is now tested the purchase of an already purchased content ("+contentsList[2].name+") " +
    "that should raise an error:");
  try {
    grantAccess(contentsList[0].address);
    //catalogContract.getContent(contentsList[2].address, getParams(web3.eth.accounts[0], value));
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
function consumeContent(contentAddress, account = web3.eth.accounts[0]) {
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
  console.log(" - "+accessibleContents[0].address);
  consumeContent(accessibleContents[0].address, web3.eth.accounts[0]);
  if (catalogContract.hasAccess(web3.eth.accounts[0], accessibleContents[0].address))
    throw "The content still consumable: something went wrong.";
  else console.log("The content is no more consumable: OK.");
  // apply for a premium account
  const premiumCost = catalogContract.premiumCost();
  console.log("\nSubscribing a Premium account on the first account ("+web3.eth.accounts[0]+").");
  catalogContract.buyPremium(getParams(web3.eth.accounts[0], premiumCost));
  console.log(" - isPremium("+web3.eth.accounts[0]+"): "+catalogContract.isPremium(web3.eth.accounts[0])+
    " (must be true).");
  // gift a premium account
  console.log("\nGifting a Premium account from the first account to the second one ("+web3.eth.accounts[1]+").");
  catalogContract.giftPremium(web3.eth.accounts[1], getParams(web3.eth.accounts[0], premiumCost));
  console.log(" - isPremium("+web3.eth.accounts[1]+"): "+catalogContract.isPremium(web3.eth.accounts[1])+
    " (must be true).");
  // consume the second content and check that it still consumable
  // (should be, because Premium account should not consume previously bought content)
  console.log("\nConsuming the first content: "+accessibleContents[0].name);
  console.log(" - "+accessibleContents[1].address);
  consumeContent(accessibleContents[1].address, web3.eth.accounts[0])
  if (!catalogContract.hasAccess(web3.eth.accounts[0], accessibleContents[1].address))
    throw "The content is no more consumable: something went wrong.";
  else console.log("The content still consumable: OK.");
}

/**
 * Big tests about the statistics functions (getStatistics, getMostPopularByGenre, getMostPopularByAuthor) and the
 * payout function. Is needed a big amount of contents.
 * @param contentsList, the list of all available contents.
 */
function bigTests(contentsList) {
  console.log("\n\n --- Big tests ---");
  console.log("For each available account, except the last ones, buy and consume 5 random contents. " +
    "It will take a while.");
  // Exclude the last account for later use
  for (let i = 0; i < web3.eth.accounts.length - 1; i++)
    for (let j = 0; j < 5; j++) {
      const index = rand(contentsList.length - 1);
      // the first account has already bought some contents
      if (!catalogContract.hasAccess(web3.eth.accounts[i], contentsList[index].address))
        grantAccess(contentsList[index].address, web3.eth.accounts[i]);
      consumeContent(contentsList[index].address, web3.eth.accounts[i]);
    }
  printStatistics(parseStatistics(catalogContract.getStatistics()));

  mostPopularByGenreTest();
  mostPopularByAuthorTest();

  // collectPayout test
  console.log("\n"+web3.toUtf8(latestByAuthor0.name())+" has enough view, so author "+web3.eth.accounts[0]+" can collect his payout");
  console.log("before - account balance: "+web3.fromWei(web3.eth.getBalance(web3.eth.accounts[0]))+", " +
    "contract balance: "+web3.eth.getBalance(catalogContract.address));
  catalogContract.collectPayout(getParams());
  console.log("after - account balance: "+web3.fromWei(web3.eth.getBalance(web3.eth.accounts[0]))+", " +
    "contract balance: "+web3.eth.getBalance(catalogContract.address));
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
  let contentsList = parseContentsList(catalogContract.getContentsList());
  console.log("\nContents in the catalog:");
  printContentsList(contentsList);

  // check the getNewContentsList
  console.log("\ngetNewContentsList: you should see the last 10 element of the previous list in the opposite order.");
  printContentsList(parseContentsList(catalogContract.getNewContentsList(10)));

  // check the suicide function of a content
  console.log("\nTesting the suicide function of a content: we have called the suicide function on the last item.");
  contentContracts[contentContracts.length - 1]._suicide(
    getParams(contentContracts[contentContracts.length - 1].author()));
  console.log("getNewContentsList: you should see a list very similar to the preceding one, " +
    "but without the first element.");
  printContentsList(parseContentsList(catalogContract.getNewContentsList(10)));

  // test getLatestByGenre and getLatestByAuthor functions
  latestByGenreTest();
  latestByAuthorTest();

  // get the new content list after the suicide
  contentsList = parseContentsList(catalogContract.getContentsList());

  // Small test about the grantAccess (getContent and giftContent),
  // both the consumeContent functions (Premium and Standard),
  // and the Premium subscription.
  // Produces verbose logs to better understand the behaviour.
  smallTests(contentsList);

  // Big tests about the statistics functions (getStatistics, getMostPopularByGenre, getMostPopularByAuthor) and the
  // payout function.
  bigTests(contentsList);

  // check the suicide function of the catalog
  console.log("\nTesting the suicide function of the catalog: all the values of content contracts should change " +
    "from a value to null. We test it with the name of the first content");
  console.log(" - before: "+ contentContracts[0].name());
  catalogContract._suicide(getParams(catalogContract.owner()));
  console.log(" - after: "+ contentContracts[0].name());
}


// noinspection JSIgnoredPromiseFromCall
main();

/*main()
  .catch(err => {
    console.error("ERROR: "+err);
    process.exit(1);
  });*/
