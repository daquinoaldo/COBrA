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
    uint private premiumCostPerHour = 0.1;

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
     * @return
     */
    function giftPremium(u) public {}

    /** Starts a new premium subscription.
     * @return
     */
    function buyPremium() public {}

}