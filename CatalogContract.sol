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

    // Messages
    string private fallbackFunctionMessage = "Unexpected call: function does not exist. " +
        "The fallback function has reverted the state.";

    // Runtime
    address private owner;

    // Structs
    mapping (address => uint) private premiumUsers;


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
     * @return an uint with the cost per hour in wei.
     */
    function getPremiumCostPerHour() public returns(uint) { return premiumCostPerHour; }

    /** Returns the number of views for each content.
     * @return
     */
    function getStatistics() public {}

    /** Returns the list of contents without the number of views.
     * @return
     */
    function getContentList() public {}

    /** Returns the list of x newest contents.
     * @return
     */
    function getNewContentsList() public {}

    /** Returns the most recent content with genre x.
     * @return
     */
    function getLatestByGenre(x) public {}

    /** Returns the content with genre x, which has received the maximum number of views
     * @return
     */
    function getMostPopularByGenre(x) public {}

    /** Returns the most recent content of the author x.
     * @return
     */
    function getLatestByAuthor(x) public {}

    /** Returns the content with most views of the author x.
     * @return
     */
    function getMostPopularByAuthor(x) public {}

    /** Returns true if x holds a still valid premium account, false otherwise.
     * @return
     */
    function isPremium(x) public {}

    /** Pays for access to content x.
     * @return
     */
    function getContent(x) public {}

    /** Requests access to content x without paying, premium accounts only.
     * @return
     */
    function getContentPremium(x) public {}

    /** Pays for granting access to content x to the user u.
     * @return
     */
    function giftContent(x, u) public {}

    /** Pays for granting a Premium Account to the user u.
     * @param u the user to whom you want to gift the subscription.
     */
    function giftPremium(address u) public {
        setPremium(u, msg.value);
    }

    /** Starts a new premium subscription.
     * @return
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
        if (premiumUsers[u] < now) premiumUsers[u] = now;
        // Increment the use expiration time
        premiumUsers[u] += hoursToBuy * 3600; // 1h = 3600s
        // If there is a remaining value refund the user
        uint remainder = v - hoursToBuy * premiumCostPerHour;
        if (remainder > 0) u.transfer(remainder);
    }

}