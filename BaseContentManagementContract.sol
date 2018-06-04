pragma solidity ^0.4.0;
contract BaseContentManagementContract {

    /* FUNCTION ORDER
        constructor
        fallback function (if exists)
        external
        public
        internal
        private
    */

    /* VARIABLES */
    address private creator;
    string fallbackFunctionMessage = "Unexpected call: function does not exist. The fallback function has reverted the state.";

    /* EVENTS */
    event FallbackFunctionCall(string message, bytes data);

    /* MODIFIERS */
    modifier onlyOwner() {
        require (msg.sender == creator);
        _;
    }

    /* FUNCTIONS */
    /** Constructor */
    constructor() public {
        creator = msg.sender;
    }

    /** Fallback function */
    function () public {
        emit FallbackFunctionCall(fallbackFunctionMessage, msg.data);
        revert(fallbackFunctionMessage);
    }

    /** Used to know who is the creator of this content.
      * @return the content creator address.
      */
    function getCreator() public constant returns(address) { return creator; }
}