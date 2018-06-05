pragma solidity ^0.4.0;
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
    uint public contentCost = 50;       // in wei
    uint public premiumCost = 1000;     // in wei
    uint public premiumTime = 5760;     // more or less a day

    uint public payAfter = 10;  // views

    uint private newContentListLength = 10;

    // Messages
    string private fallbackFunctionMessage = "Unexpected call: function does not exist. The fallback function has reverted the state.";

    // Runtime
    address private owner;

    // Structs
    struct content {
        address author;
        string genre;
        uint views;
        uint viewsFromLastPayment;
    }

    mapping (address => uint) private premiumUsers; // map a user into its subscription expiration time
    mapping (address => mapping (address => bool)) private accessibleContent;   // map a user into its accessible contents
    mapping (string => content) contentList;    // map a content title into a content struct


    /* EVENTS */
    event FallbackFunctionCall(string message, bytes data);


    /* MODIFIERS */
    modifier onlyOwner() {
        require (msg.sender == owner);
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
        // If there is some wei send it to the owner
        selfdestruct(owner);
    }

    // REQUIRED FUNCTIONS

    /** Returns the number of views for each content.
     * @return .
     */
    function getStatistics() public view {}

    /** Returns the list of contents without the number of views.
     * @return .
     */
    function getContentList() public view {}

    /** Returns the list of x newest contents.
     * @return .
     */
    function getNewContentsList() public view {
        // x = newContentListLength
    }

    /** Returns the most recent content with genre x.
     * @param g the genre of which you want to get the latest contents.
     * @return .
     */
    function getLatestByGenre(string g) public view {}

    /** Returns the content with genre x, which has received the maximum number of views
     * @param g the genre of which you want to get the most popular contents.
     * @return .
     */
    function getMostPopularByGenre(string g) public view {}

    /** Get the latest release of the author a.
     * @param a the author of whom you want to get the latest contents.
     * @return .
     */
    function getLatestByAuthor(address a) public view {}

    /** Get the chart of the author a.
     * @param a the author of whom you want to get the most popular contents.
     * @return .
     */
    function getMostPopularByAuthor(address a) public view {}

    /** Checks if a user u has an active premium subscription.
     * @param u the user of whom you want to check the premium subscription.
     * @return bool true if the user hold a still valid premium account, false otherwise.
     */
    function isPremium(address u) public view returns(bool) {
        return premiumUsers[u] >= block.number;
    }

    /** Pays for access to content x.
     * @param x the address of the block of the ContentManagementContract.
     */
    function getContent(address x) public payable {
        require(msg.value == contentCost);
        accessibleContent[msg.sender].push(x);
    }

    /** Requests access to content x without paying, premium accounts only.
     * @param x the address of the block of the ContentManagementContract.
     */
    function getContentPremium(address x) public {
        require(isPremium(msg.sender));
        accessibleContent[msg.sender].push(x);
    }

    /** Pays for granting access to content x to the user u.
     * @param x the address of the block of the ContentManagementContract.
     * @param u the user to whom you want to gift the content.
     */
    function giftContent(address x, address u) public payable {
        require(msg.value == contentCost);
        accessibleContent[u].push(x);
    }

    /** Pays for granting a Premium Account to the user u.
     * @param u the user to whom you want to gift the subscription.
     */
    function giftPremium(address u) public payable {
        setPremium(u, msg.value);
    }

    /** Starts a new premium subscription.
     */
    function buyPremium() public payable {
        setPremium(msg.sender, msg.value);
    }

    // ADDITIONAL FUNCTIONS FOR USERS

    /** Checks if a user u has access to a content x.
     * @param u the user of whom you want to check the access right.
     * @param x the content of which you want to check the access right.
     * @return bool true if the user has the access right, false otherwise.
     */
    function hasAccess(address u, address x) public view returns(bool) {
        return accessibleContent[u][x];
    }

    // AUXILIARY FUNCTIONS FOR CONTENTS CONTRACT

    /** Called from a ContentManagementContract, adds the content to the catalog.
     */
    function addMe() public {

    }


    /* INTERNAL AUXILIARY FUNCTIONS */

    /** Starts a new premium subscription for the user u based on the amount v.
     * @param u the user.
     * @param v the value.
     */
    function setPremium(address u, uint v) private {
        require (v == premiumCost);
        // If the user has never bought premium or the premium subscription is expired reset the expiration time to now
        if (!isPremium(u)) premiumUsers[u] = block.number;
        // Increment the user expiration time (if he is already premium will be premium longer)
        premiumUsers[u] += premiumTime;
    }
    /*function setPremium(address u, uint v) private {
        // Calculate the hours that the use can buy with this amount
        uint hoursToBuy = v / premiumCost;
        require (hoursToBuy > 0);
        // If the user has never bought premium or the premium subscription is expired reset the expiration time to now
        if (!isPremium(u)) premiumUsers[u] = now;
        // Increment the user expiration time
        premiumUsers[u] += hoursToBuy * 3600; // 1h = 3600s
        // If there is a remaining value refund the user
        uint remainder = v - hoursToBuy * premiumCost;
        if (remainder > 0) u.transfer(remainder);
    }*/

}