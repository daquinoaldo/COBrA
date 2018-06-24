pragma solidity ^0.4.0;

import "./BaseContentManagementContract.sol";

contract GenericContentManagementContract is BaseContentManagementContract {

    /* MODIFIERS */

    modifier notEmpty(bytes argument) {
        require(argument.length != 0);
        _;
    }

    modifier validAddress(address addr) {
        uint size;
        assembly { size := extcodesize(addr) }
        require(size > 0);
        _;
    }


    /* FUNCTIONS */

    /** Used by the author to set the name.
     * Can be called only one time.
     */
    function setName(bytes32 n) public onlyOwner {
        require(name[0] == 0);
        name = n;
    }

    /** Used by the author to set the genre.
     * Can be called only one time, but its call is not mandatory (the content can not have a genre).
     */
    function setGenre(bytes32 g) public onlyOwner {
        require(genre[0] == 0);
        genre = g;
    }

    /** Used by the author to set the content price.
     * Can be called only one time.
     */
    function setPrice(uint p) public onlyOwner {
        require(price == 0);
        price = p;
    }
}