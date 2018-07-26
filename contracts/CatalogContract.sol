pragma solidity ^0.4.0;

contract BaseContentManagementContract {
    address public owner;
    bytes32 public name;
    bytes32 public genre;
    uint public price;
    function murder() public;
}

contract CatalogContract {

    /* VARIABLES */
    // Constants
    //uint public contentCost = 0.01 ether;   // ~ 4€ - deprecated: now the price is chosen by the author
    uint public premiumCost = 0.1 ether;    // ~ 40€
    uint public premiumTime = 172800;       // ~ 1 month
    uint public payAfter = 2; // views - IMPORTANT NOTE: should be 10, but is set to 2 for testing.

    bytes32 private enjoyS = "enjoy";
    bytes32 private valueForMoneyS = "value for money";
    bytes32 private contentS = "content";
    bytes32[] public ratingCategories = [enjoyS, valueForMoneyS, contentS];

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
        uint valueForMoneySum;
        uint valueForMoneyNum;
        uint contentMeaningSum;
        uint contentMeaningNum;
    }

    struct author {
        bool alreadyFound;
        uint amount;
        uint ratio;
    }

    // map a user into his subscription expiration time
    mapping (address => uint) private premiumUsers;
    // map a user into his accessible contents
    mapping (address => mapping (address => bool)) private accessibleContent;
    address[] contentList;  // list of all contents
    // map content addresses into contents
    mapping (address => content) private contents;
    // map a user into the content that he can vote
    mapping (address => mapping (address => bool)) private pendingFeedback;

    // Support structure for suicide function
    address[] private authorsList;   // list of all authors
    mapping (address => author) private authors;


    /* EVENTS */
    event CatalogClosed();
    event GrantedAccess(address user, address content);
    event PaymentAvailable(address content);
    event BecomesPremium(address user);
    event NewContentAvailable(bytes32 name, address addr);
    event FeedbackAvailable(address content, address user);


    /* MODIFIERS */
    modifier onlyOwner() {
        require(msg.sender == owner);
        _;
    }

    modifier exists(address c) {
        require(contents[c].name != "" &&
        BaseContentManagementContract(c).owner() != 0);
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
        uint totalRatio = 0;
        for (uint i = 0; i < contentList.length; i++) {
            content memory cc = contents[contentList[i]];
            // save the author in the list if not exist
            if (!authors[cc.author].alreadyFound) {
                authors[cc.author].alreadyFound = true;
                authorsList.push(cc.author);
            }
            // calculate the payout rate for this author
            uint enjoyRate = 0;
            uint priceFairnessRate = 0;
            uint contentMeaningRate = 0;
            if (cc.enjoyNum != 0) enjoyRate = cc.enjoySum / cc.enjoyNum;
            if (cc.valueForMoneyNum != 0)
                priceFairnessRate = cc.valueForMoneySum / cc.valueForMoneyNum;
            if (cc.contentMeaningNum != 0) contentMeaningRate =
            cc.contentMeaningSum / cc.contentMeaningNum;
            uint average_rate = (enjoyRate + priceFairnessRate +
            contentMeaningRate) / 3;
            if (average_rate == 0)
                average_rate = 5;    // if no one have voted just cast to max
            // add the payout for the uncollected views to yhe author amount
            uint amount = cc.uncollectedViews * cc.price * average_rate / 5;
            authors[cc.author].amount += amount;
            // remove the amount from the balance (later we will pay this
            // amount, so we cannot count it in the balance)
            balance -= amount;
            // We calculate the rating in percentage (uint => no decimals => we
            // need precision). Then we multiply this rate for the price of the
            // content and the number of views. Then we sum it all together.
            // What we obtain is a number that grows linearly basing on the
            // number of content of this author, the price of this contents, the
            // view count and the rating, so that who do more and better
            // receives more.
            uint ratio = cc.views * cc.price * 100 * average_rate * 25;
            // 25 = 100 / 5 where 100 is the percentage and 5 is the maximum rate.
            authors[cc.author].ratio += ratio;
            // We keep track also of the total ratio off all the authors.
            totalRatio += ratio;
            // Reset the number of views and uncollected views of this content.
            // We don't need it anymore. This help to prevent reentrancy.
            contents[contentList[i]].views = 0;
            contents[contentList[i]].uncollectedViews = 0;
            // Murder all the contents in the catalog: this will free up space
            // in the blockchain and create negative gas to consume less in this
            // process: all this transfers cost a lot.
            BaseContentManagementContract(contentList[i]).murder();
        }
        if (totalRatio != 0) {  // avoid division by 0
            // Distribute the balance to the authors according with their ratio.
            // We calculate the ratio between totalRatio and a.ratio, then
            // multiply this value for the balance. This gave us how many part
            // of the balance belongs to the author. We add it to the author
            // amount.
            for (i = 0; i < authorsList.length; i++) {
                author memory a = authors[authorsList[i]];
                amount = a.ratio * balance / totalRatio;
                uint toBeTransferred = a.amount = amount;
                balance -= amount;
                // reset the author to prevent reentrancy
                authors[authorsList[i]].amount = 0;
                authors[authorsList[i]].ratio = 0;
                // transfer the amount to the owner
                if (toBeTransferred != 0) authorsList[i].transfer(toBeTransferred);
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
            contents[c].valueForMoneySum += r;
            contents[c].valueForMoneyNum++;
        }
        if (y == "content") {
            contents[c].contentMeaningSum += r;
            contents[c].contentMeaningNum++;
        }
        pendingFeedback[msg.sender][c] = false;
    }

    /** Used to know the reached payout of a content.
     * @param x the content.
     * @return the reached payout
     * or 0 if the content does not have received enough views.
     * Gas: the author (who receives money) pays.
     */
    function payoutAvailable(address x) public view
    returns(uint) {
        content memory c = contents[x];
        uint uncollectedViews = c.uncollectedViews;
        if (uncollectedViews < payAfter) return 0;
        uint enjoyRate = 0;
        uint priceFairnessRate = 0;
        uint contentMeaningRate = 0;
        if (c.enjoyNum != 0) enjoyRate = c.enjoySum / c.enjoyNum;
        if (c.valueForMoneyNum != 0)
            priceFairnessRate = c.valueForMoneySum / c.valueForMoneyNum;
        if (c.contentMeaningNum != 0)
            contentMeaningRate = c.contentMeaningSum / c.contentMeaningNum;
        uint average_rate = (enjoyRate + priceFairnessRate +
        contentMeaningRate) / 3;
        if (average_rate == 0)
            average_rate = 5;    // if no one have voted just cast to max
        uint amount = c.price * uncollectedViews * average_rate / 5;
        return amount;
    }

    /** Used by the authors to collect their reached payout.
     * The content must has been visited at least payAfter times.
     * @param x the content.
     * (the author should have received the event).
     * Gas: the author (who receives money) pays.
     */
    function collectPayout(address x) public {
        require (contents[x].author == msg.sender);
        uint amount = payoutAvailable(x);
        require(amount > 0);
        contents[x].uncollectedViews = 0;
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
        contents[cc] = content(cc.name(), cc.owner(), cc.genre(), cc.price(),
            0, 0, 0, 0, 0, 0, 0, 0);
        contentList.push(cc);
        emit NewContentAvailable(cc.name(), cc);
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
        emit FeedbackAvailable(msg.sender, u);
        contents[msg.sender].views++;
        contents[msg.sender].uncollectedViews++;
        /* Notice the author if his contents has enough views.
         * Note that the event is emitted only once, when the number of views
         * is exactly equal to payAfter: it is not an oversight but a caution
         * not to spam too much. Can be changed in >= if this contract is
         * deployed in a dedicated blockchain. */
        if (contents[msg.sender].uncollectedViews == payAfter) {
            emit PaymentAvailable(msg.sender);
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
        for (uint i = 0; i < contentList.length; i++) {
            // lazy if: skip the storage read if found is true
            if (!found && contentList[i] == msg.sender) {
                found = true;
            }
            if (found && i < contentList.length - 1) {
                // move all the following items back of 1 position
                contentList[i] = contentList[i+1];
            }
        }
        if (found) {
            // and finally delete the last item
            delete contentList[contentList.length - 1];
            contentList.length--;
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
        bytes32[] memory names = new bytes32[](contentList.length);
        uint[] memory views = new uint[](contentList.length);
        for (uint i = 0; i < contentList.length; i++) {
            content memory c = contents[contentList[i]];
            names[i] = c.name;
            views[i] = c.views;
        }
        return (names, contentList, views);
    }

    /** Returns the list of contents without the number of views.
     * @return (string[], address[]) names and addresses: each content in names
     * is associated with its address in addresses.
     * Gas: no one pay.
     * Burden: O(n).
     */
    function getContentList() public view returns(bytes32[], address[]) {
        bytes32[] memory names = new bytes32[](contentList.length);
        for (uint i = 0; i < contentList.length; i++) {
            names[i] = contents[contentList[i]].name;
        }
        return (names, contentList);
    }

    /** Returns the list of contents with all information.
     * @return (address[], bytes32[], address[], bytes32[], uint[], uint[]).
     * In the position n we got in order address,
     * name, author, genre, price, views of the content n.
     * Gas: no one pay.
     * Burden: O(n).
     */
    function getFullContentList() public view
    returns(address[], bytes32[], address[], bytes32[], uint[], uint[]) {
        bytes32[] memory name = new bytes32[](contentList.length);
        address[] memory authorAddr = new address[](contentList.length);
        bytes32[] memory genre = new bytes32[](contentList.length);
        uint[] memory price = new uint[](contentList.length);
        uint[] memory views = new uint[](contentList.length);
        for (uint i = 0; i < contentList.length; i++) {
            content memory c = contents[contentList[i]];
            name[i] = c.name;
            authorAddr[i] = c.author;
            genre[i] = c.genre;
            price[i] = c.price;
            views[i] = c.views;
        }
        return (contentList, name, authorAddr, genre, price, views);
    }

    /** Returns ratings list of contents.
     * @return (address[], uint[], uint[], uint[], uint[]). In the position n we
     * got in order address, average rating enjoy rating, value for money rating
     * and content meaning rating of the content n.
     * Gas: no one pay.
     * Burden: O(n).
     */
    function getRatingsList() public view
    returns(address[], uint[], uint[], uint[], uint[]) {
        uint[] memory averageRating = new uint[](contentList.length);
        uint[] memory enjoy = new uint[](contentList.length);
        uint[] memory priceFairness = new uint[](contentList.length);
        uint[] memory contentMeaning = new uint[](contentList.length);
        for (uint i = 0; i < contentList.length; i++) {
            content memory c = contents[contentList[i]];
            if (c.enjoyNum != 0)
                enjoy[i] = c.enjoySum / c.enjoyNum;
            if (c.valueForMoneyNum != 0)
                priceFairness[i] = c.valueForMoneySum / c.valueForMoneyNum;
            if (c.contentMeaningNum != 0)
                contentMeaning[i] = c.contentMeaningSum / c.contentMeaningNum;
            averageRating[i] =
            (enjoy[i] + priceFairness[i] + contentMeaning[i]) / 3;
        }
        return (contentList, averageRating, enjoy, priceFairness,
        contentMeaning);
    }

    /** Returns all the information about a content.
     * @param addr address of the content.
     * @return (bytes32, address, bytes32, uint, uint) corresponding to name,
     * author, genre, price and views of the content.
     * Gas: no one pay.
     * Burden: O(n).
     */
    function getContentInfo(address addr) public view
    returns(bytes32, address, bytes32, uint, uint) {
        content memory c = contents[addr];
        return (c.name, c.author, c.genre, c.price, c.views);
    }

    /** Returns ratings for a content.
     * @param addr address of the content.
     * @return returns(uint, uint, uint, uint) corresponding to total, enjoy,
     * priceFairness and contentMeaning rating of the content.
     * Gas: no one pay.
     * Burden: O(n).
     */
    function getContentRatings(address addr) public view
    returns(uint, uint, uint, uint) {
        content memory c = contents[addr];
        uint total = 0;
        uint enjoy = 0;
        uint priceFairness = 0;
        uint contentMeaning = 0;
        if (c.enjoyNum != 0)
            enjoy = c.enjoySum / c.enjoyNum;
        if (c.valueForMoneyNum != 0)
            priceFairness = c.valueForMoneySum / c.valueForMoneyNum;
        if (c.contentMeaningNum != 0)
            contentMeaning = c.contentMeaningSum / c.contentMeaningNum;
        total = (enjoy + priceFairness + contentMeaning) / 3;
        return (total, enjoy, priceFairness, contentMeaning);
    }

    /** Returns the list of n newest contents.
     * @param n the number of item that you want in the list.
     * @return (string[], address[]) names and addresses ordered from the
     * newest: each content in names is associated with its address in
     * addresses.
     * Gas: no one pay.
     * Burden: O(x) ~ O(1).
     */
    function getNewContentList(uint n) public view
    returns(bytes32[], address[]) {
        uint listLength = n;
        // If i have less than chartListLength element in the contentList I
        // have to return contentList.length elements
        if (contentList.length < listLength) listLength = contentList.length;
        // NOTE: I assume that the latest content is not the last deployed
        // contract in the blockchain (with the highest block number), but is
        // the last added to the catalog (that ideally is when is "published").
        bytes32[] memory names = new bytes32[](listLength);
        address[] memory addresses = new address[](listLength);
        for (uint i = 0; i < listLength; i++) {
            // add it in reverse order: the latest first
            address a = contentList[contentList.length - 1 - i];
            names[i] = contents[a].name;
            addresses[i] = a;
        }
        return (names, addresses);
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
        int i = int(contentList.length - 1);
        while (i >= 0)  {
            address addr = contentList[uint(i)];
            content memory c = contents[addr];
            if (c.genre == g) {
                return (c.name, addr);
            }
            i--;
        }
        // fallback, return empty if not exist a release of g
        return("", 0);
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
        int i = int(contentList.length - 1);
        while (i >= 0)  {
            address addr = contentList[uint(i)];
            content memory c = contents[addr];
            if (c.author == a) {
                return (c.name, addr);
            }
            i--;
        }
        // fallback, return empty if not exist a release of a
        return("", 0);
    }

    /** Get the most popular content.
     * @return (string, address) name and address of the content.
     * If there are 2 or more content with the same number of view the oldest
     * comes first.
     * Gas: no one pay.
     * Burden: O(n).
     */
    function getMostPopular() public view
    returns(bytes32, address) {
        int maxViews = -1;
        bytes32 maxName;
        address maxAddress;
        for (uint i = 0; i < contentList.length; i++) {
            address addr = contentList[i];
            content memory c = contents[addr];
            if (int(c.views) > maxViews) {
                maxViews = int(c.views);
                maxName = c.name;
                maxAddress = addr;
            }
        }
        return (maxName, maxAddress);
    }

    /** Get the most popular release of genre g.
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
        for (uint i = 0; i < contentList.length; i++) {
            address addr = contentList[i];
            content memory c = contents[addr];
            if (c.genre == g && int(c.views) > maxViews) {
                maxViews = int(c.views);
                maxName = c.name;
                maxAddress = addr;
            }
        }
        return (maxName, maxAddress);
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
        for (uint i = 0; i < contentList.length; i++) {
            address addr = contentList[i];
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
        for (uint i = 0; i < contentList.length; i++) {
            address addr = contentList[i];
            content memory c = contents[addr];
            uint rate = 0;
            if ((y[0] == 0 || y == "enjoy") && c.enjoyNum != 0) {
                rate += c.enjoySum / c.enjoyNum;
            }
            if ((y[0] == 0 || y == "value for money")
            && c.valueForMoneyNum != 0) {
                rate += c.valueForMoneySum / c.valueForMoneyNum;
            }
            if ((y[0] == 0 || y == "content") && c.contentMeaningNum != 0) {
                rate += c.contentMeaningSum / c.contentMeaningNum;
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
        for (uint i = 0; i < contentList.length; i++) {
            address addr = contentList[i];
            content memory c = contents[addr];
            if (c.genre == g) {
                uint rate = 0;
                if ((y[0] == 0 || y == "enjoy") && c.enjoyNum != 0) {
                    rate += c.enjoySum / c.enjoyNum;
                }
                if ((y[0] == 0 || y == "value for money")
                && c.valueForMoneyNum != 0) {
                    rate += c.valueForMoneySum / c.valueForMoneyNum;
                }
                if ((y[0] == 0 || y == "content") && c.contentMeaningNum != 0) {
                    rate += c.contentMeaningSum / c.contentMeaningNum;
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
        for (uint i = 0; i < contentList.length; i++) {
            address addr = contentList[i];
            content memory c = contents[addr];
            if (c.author == a) {
                uint rate = 0;
                if ((y[0] == 0 || y == "enjoy") && c.enjoyNum != 0) {
                    rate += c.enjoySum / c.enjoyNum;
                }
                if ((y[0] == 0 || y == "value for money")
                && c.valueForMoneyNum != 0) {
                    rate += c.valueForMoneySum / c.valueForMoneyNum;
                }
                if ((y[0] == 0 || y == "content") && c.contentMeaningNum != 0) {
                    rate += c.contentMeaningSum / c.contentMeaningNum;
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
        emit BecomesPremium(u);
        balance += msg.value;
    }

    /** Grant access for the content x to the user v.
    * @param u the user.
    * @param x the content.
    */
    function grantAccess(address u, address x) private {
        // do not manage the extra value,
        // just require exactly what the content cost
        require(msg.value == contents[x].price);
        // prevent double purchase of contents
        require(!accessibleContent[u][x]);
        // the author cannot buy his contents
        // this also ensure that an author cannot vote its content to increase
        // the withdrawal
        require(contents[x].author != u);
        // grant access
        accessibleContent[u][x] = true;
        emit GrantedAccess(u, x);
        // update balance
        balance += msg.value;
    }
}