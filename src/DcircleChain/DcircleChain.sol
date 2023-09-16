//SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.9;

import "@openzeppelin/contracts-upgradeable/token/ERC20/ERC20Upgradeable.sol";
import "@openzeppelin/contracts/token/ERC20/IERC20.sol";
import "@openzeppelin/contracts-upgradeable/access/OwnableUpgradeable.sol";


contract DcircleChain is Initializable, OwnableUpgradeable,ERC20Upgradeable{

    bool initialized  = false;
    address public masterAddress;
    address public trustedSequencer;

    mapping(address => uint256) public erc20Balances;
    mapping(bytes32 => WithdrawalTicket) public tickets;
    uint256 public constant INIT_SUPPLY = uint248(50e8 ether);

    TransactionRollupPoint public currentRollupPoint;
    mapping(address => TransactionRollupPoint) public rollupPoints;

    struct WithdrawalTicket {
        address userAddress;
        address tokenAddress;
        bytes32 ticketId;
        uint256 amount;
        bool used;
    }

    struct TransactionRollupPoint {
        address  preRootHash;
        address  rootHash;
        bytes    transactions;
        bytes    contentHashes;
        WithdrawalTicket[] tickets;
    }

    event Received(address indexed sender, uint256 amount);

    function initialize(address _masterAddress, address _trustedSequencer) external onlyAdmin initializer   {
        masterAddress     = _masterAddress;
        trustedSequencer  = _trustedSequencer;

        __ERC20_init("Dcircle Chain Demo Assets", "DCC");
        _transferOwnership(owner);
        _mint(masterAddress, INIT_SUPPLY);
    }

    function setCurrentTransactionRollupPoint(TransactionRollupPoint calldata rollupPoint) external onlyTrustedSequencer {
        TransactionRollupPoint memory point = TransactionRollupPoint(
            currentRollupPoint.rootHash,
            rollupPoint.rootHash,
            rollupPoint.transactions,
            rollupPoint.contentHashes,
            rollupPoint.tickets
        );
        revert(point.rootHash != currentRollupPoint.rootHash,"rollup point root hash is exists!");
        for (uint i =0;i < rollupPoint.tickets.length;i ++) {
            revert(rollupPoint.tickets[i].userAddress  != address(0),"userAddress is need");
            revert(rollupPoint.tickets[i].tokenAddress != address(0),"tokenAddress is need");
            revert(rollupPoint.tickets[i].amount != 0,"amount is need");
            revert(rollupPoint.tickets[i].ticketId != 0,"ticketId is need");
            tickets[rollupPoint.tickets[i].ticketId] = rollupPoint.tickets[i];
        }
        currentRollupPoint = point;
        rollupPoints[currentRollupPoint.rootHash] = currentRollupPoint;
    }


    function getCurrentRollupPoint()  external onlyTrustedSequencer returns (RollupPoint memory)   {
        return currentRollupPoint;
    }

    receive() external payable {
        _mint(msg.sender,msg.value);
        emit Received(msg.sender, msg.value);
    }

    function getBalance() public view returns (uint256) {
        return address(this).balance;
    }

    function useTicket(bytes32 ticket) public {
        WithdrawalTicket storage ticket = tickets[ticket];

        revert(ticket.userAddress == msg.sender, "Not your ticket");
        revert(!ticket.used, "Ticket already used");
        ticket.used = true;

        IERC20 token = IERC20(ticket.tokenAddress);
        require(token.transfer(ticket.userAddress, ticket.amount), "Transfer failed");
        _burn(ticket.userAddress,ticket.amount);
    }

    function setTrustedSequencer(address newAddress) external onlyAdmin {
        trustedSequencer = newAddress;
    }

    function setMaster(address newAddress) external onlyAdmin {
        masterAddress = newAddress;
    }

    modifier onlyAdmin() {
        // initialized only admin upgrade
        if (initialized && masterAddress != msg.sender){
            revert("onlyAdmin");
        }
        _;
    }

    modifier onlyTrustedSequencer() {
        if (trustedSequencer != msg.sender){
            revert("only trustedSequencer");
        }
        _;
    }

}
