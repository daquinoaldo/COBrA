pragma solidity ^0.4.0;

contract CatalogContract {
    function hasAccess(address u, address x) public view returns(bool);
    function consumeContent(address u) public;
    function addMe() public;
    function removesMe() public;
}

contract BaseContentManagementContract {

    /* VARIABLES */

    // Runtime
    address public catalog;
    address public author;
    bytes32 public name;
    bytes32 public genre;
    bytes internal content;
    bool private published = false;
    CatalogContract private catalogContract;


    /* EVENTS */
    event FallbackFunctionCall(string message, bytes data);
    event ContentPublished();
    event ContentDeleted();
    event contentConsumed(address user);


    /* MODIFIERS */
    modifier onlyOwner() {
        require(msg.sender == author, "Only the author can perform this action.");
        _;
    }

    modifier validAddress(address addr) {
        uint size;
        assembly { size := extcodesize(addr) }
        require(size > 0, "The address is not valid.");
        _;
    }


    /* FUNCTIONS */
    /** Constructor */
    constructor() public {
        author = msg.sender;
    }

    /** Fallback function */
    function () public {
        revert();
    }

    /** Suicide function, can be called only by the owner */
    function _suicide() public onlyOwner {
        // notice the catalog
        catalogContract.removesMe();
        // emit an event
        emit ContentDeleted();
        // if there is some wei send it to the author
        selfdestruct(author);
    }

    /** Suicide function, can be called only by the owner */
    function murder() public validAddress(catalog) {
        require(msg.sender == catalog);
        // emit an event
        emit ContentDeleted();
        // if there is some wei send it to the author
        selfdestruct(author);
    }

    /** Used by the customers to consume this content after requesting the access.
     * @return the content.
     */
    function consumeContent() public returns(bytes) {
        require(published, "The content is not yet published.");
        require(catalogContract.hasAccess(msg.sender, this), "You must reserve this content before accessing it. Please contact the catalog.");
        catalogContract.consumeContent(msg.sender);
        emit contentConsumed(msg.sender);
        return content;
    }

    /** Used by the author to publish the content.
     * @param c the address of the catalog in which publish the content.
     * The author must specify name and content of this contract before calling this function.
     * Can be called only one time.
     */
    function publish(address c) public onlyOwner validAddress(c) {
        require(!published, "This contract is already published in the catalog.");
        require(name[0] != 0 && content.length != 0, "Both name and content must be set before publish the content in the catalog.");
        published = true;
        catalog = c;
        catalogContract = CatalogContract(c);
        catalogContract.addMe();
        emit ContentPublished();
    }
}