package com.sarftec.riddleme

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sarftec.riddleme.database.RiddleDatabaseSetup
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StartActivity : AppCompatActivity() {

    @Inject
    lateinit var databaseSetup: RiddleDatabaseSetup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenCreated {
            databaseSetup.setupDatabase()
            val intent = Intent(this@StartActivity , LoadActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.no_anim, R.anim.no_anim)
        }
    }
}