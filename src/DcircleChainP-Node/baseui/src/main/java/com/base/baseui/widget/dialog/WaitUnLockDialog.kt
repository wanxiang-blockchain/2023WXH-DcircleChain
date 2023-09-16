package com.base.baseui.widget.dialog

import android.content.Context
import androidx.viewbinding.ViewBinding
import com.base.baseui.databinding.DialogWaitUnlockBinding
import com.base.foundation.DCircleScope
import com.base.thridpart.setOnClickDelay
import kotlinx.coroutines.launch

class WaitUnLockDialog(context: Context, private var owner:Owner = object : Owner {
    override suspend fun onWaitUnLockDialogDismiss(dialog: WaitUnLockDialog) {
    }
}) : BaseBottomSheetDialog(context) {
    interface Owner {
        suspend fun onWaitUnLockDialogDismiss(dialog:WaitUnLockDialog)
    }
    lateinit var binding: DialogWaitUnlockBinding
    override fun initViewBinding(): ViewBinding {
        binding = DialogWaitUnlockBinding.inflate(layoutInflater)
        return binding
    }

    override fun afterShow() {
        super.afterShow()
        binding.tvDone.setOnClickDelay {
            dismiss()
        }
    }

    override fun dismiss() {
        super.dismiss()
        DCircleScope.launch {
            owner.onWaitUnLockDialogDismiss(this@WaitUnLockDialog)
        }
    }
}