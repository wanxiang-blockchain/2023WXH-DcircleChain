package com.example.myapplication.fundation.wallet

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.room.*
import com.example.myapplication.fundation.contracts.Token
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.*
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthGetTransactionCount
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.Transfer
import org.web3j.tx.gas.ContractGasProvider
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.tx.gas.StaticGasProvider
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*


@Entity
data class User(
    @PrimaryKey val address:String,
    val privateKey: String,
    val mnemonic: String,
    val walletFilePath:String
)

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Insert
    fun insertAll(vararg users: User)

    @Query( "SELECT * FROM user WHERE address = :address LIMIT 1")
    fun findByAddress(address: String?): User?

    @Query( "SELECT * FROM user LIMIT 1")
    fun me(): User?
}

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao?
}

class PolygonWallet {
    private var credentials: Credentials? = null
    private var address = ""
    private var mnemonic = ""
    private var privateKey = ""
    private var walletFilePath = ""
    private val web3: Web3j = Web3j.build(HttpService("https://polygon-mumbai.g.alchemy.com/v2/UJULbhnCQP1IbZsdpvbevJG0jR1eBMGF"))

    constructor(
        credentials: Credentials?,
        walletAddress: String,
        mnemonic: String,
        privateKey: String,
        walletFilePath: String
    ) {
        this.credentials = credentials
        this.address = walletAddress
        this.mnemonic = mnemonic
        this.privateKey = privateKey
        this.walletFilePath = walletFilePath
    }

    companion object {
        val DC_CHAINID:Long = 80001
        val DCTOKEN_ADDRESS:String     = "0xEe59199d938081183b95b618D2d5869573bd5173";
        val DCChainToken_Address :String = "0x04573BA536145325fA24e0173B2F4CC4583833C2";
        // 从助记词创建钱包
        fun createWalletFromMnemonic(context: Context, mnemonic: String, password: String) :PolygonWallet{
            var wallet: Bip39Wallet =
                WalletUtils.generateBip39WalletFromMnemonic(password, mnemonic, context.filesDir)
            var credentials = WalletUtils.loadBip39Credentials(password, mnemonic)
            var walletAddress = credentials?.address.toString()
            var mnemonic = wallet.mnemonic
            var privateKey = credentials?.ecKeyPair?.privateKey.toString()
            var walletFilePath = context.filesDir.path + wallet.filename
            return PolygonWallet(credentials,walletAddress,mnemonic,privateKey, walletFilePath)
        }

        // 从钱包文件创建钱包
        fun createWallet(context: Context, password: String):PolygonWallet {
            var wallet: Bip39Wallet = WalletUtils.generateBip39Wallet(password, context.filesDir)
            var credentials = WalletUtils.loadBip39Credentials(password, wallet.mnemonic)
            var walletAddress = credentials.address.toString()
            var mnemonic = wallet.mnemonic
            var privateKey = credentials.ecKeyPair?.privateKey.toString()
            var walletFilePath = context.filesDir.path + wallet.filename
            return PolygonWallet(credentials,walletAddress,mnemonic,privateKey, walletFilePath)
        }

        // 从助记词读取钱包
        fun loadWalletWithMnemonic(context: Context,user:User):PolygonWallet {
            var credentials = WalletUtils.loadBip39Credentials("", user.mnemonic)
            var walletAddress = credentials.address.toString()
            var privateKey = credentials.ecKeyPair?.privateKey.toString()
            return PolygonWallet(credentials,walletAddress,user.mnemonic,privateKey, user.walletFilePath)
        }
    }

    fun getAddress():String {
        return address
    }

    fun getMnemonic():String {
        return mnemonic
    }

    fun getPrivateKey():String {
        return privateKey
    }

    fun getWalletFilePath():String {
        return walletFilePath
    }

    // 获取钱包余额
    fun getBalance(): Double? {
        var b = web3.ethGetBalance(getAddress(),DefaultBlockParameterName.LATEST).send()
        return b.balance.toFloat() /1000000000000000000.0
    }

    fun getTokenBalance(tokenAddress:String): BigInteger? {
        var token = Token.load(tokenAddress,web3,credentials,DefaultGasProvider())
        return token.balanceOf(getAddress()).send()
    }

    // 发送以太币
    fun sendEther(toAddress: String, amount: BigDecimal): String {
        val transactionReceipt = Transfer.sendFunds(
            web3, credentials, toAddress, amount, Convert.Unit.ETHER
        ).send()
        return transactionReceipt.transactionHash
    }

    fun deposit(amount: BigInteger) {
        var token = Token.load(DCTOKEN_ADDRESS,web3,credentials,DefaultGasProvider())
        Log.d("deposit: ", token.deposits(amount,token.requestCurrentGasPrice()).send().toString())
    }

    // 提现：用户将代币发送到应用的钱包，应用将法币发送到用户的银行账户
    fun withdraw(fromAddress: String, tokenAddress: String ,amount: BigDecimal) {
        val function = org.web3j.abi.datatypes.Function(
            "withdraw",
            listOf(
                Uint256(amount.toLong())
            ),
            listOf(object : TypeReference<Uint256>() {})
        )
        // 创建交易
        val transaction = Transaction.createEthCallTransaction(getAddress(), DCTOKEN_ADDRESS, FunctionEncoder.encode(function))
        // 执行交易
        val ethCall = web3.ethCall(transaction, DefaultBlockParameterName.LATEST).send()
        // 解码结果
        val response = FunctionReturnDecoder.decode(ethCall.value, function.outputParameters)
        Log.d("deposit: ",response.toString())
    }

   suspend fun transfer(toAddress:String, amount: BigInteger):String {
        // 创建一个交易预览
        val transactionPre = Transaction.createEtherTransaction(
            getAddress(), null, null, null, toAddress, amount
        )
        // 估算 Gas 用量
        val ethEstimateGas = web3.ethEstimateGas(transactionPre).send()
        if (ethEstimateGas.error != null) {
            Log.e("DC Wallet", ethEstimateGas.error.message)
            throw Exception("Gas 用量不足 :error ${ethEstimateGas.error.message}")
        }

        val gasLimit = ethEstimateGas.amountUsed
        Log.d("DC Wallet gasLimit",gasLimit.toString())

        // 计算gas fee
        val ethGasPrice = web3.ethGasPrice().send()
        val gasPrice = ethGasPrice.gasPrice
        Log.d("DC Wallet gasPrice",ethGasPrice.gasPrice.toString())

        val ethGetTransactionCount =
            web3.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send()
        val nonce = ethGetTransactionCount.transactionCount
        Log.d("DC Wallet nonce",nonce.toString())

        val rawTransaction = RawTransaction.createEtherTransaction(
            nonce,
            gasPrice,
            gasLimit,
            toAddress,
            amount
        )

        // 使用你的私钥和链 ID 对交易进行签名
        val signedRawTransaction: ByteArray? =
            TransactionEncoder.signMessage(rawTransaction, DC_CHAINID, credentials)

        // 发送交易
        val hexValue = Numeric.toHexString(signedRawTransaction)
        val response = web3.ethSendRawTransaction(hexValue).sendAsync().get()

        if (response.error != null) {
            Log.e("DC Wallet",response.error.message)
            throw Exception("交易失败：${response.error.message}")
        } else {
            // 获取交易 hash
            val transactionHash = response.transactionHash
            Log.d("DC Wallet",transactionHash)
            return transactionHash
        }
       return ""
    }

     fun approve(tokenAddress:String, amount: BigInteger):String {
        val inputArgs  = listOf(Address(tokenAddress), Uint256(amount))
        // 合约返回值容器
        val outputArgs: List<TypeReference<*>> = ArrayList()
        val transferData = FunctionEncoder.encode(org.web3j.abi.datatypes.Function("approve", inputArgs, outputArgs))
        // 创建一个交易预览
        val transactionPre = Transaction.createFunctionCallTransaction(
            getAddress(), null, null, null, DCChainToken_Address, transferData
        )

        // 估算 Gas 用量
        val ethEstimateGas = web3.ethEstimateGas(transactionPre).send()
        if (ethEstimateGas.error != null) {
            Log.e(
                "DC Wallet ethEstimateGas",
                ethEstimateGas.error.message + " CODE:" + ethEstimateGas.error.code
            )
            throw Exception("获取Gas用量失败："+ethEstimateGas.error.message)
        }

        val gasLimit = ethEstimateGas.amountUsed
        Log.d("DC Wallet gasLimit", gasLimit.toString())

        // 计算gas fee
        val ethGasPrice = web3.ethGasPrice().send()
        if (ethGasPrice.error != null) {
            Log.e("DC Wallet ethGasPrice", ethGasPrice.error.message)
            throw Exception("获取Gas费失败："+ethGasPrice.error.message)
        }
        val gasPrice = ethGasPrice.gasPrice
        Log.d("DC Wallet gasPrice", ethGasPrice.gasPrice.toString())

        val ethGetTransactionCount =
            web3.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send()
        val nonce = ethGetTransactionCount.transactionCount
        Log.d("DC Wallet nonce", nonce.toString())

        var rawManager = RawTransactionManager(web3, credentials)
        var ethSendTransaction = rawManager.sendEIP1559Transaction(
            DC_CHAINID,
            gasPrice,
            gasPrice,
            gasLimit,
            DCChainToken_Address,
            transferData,
            BigInteger.ZERO
        );
        if (ethSendTransaction.error != null) {
            Log.e("DC Wallet ethSendTransaction", ethSendTransaction.error.message)
            throw Exception("交易失败："+ethSendTransaction.error.message)
        } else {
            // 获取交易 hash
            var transactionHash = ethSendTransaction.getTransactionHash();
            Log.d("DC Wallet transactionHash", transactionHash)
            return transactionHash
        }
    }

    suspend fun transferContract(tokenAddress:String,toAddress:String, amount: BigInteger):String {
        val inputArgs  = listOf(Address(toAddress), Uint256(amount))
        // 合约返回值容器
        val outputArgs: List<TypeReference<*>> = ArrayList()
        val transferData = FunctionEncoder.encode(org.web3j.abi.datatypes.Function("transfer", inputArgs, outputArgs))
        // 创建一个交易预览
        val transactionPre = Transaction.createFunctionCallTransaction(
            getAddress(), null, null, null, tokenAddress, transferData
        )

        // 估算 Gas 用量
        val ethEstimateGas = web3.ethEstimateGas(transactionPre).send()
        if (ethEstimateGas.error != null) {
//            alert(context,"gasfee","账户(Matic) gasfee 不足 请联系何文阳")
            Log.e(
                "DC Wallet ethEstimateGas",
                ethEstimateGas.error.message + " CODE:" + ethEstimateGas.error.code
            )
            throw Exception("获取Gas用量失败："+ethEstimateGas.error.message)
        }

        val gasLimit = ethEstimateGas.amountUsed
        Log.d("DC Wallet gasLimit", gasLimit.toString())

        // 计算gas fee
        val ethGasPrice = web3.ethGasPrice().send()
        if (ethGasPrice.error != null) {
//                alert(context, "gasfee", "账户(Matic) gasfee 不足 请联系何文阳")
            Log.e("DC Wallet ethGasPrice", ethGasPrice.error.message)
            throw Exception("获取Gas费失败："+ethGasPrice.error.message)
        }
        val gasPrice = ethGasPrice.gasPrice
        Log.d("DC Wallet gasPrice", ethGasPrice.gasPrice.toString())

        val ethGetTransactionCount =
            web3.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send()
        val nonce = ethGetTransactionCount.transactionCount
        Log.d("DC Wallet nonce", nonce.toString())

//            val rawTransaction = RawTransaction.createFunctionCallTransaction(
//                getAddress(), null, null, null, DCTOKEN_ADDRESS, transferData
//            )
//            val signedMessage = TransactionEncoder.signMessage(rawTransaction, DC_CHAINID, credentials)


        var rawManager = RawTransactionManager(web3, credentials)
        var ethSendTransaction = rawManager.sendEIP1559Transaction(
            DC_CHAINID,
            gasPrice,
            gasPrice,
            gasLimit,
            tokenAddress,
            transferData,
            BigInteger.ZERO
        );
        if (ethSendTransaction.error != null) {
//                alert(context, "交易", "交易失败:" + ethSendTransaction.error.message)
            Log.e("DC Wallet ethSendTransaction", ethSendTransaction.error.message)
            throw Exception("交易失败："+ethSendTransaction.error.message)
        } else {
            // 获取交易 hash
            var transactionHash = ethSendTransaction.getTransactionHash();
//                alert(context, "交易成功", "TX:" + transactionHash)
            Log.d("DC Wallet transactionHash", transactionHash)
            return transactionHash
        }
    }


    suspend fun depositsWithToken(contractAddress:String,tokenAddress:String,toAddress:String, amount: BigInteger):String {

        approve(DCTOKEN_ADDRESS,amount)

        val inputArgs  = listOf(Address(tokenAddress),Address(toAddress), Uint256(amount))
        // 合约返回值容器
        val outputArgs: List<TypeReference<*>> = ArrayList()
        val transferData = FunctionEncoder.encode(
            org.web3j.abi.datatypes.Function("depositsWithToken", inputArgs, outputArgs)
        )
        // 创建一个交易预览
        val transactionPre = Transaction.createFunctionCallTransaction(
            getAddress(), null, null, null, contractAddress, transferData
        )

        // 估算 Gas 用量
        val ethEstimateGas = web3.ethEstimateGas(transactionPre).send()
        if (ethEstimateGas.error != null) {
//            alert(context,"gasfee","账户(Matic) gasfee 不足 请联系何文阳")
            Log.e(
                "DC Wallet ethEstimateGas",
                ethEstimateGas.error.message + " CODE:" + ethEstimateGas.error.code
            )
            throw Exception("获取Gas用量失败："+ethEstimateGas.error.message)
        }

        val gasLimit = ethEstimateGas.amountUsed
        Log.d("DC Wallet gasLimit", gasLimit.toString())

        // 计算gas fee
        val ethGasPrice = web3.ethGasPrice().send()
        if (ethGasPrice.error != null) {
//                alert(context, "gasfee", "账户(Matic) gasfee 不足 请联系何文阳")
            Log.e("DC Wallet ethGasPrice", ethGasPrice.error.message)
            throw Exception("获取Gas费失败："+ethGasPrice.error.message)
        }
        val gasPrice = ethGasPrice.gasPrice
        Log.d("DC Wallet gasPrice", ethGasPrice.gasPrice.toString())

        val ethGetTransactionCount =
            web3.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send()
        val nonce = ethGetTransactionCount.transactionCount
        Log.d("DC Wallet nonce", nonce.toString())

//            val rawTransaction = RawTransaction.createFunctionCallTransaction(
//                getAddress(), null, null, null, DCTOKEN_ADDRESS, transferData
//            )
//            val signedMessage = TransactionEncoder.signMessage(rawTransaction, DC_CHAINID, credentials)


        var rawManager = RawTransactionManager(web3, credentials)
        var ethSendTransaction = rawManager.sendEIP1559Transaction(
            DC_CHAINID,
            gasPrice,
            gasPrice,
            gasLimit,
            contractAddress,
            transferData,
            BigInteger.ZERO
        );
        if (ethSendTransaction.error != null) {
//                alert(context, "交易", "交易失败:" + ethSendTransaction.error.message)
            Log.e("DC Wallet ethSendTransaction", ethSendTransaction.error.message)
            throw Exception("交易失败："+ethSendTransaction.error.message)
        } else {
            // 获取交易 hash
            var transactionHash = ethSendTransaction.getTransactionHash();
//                alert(context, "交易成功", "TX:" + transactionHash)
            Log.d("DC Wallet transactionHash", transactionHash)
            return transactionHash
        }
    }

    private fun getNonce(from: String): BigInteger? {
        val transactionCount: EthGetTransactionCount =
            web3.ethGetTransactionCount(from, DefaultBlockParameterName.LATEST).sendAsync().get()
        val nonce = transactionCount.transactionCount
        Log.i(TAG, "transfer nonce : $nonce")
        return nonce.add(BigInteger.valueOf(1))
    }

    private fun getBaseFee():BigInteger {
        var fee = BigInteger.ZERO
        var number = web3.ethBlockNumber().send()
        var block = web3.ethGetBlockByNumber(
            DefaultBlockParameter.valueOf(number.blockNumber),
            true
        ).send().block
        if (block != null) {
            var baseFee = block.baseFeePerGas
            fee = BigInteger(baseFee.substring(2,baseFee.length),16)
        }
        return fee
    }

    private fun getMaxPriorityFeePerGas():BigInteger {
        var fee = BigInteger.ZERO
        var number = web3.ethBlockNumber().send()
        var ethtrans = web3.ethGetTransactionByBlockNumberAndIndex(
            DefaultBlockParameter.valueOf(number.blockNumber),BigInteger.ONE).send()
        var trans = ethtrans.transaction.get()
        var maxprior = trans.maxPriorityFeePerGas
        if (maxprior != null) {
            fee = BigInteger(maxprior.substring(2,maxprior.length),16)
        }
        return fee
    }

    private fun getMaxFeePerGas():BigInteger {
        var baseFee = getBaseFee()
        var maxPriorFee = getMaxPriorityFeePerGas().multiply(BigInteger.valueOf(1_000_000_000L))
        var maxFeePerGas = baseFee.multiply(BigInteger.valueOf(2)).add(maxPriorFee)
        return maxFeePerGas
    }
}