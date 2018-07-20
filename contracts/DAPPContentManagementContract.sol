pragma solidity ^0.4.0;

import "./GenericContentManagementContract.sol";

contract DAPPContentManagementContract is GenericContentManagementContract {

    bytes32 public hostname;
    uint public port;

    /** Used by the author to set the server hostname.
     */
    function setHostname(bytes32 h) public onlyOwner {
        hostname = h;
    }

    /** Used by the author to set the server port.
     */
    function setPort(uint p) public onlyOwner {
        port = p;
    }

    /** Used by the author to publish the content.
     * @param c the address of the catalog in which publish the content.
     * The author must specify the name of this content and the author-server
     * hostname and port before calling this function.
     * Can be called only one time.
     */
    function publish(address c) public onlyOwner {
        require(hostname[0] != 0 && port != 0);
        super.publish(c);
    }
}