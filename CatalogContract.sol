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
    uint private premiumCostPerHour = 1000;  // in wei
    uint private newContentListLength = 10;

    // Messages
    string private fallbackFunctionMessage = "Unexpected call: function does not exist. " +
    "The fallback function has reverted the state.";

    // Runtime
    address private owner;

    // Structs
    mapping (address => uint) private premiumUsers;             // map a user to its subscription expiration time
    mapping (address => address[]) private accessibleContent;   // map a user to its accessible contents


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

    /** Returns the cost per hour of a premium subscription.
     * @return uint the cost per hour in wei.
     */
    function getPremiumCostPerHour() public returns(uint) { return premiumCostPerHour; }

    /** Returns the number of views for each content.
     * @return .
     */
    function getStatistics() public {}

    /** Returns the list of contents without the number of views.
     * @return .
     */
    function getContentList() public {}

    /** Returns the list of x newest contents.
     * @return .
     */
    function getNewContentsList() public {
        // x = newContentListLength
    }

    /** Returns the most recent content with genre x.
     * @param g the genre of which you want to get the latest contents.
     * @return .
     */
    function getLatestByGenre(genre g) public {}

    /** Returns the content with genre x, which has received the maximum number of views
     * @param g the genre of which you want to get the most popular contents.
     * @return .
     */
    function getMostPopularByGenre(genre g) public {}

    /** Get the latest release of the author a.
     * @param a the author of whom you want to get the latest contents.
     * @return .
     */
    function getLatestByAuthor(address a) public {}

    /** Get the chart of the author a.
     * @param a the author of whom you want to get the most popular contents.
     * @return .
     */
    function getMostPopularByAuthor(address a) public {}

    /** Checks if a user u has an active premium subscription.
     * @param u the user to whom you want to check the premium subscription.
     * @return bool true if the user hold a still valid premium account, false otherwise.
     */
    function isPremium(address u) public returns(bool) {
        return premiumUsers[u] > now;
    }

    /** Pays for access to content x.
     * @param x the address of the block of the ContentManagementContract.
     */
    function getContent(address x) public {}

    /** Requests access to content x without paying, premium accounts only.
     * @param x the address of the block of the ContentManagementContract.
     */
    function getContentPremium(address x) public {}

    /** Pays for granting access to content x to the user u.
     * @param x the address of the block of the ContentManagementContract.
     * @param u the user to whom you want to gift the content.
     */
    function giftContent(address x, address u) public {}

    /** Pays for granting a Premium Account to the user u.
     * @param u the user to whom you want to gift the subscription.
     */
    function giftPremium(address u) public {
        setPremium(u, msg.value);
    }

    /** Starts a new premium subscription.
     */
    function buyPremium() public {
        setPremium(msg.sender, msg.value);
    }


    /* AUXILIARY FUNCTIONS */

    /** Starts a new premium subscription for the user u based on the amount v.
     * @param u the user.
     * @param v the value.
     */
    function setPremium(address u, uint v) private {
        // Calculate the hours that the use can buy with this amount
        uint hoursToBuy = v / premiumCostPerHour;
        require (hoursToBuy > 0);
        // If the user has never bought premium or the premium subscription is expired reset the expiration time to now
        if (!isPremium(u)) premiumUsers[u] = now;
        // Increment the use expiration time
        premiumUsers[u] += hoursToBuy * 3600; // 1h = 3600s
        // If there is a remaining value refund the user
        uint remainder = v - hoursToBuy * premiumCostPerHour;
        if (remainder > 0) u.transfer(remainder);
    }

}