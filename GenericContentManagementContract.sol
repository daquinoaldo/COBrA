pragma solidity ^0.4.0;

import "./BaseContentManagementContract.sol";

contract GenericContentManagementContract is BaseContentManagementContract {

    /* VARIABLES */
    /* Already declared as private in base BaseContentManagementContract
     * Declared again, because if we declare it internal who extends the
     * contract can see it and get access the content for free.
      */
    bytes private content;


    /* MODIFIERS */

    modifier notNull(bytes32 argument) {
        require(argument[0] != 0, "The argument can not be null.");
        _;
    }

    modifier notEmpty(bytes argument) {
        require(argument.length != 0, "The argument can not be empty.");
        _;
    }

    modifier validAddress(address addr) {
        uint size;
        assembly { size := extcodesize(addr) }
        require(size > 0, "The address is not valid.");
        _;
    }


    /* FUNCTIONS */

    /** Used by the author to set the content.
     * Can be called only one time.
     */
    function setContent(bytes c) public onlyOwner notEmpty(c) {
        require(content.length == 0, "The content can not be overwritten. Use the suicide function to delete this content and create a new one.");
        content = c;
    }

    /** Used by the author to set the name.
     * Can be called only one time.
     */
    function setName(bytes32 n) public onlyOwner notNull(n) {
        require(name[0] == 0, "The name can not be overwritten. Use the suicide function to delete this content and create a new one.");
        name = n;
    }

    /** Used by the author to set the genre.
     * Can be called only one time, but its call is not mandatory (the content can not have a genre).
     */
    function setGenre(bytes32 g) public onlyOwner notNull(g) {
        require(genre[0] == 0, "The name can not be overwritten. Use the suicide function to delete this content and create a new one.");
        genre = g;
    }
}