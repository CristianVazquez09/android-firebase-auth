package com.wolfpack.ui.materia

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.wolfpack.data.model.Materia
import com.wolfpack.ui.base.BaseActivity

class MateriaFormActivity : BaseActivity() {
    override fun requiresAuth() = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    companion object {
        fun editIntent(context: Context, materia: Materia): Intent =
            Intent(context, MateriaFormActivity::class.java).putExtra("extra_materia", materia)
    }
}