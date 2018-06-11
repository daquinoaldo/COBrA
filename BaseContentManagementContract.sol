pragma solidity ^0.4.0;

contract CatalogContract {
    function hasAccess(address u, address x) public view returns(bool);
    function consumeContent(address u, address x) public;
    function addMe() public;
    function removesMe() public;
}

contract BaseContentManagementContract {

    /* VARIABLES */

    // Messages
    string private fallbackFunctionMessage = "Unexpected call: function does not exist. The fallback function has reverted the state.";

    // Runtime
    address public catalog;
    address public author;
    bytes32 public name;
    bytes32 public genre;

    bytes private content;
    bool private published = false;
    CatalogContract private catalogContract;


    /* EVENTS */
    event FallbackFunctionCall(string message, bytes data);
    event contentConsumed(address user);


    /* MODIFIERS */
    modifier onlyOwner() {
        require (msg.sender == author, "Only the author can perform this action.");
        _;
    }

    modifier notNull(bytes32 argument) {
        require (argument[0] != 0, "The argument can not be null.");
        _;
    }

    modifier notEmpty(bytes argument) {
        require (argument.length != 0, "The argument can not be empty.");
        _;
    }

    modifier validAddress(address addr) {
        uint size;
        assembly { size := extcodesize(addr) }
        require (size > 0, "The address is not valid.");
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
    function _suicide() public onlyOwner {
        catalogContract.removesMe();
        // If there is some wei send it to the author
        selfdestruct(author);
    }

    /** Suicide function, can be called only by the owner */
    function murder() public validAddress(catalog) {
        require(msg.sender == catalog);
        // If there is some wei send it to the catalog
        selfdestruct(catalog);
    }

    /** Used by the customers to consume this content after requesting the access.
     * @return the content.
     */
    function consumeContent() public returns(bytes) {
        require(published, "The content is not yet published.");
        require(catalogContract.hasAccess(msg.sender, this), "You must reserve this content before accessing it. Please contact the catalog.");
        catalogContract.consumeContent(msg.sender, this);
        emit contentConsumed(msg.sender);
        return content;
    }

    /** Used by the author to set the content.
     * Can be called only one time.
     */
    function setContent(bytes c) public onlyOwner notEmpty(c) {
        require (content.length == 0, "The content can not be overwritten. Use the suicide function to delete this content and create a new one.");
        content = c;
    }

    /** Used by the author to set the name.
     * Can be called only one time.
     */
    function setName(bytes32 n) public onlyOwner notNull(n) {
        require (name[0] == 0, "The name can not be overwritten. Use the suicide function to delete this content and create a new one.");
        name = n;
    }

    /** Used by the author to set the genre.
     * Can be called only one time, but its call is not mandatory (the content can not have a genre).
     */
    function setGenre(bytes32 g) public onlyOwner notNull(g) {
        require (genre[0] == 0, "The name can not be overwritten. Use the suicide function to delete this content and create a new one.");
        genre = g;
    }

    /** Used by the author to publish the content.
     * @param c the address of the catalog in which publish the content.
     * The author must specify name and content of this contract before calling this function.
     * Can be called only one time.
     */
    function publish(address c) public onlyOwner validAddress(c) {
        require (!published, "This contract is already published in the catalog.");
        require (name[0] != 0 && content.length != 0, "Both name and content must be set before publish the content in the catalog.");
        published = true;
        catalog = c;
        catalogContract = CatalogContract(c);
        catalogContract.addMe();
    }
}