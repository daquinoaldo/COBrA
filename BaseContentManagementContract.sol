pragma solidity ^0.4.0;

contract CatalogContract {
    function hasAccess(address u, address x) public view returns(bool);
    function consumeContent(address u, address x) public;
}

contract BaseContentManagementContract {

    /* VARIABLES */

    // Constants
    address CATALOG_ADDRESS; //TODO

    // Messages
    string private fallbackFunctionMessage = "Unexpected call: function does not exist. The fallback function has reverted the state.";

    // Runtime
    address author;
    bytes private content;
    CatalogContract private catalogContract;


    /* EVENTS */
    event FallbackFunctionCall(string message, bytes data);
    event contentConsumed(address user);


    /* MODIFIERS */
    modifier onlyOwner() {
        require (msg.sender == author);
        _;
    }


    /* FUNCTIONS */

    /** Constructor */
    constructor() public {
        author = msg.sender;
        catalogContract = CatalogContract(CATALOG_ADDRESS);
    }

    /** Fallback function */
    function () public {
        emit FallbackFunctionCall(fallbackFunctionMessage, msg.data);
        revert(fallbackFunctionMessage);
    }

    /** Suicide function, can be called only by the owner */
    function _suicide() public onlyOwner {
        // If there is some wei send it to the author
        selfdestruct(author);
    }

    /** Used by the author to set the content.
      */
    function setContent(bytes c) public onlyOwner {
        if (content.length != 0) revert("The content cannot be overwritten. Use the suicide function to delete this content and create a new one.");
        content = c;
    }

    /** Used by the customers to consume this content after requesting the access.
      * @return the content.
      */
    function consumeContent() public view returns(bytes) {
        require(catalogContract.hasAccess(msg.sender, this));
        consumeContent(msg.sender, this);
        emit contentConsumed(msg.sender);
        return content;
    }
}