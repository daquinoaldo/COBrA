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
    uint price; // is assumed that the content can be free, and so the price is 0.
    bool private published = false;
    CatalogContract private catalogContract;


    /* EVENTS */
    event FallbackFunctionCall(string message, bytes data);
    event ContentPublished();
    event ContentDeleted();
    event contentConsumed(address user);


    /* MODIFIERS */
    modifier onlyOwner() {
        require(msg.sender == author);
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
    function murder() public {
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
        require(published);
        require(catalogContract.hasAccess(msg.sender, this));
        catalogContract.consumeContent(msg.sender);
        emit contentConsumed(msg.sender);
    }

    /** Used by the author to publish the content.
     * @param c the address of the catalog in which publish the content.
     * The author must specify name and content of this contract before calling this function.
     * Can be called only one time.
     */
    function publish(address c) public onlyOwner {
        require(!published);
        require(name[0] != 0);
        published = true;
        catalog = c;
        catalogContract = CatalogContract(c);
        catalogContract.addMe();
        emit ContentPublished();
    }
}