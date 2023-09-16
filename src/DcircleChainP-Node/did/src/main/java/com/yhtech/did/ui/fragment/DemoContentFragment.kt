package com.yhtech.did.ui.fragment

import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.base.foundation.BaseFragment
import com.base.foundation.DCircleScope
import com.base.foundation.db.DIDArticle
import com.base.foundation.db.FindByCreatorUid
import com.base.foundation.getUs
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SizeUtils
import com.google.gson.Gson
import com.yhtech.did.ui.api.GetDIDMeCreated
import com.yhtech.did.databinding.FragmentDemoContentBinding
import com.yhtech.did.ui.rv.DemoDidContentAdapter
import com.yhtech.image_preview.ui.UIQueue
import com.yhtech.image_preview.ui.invoke
import kotlinx.coroutines.launch

class DemoContentFragment: BaseFragment() {
    private var queue: UIQueue = UIQueue()
    lateinit var binding: FragmentDemoContentBinding
    private lateinit var mDidContentAdapter: DemoDidContentAdapter

    override fun initViewBinding(): ViewBinding {
        binding = FragmentDemoContentBinding.inflate(LayoutInflater.from(context),null,false)
        initPageView()
        return binding
    }

    override fun initPageView() {
        getUs().nc.addObserver(this, DIDArticle.ChangedEvent::class.java) {
            Log.d("CreationCenterActivity", "DIDArticle.ChangedEvent event=${Gson().toJson(it)}")
            DCircleScope.launch {
                queue.invoke {
                    loadFromDb(it.editing)
                }
            }
        }

        DCircleScope.launch {
            queue.invoke {
                loadFromDb()
            }
            loadFromServer()
        }
    }
    private suspend fun loadFromServer() {
        GetDIDMeCreated()
    }


    private suspend fun scrollToEditing(address: String) {
        Log.d("CreationCenterActivity", "scrollToEditing address=${address} start")
        val docs = DIDArticle.FindByCreatorUid(getUs().getUid()).toMutableList()
        docs.find { it.Address == address }?.apply {
            val index = docs.indexOf(this)
            Log.d("CreationCenterActivity", "scrollToEditing index=${index}")
            binding.rv.post {
                binding.rv.scrollToPosition(index)
            }
        }
    }



    private suspend fun loadFromDb(editing: Boolean? = false) {
        mDidContentAdapter = DemoDidContentAdapter()
        adapterGridLM()
        val docs = DIDArticle.FindByCreatorUid(getUs().getUid()).toMutableList()
        docs.add(DIDArticle().apply { Address ="add" })
        LogUtils.d("DemoContentFragment", docs)
        getUs().nc.removeEvent(this, mDidContentAdapter.data.map { DIDArticle.ChangedEvent(it.Address) }.toMutableList())
        getUs().nc.addObserver(this, docs.map { DIDArticle.ChangedEvent(it.Address) }.toMutableList()) {
            DCircleScope.launch {
                if (it.editing) {
                    scrollToEditing(it.address)
                }
            }
        }

        if (docs.isEmpty()) {
            mDidContentAdapter.setList(mutableListOf())
            return
        }

        if (mDidContentAdapter.data.isEmpty()) {
            mDidContentAdapter.setList(docs)
            Log.d("aaaa",docs.toString())
            return
        }

        Log.d("CreationCenterActivity", "loadFromDB start")
        for (doc in docs) {
            Log.d("CreationCenterActivity", "loadFromDB address=${doc.Address} state=${doc.Status}")
        }
        Log.d("CreationCenterActivity", "loadFromDB end")

        mDidContentAdapter.getDiffer().submitList(docs.toMutableList())
        if (editing == true){
            binding.rv.post{
                binding.rv.scrollToPosition(0)
            }
        }
    }
    private fun adapterGridLM() {
            binding.rv.apply {
               layoutManager = GridLayoutManager(context, 3)
                if (itemDecorationCount > 0) {
                    removeItemDecorationAt(0)
                }
                this.addItemDecoration(GridItemDecoration())
                adapter = mDidContentAdapter
            }
    }
    private class GridItemDecoration : RecyclerView.ItemDecoration() {

        val d = SizeUtils.dp2px(20F) / 3

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            val position = parent.getChildAdapterPosition(view)
            val column = position % 3                                           //所在的列
            outRect.left = column * d / 3
            outRect.right = (d - (column + 1) * d / 3)
            outRect.top = d
        }
    }
}