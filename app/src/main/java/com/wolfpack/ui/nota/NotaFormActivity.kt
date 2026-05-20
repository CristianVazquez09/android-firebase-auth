package com.wolfpack.ui.nota

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.wolfpack.data.model.Nota
import com.wolfpack.ui.base.BaseActivity

class NotaFormActivity : BaseActivity() {
    override fun requiresAuth() = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    companion object {
        fun editIntent(context: Context, nota: Nota): Intent =
            Intent(context, NotaFormActivity::class.java).putExtra("extra_nota", nota)
    }
}