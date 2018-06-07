pragma solidity ^0.4.0;

contract BaseContentManagementContract {
    address public author;
    bytes32 public name = "";
    bytes32 public genre = "";
}

contract CatalogContract {

    /* FUNCTION ORDER
        constructor
        fallback function (if exists)
        external
        public
        internal
        private
    */

    /* VARIABLES */
    // Constants
    uint public contentCost = 10000;       // in wei
    uint public premiumCost = 100000;     // in wei
    uint public premiumTime = 5760;     // more or less a day

    uint public payAfter = 10;  // views

    uint public chartListLength = 10;

    // Messages
    string private fallbackFunctionMessage = "Unexpected call: function does not exist. The fallback function has reverted the state.";

    // Runtime
    address private owner;
    uint private balance = 0;

    // Structs
    struct content {
        bytes32 name;
        address author;
        bytes32 genre;
        uint views;
        uint uncollectedViews;
    }

    struct author {
        bool alreadyFound;
        uint views;
        uint uncollectedViews;
    }

    mapping (address => uint) private premiumUsers; // map a user into his subscription expiration time
    mapping (address => mapping (address => bool)) private accessibleContent;   // map a user into his accessible contents
    address[] addressesList;  // list of all contents
    mapping (address => content) contents;  // map content addresses into contents


    /* EVENTS */
    event FallbackFunctionCall(string message, bytes data);
    event grantedAccess(address user, address content);
    event paymentAvailable(address content);
    event becomesPremium(address user);


    /* MODIFIERS */
    modifier onlyOwner() {
        require (msg.sender == owner);
        _;
    }

    modifier exists(address c) {
        require (contents[c].name != "" && BaseContentManagementContract(c).author() != 0);
        _;
    }


    /* FUNCTIONS */

    /** Constructor */
    constructor() public {
        owner = msg.sender;
    }

    /** Fallback function */
    function () public {
        emit FallbackFunctionCall(fallbackFunctionMessage, msg.data);
        revert(fallbackFunctionMessage);
    }

    /** Suicide function, can be called only by the owner */
    function _suicide() public onlyOwner {
        // Distribute the balance to the authors according with their views count
        uint totalViews = 0;
        uint totalUncollectedViews = 0;
        address[] memory authorsList;
        mapping (address => author) authors;
        // Calculate total views and how many views has to be payed for each author
        for (uint i = 0; i < addressesList.length; i++) {
            content memory c = contents(addressesList[i]);  // perform only one storage read
            if (!authors(c.author).alreadyFound) {
                authorsList.push(c.author);
                authors(c.author).alreadyFound = true;
            }
            authors(c.author).views += c.views;
            totalViews += c.views;
            authors(c.author).uncollectedViews += c.uncollectedViews;
            totalUncollectedViews += c.uncollectedViews;
        }
        // subtract from the balance the amount that has to be payed for the uncollected views to the authors
        balance -= totalUncollectedViews * contentCost;
        for (i = 0; i < authorsList.length; i++) {
            // for each author pay the uncollected views
            uint amountFromUncollectedViews = authors(authorsList[i]).uncollectedViews * contentCost;
            // distribute the remaining balance to the authors according with their views count
            uint amountFromPremium = balance * authors(authorsList[i]).views / totalViews;
            authorsList[i].transfer(amountFromUncollectedViews + amountFromPremium);
        }
        // should not, but if there is some wei in excess transfer it to the owner
        selfdestruct(owner);
    }

    // REQUIRED FUNCTIONS

    /** Returns the number of views for each content.
     * @return (bytes32[], uint[]), names and views:
     * each content in names is associated with the views number in views
     * Gas: no one pay.
     * Burden: O(n).
     */
    function getStatistics() public view returns(bytes32[], uint[]) {
        bytes32[] memory names = new bytes32[](addressesList.length);
        uint[] memory views = new uint[](addressesList.length);
        for (uint i = 0; i < addressesList.length; i++) {
            content memory c = contents[addressesList[i]]; // perform only one storage read
            names[i] = c.name;
            views[i] = c.views;
        }
        return (names, views);
    }
    /*function getStatistics() public view returns(stats[]) {
        stats[] memory statistics = new stats[](contentList.length);
        for (uint i = 0; i < contentList.length; i++) {
            statistics[i] = stats(contentList[i].name, contentList[i].views);
        }
        return statistics;
    }*/

    /** Returns the list of contents without the number of views.
     * @return string[] with the content names.
     * Gas: no one pay.
     * Burden: O(n).
     */
    function getContentList() public view returns(bytes32[]) {
        bytes32[] memory list = new bytes32[](addressesList.length);
        for (uint i = 0; i < addressesList.length; i++) {
            list[i] = contents[addressesList[i]].name;
        }
        return list;
    }

    /** Returns the list of x newest contents.
     * @return string[] with the content names ordered from the newest.
     * Gas: no one pay.
     * Burden: O(x) ~ O(1).
     */
    function getNewContentsList() public view returns(bytes32[]) {
        // NOTE: I assume that the latest content is not the last deployed contract in the blockchain (with the highest
        // block number), but is the last added to the catalog (that ideally is when is "published").
        bytes32[] memory list = new bytes32[](chartListLength);
        for (uint i = 0; i < chartListLength; i++) {
            // add it in reverse order: the latest first
            list[i] = contents[addressesList[addressesList.length - 1 - i]].name;
        }
        return list;
    }

    /** Get the latest release of genre g.
     * @param g the genre of which you want to get the latest contents.
     * @return string[] with the content names.
     * Gas: no one pay.
     * Burden: < O(n).
     */
    function getLatestByGenre(bytes32 g) public view returns(bytes32[]) {
        uint i = 0;
        bytes32[] memory list = new bytes32[](chartListLength);
        uint j = addressesList.length - 1;
        while (i < chartListLength && j >= 0)  {
            content memory c = contents[addressesList[j]];   // perform only one storage read
            if (c.genre == g) {
                list[i] = c.name;
                i++;
            }
            j--;
        }
        return list;
    }

    /** Get the chart of the genre g.
     * @param g the genre of which you want to get the most popular contents.
     * @return string[] with the content names ordered from the most popular an then for block number (if there are 2
     * or more content with the same number of view the oldest comes first).
     * Gas: no one pay.
     * Burden: chartListLength * O(n) => between O(n) and O(n^2).
     */
    function getMostPopularByGenre(bytes32 g) public view returns(bytes32[]) {
        uint listLength = chartListLength;
        // If i have less than chartListLength element in the contentList I have to return contentList.length elements
        if (addressesList.length < listLength) listLength = addressesList.length;
        bytes32[] memory list = new bytes32[](listLength);
        for (uint i = 0; i < listLength; i++) {
            int maxViews = -1;
            bytes32 maxName;
            for (uint j = 0; j < addressesList.length; j++) {
                content memory c = contents[addressesList[j]]; // perform only one storage read
                // check if is gt the last found (and of course the genre is g)
                if (c.genre == g && int(c.views) > maxViews) {
                    // check if not already in the array
                    uint k = 0;
                    bool alreadyFound = false;
                    while (k < list.length && !alreadyFound) {
                        if (list[k] == c.name) alreadyFound = true;
                        k++;
                    }
                    if (!alreadyFound) {
                        maxViews = int(c.views);
                        maxName = c.name;
                    }
                }
            }
            list[i] = maxName;
        }
        return list;
    }

    /** Get the latest release of the author a.
     * @param a the author of whom you want to get the latest contents.
     * @return string[] with the content names.
     * Gas: no one pay.
     * Burden: < O(n).
     */
    function getLatestByAuthor(address a) public view returns(bytes32[]) {
        uint i = 0;
        bytes32[] memory list = new bytes32[](chartListLength);
        uint j = addressesList.length - 1;
        while (i < chartListLength && j >= 0)  {
            content memory c = contents[addressesList[j]];   // perform only one storage read
            if (c.author == a) {
                list[i] = c.name;
                i++;
            }
            j--;
        }
        return list;
    }

    /** Get the chart of the author a.
     * @param a the author of whom you want to get the most popular contents.
     * @return string[] with the content names ordered from the most popular an then for block number (if there are 2
     * or more content with the same number of view the oldest comes first).
     * Gas: no one pay.
     * Burden: chartListLength * O(n) => between O(n) and O(n^2).
     */
    function getMostPopularByAuthor(address a) public view returns(bytes32[]) {
        uint listLength = chartListLength;
        // If i have less than chartListLength element in the contentList I have to return contentList.length elements
        if (addressesList.length < listLength) listLength = addressesList.length;
        bytes32[] memory list = new bytes32[](listLength);
        for (uint i = 0; i < listLength; i++) {
            int maxViews = -1;
            bytes32 maxName;
            for (uint j = 0; j < addressesList.length; j++) {
                content memory c = contents[addressesList[j]]; // perform only one storage read
                // check if is gt the last found (and of course the author is a)
                if (c.author == a && int(c.views) > maxViews) {
                    // check if not already in the array
                    uint k = 0;
                    bool alreadyFound = false;
                    while (k < list.length && !alreadyFound) {
                        if (list[k] == c.name) alreadyFound = true;
                        k++;
                    }
                    if (!alreadyFound) {
                        maxViews = int(c.views);
                        maxName = c.name;
                    }
                }
            }
            list[i] = maxName;
        }
        return list;
    }

    /** Checks if a user u has an active premium subscription.
     * @param u the user of whom you want to check the premium subscription.
     * @return bool true if the user hold a still valid premium account, false otherwise.
     * Gas: no one pay.
     * Burden: small.
     */
    function isPremium(address u) public view returns(bool) {
        return premiumUsers[u] >= block.number;
    }

    /** Pays for access to content x.
     * @param x the address of the block of the ContentManagementContract.
     * Gas: who requests the content pays.
     */
    function getContent(address x) public payable exists(x) {
        grantAccess(msg.sender, x);
    }

    /** Requests access to content x without paying, premium accounts only.
     * @param x the address of the block of the ContentManagementContract.
     * Gas: who requests the content pays.
     */
    /* DEPRECATED: a user could only pay a premium cycle and access all content (at most once) in the future,
     * even if the premium account is no longer active. In this case the premium account would have turned into a
     * "bundle" of content rather than a subscription. Since this is not the expected behavior, the function has
     * been abolished and now a premium user can consume all content without having to first request access to it
     * as long as his premium subscription still valid.
     * Access to content from a premium account will not affect previously purchased content. The user can still
     * consume the purchased content (once only) when the subscription has ended, even if that content has been
     * accessed by this user multiple times during his premium account.
     * In addition, a premium account can also purchase content. They will be consumable (once only) when the
     * premium subscription has ended.
    function getContentPremium(address x) public exists(x) {
        require(isPremium(msg.sender));
        accessibleContent[msg.sender][x] = true;
        emit grantedAccess(x, msg.sender);
    }*/

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

    // AUXILIARY FUNCTIONS FOR CONTENTS CONTRACT

    /** Checks if a user u has access to a content x.
     * @param u the user of whom you want to check the access right.
     * @param x the content of which you want to check the access right.
     * @return bool true if the user has the access right, false otherwise.
     * Gas: no one pay.
     * Burden: small.
     */
    function hasAccess(address u, address x) public view exists(x) returns(bool) {
        // lazy or, premium first because we suppose they consume more content than standard users
        return isPremium(u) || accessibleContent[u][x];
    }

    /** Notice the catalog that the user u has consumed the content x.
     * @param u the user that consume the content.
     * @param x the content that has been consumed.
     * Gas: the user that consumes the content pays.
     */
    function consumeContent(address u, address x) public exists(x) {
        // Premium users can consume contents for free and are not considered in the count of views
        if (isPremium(u)) return;
        delete accessibleContent[u][x];
        contents[x].views++;
        contents[x].uncollectedViews++;
        /* Notice the author if his content has enough views.
         * note that the event is emitted only once, when the number of views is exactly equal to payAfter:
         * it is not an oversight but a caution not to spam too much.
         * Can be changed in >= if this contract is deployed in a dedicated blockchain.
         */
        if (contents[x].uncollectedViews == payAfter) {
            emit paymentAvailable(x);
        }
    }

    /** Used by the authors to collect their reached payout.
     * @param x the content of which collect the payout.
     * The content must has been visited at least payAfter (the author should have received the event).
     * Gas: the author (who receives money) pays.
     */
    function collectPayout(address x) public exists(x) {
        require(contents[x].author == msg.sender, "Only the author of the content can collect the payout.");
        require(contents[x].uncollectedViews >= payAfter, "The content has not received enough views. Please listen for a paymentAvailable event on this content address.");
        uint uncollectedViews = contents[x].uncollectedViews;
        contents[x].uncollectedViews = 0;
        uint amount = contentCost * contents[x].uncollectedViews;
        balance -= amount;
        contents[x].author.transfer(amount);
    }

    /** Called from a ContentManagementContract, adds the content to the catalog.
     * Gas: the author pays.
     */
    function addMe() public {
        BaseContentManagementContract cc = BaseContentManagementContract(msg.sender);
        content memory c = content(cc.name(), cc.author(), cc.genre(), 0, 0);
        contents[cc] = c;
        addressesList.push(cc);
    }

    /** Called from a ContentManagementContract, removes the content from the catalog (used by the suicide function).
     * Gas: the author pays.
     */
    function removesMe() public exists(msg.sender) {
        delete contents[msg.sender];
        bool found = false;
        // Search the address in the array
        for (uint i = 0; i < addressesList.length; i++) {
            if (!found && addressesList[i] == msg.sender) {   // lazy if: skip the storage read if found is true
                found = true;
            }
            if (found) {
                // move all the following items back of 1 position
                addressesList[i] = addressesList[i+1];
            }
        }
        if (found) {
            // and finally delete the last item
            delete addressesList[addressesList.length - 1];
            addressesList.length--;
        }
    }


    /* INTERNAL AUXILIARY FUNCTIONS */

    /** Starts a new premium subscription for the user u based on the amount v.
     * @param u the user.
     * @param v the value.
     */
    function setPremium(address u) private {
        require (msg.value = premiumCost);
        // If the user has never bought premium or the premium subscription is expired reset the expiration time to now
        if (!isPremium(u)) premiumUsers[u] = block.number;
        // Increment the user expiration time (if he is already premium will be premium longer)
        premiumUsers[u] += premiumTime;
        emit becomesPremium(u);
        balance += msg.value;
    }

    /** Grant access for the content x to the user v.
    * @param u the user.
    * @param x the content.
    */
    function grantAccess(address u, address x) private {
        require(msg.value == contentCost);
        accessibleContent[u][x] = true;
        emit grantedAccess(u, x);
        balance += msg.value;
    }
}