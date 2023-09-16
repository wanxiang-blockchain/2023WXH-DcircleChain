package com.example.myapplication.ui.dashboard

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.databinding.FragmentDashboardBinding
import com.example.myapplication.fundation.ThirdWallet
import com.example.myapplication.fundation.ThirdWalletHelper
import com.example.myapplication.fundation.db.DCWalletDataBase
import com.example.myapplication.fundation.wallet.PolygonWallet
import kotlinx.coroutines.*
import java.math.BigInteger
import kotlin.concurrent.thread




class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var startSync:Boolean = false

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
                ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.blance
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        var user = DCWalletDataBase.getUserDao(context!!)?.me()
        var wallet = PolygonWallet.loadWalletWithMnemonic(context!!,user!!)

        thread(start = true) {
            binding.address.text =  "地址:${wallet.getAddress()}"
            Log.d("DashboardFragment","地址:${wallet.getAddress()}")
            syncBalanceTask(dashboardViewModel,wallet)
        }

        var transferBtn :Button = binding.transferBtn
        transferBtn.setOnClickListener(View.OnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                var result:Deferred<String> = async {
                    try {
                        return@async wallet.transferContract(
                            PolygonWallet.DCTOKEN_ADDRESS,
                            "0x3cF26E1590b443A7D015D9A9E0A68EFadeB68782",
                            BigInteger.valueOf(100)
                        )
                    } catch (e:java.lang.Exception) {
                        alert(inflater.context,"错误",e.message.toString())
                        return@async ""
                    }
                }
                var txn = result.await()
                if (txn != "") {
                    alert(inflater.context, "操作成功", "交易已经提交:txn:${txn}")
                }
            }
        })

        var depositBtn :Button = binding.depositBtn
        depositBtn.setOnClickListener(View.OnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                var result:Deferred<String> = async {
                    try {
                        return@async wallet.depositsWithToken(
                            PolygonWallet.DCTOKEN_ADDRESS,
                            PolygonWallet.DCChainToken_Address,
                            "0x3cF26E1590b443A7D015D9A9E0A68EFadeB68782",
                            BigInteger.valueOf(100)
                        )
                    } catch (e:java.lang.Exception) {
                        alert(inflater.context,"错误",e.message.toString())
                        return@async ""
                    }
                }
                var txn = result.await()
                if (txn != "") {
                    alert(inflater.context, "操作成功", "交易已经提交:txn:${txn}")
                }
            }
        })
        return root
    }

    fun alert(context:Context,title:String,message: String) {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                var ethEstimateGasAlert = AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(
                        "确定",
                        DialogInterface.OnClickListener { dialog, which -> // Handle button click
                            dialog.dismiss()
                        }
                    )
                    .create()
                ethEstimateGasAlert.show()
            }
        }
    }

    fun syncBalanceTask(dashboardViewModel:DashboardViewModel,wallet:PolygonWallet) {
        if (startSync == false) {
            GlobalScope.launch {
                while (true) {
                    var mainBlance = "MATIC:" + wallet.getBalance().toString() + "MATIC"
                    var tokenBlance =
                        "\nTOKEN:" + wallet.getTokenBalance(PolygonWallet.DCTOKEN_ADDRESS)
                            .toString()
                    var dcc = wallet.getTokenBalance(PolygonWallet.DCChainToken_Address)!!
                        .divide(BigInteger("100000000000000000"))
                    var dcCoinBlance =
                        "\nDcChain Coin(DCC):" +dcc
                                .toString()
                    var blanceString = "余额\n${mainBlance} ${dcCoinBlance} ${tokenBlance}"
                    dashboardViewModel.changeText(blanceString)
                    delay(5000)
                    Log.i("syncBalance", blanceString)
                }
            }
            startSync= true
        }
    }

    fun startMetaMaskTokenTransaction(context:Context,wallet:ThirdWallet,tokenAddress: String, toAddress: String, amount: Int) {
        val packageName = ThirdWalletHelper.getWalletPackage(wallet)
        val intent = Intent(Intent.ACTION_VIEW)
//        intent.data = Uri.parse("ethereum:token-transfer?address=$tokenAddress&to=$toAddress&uint256=$amount")
        intent.data = Uri.parse("ethereum:${tokenAddress}/?address=$toAddress&value=$amount")
        intent.setPackage(packageName)

        if (isPackageInstalled(context,packageName)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Toast.makeText(context,"启动 ${wallet.name}!",Toast.LENGTH_SHORT).show()
            context.startActivity(intent)
        } else {
            // MetaMask 应用未安装
            // 可以在这里提示用户安装 MetaMask 应用
            Toast.makeText(context,"${wallet.name} not installed!",Toast.LENGTH_SHORT).show()
        }
    }

    fun isPackageInstalled(context: Context, packageName: String?): Boolean {
        return try {
            val pm: PackageManager = context.getPackageManager()
            pm.getPackageInfo(packageName!!, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}