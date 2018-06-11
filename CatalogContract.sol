pragma solidity ^0.4.0;

contract BaseContentManagementContract {
    address public author;
    bytes32 public name = "";
    bytes32 public genre = "";
    function murder() public;
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
    address public owner;
    uint private balance = 0;

    // Structs
    struct content {
        bytes32 name;
        address author;
        bytes32 genre;
        uint views;
    }

    struct author {
        bool alreadyFound;
        uint views;
        uint uncollectedViews;
    }

    mapping (address => uint) private premiumUsers; // map a user into his subscription expiration time
    mapping (address => mapping (address => bool)) private accessibleContent;   // map a user into his accessible contents
    address[] contentsList;  // list of all contents
    mapping (address => content) contents;  // map content addresses into contents
    address[] authorsList;   // list of all authors
    mapping (address => author) authors;    // map author address in its struct


    /* EVENTS */
    event FallbackFunctionCall(string message, bytes data);
    event grantedAccess(address user, address content);
    event paymentAvailable(address content);
    event becomesPremium(address user);


    /* MODIFIERS */
    modifier onlyOwner() {
        require (msg.sender == owner,
            "Only the contract owner can perform this action.");
        _;
    }

    modifier exists(address c) {
        require (contents[c].name != "" &&
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
        emit FallbackFunctionCall(fallbackFunctionMessage, msg.data);
        revert(fallbackFunctionMessage);
    }

    /** Suicide function, can be called only by the owner */
    function _suicide() public onlyOwner {
        // Murder all the contents in the catalog: this will free up space in
        // the blockchain and create negative gas to consume less in this
        // process: all this transfers cost a lot.
        for (uint i = 0; i < contentsList.length; i++) {
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
        balance -= totalUncollectedViews * contentCost;
        for (i = 0; i < authorsList.length; i++) {
            a = authors[authorsList[i]];
            // for each author pay the uncollected views
            uint amountFromUncollectedViews = a.uncollectedViews * contentCost;
            // distribute the remaining balance to the authors according with
            // their views count
            uint amountFromPremium = balance * a.views / totalViews;
            authorsList[i].transfer(amountFromUncollectedViews +
                amountFromPremium);
        }
        // should not, but if there is some wei in excess transfer it to the
        // owner
        selfdestruct(owner);
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
        // lazy or, premium first because we suppose they consume more content
        // than standard users
        return isPremium(u) || accessibleContent[u][x];
    }

    /** Notice the catalog that the user u has consumed the content x.
     * @param u the user that consume the content.
     * @param x the content that has been consumed.
     * Gas: the user that consumes the content pays.
     */
    function consumeContent(address u, address x) public exists(x) {
        // Premium users can consume contents for free and are not considered
        // in the count of views
        if (isPremium(u)) return;
        delete accessibleContent[u][x];
        contents[x].views++;
        address a = contents[x].author; // perform only one storage read
        authors[a].views++;
        authors[a].uncollectedViews++;
        /* Notice the author if his contents has enough views.
         * Note that the event is emitted only once, when the number of views
         * is exactly equal to payAfter: it is not an oversight but a caution
         * not to spam too much. Can be changed in >= if this contract is
         * deployed in a dedicated blockchain. */
        if (authors[a].uncollectedViews == payAfter) {
            emit paymentAvailable(a);
        }
    }

    /** Used by the authors to collect their reached payout.
     * The author contents must has been visited at least payAfter times.
     * (the author should have received the event).
     * Gas: the author (who receives money) pays.
     */
    function collectPayout() public {
        uint uncollectedViews = authors[msg.sender].uncollectedViews;
        require(uncollectedViews >= payAfter, "Your contents have not received\
    enough views. Please listen for a paymentAvailable event relative\
    to your address.");
        authors[msg.sender].uncollectedViews = 0;
        uint amount = contentCost * uncollectedViews;
        balance -= amount;
        msg.sender.transfer(amount);
    }

    /** Called from a ContentManagementContract, adds the content to the catalog.
     * Gas: the author pays.
     */
    function addMe() public {
        BaseContentManagementContract cc =
        BaseContentManagementContract(msg.sender);
        contents[cc] = content(cc.name(), cc.author(), cc.genre(), 0);
        contentsList.push(cc);
        authors[cc.author()].alreadyFound = true;
        authorsList.push(cc.author());
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
            if (found) {
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
     * @return (bytes32[], uint[], address[]), names, views and addresses:
     * each content in names is associated with the views number in views and
     * with its address in addresses.
     * Gas: no one pay.
     * Burden: O(n).
     */
    function getStatistics() public view returns(bytes32[], uint[], address[]) {
        bytes32[] memory names = new bytes32[](contentsList.length);
        uint[] memory views = new uint[](contentsList.length);
        for (uint i = 0; i < contentsList.length; i++) {
            content memory c = contents[contentsList[i]]; // perform only one storage read
            names[i] = c.name;
            views[i] = c.views;
        }
        return (names, views, contentsList);
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
    function getNewContentsList() public view returns(bytes32[], address[]) {
        // NOTE: I assume that the latest content is not the last deployed contract in the blockchain (with the highest
        // block number), but is the last added to the catalog (that ideally is when is "published").
        bytes32[] memory names = new bytes32[](chartListLength);
        address[] memory addresses = new address[](chartListLength);
        for (uint i = 0; i < chartListLength; i++) {
            // add it in reverse order: the latest first
            address a = contentsList[contentsList.length - 1 - i];
            names[i] = contents[a].name;
            addresses[i] = a;
        }
        return (names, addresses);
    }

    /** Get the latest release of genre g.
     * @param g the genre of which you want to get the latest contents.
     * @return (string[], address[]) names and addresses ordered from the
     * newest: each content in names is associated with its address in addresses.
     * Gas: no one pay.
     * Burden: < O(n).
     */
    function getLatestByGenre(bytes32 g) public view returns(bytes32[], address[]) {
        uint i = 0;
        bytes32[] memory names = new bytes32[](chartListLength);
        address[] memory addresses = new address[](chartListLength);
        uint j = contentsList.length - 1;
        while (i < chartListLength && j >= 0)  {
            address addr = contentsList[j];
            content memory c = contents[addr];   // perform only one storage read
            if (c.genre == g) {
                names[i] = c.name;
                addresses[i] = addr;
                i++;
            }
            j--;
        }
        return (names, addresses);
    }

    /** Get the chart of the genre g.
     * @param g the genre of which you want to get the most popular contents.
     * @return (string[], address[]) names and addresses ordered from the
     * most popular and then for block number (if there are 2 or more content
     * with the same number of view the oldest comes first). Each content in
     * names is associated with its address in addresses.
     * Gas: no one pay.
     * Burden: chartListLength * O(n) => between O(n) and O(n^2).
     */
    function getMostPopularByGenre(bytes32 g) public view returns(bytes32[], address[]) {
        uint listLength = chartListLength;
        // If i have less than chartListLength element in the contentsList I
        // have to return contentsList.length elements
        if (contentsList.length < listLength) listLength = contentsList.length;
        bytes32[] memory names = new bytes32[](listLength);
        address[] memory addresses = new address[](listLength);
        for (uint i = 0; i < listLength; i++) {
            int maxViews = -1;
            bytes32 maxName;
            address maxAddress;
            for (uint j = 0; j < contentsList.length; j++) {
                address addr = contentsList[j];
                content memory c = contents[addr];
                // check if is gt the last found (and of course the genre is g)
                if (c.genre == g && int(c.views) > maxViews) {
                    // check if not already in the array
                    uint k = 0;
                    bool alreadyFound = false;
                    while (k < names.length && !alreadyFound) {
                        if (names[k] == c.name) alreadyFound = true;
                        k++;
                    }
                    if (!alreadyFound) {
                        maxViews = int(c.views);
                        maxName = c.name;
                        maxAddress = addr;
                    }
                }
            }
            names[i] = maxName;
            addresses[i] = maxAddress;
        }
        return (names, addresses);
    }

    /** Get the latest release of the author a.
     * @param a the author of whom you want to get the latest contents.
     * @return (string[], address[]) names and addresses ordered from the
     * newest: each content in names is associated with its address in addresses.
     * Gas: no one pay.
     * Burden: < O(n).
     */
    function getLatestByAuthor(address a) public view returns(bytes32[], address[]) {
        uint i = 0;
        bytes32[] memory names = new bytes32[](chartListLength);
        address[] memory addresses = new address[](chartListLength);
        uint j = contentsList.length - 1;
        while (i < chartListLength && j >= 0)  {
            address addr = contentsList[j];
            content memory c = contents[addr];   // perform only one storage read
            if (c.author == a) {
                names[i] = c.name;
                addresses[i] = addr;
                i++;
            }
            j--;
        }
        return (names, addresses);
    }

    /** Get the chart of the author a.
     * @param a the author of whom you want to get the most popular contents.
     * @return (string[], address[]) names and addresses ordered from the
     * most popular and then for block number (if there are 2 or more content
     * with the same number of view the oldest comes first). Each content in
     * names is associated with its address in addresses.
     * Gas: no one pay.
     * Burden: chartListLength * O(n) => between O(n) and O(n^2).
     */
    function getMostPopularByAuthor(address a) public view returns(bytes32[], address[]) {
        uint listLength = chartListLength;
        // If i have less than chartListLength element in the contentsList I
        // have to return contentsList.length elements
        if (contentsList.length < listLength) listLength = contentsList.length;
        bytes32[] memory names = new bytes32[](listLength);
        address[] memory addresses = new address[](listLength);
        for (uint i = 0; i < listLength; i++) {
            int maxViews = -1;
            bytes32 maxName;
            address maxAddress;
            for (uint j = 0; j < contentsList.length; j++) {
                address addr = contentsList[j];
                content memory c = contents[addr];
                // check if is gt the last found (and of course the author is a)
                if (c.author == a && int(c.views) > maxViews) {
                    // check if not already in the array
                    uint k = 0;
                    bool alreadyFound = false;
                    while (k < names.length && !alreadyFound) {
                        if (names[k] == c.name) alreadyFound = true;
                        k++;
                    }
                    if (!alreadyFound) {
                        maxViews = int(c.views);
                        maxName = c.name;
                        maxAddress = addr;
                    }
                }
            }
            names[i] = maxName;
            addresses[i] = maxAddress;
        }
        return (names, addresses);
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
        require (msg.value == premiumCost);
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
        require(msg.value == contentCost);
        accessibleContent[u][x] = true;
        emit grantedAccess(u, x);
        balance += msg.value;
    }
}