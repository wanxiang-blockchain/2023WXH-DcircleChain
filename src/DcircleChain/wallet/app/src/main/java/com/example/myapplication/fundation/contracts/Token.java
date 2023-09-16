package com.example.myapplication.fundation.contracts;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.5.0.
 */
@SuppressWarnings("rawtypes")
public class Token extends Contract {
    public static final String BINARY = "60c060405260086080908152672221902a37b5b2b760c11b60a0525f90610026908261012a565b506040805180820190915260048152632221aa2160e11b602082015260019061004f908261012a565b5062989680600255348015610062575f80fd5b50600254335f81815260056020526040902091909155600380546001600160a01b03191690911790556101e5565b634e487b7160e01b5f52604160045260245ffd5b600181811c908216806100b857607f821691505b6020821081036100d657634e487b7160e01b5f52602260045260245ffd5b50919050565b601f821115610125575f81815260208120601f850160051c810160208610156101025750805b601f850160051c820191505b818110156101215782815560010161010e565b5050505b505050565b81516001600160401b0381111561014357610143610090565b6101578161015184546100a4565b846100dc565b602080601f83116001811461018a575f84156101735750858301515b5f19600386901b1c1916600185901b178555610121565b5f85815260208120601f198616915b828110156101b857888601518255948401946001909101908401610199565b50858210156101d557878501515f19600388901b60f8161c191681555b5050505050600190811b01905550565b61060d806101f25f395ff3fe60806040526004361061009a575f3560e01c806370a082311161006257806370a082311461014a57806373faa2db1461017e5780638da5cb5b146101ba57806395d89b41146101d9578063a9059cbb146101ed578063b02c43d01461020c575f80fd5b806306fdde031461009e57806318160ddd146100c857806318e0db25146100eb5780632e1a7d4d1461012257806340c10f1914610137575b5f80fd5b3480156100a9575f80fd5b506100b261021f565b6040516100bf919061049a565b60405180910390f35b3480156100d3575f80fd5b506100dd60025481565b6040519081526020016100bf565b3480156100f6575f80fd5b5060045461010a906001600160a01b031681565b6040516001600160a01b0390911681526020016100bf565b6101356101303660046104e5565b6102aa565b005b610135610145366004610517565b610320565b348015610155575f80fd5b506100dd61016436600461053f565b6001600160a01b03165f9081526005602052604090205490565b348015610189575f80fd5b5061013561019836600461053f565b600480546001600160a01b0319166001600160a01b0392909216919091179055565b3480156101c5575f80fd5b5060035461010a906001600160a01b031681565b3480156101e4575f80fd5b506100b2610391565b3480156101f8575f80fd5b50610135610207366004610517565b61039e565b61013561021a3660046104e5565b61047c565b5f805461022b9061055f565b80601f01602080910402602001604051908101604052809291908181526020018280546102579061055f565b80156102a25780601f10610279576101008083540402835291602001916102a2565b820191905f5260205f20905b81548152906001019060200180831161028557829003601f168201915b505050505081565b335f908152600560205260409020548111156102fa5760405162461bcd60e51b815260206004820152600a60248201526909cdee840cadcdeeaced60b31b60448201526064015b60405180910390fd5b335f90815260056020526040812080548392906103189084906105ab565b909155505050565b6001600160a01b0382165f90815260056020526040812080548392906103479084906105c4565b90915550506040518181526001600160a01b0383169030907fab8530f87dc9b59234c4623bf917212bb2536d647574c8e7e5da92c2ede0c9f8906020015b60405180910390a35050565b6001805461022b9061055f565b335f908152600560205260409020548111156103f05760405162461bcd60e51b81526020600482015260116024820152704e6f7420656e6f75676820746f6b656e7360781b60448201526064016102f1565b335f908152600560205260408120805483929061040e9084906105ab565b90915550506001600160a01b0382165f908152600560205260408120805483929061043a9084906105c4565b90915550506040518181526001600160a01b0383169033907fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef90602001610385565b335f90815260056020526040812080548392906103189084906105c4565b5f6020808352835180828501525f5b818110156104c5578581018301518582016040015282016104a9565b505f604082860101526040601f19601f8301168501019250505092915050565b5f602082840312156104f5575f80fd5b5035919050565b80356001600160a01b0381168114610512575f80fd5b919050565b5f8060408385031215610528575f80fd5b610531836104fc565b946020939093013593505050565b5f6020828403121561054f575f80fd5b610558826104fc565b9392505050565b600181811c9082168061057357607f821691505b60208210810361059157634e487b7160e01b5f52602260045260245ffd5b50919050565b634e487b7160e01b5f52601160045260245ffd5b818103818111156105be576105be610597565b92915050565b808201808211156105be576105be61059756fea264697066735822122087c5894d43385bccf80169f45b6bd00272b46bb82e8c0daff10dfc8409abca5264736f6c63430008150033";

    public static final String FUNC_ANCHORING = "anchoring";

    public static final String FUNC_ANCHORING_POINT = "anchoring_point";

    public static final String FUNC_BALANCEOF = "balanceOf";

    public static final String FUNC_DEPOSITS = "deposits";

    public static final String FUNC_MINT = "mint";

    public static final String FUNC_NAME = "name";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_SYMBOL = "symbol";

    public static final String FUNC_TOTALSUPPLY = "totalSupply";

    public static final String FUNC_TRANSFER = "transfer";

    public static final String FUNC_WITHDRAW = "withdraw";

    public static final Event MINT_EVENT = new Event("Mint", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event TRANSFER_EVENT = new Event("Transfer", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected Token(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Token(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Token(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Token(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

//    public static List<MintEventResponse> getMintEvents(TransactionReceipt transactionReceipt) {
//        List<EventValuesWithLog> valueList = (List<EventValuesWithLog>) staticExtractEventParametersWithLog(MINT_EVENT, transactionReceipt);
//        ArrayList<MintEventResponse> responses = new ArrayList<MintEventResponse>(valueList.size());
//        for (EventValuesWithLog eventValues : valueList) {
//            MintEventResponse typedResponse = new MintEventResponse();
//            typedResponse.log = eventValues.getLog();
//            typedResponse._from = (String) eventValues.getIndexedValues().get(0).getValue();
//            typedResponse._to = (String) eventValues.getIndexedValues().get(1).getValue();
//            typedResponse._value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
//            responses.add(typedResponse);
//        }
//        return responses;
//    }

    public static MintEventResponse getMintEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(MINT_EVENT, log);
        MintEventResponse typedResponse = new MintEventResponse();
        typedResponse.log = log;
        typedResponse._from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse._to = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse._value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<MintEventResponse> mintEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getMintEventFromLog(log));
    }

    public Flowable<MintEventResponse> mintEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(MINT_EVENT));
        return mintEventFlowable(filter);
    }

//    public static List<TransferEventResponse> getTransferEvents(TransactionReceipt transactionReceipt) {
//        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TRANSFER_EVENT, transactionReceipt);
//        ArrayList<TransferEventResponse> responses = new ArrayList<TransferEventResponse>(valueList.size());
//        for (EventValuesWithLog eventValues : valueList) {
//            TransferEventResponse typedResponse = new TransferEventResponse();
//            typedResponse.log = eventValues.getLog();
//            typedResponse._from = (String) eventValues.getIndexedValues().get(0).getValue();
//            typedResponse._to = (String) eventValues.getIndexedValues().get(1).getValue();
//            typedResponse._value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
//            responses.add(typedResponse);
//        }
//        return responses;
//    }

    public static TransferEventResponse getTransferEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TRANSFER_EVENT, log);
        TransferEventResponse typedResponse = new TransferEventResponse();
        typedResponse.log = log;
        typedResponse._from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse._to = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse._value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<TransferEventResponse> transferEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getTransferEventFromLog(log));
    }

    public Flowable<TransferEventResponse> transferEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TRANSFER_EVENT));
        return transferEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> anchoring(String anchoringHash) {
        final Function function = new Function(
                FUNC_ANCHORING, 
                Arrays.<Type>asList(new Address(160, anchoringHash)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> anchoring_point() {
        final Function function = new Function(FUNC_ANCHORING_POINT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> balanceOf(String account) {
        final Function function = new Function(FUNC_BALANCEOF, 
                Arrays.<Type>asList(new Address(160, account)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> deposits(BigInteger total, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_DEPOSITS, 
                Arrays.<Type>asList(new Uint256(total)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> mint(String to, BigInteger amount, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_MINT, 
                Arrays.<Type>asList(new Address(160, to),
                new Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<String> name() {
        final Function function = new Function(FUNC_NAME, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> symbol() {
        final Function function = new Function(FUNC_SYMBOL, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> totalSupply() {
        final Function function = new Function(FUNC_TOTALSUPPLY, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transfer(String to, BigInteger amount) {
        final Function function = new Function(
                FUNC_TRANSFER, 
                Arrays.<Type>asList(new Address(160, to),
                new Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> withdraw(BigInteger amount, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_WITHDRAW, 
                Arrays.<Type>asList(new Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    @Deprecated
    public static Token load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Token(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Token load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Token(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Token load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Token(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Token load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Token(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Token> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Token.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    public static RemoteCall<Token> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Token.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Token> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Token.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Token> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Token.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static class MintEventResponse extends BaseEventResponse {
        public String _from;

        public String _to;

        public BigInteger _value;
    }

    public static class TransferEventResponse extends BaseEventResponse {
        public String _from;

        public String _to;

        public BigInteger _value;
    }
}
