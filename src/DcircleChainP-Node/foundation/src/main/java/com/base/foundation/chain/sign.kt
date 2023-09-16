package com.base.foundation.chain

import com.base.foundation.DCircleChainID
import com.base.foundation.SignUtils
import com.base.foundation.getUs
import com.base.foundation.getWalletKey
import com.base.foundation.utils.Tuple
import com.base.foundation.utils.fromHex
import com.base.thridpart.toHexString
import com.google.gson.annotations.Expose
import org.web3j.crypto.Keys
import org.web3j.rlp.RlpDecoder
import org.web3j.rlp.RlpEncoder
import org.web3j.rlp.RlpList
import org.web3j.rlp.RlpString
import wallet.core.jni.Hash
import wallet.core.jni.PublicKey
import java.math.BigInteger
import java.util.Date

// 先根据address获取nonce去签名->然后发服务器验证
// 处理的异常情况
//  1) 验证过程中net断了  签名不要重新生成，发送签名数据的hash(signREsult.getSignHash())去服务器问
//  2) nonce 验证失败需要重新去走签名流程
// 重要的说明
// 签名RLP编码  请看 RLP 编码


const val DCircle_ADDRESS = "0x3cF26E1590b443A7D015D9A9E0A68EFadeB68782"

enum class ChainId(var id: Byte) {
    DEV(3),
    ALPHA(2),
    BETA(1),
    RELEASE(1),
}

enum class OpCode(var code: BigInteger) {
    PCLogin(BigInteger.valueOf(0x01)),
    JoinGroup(BigInteger.valueOf(0x02)),
    CreateGroup(BigInteger.valueOf(0x03)),
    DismissGroup(BigInteger.valueOf(0x04)),
    DeleteGroupMember(BigInteger.valueOf(0x05)),
    ExitFromGroup(BigInteger.valueOf(0x06)),
    SetDIDArticleConfirmed(BigInteger.valueOf(0x07)),
    SetDIDArticleToken(BigInteger.valueOf(0x08)),
    BuyDIDArticle(BigInteger.valueOf(0x09)),
    ShareDIDArticleToChat(BigInteger.valueOf(0x0A)),
    SetDIDArticleAbstract(BigInteger.valueOf(0x0B)),
    DeleteDIDArticleInGroup(BigInteger.valueOf(0x0C)),
    AddGroupAdminer(BigInteger.valueOf(0x0D)),
    DeleteGroupAdminer(BigInteger.valueOf(0x0E)),
    AuthorizedAccessDCircle(BigInteger.valueOf(0x0F)),
    RevokeAccessDCircle(BigInteger.valueOf(0x10)),
    ReferenceArticle(BigInteger.valueOf(0x11)),
    DidInviteDownLoad(BigInteger.valueOf(0x12)),
    //群信息上链-头像
    GroupInfoAvatarOnChain(BigInteger.valueOf(0x13)),
    //群外链邀请
    GroupInviteDownLoad(BigInteger.valueOf(0x14)),
    //添加精华消息
    AddMessageElite(BigInteger.valueOf(0x15)),
    //移除精华消息
    DeleteMessageElite(BigInteger.valueOf(0x16)),
    //群信息上链-名字
    GroupInfoNameOnChain(BigInteger.valueOf(0x17)),
    //审核入群申请
    HandApplyForJoinChat(BigInteger.valueOf(0x18)),
    //生成群邀请码
    GroupInviteCode(BigInteger.valueOf(0x19));

    companion object {
        // 辅助方法，通过整数值获取相应的枚举常量
        fun fromInt(value: Int): OpCode? {
            return values().find { it.code == BigInteger.valueOf(value.toLong()) }
        }
    }
}

fun OpCode.Companion.valueOf(value: Int): OpCode? {
    for (r in OpCode.values()) {
        if (r.code.toInt() == value) {
            return r
        }
    }
    return null
}

class RLPEntity {
    private constructor()
    constructor(
        nonce: BigInteger,
        fromEthAddress: String,
        toEthAddress: String,
        opCode: BigInteger,
        signTime: Long,
        payLoad: ByteArray,
        chainID: Byte
    ) {
        this.nonce    =  nonce
        this.opCode   = opCode
        this.signTime = signTime
        this.payLoad  = payLoad
        this.chainID  = chainID
        this.fromEthAddress = fromEthAddress
        this.toEthAddress   = toEthAddress
    }

    // RLP(Nonce, From, To, OpCode, SignTime, Payload, ChainId)
    fun RLPEncode(): ByteArray {
        return RlpEncoder.encode(
            RlpList(
                RlpString.create(nonce),
                RlpString.create(fromEthAddress),
                RlpString.create(toEthAddress),
                RlpString.create(opCode),
                RlpString.create(signTime),
                RlpString.create(payLoad),
                RlpString.create(chainID)
            )
        )
    }

    companion object {
        // RLP(Nonce, From, To, OpCode, SignTime, Payload, ChainId)
        fun RLPDecode(rlpEncodeMessage: ByteArray):RLPEntity {
            val decodedValues = RlpDecoder.decode(rlpEncodeMessage).values
            val nonce: BigInteger = (decodedValues[0] as RlpString).asPositiveBigInteger()
            val fromEthAddress:String = (decodedValues[1] as RlpString).asString()
            val toEthAddress:String = (decodedValues[2] as RlpString).asString()
            val opCode: BigInteger = (decodedValues[3] as RlpString).asPositiveBigInteger()
            val signTime:Long = (decodedValues[4] as RlpString).asPositiveBigInteger().toLong()
            val payLoad:ByteArray = (decodedValues[5] as RlpString).bytes
            val chainID:Byte = (decodedValues[6] as RlpString).bytes[0]
            return RLPEntity(nonce,fromEthAddress,toEthAddress,opCode,signTime,payLoad,chainID)
        }
    }

    fun getFromEthAddress():String {
        return fromEthAddress
    }

    fun getToEthAddress():String {
        return toEthAddress
    }

    fun getOpCode(): BigInteger {
        return opCode
    }

    fun getChainID():Byte {
        return chainID
    }

    fun getPayLoad():ByteArray {
        return payLoad
    }

    private lateinit var nonce: BigInteger
    private lateinit var fromEthAddress: String
    private lateinit var toEthAddress: String
    private lateinit var opCode: BigInteger
    private  var signTime: Long = 0
    private lateinit var payLoad: ByteArray
    private var chainID: Byte = 0

}


class SignResult {
    constructor()
    constructor(signature: String, nonce: BigInteger, TxnHash:String, message:ByteArray) {
        this.signature = signature
        this.nonce = nonce
        this.TxnHash = TxnHash
        this.message = message.toHexString()
    }

    constructor(signature: String, nonce: BigInteger, TxnHash:String, message:ByteArray, from:String, to:String) {
        this.signature = signature
        this.nonce = nonce
        this.TxnHash = TxnHash
        this.message = message.toHexString()
        this.from = from
        this.to = to
    }
    constructor(signature: String, TxnHash:String, message:ByteArray) {
        this.signature = signature
        this.TxnHash = TxnHash
        this.message = message.toHexString()
    }

    // ------------------ 以下字段和服务器保持一致-----------
    private  var signature:String = ""
    private  var nonce:BigInteger = BigInteger.ONE
    private  var from =""
    private  var message :String = ""
    // ------------------ 以上字段和服务器保持一致-----------

    @Expose(serialize = false)
    private  var TxnHash:String = ""

    @Expose(serialize = false)
    private  var to=""

    @Expose(serialize = false)
    var payload: ByteArray = byteArrayOf()

    @Expose(serialize = false)
    var actionId:String = ""

    fun getSignature():String {
        return signature
    }

    fun getNonce():BigInteger {
        return nonce
    }

    fun getTxnHash():String {
        return TxnHash
    }

    fun getMessage():ByteArray {
        return fromHex(message)
    }

    fun getRPLMessage():String {
        return message
    }
}


fun verifyLoginSignature(toEthAddress:String,chainID: Byte, rlpEncodeMessage: ByteArray, signature: ByteArray): Boolean {
    return verify(toEthAddress,OpCode.PCLogin,chainID,rlpEncodeMessage,signature)
}

fun  sign(hexPrivateKey:String, rlpEntity: RLPEntity): String {
    return SignUtils.sign(hexPrivateKey,rlpEntity.RLPEncode())
}

fun verify(toEthAddress:String,opCode: OpCode,chainID: Byte, rlpEncodeMessage: ByteArray, signature: ByteArray): Boolean {
    val rlpHashData = Hash.keccak256(rlpEncodeMessage)
    val pubKey: PublicKey = PublicKey.recover(signature,rlpHashData) ?: return false
    val address: String = Keys.getAddress(pubKey.toString()) ?: return false
    val rlp = RLPEntity.RLPDecode(rlpEncodeMessage)
    if (toEthAddress != rlp.getToEthAddress()) {
        return false
    }
    if (address != rlp.getFromEthAddress()) {
        return false
    }
    if (chainID != rlp.getChainID()) {
        return false
    }
    if (opCode.code != rlp.getOpCode()) {
        return false
    }
    return true
}

// 先根据address获取nonce去签名->然后发服务器验证
// 处理的异常情况
//  1) 验证过程中net断了  签名不要重新生成，发送签名数据的hash(signREsult.getSignHash())去服务器问
//  2) nonce 验证失败需要重新去走签名流程

suspend fun GetLoginSign(priKey: String, mineAddress: String, payload:ByteArray):Tuple<SignResult,Error?> {
    val nonce = GetNonceSus(mineAddress) ?: return Tuple(SignResult(), Error("get nonce fail"))

    val signTime = Date().time
    val rlp = RLPEntity(nonce, mineAddress, DCircle_ADDRESS, OpCode.PCLogin.code, signTime, payload, DCircleChainID.id)
    val message = rlp.RLPEncode()
    val rlpHash = Hash.keccak256(message).toHexString()
    val signature = sign(priKey,rlp)
    val requestNonce = nonce.add(BigInteger.valueOf(1))
    val  result = SignResult(signature,requestNonce,rlpHash,message)
    return Tuple(result,null)
}


/**
 * @param toEthAddress 公开群传chatId，私有群传入0x00,私聊0x00
 * @param didArticleAddress 文章地址
 */
fun NewPayloadForShareDIDArticleToChat(toEthAddress: String, didArticleAddress:String):ByteArray {
    return "ToAddress: $toEthAddress\nContentAddress: $didArticleAddress".toByteArray(Charsets.UTF_8)
}
class GetSignResultItem(var toEthAddress: String, var opcode: OpCode, var payload: ByteArray, val actionId:String="")

fun GetSignResult(items:Array<GetSignResultItem>, nonce: BigInteger, fromEthAddress: String = getUs().getUid()):Array<SignResult?> {
    val signResults:MutableList<SignResult> = mutableListOf()
    var _nonce = nonce
    for (item in items) {
        val signTime = Date().time
        val rlp = RLPEntity(
            _nonce,
            fromEthAddress,
            item.toEthAddress,
            item.opcode.code,
            signTime,
            item.payload,
            DCircleChainID.id
        )
        val message = rlp.RLPEncode() //message
        val rlpHash = Hash.keccak256(message).toHexString() //signHash
        var signature = ""
        getWalletKey().privateKey.let { priKey ->
            if (item.opcode==OpCode.AuthorizedAccessDCircle) {
                return@let
            }

            assert(priKey.isNotEmpty()) {
                "wallet's privateKey is empty, opcode=${item.opcode}, please check you logic."
            }

            if (priKey.isEmpty()) {
                return@let
            }
            signature = sign(priKey, rlp)
        }
        _nonce = _nonce.add(BigInteger.valueOf(1))
        val signResult = SignResult(signature, _nonce, rlpHash, message, fromEthAddress, item.toEthAddress)
        signResult.payload = item.payload
        signResult.actionId = item.actionId
        signResults.add(signResult)
    }

    return signResults.toTypedArray()
}