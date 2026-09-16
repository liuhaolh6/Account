package com.example.ledger.lab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ledger.ui.theme.MyLedgerTheme

/**
 * 实验1 独立入口。
 *
 * 之所以单独开一个 Activity 而不是改 [com.example.ledger.MainActivity]：
 * 基础实验的演示内容与记账业务无关，独立入口既能完整演示，
 * 又不会破坏原有导航与数据流。
 */
class Lab1Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MyLedgerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Lab1Screen()
                }
            }
        }
    }
}
