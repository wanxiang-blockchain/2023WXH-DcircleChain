package com.base.foundation;


import static com.base.foundation.chain.SignKt.sign;
import static com.base.foundation.chain.SignKt.verifyLoginSignature;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import static org.junit.Assert.*;

import com.base.foundation.chain.ChainId;
import com.base.foundation.chain.OpCode;
import com.base.foundation.chain.RLPEntity;
import com.base.foundation.utils.HexUtils;

import java.math.BigInteger;
import java.util.Date;

import wallet.core.jni.Hash;
import wallet.core.jni.PublicKey;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
  @Test
  public void addition_isCorrect() {
    assertEquals(4, 2 + 2);
  }

  @Test
  public void test_sign() {
      String key = "CFA4411FEFD48594A22C1735E3FA90C977136AA6955336C46F65DD5DD59F8160";

      String to   = "0xa4a1bba4A78012BA2082E1e349054860729Bcca0";
      String form = "0x3cF26E1590b443A7D015D9A9E0A68EFadeB68782";
      byte[] payload= new byte[]{0x1,0x2};
      byte DevEnv = 0x3;
      BigInteger LOGIN_OPCODE = BigInteger.valueOf(1);
      BigInteger nonce = BigInteger.valueOf(1);
      long signTime = new Date().getTime();

      RLPEntity rlp = new RLPEntity(nonce, form, to, LOGIN_OPCODE,111, payload, DevEnv);
      String sign = sign(key, rlp);
      System.out.println(sign);
  }

    @Test
    public void test_rlpencode() {
        String to   = "0x3cF26E1590b443A7D015D9A9E0A68EFadeB68782";
        String form = "0x73d6bA8a00CfDc12EA9Eef199d0b4e304314cDB2";

        //Nonce: 0, From: "0x73d6bA8a00CfDc12EA9Eef199d0b4e304314cDB2",
        // To: "0x3cF26E1590b443A7D015D9A9E0A68EFadeB68782",
        // OpCode: 1,
        // SignTime:
        // 1680078715, Payload: [], ChainId: 3

        RLPEntity rlp = new RLPEntity(BigInteger.valueOf(0), form, to, OpCode.PCLogin.getCode(), 1680078715, "".getBytes(), (byte) 3);
        System.out.println(HexUtils.toHex(rlp.RLPEncode()));
        RLPEntity rlpDecode = RLPEntity.Companion.RLPDecode(rlp.RLPEncode());
        System.out.println(rlpDecode.getFromEthAddress());
    }

    @Test
    public void test_verify_sign() {
        String to   = "0xa4a1bba4A78012BA2082E1e349054860729Bcca0";
        String form = "0x98d590195f2d3E221bA9941a23c5D2A44d1592ce";
        byte[] payload= "".getBytes();
        byte DevEnv = ChainId.DEV.getId();
        BigInteger LOGIN_OPCODE = OpCode.PCLogin.getCode();

        BigInteger nonce = BigInteger.valueOf(1);
        byte[] message = new RLPEntity(nonce, form, to, LOGIN_OPCODE,111, payload, DevEnv).RLPEncode();
        byte[] signByte = HexUtils.fromHex("sasadasdasdasdasdasdasdasda");
        String formAddress = "0x3cF26E1590b443A7D015D9A9E0A68EFadeB68782";

        Boolean signResult = verifyLoginSignature(formAddress,DevEnv,message,signByte);
        System.out.println(signResult);
    }
    @Test
    public void test_pubkey(){
        byte[] signature = Hex.decode("30450221009671546E55CD62E0F304D629F07923477E0CA126E70D115EF2D962AE93EAB0E80220275F2FBEF560DFED98EA204EBE662AB6C57E54BF25B9357CFE7EE034CCD6EE31");
        byte[] message = Hash.keccak256("2A9D2030C0875DD1B0EB37EEF0A4AF64DF614C2335DA1DB1F8C09CD8DF048C19".getBytes());
        System.out.println(new String(PublicKey.recover(signature,message).data()));
    }

    @Test
    public void test_keccak256() {
        String featureMetaRootHash = "476d3f775da5774f8a90a0fa0aad6df9d78996b6b7413b8b4232bf237b20b74d";
        String enMetaRootHash  = "32fe955fda3bf1eb7e56fd1c5e5589790b0a17a4578357972a7f4791cf8e00e4";
        String requestRootHash = HexUtils.toHex(Hash.keccak256((featureMetaRootHash+enMetaRootHash).getBytes()));
        System.out.println(requestRootHash);
    }

    @Test
    public void test_dateTime() {
      System.out.println(new Date().getTime());
    }
}