pragma solidity ^0.4.0;

contract BaseContentManagementContract {
    address public author;
    bytes32 public name;
    bytes32 public genre;
    uint public price;
    function murder() public;
}

contract CatalogContract {

    /* VARIABLES */
    // Constants
    uint public contentCost = 0.01 ether;   // ~ 4€
    uint public premiumCost = 0.1 ether;    // ~ 40€
    uint public premiumTime = 172800;       // ~ 1 month
    uint public payAfter = 10;  // views

    // Runtime
    address public owner;
    uint private balance = 0;

    // Structs
    struct content {
        bytes32 name;
        address author;
        bytes32 genre;
        uint price;
        uint views;
        uint uncollectedViews;
        // Feedback category:
        // - how much do you enjoy the content (personal opinion)
        // - price quality ratio
        // - how do you think the content is good (objective opinion, based on
        // the meaning it would like to have)
        uint enjoySum;
        uint enjoyNum;
        uint priceFairnessSum;
        uint priceFairnessNum;
        uint contentMeaningSum;
        uint contentMeaningNum;
    }

    struct author {
        bool alreadyFound;
        uint views;
        uint uncollectedViews;
    }

    // map a user into his subscription expiration time
    mapping (address => uint) private premiumUsers;
    // map a user into his accessible contents
    mapping (address => mapping (address => bool)) private accessibleContent;
    address[] contentsList;  // list of all contents
    // map content addresses into contents
    mapping (address => content) private contents;
    // map a user into the content that he can vote
    mapping (address => mapping (address => bool)) private pendingFeedback;

    // Support structure for suicide function
    address[] private authorsList;   // list of all authors
    mapping (address => author) private authors;


    /* EVENTS */
    event FallbackFunctionCall(string message, bytes data);
    event CatalogClosed();
    event grantedAccess(address user, address content);
    event paymentAvailable(address content);
    event becomesPremium(address user);
    event newContentAvailable(bytes32 name, address addr);


    /* MODIFIERS */
    modifier onlyOwner() {
        require(msg.sender == owner);
        _;
    }

    modifier exists(address c) {
        require(contents[c].name != "" &&
        BaseContentManagementContract(c).author() != 0);
        _;
    }


    /* FUNCTIONS */

    /** Constructor */
    constructor() public {
        owner = msg.sender;
    }

    /** Fallback function */
    function () public {
        revert();
    }

    /** Suicide function, can be called only by the owner */
    function _suicide() public onlyOwner {
        for (uint i = 0; i < contentsList.length; i++) {
            BaseContentManagementContract cc =
            BaseContentManagementContract(contentsList[i]);
            if (!authors[cc.author()].alreadyFound) {
                authors[cc.author()].alreadyFound = true;
                authorsList.push(cc.author());
            }
            // Murder all the contents in the catalog: this will free up space
            // in the blockchain and create negative gas to consume less in this
            // process: all this transfers cost a lot.
            BaseContentManagementContract(contentsList[i]).murder();
        }
        // Distribute the balance to the authors according with their views
        // count
        uint totalViews = 0;
        uint totalUncollectedViews = 0;
        // Calculate totals of views and uncollectedViews of all the authors
        for (i = 0; i < authorsList.length; i++) {
            author memory a = authors[authorsList[i]];
            totalViews += a.views;
            totalUncollectedViews += a.uncollectedViews;
        }
        // subtract from the balance the amount that has to be payed for the
        // uncollected views to the authors
        if (totalViews != 0) {
            balance -= totalUncollectedViews * contentCost;
            for (i = 0; i < authorsList.length; i++) {
                a = authors[authorsList[i]];
                // for each author pay the uncollected views
                uint256 amountFromUncollectedViews =
                a.uncollectedViews * contentCost;
                // distribute the remaining balance to the authors according
                // with their views count
                uint256 amountFromPremium = balance * a.views / totalViews;
                uint256 amount = amountFromUncollectedViews + amountFromPremium;
                if (amount != 0) authorsList[i].transfer(amount);
            }
        }
        // emit an event
        emit CatalogClosed();
        // Transfer weis in excess to the owner
        selfdestruct(owner);
    }

    /** Pays for access to content x.
     * @param x the address of the block of the ContentManagementContract.
     * Gas: who requests the content pays.
     */
    function getContent(address x) public payable exists(x) {
        grantAccess(msg.sender, x);
    }

    /** Pays for granting access to content x to the user u.
     * @param x the address of the block of the ContentManagementContract.
     * @param u the user to whom you want to gift the content.
     * Gas: who gift pays.
     */
    function giftContent(address x, address u) public payable exists(x) {
        grantAccess(u, x);
    }

    /** Pays for granting a Premium Account to the user u.
     * @param u the user to whom you want to gift the subscription.
     * Gas: who gift pays.
     */
    function giftPremium(address u) public payable {
        setPremium(u);
    }

    /** Starts a new premium subscription.
     * Gas: who subscribe pays.
     */
    function buyPremium() public payable {
        setPremium(msg.sender);
    }

    /** Leave a feedback on a content.
     * @param c the content address.
     * @param y the category in which leave the feedback.
     *        Can be "enjoy", "value for money" or "content".
     * @param r the vote that you want to assign, from 1 to 5.
     */
    function leaveFeedback(address c, bytes32 y, uint r) public {
        require(r <= 5 && pendingFeedback[msg.sender][c]);
        if (y == "enjoy") {
            contents[c].enjoySum += r;
            contents[c].enjoyNum++;
        }
        if (y == "value for money") {
            contents[c].priceFairnessSum += r;
            contents[c].priceFairnessNum++;
        }
        if (y == "content") {
            contents[c].contentMeaningSum += r;
            contents[c].contentMeaningNum++;
        }
        pendingFeedback[msg.sender][c] = false;
    }

    /** Used by the authors to collect their reached payout.
     * The content must has been visited at least payAfter times.
     * @param x the content.
     * (the author should have received the event).
     * Gas: the author (who receives money) pays.
     */
    function collectPayout(address x) public {
        content memory c = contents[x];
        require (c.author == msg.sender);
        uint uncollectedViews = c.uncollectedViews;
        require(uncollectedViews >= payAfter);
        contents[x].uncollectedViews = 0;
        uint average_rate = (c.enjoySum / c.enjoyNum +
        c.priceFairnessSum / c.priceFairnessNum +
        c.contentMeaningSum / c.contentMeaningNum) / 3;
        /*uint average_rate = (c.enjoySum + c.priceFairnessSum + c.contentSum) /
        (c.enjoyNum + c.priceFairnessNum + c.contentNum);*/
        uint amount = c.price * uncollectedViews * average_rate / 5;
        balance -= amount;
        msg.sender.transfer(amount);
    }

    /** Called from a ContentManagementContract.
     * Adds the content to the catalog.
     * Gas: the author pays.
     */
    function addMe() public {
        BaseContentManagementContract cc =
        BaseContentManagementContract(msg.sender);
        contents[cc] = content(cc.name(), cc.author(), cc.genre(), cc.price(),
            0, 0, 0, 0, 0, 0, 0, 0);
        contentsList.push(cc);
        emit newContentAvailable(cc.name(), cc);
    }

    /** Notice the catalog that the user u has consumed the content x.
     * @param u the user that consume the content.
     * Gas: the user that consumes the content pays.
     */
    function consumeContent(address u) public exists(msg.sender) {
        // Premium users can consume contents for free and are not considered
        // in the count of views
        if (isPremium(u)) return;
        // Only contents can call this function, so the content to be delete
        // is the msg.sender
        delete accessibleContent[u][msg.sender];
        pendingFeedback[u][msg.sender] = true;
        contents[msg.sender].views++;
        contents[msg.sender].uncollectedViews++;
        /* Notice the author if his contents has enough views.
         * Note that the event is emitted only once, when the number of views
         * is exactly equal to payAfter: it is not an oversight but a caution
         * not to spam too much. Can be changed in >= if this contract is
         * deployed in a dedicated blockchain. */
        if (contents[msg.sender].uncollectedViews == payAfter) {
            emit paymentAvailable(msg.sender);
        }
    }

    /** Called from a ContentManagementContract, removes the content from the
     * catalog (used by the suicide function).
     * Gas: the author pays.
     */
    function removesMe() public exists(msg.sender) {
        delete contents[msg.sender];
        bool found = false;
        // Search the address in the array
        for (uint i = 0; i < contentsList.length; i++) {
            // lazy if: skip the storage read if found is true
            if (!found && contentsList[i] == msg.sender) {
                found = true;
            }
            if (found && i < contentsList.length - 1) {
                // move all the following items back of 1 position
                contentsList[i] = contentsList[i+1];
            }
        }
        if (found) {
            // and finally delete the last item
            delete contentsList[contentsList.length - 1];
            contentsList.length--;
        }
    }

    /** Returns the number of views for each content.
     * @return (bytes32[], uint[], address[]), names, addresses and views:
     * each content in names is associated with the views number in views and
     * with its address in addresses.
     * Gas: no one pay.
     * Burden: O(n).
     */
    function getStatistics() public view returns(bytes32[], address[], uint[]) {
        bytes32[] memory names = new bytes32[](contentsList.length);
        uint[] memory views = new uint[](contentsList.length);
        for (uint i = 0; i < contentsList.length; i++) {
            content memory c = contents[contentsList[i]];
            names[i] = c.name;
            views[i] = c.views;
        }
        return (names, contentsList, views);
    }

    /** Returns the list of contents without the number of views.
     * @return (string[], address[]) names and addresses: each content in names
     * is associated with its address in addresses.
     * Gas: no one pay.
     * Burden: O(n).
     */
    function getContentsList() public view returns(bytes32[], address[]) {
        bytes32[] memory names = new bytes32[](contentsList.length);
        for (uint i = 0; i < contentsList.length; i++) {
            names[i] = contents[contentsList[i]].name;
        }
        return (names, contentsList);
    }

    /** Returns the list of x newest contents.
     * @return (string[], address[]) names and addresses ordered from the
     * newest: each content in names is associated with its address in addresses.
     * Gas: no one pay.
     * Burden: O(x) ~ O(1).
     */
    function getNewContentsList(uint n) public view
    returns(bytes32[], address[]) {
        uint listLength = n;
        // If i have less than chartListLength element in the contentsList I
        // have to return contentsList.length elements
        if (contentsList.length < listLength) listLength = contentsList.length;
        // NOTE: I assume that the latest content is not the last deployed
        // contract in the blockchain (with the highest block number), but is
        // the last added to the catalog (that ideally is when is "published").
        bytes32[] memory names = new bytes32[](listLength);
        address[] memory addresses = new address[](listLength);
        for (uint i = 0; i < listLength; i++) {
            // add it in reverse order: the latest first
            address a = contentsList[contentsList.length - 1 - i];
            names[i] = contents[a].name;
            addresses[i] = a;
        }
        return (names, addresses);
    }

    /** Returns the list of contents with all information.
     * @return (address[], bytes32[], address[], bytes32[], uint[], uint[]). In the position n we got in order address,
     * name, author, genre, price, views of the content n.
     * Gas: no one pay.
     * Burden: O(n).
     */
    function getFullContentsList() public view
    returns(address[], bytes32[], address[], bytes32[], uint[], uint[]) {
        bytes32[] memory name = new bytes32[](contentsList.length);
        address[] memory authorAddr = new address[](contentsList.length);
        bytes32[] memory genre = new bytes32[](contentsList.length);
        uint[] memory price = new uint[](contentsList.length);
        uint[] memory views = new uint[](contentsList.length);
        for (uint i = 0; i < contentsList.length; i++) {
            content memory c = contents[contentsList[i]];
            name[i] = c.name;
            authorAddr[i] = c.author;
            genre[i] = c.genre;
            price[i] = c.price;
            views[i] = c.views;
        }
        return (contentsList, name, authorAddr, genre, price, views);
    }

    /** Returns ratings list of contents.
     * @return (address[], uint[], uint[], uint[]). In the position n we got in order address, enjoy rating, value for
     * money rating and content meaning rating of the content n.
     * Gas: no one pay.
     * Burden: O(n).
     */
    function getRatingsList() public view
    returns(address[], uint[], uint[], uint[]) {
        uint[] memory enjoy = new uint[](contentsList.length);
        uint[] memory priceFairness = new uint[](contentsList.length);
        uint[] memory contentMeaning = new uint[](contentsList.length);
        for (uint i = 0; i < contentsList.length; i++) {
            content memory c = contents[contentsList[i]];
            enjoy[i] = c.enjoySum / c.enjoyNum;
            priceFairness[i] = c.priceFairnessSum / c.priceFairnessNum;
            contentMeaning[i] = c.contentMeaningSum / c.contentMeaningNum;
        }
        return (contentsList, enjoy, priceFairness, contentMeaning);
    }

    /** Get the latest release of genre g.
     * @param g the genre of which you want to get the latest content.
     * @return (bytes32, address) names and addresses of the content.
     * Gas: no one pay.
     * Burden: < O(n).
     */
    function getLatestByGenre(bytes32 g) public view returns(bytes32, address) {
        // using int because i can be negative if the list is empty or there
        // aren't element of genre g. Should not fail.
        int i = int(contentsList.length - 1);
        while (i >= 0)  {
            address addr = contentsList[uint(i)];
            content memory c = contents[addr];
            if (c.genre == g) {
                return (c.name, addr);
            }
            i--;
        }
        // fallback, return empty if not exist a release of g
        return("", 0);
    }

    /** Get most popular release of genre g.
     * @param g the genre of which you want to get the most popular content.
     * @return (string, address) name and address of the content.
     * If there are 2 or more content with the same number of view the oldest
     * comes first.
     * Gas: no one pay.
     * Burden: O(n).
     */
    function getMostPopularByGenre(bytes32 g) public view
    returns(bytes32, address) {
        int maxViews = -1;
        bytes32 maxName;
        address maxAddress;
        for (uint i = 0; i < contentsList.length; i++) {
            address addr = contentsList[i];
            content memory c = contents[addr];
            if (c.genre == g && int(c.views) > maxViews) {
                maxViews = int(c.views);
                maxName = c.name;
                maxAddress = addr;
            }
        }
        return (maxName, maxAddress);
    }

    /** Get the latest release of the author a.
     * @param a the author of whom you want to get the latest content.
     * @return (bytes32, address) names and addresses of the content.
     * Gas: no one pay.
     * Burden: < O(n).
     */
    function getLatestByAuthor(address a) public view
    returns(bytes32, address) {
        // using int because i can be negative if the list is empty or there
        // aren't element of genre g. Should not fail.
        int i = int(contentsList.length - 1);
        while (i >= 0)  {
            address addr = contentsList[uint(i)];
            content memory c = contents[addr];
            if (c.author == a) {
                return (c.name, addr);
            }
            i--;
        }
        // fallback, return empty if not exist a release of a
        return("", 0);
    }

    /** Get the most popular release of the author a.
     * @param a the author of which you want to get the most popular content.
     * @return (string, address) name and address of the content.
     * If there are 2 or more content with the same number of view the oldest
     * comes first.
     * Gas: no one pay.
     * Burden: O(n).
     */
    function getMostPopularByAuthor(address a) public view
    returns(bytes32, address) {
        int maxViews = -1;
        bytes32 maxName;
        address maxAddress;
        for (uint i = 0; i < contentsList.length; i++) {
            address addr = contentsList[i];
            content memory c = contents[addr];
            if (c.author == a && int(c.views) > maxViews) {
                maxViews = int(c.views);
                maxName = c.name;
                maxAddress = addr;
            }
        }
        return (maxName, maxAddress);
    }

    /** Get the release with highest rating in category y.
     * @param y the category, optional. If not specified returns the content
     * with the maximum average rating.
     * @return (string, address) name and address of the content.
     * If there are 2 or more content with the same number of view the oldest
     * comes first.
     * Gas: no one pay.
     * Burden: O(n).
     */
    function getMostRated(bytes32 y) public view returns(bytes32, address) {
        int maxRate = -1;
        bytes32 maxName;
        address maxAddress;
        for (uint i = 0; i < contentsList.length; i++) {
            address addr = contentsList[i];
            content memory c = contents[addr];
            uint rate = 0;
            if (y == "enjoy") {
                rate = c.enjoySum / c.enjoyNum;
            }
            if (y == "value for money") {
                rate = c.priceFairnessSum / c.priceFairnessNum;
            }
            if (y == "content") {
                rate = c.contentMeaningSum / c.contentMeaningSum;
            }
            if (int(rate) > maxRate) {
                maxRate = int(rate);
                maxName = c.name;
                maxAddress = addr;
            }
        }
        return (maxName, maxAddress);
    }

    /** Get the release with highest rating in category y with genre g.
     * @param g the genre.
     * @param y the category, optional. If not specified returns the content
     * with the maximum average rating.
     * @return (string, address) name and address of the content.
     * If there are 2 or more content with the same number of view the oldest
     * comes first.
     * Gas: no one pay.
     * Burden: O(n).
     */
    function getMostRatedByGenre(bytes32 g, bytes32 y) public view
    returns(bytes32, address) {
        int maxRate = -1;
        bytes32 maxName;
        address maxAddress;
        for (uint i = 0; i < contentsList.length; i++) {
            address addr = contentsList[i];
            content memory c = contents[addr];
            if (c.genre == g) {
                uint rate = 0;
                if (y == "enjoy") {
                    rate = c.enjoySum / c.enjoyNum;
                }
                if (y == "value for money") {
                    rate = c.priceFairnessSum / c.priceFairnessNum;
                }
                if (y == "content") {
                    rate = c.contentMeaningSum / c.contentMeaningSum;
                }
                if (int(rate) > maxRate) {
                    maxRate = int(rate);
                    maxName = c.name;
                    maxAddress = addr;
                }

            }
        }
        return (maxName, maxAddress);
    }

    /** Get the release with highest rating in category y by author a.
     * @param a the author.
     * @param y the category, optional. If not specified returns the content
     * with the maximum average rating.
     * @return (string, address) name and address of the content.
     * If there are 2 or more content with the same number of view the oldest
     * comes first.
     * Gas: no one pay.
     * Burden: O(n).
     */
    function getMostRatedByAuthor(address a, bytes32 y) public view
    returns(bytes32, address) {
        int maxRate = -1;
        bytes32 maxName;
        address maxAddress;
        for (uint i = 0; i < contentsList.length; i++) {
            address addr = contentsList[i];
            content memory c = contents[addr];
            if (c.author == a) {
                uint rate = 0;
                if (y == "enjoy") {
                    rate = c.enjoySum / c.enjoyNum;
                }
                if (y == "value for money") {
                    rate = c.priceFairnessSum / c.priceFairnessNum;
                }
                if (y == "content") {
                    rate = c.contentMeaningSum / c.contentMeaningNum;
                }
                if (int(rate) > maxRate) {
                    maxRate = int(rate);
                    maxName = c.name;
                    maxAddress = addr;
                }

            }
        }
        return (maxName, maxAddress);
    }

    /** Checks if a user u has access to a content x.
     * @param u the user of whom you want to check the access right.
     * @param x the content of which you want to check the access right.
     * @return bool true if the user has the access right, false otherwise.
     * Gas: no one pay.
     * Burden: small.
     */
    function hasAccess(address u, address x) public view exists(x)
    returns(bool) {
        // lazy or, premium first because we suppose they consume more content
        // than standard users
        return isPremium(u) || accessibleContent[u][x];
    }

    /** Checks if a user u has an active premium subscription.
     * @param u the user of whom you want to check the premium subscription.
     * @return bool true if the user hold a still valid premium account, false
     * otherwise.
     * Gas: no one pay.
     * Burden: small.
     */
    function isPremium(address u) public view returns(bool) {
        return premiumUsers[u] >= block.number;
    }


    /* INTERNAL AUXILIARY FUNCTIONS */

    /** Starts a new premium subscription for the user u based on the amount v.
     * @param u the user.
     */
    function setPremium(address u) private {
        require(msg.value == premiumCost);
        // If the user has never bought premium or the premium subscription is
        // expired reset the expiration time to now
        if (!isPremium(u)) premiumUsers[u] = block.number;
        // Increment the user expiration time
        // (if he is already premium will be premium longer)
        premiumUsers[u] += premiumTime;
        emit becomesPremium(u);
        balance += msg.value;
    }

    /** Grant access for the content x to the user v.
    * @param u the user.
    * @param x the content.
    */
    function grantAccess(address u, address x) private {
        require(msg.value == contents[x].price);
        require(!accessibleContent[u][x]);
        accessibleContent[u][x] = true;
        emit grantedAccess(u, x);
        balance += msg.value;
    }
}