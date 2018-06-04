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
    address private author;
    string fallbackFunctionMessage = "Unexpected call: function does not exist. The fallback function has reverted the state.";


    /* EVENTS */
    event FallbackFunctionCall(string message, bytes data);


    /* MODIFIERS */
    modifier onlyOwner() {
        require (msg.sender == author);
        _;
    }


    /* FUNCTIONS */

    /** Constructor */
    constructor() public {
        author = msg.sender;
    }

    /** Fallback function */
    function () public {
        emit FallbackFunctionCall(fallbackFunctionMessage, msg.data);
        revert(fallbackFunctionMessage);
    }

    /** Suicide function, can be called only by the owner */
    function suicide() public onlyOwner {
        // If there is some wei send it to the author
        this.selfdestruct(author);
    }

    /** Used to know who is the creator of this content.
      * @return the content creator address.
      */
    function getAuthor() public constant returns(address) { return author; }
}