// Code generated - DO NOT EDIT.
// This file is a generated binding and any manual changes will be lost.

package contracts

import (
	"errors"
	"math/big"
	"strings"

	ethereum "github.com/ethereum/go-ethereum"
	"github.com/ethereum/go-ethereum/accounts/abi"
	"github.com/ethereum/go-ethereum/accounts/abi/bind"
	"github.com/ethereum/go-ethereum/common"
	"github.com/ethereum/go-ethereum/core/types"
	"github.com/ethereum/go-ethereum/event"
)

// Reference imports to suppress errors if they are not otherwise used.
var (
	_ = errors.New
	_ = big.NewInt
	_ = strings.NewReader
	_ = ethereum.NotFound
	_ = bind.Bind
	_ = common.Big1
	_ = types.BloomLookup
	_ = event.NewSubscription
	_ = abi.ConvertType
)

// DCRC20RollupPoint is an auto generated low-level Go binding around an user-defined struct.
type DCRC20RollupPoint struct {
	PreRootHash   common.Address
	RootHash      common.Address
	Transactions  []byte
	ContentHashes []byte
	RollupTime    *big.Int
}

// DCChainMetaData contains all meta data concerning the DCChain contract.
var DCChainMetaData = &bind.MetaData{
	ABI: "[{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"internalType\":\"address\",\"name\":\"owner\",\"type\":\"address\"},{\"indexed\":true,\"internalType\":\"address\",\"name\":\"spender\",\"type\":\"address\"},{\"indexed\":false,\"internalType\":\"uint256\",\"name\":\"value\",\"type\":\"uint256\"}],\"name\":\"Approval\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"uint8\",\"name\":\"version\",\"type\":\"uint8\"}],\"name\":\"Initialized\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"internalType\":\"address\",\"name\":\"from\",\"type\":\"address\"},{\"indexed\":true,\"internalType\":\"address\",\"name\":\"to\",\"type\":\"address\"},{\"indexed\":false,\"internalType\":\"uint256\",\"name\":\"value\",\"type\":\"uint256\"}],\"name\":\"Transfer\",\"type\":\"event\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"owner\",\"type\":\"address\"},{\"internalType\":\"address\",\"name\":\"spender\",\"type\":\"address\"}],\"name\":\"allowance\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"spender\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"approve\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"account\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"currentRollupPoint\",\"outputs\":[{\"internalType\":\"address\",\"name\":\"preRootHash\",\"type\":\"address\"},{\"internalType\":\"address\",\"name\":\"rootHash\",\"type\":\"address\"},{\"internalType\":\"bytes\",\"name\":\"transactions\",\"type\":\"bytes\"},{\"internalType\":\"bytes\",\"name\":\"contentHashes\",\"type\":\"bytes\"},{\"internalType\":\"uint256\",\"name\":\"rollupTime\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"decimals\",\"outputs\":[{\"internalType\":\"uint8\",\"name\":\"\",\"type\":\"uint8\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"spender\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"subtractedValue\",\"type\":\"uint256\"}],\"name\":\"decreaseAllowance\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"tokenAddress\",\"type\":\"address\"},{\"internalType\":\"address\",\"name\":\"masterAddress\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"depositsWithToken\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"getCurrentRollupPoint\",\"outputs\":[{\"components\":[{\"internalType\":\"address\",\"name\":\"preRootHash\",\"type\":\"address\"},{\"internalType\":\"address\",\"name\":\"rootHash\",\"type\":\"address\"},{\"internalType\":\"bytes\",\"name\":\"transactions\",\"type\":\"bytes\"},{\"internalType\":\"bytes\",\"name\":\"contentHashes\",\"type\":\"bytes\"},{\"internalType\":\"uint256\",\"name\":\"rollupTime\",\"type\":\"uint256\"}],\"internalType\":\"structDCRC20.RollupPoint\",\"name\":\"\",\"type\":\"tuple\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"spender\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"addedValue\",\"type\":\"uint256\"}],\"name\":\"increaseAllowance\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"_masterAddress\",\"type\":\"address\"},{\"internalType\":\"address\",\"name\":\"_trustedSequencer\",\"type\":\"address\"}],\"name\":\"initialize\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"masterAddress\",\"outputs\":[{\"internalType\":\"address\",\"name\":\"\",\"type\":\"address\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"name\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"\",\"type\":\"address\"}],\"name\":\"rollupPoints\",\"outputs\":[{\"internalType\":\"address\",\"name\":\"preRootHash\",\"type\":\"address\"},{\"internalType\":\"address\",\"name\":\"rootHash\",\"type\":\"address\"},{\"internalType\":\"bytes\",\"name\":\"transactions\",\"type\":\"bytes\"},{\"internalType\":\"bytes\",\"name\":\"contentHashes\",\"type\":\"bytes\"},{\"internalType\":\"uint256\",\"name\":\"rollupTime\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"components\":[{\"internalType\":\"address\",\"name\":\"preRootHash\",\"type\":\"address\"},{\"internalType\":\"address\",\"name\":\"rootHash\",\"type\":\"address\"},{\"internalType\":\"bytes\",\"name\":\"transactions\",\"type\":\"bytes\"},{\"internalType\":\"bytes\",\"name\":\"contentHashes\",\"type\":\"bytes\"},{\"internalType\":\"uint256\",\"name\":\"rollupTime\",\"type\":\"uint256\"}],\"internalType\":\"structDCRC20.RollupPoint\",\"name\":\"rollupPoint\",\"type\":\"tuple\"}],\"name\":\"setCurrentRollupPoint\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"newAddress\",\"type\":\"address\"}],\"name\":\"setMaster\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"newAddress\",\"type\":\"address\"}],\"name\":\"setTrustedSequencer\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"symbol\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"tokenDecimal\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"totalSupply\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"to\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"from\",\"type\":\"address\"},{\"internalType\":\"address\",\"name\":\"to\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"transferFrom\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"trustedSequencer\",\"outputs\":[{\"internalType\":\"address\",\"name\":\"\",\"type\":\"address\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"tokenAddress\",\"type\":\"address\"},{\"internalType\":\"address\",\"name\":\"toAddress\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"withdrawal\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]",
}

// DCChainABI is the input ABI used to generate the binding from.
// Deprecated: Use DCChainMetaData.ABI instead.
var DCChainABI = DCChainMetaData.ABI

// DCChain is an auto generated Go binding around an Ethereum contract.
type DCChain struct {
	DCChainCaller     // Read-only binding to the contract
	DCChainTransactor // Write-only binding to the contract
	DCChainFilterer   // Log filterer for contract events
}

// DCChainCaller is an auto generated read-only Go binding around an Ethereum contract.
type DCChainCaller struct {
	contract *bind.BoundContract // Generic contract wrapper for the low level calls
}

// DCChainTransactor is an auto generated write-only Go binding around an Ethereum contract.
type DCChainTransactor struct {
	contract *bind.BoundContract // Generic contract wrapper for the low level calls
}

// DCChainFilterer is an auto generated log filtering Go binding around an Ethereum contract events.
type DCChainFilterer struct {
	contract *bind.BoundContract // Generic contract wrapper for the low level calls
}

// DCChainSession is an auto generated Go binding around an Ethereum contract,
// with pre-set call and transact options.
type DCChainSession struct {
	Contract     *DCChain          // Generic contract binding to set the session for
	CallOpts     bind.CallOpts     // Call options to use throughout this session
	TransactOpts bind.TransactOpts // Transaction auth options to use throughout this session
}

// DCChainCallerSession is an auto generated read-only Go binding around an Ethereum contract,
// with pre-set call options.
type DCChainCallerSession struct {
	Contract *DCChainCaller // Generic contract caller binding to set the session for
	CallOpts bind.CallOpts  // Call options to use throughout this session
}

// DCChainTransactorSession is an auto generated write-only Go binding around an Ethereum contract,
// with pre-set transact options.
type DCChainTransactorSession struct {
	Contract     *DCChainTransactor // Generic contract transactor binding to set the session for
	TransactOpts bind.TransactOpts  // Transaction auth options to use throughout this session
}

// DCChainRaw is an auto generated low-level Go binding around an Ethereum contract.
type DCChainRaw struct {
	Contract *DCChain // Generic contract binding to access the raw methods on
}

// DCChainCallerRaw is an auto generated low-level read-only Go binding around an Ethereum contract.
type DCChainCallerRaw struct {
	Contract *DCChainCaller // Generic read-only contract binding to access the raw methods on
}

// DCChainTransactorRaw is an auto generated low-level write-only Go binding around an Ethereum contract.
type DCChainTransactorRaw struct {
	Contract *DCChainTransactor // Generic write-only contract binding to access the raw methods on
}

// NewDCChain creates a new instance of DCChain, bound to a specific deployed contract.
func NewDCChain(address common.Address, backend bind.ContractBackend) (*DCChain, error) {
	contract, err := bindDCChain(address, backend, backend, backend)
	if err != nil {
		return nil, err
	}
	return &DCChain{DCChainCaller: DCChainCaller{contract: contract}, DCChainTransactor: DCChainTransactor{contract: contract}, DCChainFilterer: DCChainFilterer{contract: contract}}, nil
}

// NewDCChainCaller creates a new read-only instance of DCChain, bound to a specific deployed contract.
func NewDCChainCaller(address common.Address, caller bind.ContractCaller) (*DCChainCaller, error) {
	contract, err := bindDCChain(address, caller, nil, nil)
	if err != nil {
		return nil, err
	}
	return &DCChainCaller{contract: contract}, nil
}

// NewDCChainTransactor creates a new write-only instance of DCChain, bound to a specific deployed contract.
func NewDCChainTransactor(address common.Address, transactor bind.ContractTransactor) (*DCChainTransactor, error) {
	contract, err := bindDCChain(address, nil, transactor, nil)
	if err != nil {
		return nil, err
	}
	return &DCChainTransactor{contract: contract}, nil
}

// NewDCChainFilterer creates a new log filterer instance of DCChain, bound to a specific deployed contract.
func NewDCChainFilterer(address common.Address, filterer bind.ContractFilterer) (*DCChainFilterer, error) {
	contract, err := bindDCChain(address, nil, nil, filterer)
	if err != nil {
		return nil, err
	}
	return &DCChainFilterer{contract: contract}, nil
}

// bindDCChain binds a generic wrapper to an already deployed contract.
func bindDCChain(address common.Address, caller bind.ContractCaller, transactor bind.ContractTransactor, filterer bind.ContractFilterer) (*bind.BoundContract, error) {
	parsed, err := DCChainMetaData.GetAbi()
	if err != nil {
		return nil, err
	}
	return bind.NewBoundContract(address, *parsed, caller, transactor, filterer), nil
}

// Call invokes the (constant) contract method with params as input values and
// sets the output to result. The result type might be a single field for simple
// returns, a slice of interfaces for anonymous returns and a struct for named
// returns.
func (_DCChain *DCChainRaw) Call(opts *bind.CallOpts, result *[]interface{}, method string, params ...interface{}) error {
	return _DCChain.Contract.DCChainCaller.contract.Call(opts, result, method, params...)
}

// Transfer initiates a plain transaction to move funds to the contract, calling
// its default method if one is available.
func (_DCChain *DCChainRaw) Transfer(opts *bind.TransactOpts) (*types.Transaction, error) {
	return _DCChain.Contract.DCChainTransactor.contract.Transfer(opts)
}

// Transact invokes the (paid) contract method with params as input values.
func (_DCChain *DCChainRaw) Transact(opts *bind.TransactOpts, method string, params ...interface{}) (*types.Transaction, error) {
	return _DCChain.Contract.DCChainTransactor.contract.Transact(opts, method, params...)
}

// Call invokes the (constant) contract method with params as input values and
// sets the output to result. The result type might be a single field for simple
// returns, a slice of interfaces for anonymous returns and a struct for named
// returns.
func (_DCChain *DCChainCallerRaw) Call(opts *bind.CallOpts, result *[]interface{}, method string, params ...interface{}) error {
	return _DCChain.Contract.contract.Call(opts, result, method, params...)
}

// Transfer initiates a plain transaction to move funds to the contract, calling
// its default method if one is available.
func (_DCChain *DCChainTransactorRaw) Transfer(opts *bind.TransactOpts) (*types.Transaction, error) {
	return _DCChain.Contract.contract.Transfer(opts)
}

// Transact invokes the (paid) contract method with params as input values.
func (_DCChain *DCChainTransactorRaw) Transact(opts *bind.TransactOpts, method string, params ...interface{}) (*types.Transaction, error) {
	return _DCChain.Contract.contract.Transact(opts, method, params...)
}

// Allowance is a free data retrieval call binding the contract method 0xdd62ed3e.
//
// Solidity: function allowance(address owner, address spender) view returns(uint256)
func (_DCChain *DCChainCaller) Allowance(opts *bind.CallOpts, owner common.Address, spender common.Address) (*big.Int, error) {
	var out []interface{}
	err := _DCChain.contract.Call(opts, &out, "allowance", owner, spender)

	if err != nil {
		return *new(*big.Int), err
	}

	out0 := *abi.ConvertType(out[0], new(*big.Int)).(**big.Int)

	return out0, err

}

// Allowance is a free data retrieval call binding the contract method 0xdd62ed3e.
//
// Solidity: function allowance(address owner, address spender) view returns(uint256)
func (_DCChain *DCChainSession) Allowance(owner common.Address, spender common.Address) (*big.Int, error) {
	return _DCChain.Contract.Allowance(&_DCChain.CallOpts, owner, spender)
}

// Allowance is a free data retrieval call binding the contract method 0xdd62ed3e.
//
// Solidity: function allowance(address owner, address spender) view returns(uint256)
func (_DCChain *DCChainCallerSession) Allowance(owner common.Address, spender common.Address) (*big.Int, error) {
	return _DCChain.Contract.Allowance(&_DCChain.CallOpts, owner, spender)
}

// BalanceOf is a free data retrieval call binding the contract method 0x70a08231.
//
// Solidity: function balanceOf(address account) view returns(uint256)
func (_DCChain *DCChainCaller) BalanceOf(opts *bind.CallOpts, account common.Address) (*big.Int, error) {
	var out []interface{}
	err := _DCChain.contract.Call(opts, &out, "balanceOf", account)

	if err != nil {
		return *new(*big.Int), err
	}

	out0 := *abi.ConvertType(out[0], new(*big.Int)).(**big.Int)

	return out0, err

}

// BalanceOf is a free data retrieval call binding the contract method 0x70a08231.
//
// Solidity: function balanceOf(address account) view returns(uint256)
func (_DCChain *DCChainSession) BalanceOf(account common.Address) (*big.Int, error) {
	return _DCChain.Contract.BalanceOf(&_DCChain.CallOpts, account)
}

// BalanceOf is a free data retrieval call binding the contract method 0x70a08231.
//
// Solidity: function balanceOf(address account) view returns(uint256)
func (_DCChain *DCChainCallerSession) BalanceOf(account common.Address) (*big.Int, error) {
	return _DCChain.Contract.BalanceOf(&_DCChain.CallOpts, account)
}

// CurrentRollupPoint is a free data retrieval call binding the contract method 0x50fe29b6.
//
// Solidity: function currentRollupPoint() view returns(address preRootHash, address rootHash, bytes transactions, bytes contentHashes, uint256 rollupTime)
func (_DCChain *DCChainCaller) CurrentRollupPoint(opts *bind.CallOpts) (struct {
	PreRootHash   common.Address
	RootHash      common.Address
	Transactions  []byte
	ContentHashes []byte
	RollupTime    *big.Int
}, error) {
	var out []interface{}
	err := _DCChain.contract.Call(opts, &out, "currentRollupPoint")

	outstruct := new(struct {
		PreRootHash   common.Address
		RootHash      common.Address
		Transactions  []byte
		ContentHashes []byte
		RollupTime    *big.Int
	})
	if err != nil {
		return *outstruct, err
	}

	outstruct.PreRootHash = *abi.ConvertType(out[0], new(common.Address)).(*common.Address)
	outstruct.RootHash = *abi.ConvertType(out[1], new(common.Address)).(*common.Address)
	outstruct.Transactions = *abi.ConvertType(out[2], new([]byte)).(*[]byte)
	outstruct.ContentHashes = *abi.ConvertType(out[3], new([]byte)).(*[]byte)
	outstruct.RollupTime = *abi.ConvertType(out[4], new(*big.Int)).(**big.Int)

	return *outstruct, err

}

// CurrentRollupPoint is a free data retrieval call binding the contract method 0x50fe29b6.
//
// Solidity: function currentRollupPoint() view returns(address preRootHash, address rootHash, bytes transactions, bytes contentHashes, uint256 rollupTime)
func (_DCChain *DCChainSession) CurrentRollupPoint() (struct {
	PreRootHash   common.Address
	RootHash      common.Address
	Transactions  []byte
	ContentHashes []byte
	RollupTime    *big.Int
}, error) {
	return _DCChain.Contract.CurrentRollupPoint(&_DCChain.CallOpts)
}

// CurrentRollupPoint is a free data retrieval call binding the contract method 0x50fe29b6.
//
// Solidity: function currentRollupPoint() view returns(address preRootHash, address rootHash, bytes transactions, bytes contentHashes, uint256 rollupTime)
func (_DCChain *DCChainCallerSession) CurrentRollupPoint() (struct {
	PreRootHash   common.Address
	RootHash      common.Address
	Transactions  []byte
	ContentHashes []byte
	RollupTime    *big.Int
}, error) {
	return _DCChain.Contract.CurrentRollupPoint(&_DCChain.CallOpts)
}

// Decimals is a free data retrieval call binding the contract method 0x313ce567.
//
// Solidity: function decimals() view returns(uint8)
func (_DCChain *DCChainCaller) Decimals(opts *bind.CallOpts) (uint8, error) {
	var out []interface{}
	err := _DCChain.contract.Call(opts, &out, "decimals")

	if err != nil {
		return *new(uint8), err
	}

	out0 := *abi.ConvertType(out[0], new(uint8)).(*uint8)

	return out0, err

}

// Decimals is a free data retrieval call binding the contract method 0x313ce567.
//
// Solidity: function decimals() view returns(uint8)
func (_DCChain *DCChainSession) Decimals() (uint8, error) {
	return _DCChain.Contract.Decimals(&_DCChain.CallOpts)
}

// Decimals is a free data retrieval call binding the contract method 0x313ce567.
//
// Solidity: function decimals() view returns(uint8)
func (_DCChain *DCChainCallerSession) Decimals() (uint8, error) {
	return _DCChain.Contract.Decimals(&_DCChain.CallOpts)
}

// MasterAddress is a free data retrieval call binding the contract method 0xd365a08e.
//
// Solidity: function masterAddress() view returns(address)
func (_DCChain *DCChainCaller) MasterAddress(opts *bind.CallOpts) (common.Address, error) {
	var out []interface{}
	err := _DCChain.contract.Call(opts, &out, "masterAddress")

	if err != nil {
		return *new(common.Address), err
	}

	out0 := *abi.ConvertType(out[0], new(common.Address)).(*common.Address)

	return out0, err

}

// MasterAddress is a free data retrieval call binding the contract method 0xd365a08e.
//
// Solidity: function masterAddress() view returns(address)
func (_DCChain *DCChainSession) MasterAddress() (common.Address, error) {
	return _DCChain.Contract.MasterAddress(&_DCChain.CallOpts)
}

// MasterAddress is a free data retrieval call binding the contract method 0xd365a08e.
//
// Solidity: function masterAddress() view returns(address)
func (_DCChain *DCChainCallerSession) MasterAddress() (common.Address, error) {
	return _DCChain.Contract.MasterAddress(&_DCChain.CallOpts)
}

// Name is a free data retrieval call binding the contract method 0x06fdde03.
//
// Solidity: function name() view returns(string)
func (_DCChain *DCChainCaller) Name(opts *bind.CallOpts) (string, error) {
	var out []interface{}
	err := _DCChain.contract.Call(opts, &out, "name")

	if err != nil {
		return *new(string), err
	}

	out0 := *abi.ConvertType(out[0], new(string)).(*string)

	return out0, err

}

// Name is a free data retrieval call binding the contract method 0x06fdde03.
//
// Solidity: function name() view returns(string)
func (_DCChain *DCChainSession) Name() (string, error) {
	return _DCChain.Contract.Name(&_DCChain.CallOpts)
}

// Name is a free data retrieval call binding the contract method 0x06fdde03.
//
// Solidity: function name() view returns(string)
func (_DCChain *DCChainCallerSession) Name() (string, error) {
	return _DCChain.Contract.Name(&_DCChain.CallOpts)
}

// RollupPoints is a free data retrieval call binding the contract method 0x96a91d1e.
//
// Solidity: function rollupPoints(address ) view returns(address preRootHash, address rootHash, bytes transactions, bytes contentHashes, uint256 rollupTime)
func (_DCChain *DCChainCaller) RollupPoints(opts *bind.CallOpts, arg0 common.Address) (struct {
	PreRootHash   common.Address
	RootHash      common.Address
	Transactions  []byte
	ContentHashes []byte
	RollupTime    *big.Int
}, error) {
	var out []interface{}
	err := _DCChain.contract.Call(opts, &out, "rollupPoints", arg0)

	outstruct := new(struct {
		PreRootHash   common.Address
		RootHash      common.Address
		Transactions  []byte
		ContentHashes []byte
		RollupTime    *big.Int
	})
	if err != nil {
		return *outstruct, err
	}

	outstruct.PreRootHash = *abi.ConvertType(out[0], new(common.Address)).(*common.Address)
	outstruct.RootHash = *abi.ConvertType(out[1], new(common.Address)).(*common.Address)
	outstruct.Transactions = *abi.ConvertType(out[2], new([]byte)).(*[]byte)
	outstruct.ContentHashes = *abi.ConvertType(out[3], new([]byte)).(*[]byte)
	outstruct.RollupTime = *abi.ConvertType(out[4], new(*big.Int)).(**big.Int)

	return *outstruct, err

}

// RollupPoints is a free data retrieval call binding the contract method 0x96a91d1e.
//
// Solidity: function rollupPoints(address ) view returns(address preRootHash, address rootHash, bytes transactions, bytes contentHashes, uint256 rollupTime)
func (_DCChain *DCChainSession) RollupPoints(arg0 common.Address) (struct {
	PreRootHash   common.Address
	RootHash      common.Address
	Transactions  []byte
	ContentHashes []byte
	RollupTime    *big.Int
}, error) {
	return _DCChain.Contract.RollupPoints(&_DCChain.CallOpts, arg0)
}

// RollupPoints is a free data retrieval call binding the contract method 0x96a91d1e.
//
// Solidity: function rollupPoints(address ) view returns(address preRootHash, address rootHash, bytes transactions, bytes contentHashes, uint256 rollupTime)
func (_DCChain *DCChainCallerSession) RollupPoints(arg0 common.Address) (struct {
	PreRootHash   common.Address
	RootHash      common.Address
	Transactions  []byte
	ContentHashes []byte
	RollupTime    *big.Int
}, error) {
	return _DCChain.Contract.RollupPoints(&_DCChain.CallOpts, arg0)
}

// Symbol is a free data retrieval call binding the contract method 0x95d89b41.
//
// Solidity: function symbol() view returns(string)
func (_DCChain *DCChainCaller) Symbol(opts *bind.CallOpts) (string, error) {
	var out []interface{}
	err := _DCChain.contract.Call(opts, &out, "symbol")

	if err != nil {
		return *new(string), err
	}

	out0 := *abi.ConvertType(out[0], new(string)).(*string)

	return out0, err

}

// Symbol is a free data retrieval call binding the contract method 0x95d89b41.
//
// Solidity: function symbol() view returns(string)
func (_DCChain *DCChainSession) Symbol() (string, error) {
	return _DCChain.Contract.Symbol(&_DCChain.CallOpts)
}

// Symbol is a free data retrieval call binding the contract method 0x95d89b41.
//
// Solidity: function symbol() view returns(string)
func (_DCChain *DCChainCallerSession) Symbol() (string, error) {
	return _DCChain.Contract.Symbol(&_DCChain.CallOpts)
}

// TokenDecimal is a free data retrieval call binding the contract method 0x5caed029.
//
// Solidity: function tokenDecimal() view returns(uint256)
func (_DCChain *DCChainCaller) TokenDecimal(opts *bind.CallOpts) (*big.Int, error) {
	var out []interface{}
	err := _DCChain.contract.Call(opts, &out, "tokenDecimal")

	if err != nil {
		return *new(*big.Int), err
	}

	out0 := *abi.ConvertType(out[0], new(*big.Int)).(**big.Int)

	return out0, err

}

// TokenDecimal is a free data retrieval call binding the contract method 0x5caed029.
//
// Solidity: function tokenDecimal() view returns(uint256)
func (_DCChain *DCChainSession) TokenDecimal() (*big.Int, error) {
	return _DCChain.Contract.TokenDecimal(&_DCChain.CallOpts)
}

// TokenDecimal is a free data retrieval call binding the contract method 0x5caed029.
//
// Solidity: function tokenDecimal() view returns(uint256)
func (_DCChain *DCChainCallerSession) TokenDecimal() (*big.Int, error) {
	return _DCChain.Contract.TokenDecimal(&_DCChain.CallOpts)
}

// TotalSupply is a free data retrieval call binding the contract method 0x18160ddd.
//
// Solidity: function totalSupply() view returns(uint256)
func (_DCChain *DCChainCaller) TotalSupply(opts *bind.CallOpts) (*big.Int, error) {
	var out []interface{}
	err := _DCChain.contract.Call(opts, &out, "totalSupply")

	if err != nil {
		return *new(*big.Int), err
	}

	out0 := *abi.ConvertType(out[0], new(*big.Int)).(**big.Int)

	return out0, err

}

// TotalSupply is a free data retrieval call binding the contract method 0x18160ddd.
//
// Solidity: function totalSupply() view returns(uint256)
func (_DCChain *DCChainSession) TotalSupply() (*big.Int, error) {
	return _DCChain.Contract.TotalSupply(&_DCChain.CallOpts)
}

// TotalSupply is a free data retrieval call binding the contract method 0x18160ddd.
//
// Solidity: function totalSupply() view returns(uint256)
func (_DCChain *DCChainCallerSession) TotalSupply() (*big.Int, error) {
	return _DCChain.Contract.TotalSupply(&_DCChain.CallOpts)
}

// TrustedSequencer is a free data retrieval call binding the contract method 0xcfa8ed47.
//
// Solidity: function trustedSequencer() view returns(address)
func (_DCChain *DCChainCaller) TrustedSequencer(opts *bind.CallOpts) (common.Address, error) {
	var out []interface{}
	err := _DCChain.contract.Call(opts, &out, "trustedSequencer")

	if err != nil {
		return *new(common.Address), err
	}

	out0 := *abi.ConvertType(out[0], new(common.Address)).(*common.Address)

	return out0, err

}

// TrustedSequencer is a free data retrieval call binding the contract method 0xcfa8ed47.
//
// Solidity: function trustedSequencer() view returns(address)
func (_DCChain *DCChainSession) TrustedSequencer() (common.Address, error) {
	return _DCChain.Contract.TrustedSequencer(&_DCChain.CallOpts)
}

// TrustedSequencer is a free data retrieval call binding the contract method 0xcfa8ed47.
//
// Solidity: function trustedSequencer() view returns(address)
func (_DCChain *DCChainCallerSession) TrustedSequencer() (common.Address, error) {
	return _DCChain.Contract.TrustedSequencer(&_DCChain.CallOpts)
}

// Approve is a paid mutator transaction binding the contract method 0x095ea7b3.
//
// Solidity: function approve(address spender, uint256 amount) returns(bool)
func (_DCChain *DCChainTransactor) Approve(opts *bind.TransactOpts, spender common.Address, amount *big.Int) (*types.Transaction, error) {
	return _DCChain.contract.Transact(opts, "approve", spender, amount)
}

// Approve is a paid mutator transaction binding the contract method 0x095ea7b3.
//
// Solidity: function approve(address spender, uint256 amount) returns(bool)
func (_DCChain *DCChainSession) Approve(spender common.Address, amount *big.Int) (*types.Transaction, error) {
	return _DCChain.Contract.Approve(&_DCChain.TransactOpts, spender, amount)
}

// Approve is a paid mutator transaction binding the contract method 0x095ea7b3.
//
// Solidity: function approve(address spender, uint256 amount) returns(bool)
func (_DCChain *DCChainTransactorSession) Approve(spender common.Address, amount *big.Int) (*types.Transaction, error) {
	return _DCChain.Contract.Approve(&_DCChain.TransactOpts, spender, amount)
}

// DecreaseAllowance is a paid mutator transaction binding the contract method 0xa457c2d7.
//
// Solidity: function decreaseAllowance(address spender, uint256 subtractedValue) returns(bool)
func (_DCChain *DCChainTransactor) DecreaseAllowance(opts *bind.TransactOpts, spender common.Address, subtractedValue *big.Int) (*types.Transaction, error) {
	return _DCChain.contract.Transact(opts, "decreaseAllowance", spender, subtractedValue)
}

// DecreaseAllowance is a paid mutator transaction binding the contract method 0xa457c2d7.
//
// Solidity: function decreaseAllowance(address spender, uint256 subtractedValue) returns(bool)
func (_DCChain *DCChainSession) DecreaseAllowance(spender common.Address, subtractedValue *big.Int) (*types.Transaction, error) {
	return _DCChain.Contract.DecreaseAllowance(&_DCChain.TransactOpts, spender, subtractedValue)
}

// DecreaseAllowance is a paid mutator transaction binding the contract method 0xa457c2d7.
//
// Solidity: function decreaseAllowance(address spender, uint256 subtractedValue) returns(bool)
func (_DCChain *DCChainTransactorSession) DecreaseAllowance(spender common.Address, subtractedValue *big.Int) (*types.Transaction, error) {
	return _DCChain.Contract.DecreaseAllowance(&_DCChain.TransactOpts, spender, subtractedValue)
}

// DepositsWithToken is a paid mutator transaction binding the contract method 0x6e8b2733.
//
// Solidity: function depositsWithToken(address tokenAddress, address masterAddress, uint256 amount) returns()
func (_DCChain *DCChainTransactor) DepositsWithToken(opts *bind.TransactOpts, tokenAddress common.Address, masterAddress common.Address, amount *big.Int) (*types.Transaction, error) {
	return _DCChain.contract.Transact(opts, "depositsWithToken", tokenAddress, masterAddress, amount)
}

// DepositsWithToken is a paid mutator transaction binding the contract method 0x6e8b2733.
//
// Solidity: function depositsWithToken(address tokenAddress, address masterAddress, uint256 amount) returns()
func (_DCChain *DCChainSession) DepositsWithToken(tokenAddress common.Address, masterAddress common.Address, amount *big.Int) (*types.Transaction, error) {
	return _DCChain.Contract.DepositsWithToken(&_DCChain.TransactOpts, tokenAddress, masterAddress, amount)
}

// DepositsWithToken is a paid mutator transaction binding the contract method 0x6e8b2733.
//
// Solidity: function depositsWithToken(address tokenAddress, address masterAddress, uint256 amount) returns()
func (_DCChain *DCChainTransactorSession) DepositsWithToken(tokenAddress common.Address, masterAddress common.Address, amount *big.Int) (*types.Transaction, error) {
	return _DCChain.Contract.DepositsWithToken(&_DCChain.TransactOpts, tokenAddress, masterAddress, amount)
}

// GetCurrentRollupPoint is a paid mutator transaction binding the contract method 0x03118713.
//
// Solidity: function getCurrentRollupPoint() returns((address,address,bytes,bytes,uint256))
func (_DCChain *DCChainTransactor) GetCurrentRollupPoint(opts *bind.TransactOpts) (*types.Transaction, error) {
	return _DCChain.contract.Transact(opts, "getCurrentRollupPoint")
}

// GetCurrentRollupPoint is a paid mutator transaction binding the contract method 0x03118713.
//
// Solidity: function getCurrentRollupPoint() returns((address,address,bytes,bytes,uint256))
func (_DCChain *DCChainSession) GetCurrentRollupPoint() (*types.Transaction, error) {
	return _DCChain.Contract.GetCurrentRollupPoint(&_DCChain.TransactOpts)
}

// GetCurrentRollupPoint is a paid mutator transaction binding the contract method 0x03118713.
//
// Solidity: function getCurrentRollupPoint() returns((address,address,bytes,bytes,uint256))
func (_DCChain *DCChainTransactorSession) GetCurrentRollupPoint() (*types.Transaction, error) {
	return _DCChain.Contract.GetCurrentRollupPoint(&_DCChain.TransactOpts)
}

// IncreaseAllowance is a paid mutator transaction binding the contract method 0x39509351.
//
// Solidity: function increaseAllowance(address spender, uint256 addedValue) returns(bool)
func (_DCChain *DCChainTransactor) IncreaseAllowance(opts *bind.TransactOpts, spender common.Address, addedValue *big.Int) (*types.Transaction, error) {
	return _DCChain.contract.Transact(opts, "increaseAllowance", spender, addedValue)
}

// IncreaseAllowance is a paid mutator transaction binding the contract method 0x39509351.
//
// Solidity: function increaseAllowance(address spender, uint256 addedValue) returns(bool)
func (_DCChain *DCChainSession) IncreaseAllowance(spender common.Address, addedValue *big.Int) (*types.Transaction, error) {
	return _DCChain.Contract.IncreaseAllowance(&_DCChain.TransactOpts, spender, addedValue)
}

// IncreaseAllowance is a paid mutator transaction binding the contract method 0x39509351.
//
// Solidity: function increaseAllowance(address spender, uint256 addedValue) returns(bool)
func (_DCChain *DCChainTransactorSession) IncreaseAllowance(spender common.Address, addedValue *big.Int) (*types.Transaction, error) {
	return _DCChain.Contract.IncreaseAllowance(&_DCChain.TransactOpts, spender, addedValue)
}

// Initialize is a paid mutator transaction binding the contract method 0x485cc955.
//
// Solidity: function initialize(address _masterAddress, address _trustedSequencer) returns()
func (_DCChain *DCChainTransactor) Initialize(opts *bind.TransactOpts, _masterAddress common.Address, _trustedSequencer common.Address) (*types.Transaction, error) {
	return _DCChain.contract.Transact(opts, "initialize", _masterAddress, _trustedSequencer)
}

// Initialize is a paid mutator transaction binding the contract method 0x485cc955.
//
// Solidity: function initialize(address _masterAddress, address _trustedSequencer) returns()
func (_DCChain *DCChainSession) Initialize(_masterAddress common.Address, _trustedSequencer common.Address) (*types.Transaction, error) {
	return _DCChain.Contract.Initialize(&_DCChain.TransactOpts, _masterAddress, _trustedSequencer)
}

// Initialize is a paid mutator transaction binding the contract method 0x485cc955.
//
// Solidity: function initialize(address _masterAddress, address _trustedSequencer) returns()
func (_DCChain *DCChainTransactorSession) Initialize(_masterAddress common.Address, _trustedSequencer common.Address) (*types.Transaction, error) {
	return _DCChain.Contract.Initialize(&_DCChain.TransactOpts, _masterAddress, _trustedSequencer)
}

// SetCurrentRollupPoint is a paid mutator transaction binding the contract method 0x6f5acd15.
//
// Solidity: function setCurrentRollupPoint((address,address,bytes,bytes,uint256) rollupPoint) returns()
func (_DCChain *DCChainTransactor) SetCurrentRollupPoint(opts *bind.TransactOpts, rollupPoint DCRC20RollupPoint) (*types.Transaction, error) {
	return _DCChain.contract.Transact(opts, "setCurrentRollupPoint", rollupPoint)
}

// SetCurrentRollupPoint is a paid mutator transaction binding the contract method 0x6f5acd15.
//
// Solidity: function setCurrentRollupPoint((address,address,bytes,bytes,uint256) rollupPoint) returns()
func (_DCChain *DCChainSession) SetCurrentRollupPoint(rollupPoint DCRC20RollupPoint) (*types.Transaction, error) {
	return _DCChain.Contract.SetCurrentRollupPoint(&_DCChain.TransactOpts, rollupPoint)
}

// SetCurrentRollupPoint is a paid mutator transaction binding the contract method 0x6f5acd15.
//
// Solidity: function setCurrentRollupPoint((address,address,bytes,bytes,uint256) rollupPoint) returns()
func (_DCChain *DCChainTransactorSession) SetCurrentRollupPoint(rollupPoint DCRC20RollupPoint) (*types.Transaction, error) {
	return _DCChain.Contract.SetCurrentRollupPoint(&_DCChain.TransactOpts, rollupPoint)
}

// SetMaster is a paid mutator transaction binding the contract method 0x26fae0d3.
//
// Solidity: function setMaster(address newAddress) returns()
func (_DCChain *DCChainTransactor) SetMaster(opts *bind.TransactOpts, newAddress common.Address) (*types.Transaction, error) {
	return _DCChain.contract.Transact(opts, "setMaster", newAddress)
}

// SetMaster is a paid mutator transaction binding the contract method 0x26fae0d3.
//
// Solidity: function setMaster(address newAddress) returns()
func (_DCChain *DCChainSession) SetMaster(newAddress common.Address) (*types.Transaction, error) {
	return _DCChain.Contract.SetMaster(&_DCChain.TransactOpts, newAddress)
}

// SetMaster is a paid mutator transaction binding the contract method 0x26fae0d3.
//
// Solidity: function setMaster(address newAddress) returns()
func (_DCChain *DCChainTransactorSession) SetMaster(newAddress common.Address) (*types.Transaction, error) {
	return _DCChain.Contract.SetMaster(&_DCChain.TransactOpts, newAddress)
}

// SetTrustedSequencer is a paid mutator transaction binding the contract method 0x6ff512cc.
//
// Solidity: function setTrustedSequencer(address newAddress) returns()
func (_DCChain *DCChainTransactor) SetTrustedSequencer(opts *bind.TransactOpts, newAddress common.Address) (*types.Transaction, error) {
	return _DCChain.contract.Transact(opts, "setTrustedSequencer", newAddress)
}

// SetTrustedSequencer is a paid mutator transaction binding the contract method 0x6ff512cc.
//
// Solidity: function setTrustedSequencer(address newAddress) returns()
func (_DCChain *DCChainSession) SetTrustedSequencer(newAddress common.Address) (*types.Transaction, error) {
	return _DCChain.Contract.SetTrustedSequencer(&_DCChain.TransactOpts, newAddress)
}

// SetTrustedSequencer is a paid mutator transaction binding the contract method 0x6ff512cc.
//
// Solidity: function setTrustedSequencer(address newAddress) returns()
func (_DCChain *DCChainTransactorSession) SetTrustedSequencer(newAddress common.Address) (*types.Transaction, error) {
	return _DCChain.Contract.SetTrustedSequencer(&_DCChain.TransactOpts, newAddress)
}

// Transfer is a paid mutator transaction binding the contract method 0xa9059cbb.
//
// Solidity: function transfer(address to, uint256 amount) returns(bool)
func (_DCChain *DCChainTransactor) Transfer(opts *bind.TransactOpts, to common.Address, amount *big.Int) (*types.Transaction, error) {
	return _DCChain.contract.Transact(opts, "transfer", to, amount)
}

// Transfer is a paid mutator transaction binding the contract method 0xa9059cbb.
//
// Solidity: function transfer(address to, uint256 amount) returns(bool)
func (_DCChain *DCChainSession) Transfer(to common.Address, amount *big.Int) (*types.Transaction, error) {
	return _DCChain.Contract.Transfer(&_DCChain.TransactOpts, to, amount)
}

// Transfer is a paid mutator transaction binding the contract method 0xa9059cbb.
//
// Solidity: function transfer(address to, uint256 amount) returns(bool)
func (_DCChain *DCChainTransactorSession) Transfer(to common.Address, amount *big.Int) (*types.Transaction, error) {
	return _DCChain.Contract.Transfer(&_DCChain.TransactOpts, to, amount)
}

// TransferFrom is a paid mutator transaction binding the contract method 0x23b872dd.
//
// Solidity: function transferFrom(address from, address to, uint256 amount) returns(bool)
func (_DCChain *DCChainTransactor) TransferFrom(opts *bind.TransactOpts, from common.Address, to common.Address, amount *big.Int) (*types.Transaction, error) {
	return _DCChain.contract.Transact(opts, "transferFrom", from, to, amount)
}

// TransferFrom is a paid mutator transaction binding the contract method 0x23b872dd.
//
// Solidity: function transferFrom(address from, address to, uint256 amount) returns(bool)
func (_DCChain *DCChainSession) TransferFrom(from common.Address, to common.Address, amount *big.Int) (*types.Transaction, error) {
	return _DCChain.Contract.TransferFrom(&_DCChain.TransactOpts, from, to, amount)
}

// TransferFrom is a paid mutator transaction binding the contract method 0x23b872dd.
//
// Solidity: function transferFrom(address from, address to, uint256 amount) returns(bool)
func (_DCChain *DCChainTransactorSession) TransferFrom(from common.Address, to common.Address, amount *big.Int) (*types.Transaction, error) {
	return _DCChain.Contract.TransferFrom(&_DCChain.TransactOpts, from, to, amount)
}

// Withdrawal is a paid mutator transaction binding the contract method 0x0029c0b4.
//
// Solidity: function withdrawal(address tokenAddress, address toAddress, uint256 amount) returns()
func (_DCChain *DCChainTransactor) Withdrawal(opts *bind.TransactOpts, tokenAddress common.Address, toAddress common.Address, amount *big.Int) (*types.Transaction, error) {
	return _DCChain.contract.Transact(opts, "withdrawal", tokenAddress, toAddress, amount)
}

// Withdrawal is a paid mutator transaction binding the contract method 0x0029c0b4.
//
// Solidity: function withdrawal(address tokenAddress, address toAddress, uint256 amount) returns()
func (_DCChain *DCChainSession) Withdrawal(tokenAddress common.Address, toAddress common.Address, amount *big.Int) (*types.Transaction, error) {
	return _DCChain.Contract.Withdrawal(&_DCChain.TransactOpts, tokenAddress, toAddress, amount)
}

// Withdrawal is a paid mutator transaction binding the contract method 0x0029c0b4.
//
// Solidity: function withdrawal(address tokenAddress, address toAddress, uint256 amount) returns()
func (_DCChain *DCChainTransactorSession) Withdrawal(tokenAddress common.Address, toAddress common.Address, amount *big.Int) (*types.Transaction, error) {
	return _DCChain.Contract.Withdrawal(&_DCChain.TransactOpts, tokenAddress, toAddress, amount)
}

// DCChainApprovalIterator is returned from FilterApproval and is used to iterate over the raw logs and unpacked data for Approval events raised by the DCChain contract.
type DCChainApprovalIterator struct {
	Event *DCChainApproval // Event containing the contract specifics and raw log

	contract *bind.BoundContract // Generic contract to use for unpacking event data
	event    string              // Event name to use for unpacking event data

	logs chan types.Log        // Log channel receiving the found contract events
	sub  ethereum.Subscription // Subscription for errors, completion and termination
	done bool                  // Whether the subscription completed delivering logs
	fail error                 // Occurred error to stop iteration
}

// Next advances the iterator to the subsequent event, returning whether there
// are any more events found. In case of a retrieval or parsing error, false is
// returned and Error() can be queried for the exact failure.
func (it *DCChainApprovalIterator) Next() bool {
	// If the iterator failed, stop iterating
	if it.fail != nil {
		return false
	}
	// If the iterator completed, deliver directly whatever's available
	if it.done {
		select {
		case log := <-it.logs:
			it.Event = new(DCChainApproval)
			if err := it.contract.UnpackLog(it.Event, it.event, log); err != nil {
				it.fail = err
				return false
			}
			it.Event.Raw = log
			return true

		default:
			return false
		}
	}
	// Iterator still in progress, wait for either a data or an error event
	select {
	case log := <-it.logs:
		it.Event = new(DCChainApproval)
		if err := it.contract.UnpackLog(it.Event, it.event, log); err != nil {
			it.fail = err
			return false
		}
		it.Event.Raw = log
		return true

	case err := <-it.sub.Err():
		it.done = true
		it.fail = err
		return it.Next()
	}
}

// Error returns any retrieval or parsing error occurred during filtering.
func (it *DCChainApprovalIterator) Error() error {
	return it.fail
}

// Close terminates the iteration process, releasing any pending underlying
// resources.
func (it *DCChainApprovalIterator) Close() error {
	it.sub.Unsubscribe()
	return nil
}

// DCChainApproval represents a Approval event raised by the DCChain contract.
type DCChainApproval struct {
	Owner   common.Address
	Spender common.Address
	Value   *big.Int
	Raw     types.Log // Blockchain specific contextual infos
}

// FilterApproval is a free log retrieval operation binding the contract event 0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925.
//
// Solidity: event Approval(address indexed owner, address indexed spender, uint256 value)
func (_DCChain *DCChainFilterer) FilterApproval(opts *bind.FilterOpts, owner []common.Address, spender []common.Address) (*DCChainApprovalIterator, error) {

	var ownerRule []interface{}
	for _, ownerItem := range owner {
		ownerRule = append(ownerRule, ownerItem)
	}
	var spenderRule []interface{}
	for _, spenderItem := range spender {
		spenderRule = append(spenderRule, spenderItem)
	}

	logs, sub, err := _DCChain.contract.FilterLogs(opts, "Approval", ownerRule, spenderRule)
	if err != nil {
		return nil, err
	}
	return &DCChainApprovalIterator{contract: _DCChain.contract, event: "Approval", logs: logs, sub: sub}, nil
}

// WatchApproval is a free log subscription operation binding the contract event 0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925.
//
// Solidity: event Approval(address indexed owner, address indexed spender, uint256 value)
func (_DCChain *DCChainFilterer) WatchApproval(opts *bind.WatchOpts, sink chan<- *DCChainApproval, owner []common.Address, spender []common.Address) (event.Subscription, error) {

	var ownerRule []interface{}
	for _, ownerItem := range owner {
		ownerRule = append(ownerRule, ownerItem)
	}
	var spenderRule []interface{}
	for _, spenderItem := range spender {
		spenderRule = append(spenderRule, spenderItem)
	}

	logs, sub, err := _DCChain.contract.WatchLogs(opts, "Approval", ownerRule, spenderRule)
	if err != nil {
		return nil, err
	}
	return event.NewSubscription(func(quit <-chan struct{}) error {
		defer sub.Unsubscribe()
		for {
			select {
			case log := <-logs:
				// New log arrived, parse the event and forward to the user
				event := new(DCChainApproval)
				if err := _DCChain.contract.UnpackLog(event, "Approval", log); err != nil {
					return err
				}
				event.Raw = log

				select {
				case sink <- event:
				case err := <-sub.Err():
					return err
				case <-quit:
					return nil
				}
			case err := <-sub.Err():
				return err
			case <-quit:
				return nil
			}
		}
	}), nil
}

// ParseApproval is a log parse operation binding the contract event 0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925.
//
// Solidity: event Approval(address indexed owner, address indexed spender, uint256 value)
func (_DCChain *DCChainFilterer) ParseApproval(log types.Log) (*DCChainApproval, error) {
	event := new(DCChainApproval)
	if err := _DCChain.contract.UnpackLog(event, "Approval", log); err != nil {
		return nil, err
	}
	event.Raw = log
	return event, nil
}

// DCChainInitializedIterator is returned from FilterInitialized and is used to iterate over the raw logs and unpacked data for Initialized events raised by the DCChain contract.
type DCChainInitializedIterator struct {
	Event *DCChainInitialized // Event containing the contract specifics and raw log

	contract *bind.BoundContract // Generic contract to use for unpacking event data
	event    string              // Event name to use for unpacking event data

	logs chan types.Log        // Log channel receiving the found contract events
	sub  ethereum.Subscription // Subscription for errors, completion and termination
	done bool                  // Whether the subscription completed delivering logs
	fail error                 // Occurred error to stop iteration
}

// Next advances the iterator to the subsequent event, returning whether there
// are any more events found. In case of a retrieval or parsing error, false is
// returned and Error() can be queried for the exact failure.
func (it *DCChainInitializedIterator) Next() bool {
	// If the iterator failed, stop iterating
	if it.fail != nil {
		return false
	}
	// If the iterator completed, deliver directly whatever's available
	if it.done {
		select {
		case log := <-it.logs:
			it.Event = new(DCChainInitialized)
			if err := it.contract.UnpackLog(it.Event, it.event, log); err != nil {
				it.fail = err
				return false
			}
			it.Event.Raw = log
			return true

		default:
			return false
		}
	}
	// Iterator still in progress, wait for either a data or an error event
	select {
	case log := <-it.logs:
		it.Event = new(DCChainInitialized)
		if err := it.contract.UnpackLog(it.Event, it.event, log); err != nil {
			it.fail = err
			return false
		}
		it.Event.Raw = log
		return true

	case err := <-it.sub.Err():
		it.done = true
		it.fail = err
		return it.Next()
	}
}

// Error returns any retrieval or parsing error occurred during filtering.
func (it *DCChainInitializedIterator) Error() error {
	return it.fail
}

// Close terminates the iteration process, releasing any pending underlying
// resources.
func (it *DCChainInitializedIterator) Close() error {
	it.sub.Unsubscribe()
	return nil
}

// DCChainInitialized represents a Initialized event raised by the DCChain contract.
type DCChainInitialized struct {
	Version uint8
	Raw     types.Log // Blockchain specific contextual infos
}

// FilterInitialized is a free log retrieval operation binding the contract event 0x7f26b83ff96e1f2b6a682f133852f6798a09c465da95921460cefb3847402498.
//
// Solidity: event Initialized(uint8 version)
func (_DCChain *DCChainFilterer) FilterInitialized(opts *bind.FilterOpts) (*DCChainInitializedIterator, error) {

	logs, sub, err := _DCChain.contract.FilterLogs(opts, "Initialized")
	if err != nil {
		return nil, err
	}
	return &DCChainInitializedIterator{contract: _DCChain.contract, event: "Initialized", logs: logs, sub: sub}, nil
}

// WatchInitialized is a free log subscription operation binding the contract event 0x7f26b83ff96e1f2b6a682f133852f6798a09c465da95921460cefb3847402498.
//
// Solidity: event Initialized(uint8 version)
func (_DCChain *DCChainFilterer) WatchInitialized(opts *bind.WatchOpts, sink chan<- *DCChainInitialized) (event.Subscription, error) {

	logs, sub, err := _DCChain.contract.WatchLogs(opts, "Initialized")
	if err != nil {
		return nil, err
	}
	return event.NewSubscription(func(quit <-chan struct{}) error {
		defer sub.Unsubscribe()
		for {
			select {
			case log := <-logs:
				// New log arrived, parse the event and forward to the user
				event := new(DCChainInitialized)
				if err := _DCChain.contract.UnpackLog(event, "Initialized", log); err != nil {
					return err
				}
				event.Raw = log

				select {
				case sink <- event:
				case err := <-sub.Err():
					return err
				case <-quit:
					return nil
				}
			case err := <-sub.Err():
				return err
			case <-quit:
				return nil
			}
		}
	}), nil
}

// ParseInitialized is a log parse operation binding the contract event 0x7f26b83ff96e1f2b6a682f133852f6798a09c465da95921460cefb3847402498.
//
// Solidity: event Initialized(uint8 version)
func (_DCChain *DCChainFilterer) ParseInitialized(log types.Log) (*DCChainInitialized, error) {
	event := new(DCChainInitialized)
	if err := _DCChain.contract.UnpackLog(event, "Initialized", log); err != nil {
		return nil, err
	}
	event.Raw = log
	return event, nil
}

// DCChainTransferIterator is returned from FilterTransfer and is used to iterate over the raw logs and unpacked data for Transfer events raised by the DCChain contract.
type DCChainTransferIterator struct {
	Event *DCChainTransfer // Event containing the contract specifics and raw log

	contract *bind.BoundContract // Generic contract to use for unpacking event data
	event    string              // Event name to use for unpacking event data

	logs chan types.Log        // Log channel receiving the found contract events
	sub  ethereum.Subscription // Subscription for errors, completion and termination
	done bool                  // Whether the subscription completed delivering logs
	fail error                 // Occurred error to stop iteration
}

// Next advances the iterator to the subsequent event, returning whether there
// are any more events found. In case of a retrieval or parsing error, false is
// returned and Error() can be queried for the exact failure.
func (it *DCChainTransferIterator) Next() bool {
	// If the iterator failed, stop iterating
	if it.fail != nil {
		return false
	}
	// If the iterator completed, deliver directly whatever's available
	if it.done {
		select {
		case log := <-it.logs:
			it.Event = new(DCChainTransfer)
			if err := it.contract.UnpackLog(it.Event, it.event, log); err != nil {
				it.fail = err
				return false
			}
			it.Event.Raw = log
			return true

		default:
			return false
		}
	}
	// Iterator still in progress, wait for either a data or an error event
	select {
	case log := <-it.logs:
		it.Event = new(DCChainTransfer)
		if err := it.contract.UnpackLog(it.Event, it.event, log); err != nil {
			it.fail = err
			return false
		}
		it.Event.Raw = log
		return true

	case err := <-it.sub.Err():
		it.done = true
		it.fail = err
		return it.Next()
	}
}

// Error returns any retrieval or parsing error occurred during filtering.
func (it *DCChainTransferIterator) Error() error {
	return it.fail
}

// Close terminates the iteration process, releasing any pending underlying
// resources.
func (it *DCChainTransferIterator) Close() error {
	it.sub.Unsubscribe()
	return nil
}

// DCChainTransfer represents a Transfer event raised by the DCChain contract.
type DCChainTransfer struct {
	From  common.Address
	To    common.Address
	Value *big.Int
	Raw   types.Log // Blockchain specific contextual infos
}

// FilterTransfer is a free log retrieval operation binding the contract event 0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef.
//
// Solidity: event Transfer(address indexed from, address indexed to, uint256 value)
func (_DCChain *DCChainFilterer) FilterTransfer(opts *bind.FilterOpts, from []common.Address, to []common.Address) (*DCChainTransferIterator, error) {

	var fromRule []interface{}
	for _, fromItem := range from {
		fromRule = append(fromRule, fromItem)
	}
	var toRule []interface{}
	for _, toItem := range to {
		toRule = append(toRule, toItem)
	}

	logs, sub, err := _DCChain.contract.FilterLogs(opts, "Transfer", fromRule, toRule)
	if err != nil {
		return nil, err
	}
	return &DCChainTransferIterator{contract: _DCChain.contract, event: "Transfer", logs: logs, sub: sub}, nil
}

// WatchTransfer is a free log subscription operation binding the contract event 0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef.
//
// Solidity: event Transfer(address indexed from, address indexed to, uint256 value)
func (_DCChain *DCChainFilterer) WatchTransfer(opts *bind.WatchOpts, sink chan<- *DCChainTransfer, from []common.Address, to []common.Address) (event.Subscription, error) {

	var fromRule []interface{}
	for _, fromItem := range from {
		fromRule = append(fromRule, fromItem)
	}
	var toRule []interface{}
	for _, toItem := range to {
		toRule = append(toRule, toItem)
	}

	logs, sub, err := _DCChain.contract.WatchLogs(opts, "Transfer", fromRule, toRule)
	if err != nil {
		return nil, err
	}
	return event.NewSubscription(func(quit <-chan struct{}) error {
		defer sub.Unsubscribe()
		for {
			select {
			case log := <-logs:
				// New log arrived, parse the event and forward to the user
				event := new(DCChainTransfer)
				if err := _DCChain.contract.UnpackLog(event, "Transfer", log); err != nil {
					return err
				}
				event.Raw = log

				select {
				case sink <- event:
				case err := <-sub.Err():
					return err
				case <-quit:
					return nil
				}
			case err := <-sub.Err():
				return err
			case <-quit:
				return nil
			}
		}
	}), nil
}

// ParseTransfer is a log parse operation binding the contract event 0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef.
//
// Solidity: event Transfer(address indexed from, address indexed to, uint256 value)
func (_DCChain *DCChainFilterer) ParseTransfer(log types.Log) (*DCChainTransfer, error) {
	event := new(DCChainTransfer)
	if err := _DCChain.contract.UnpackLog(event, "Transfer", log); err != nil {
		return nil, err
	}
	event.Raw = log
	return event, nil
}
