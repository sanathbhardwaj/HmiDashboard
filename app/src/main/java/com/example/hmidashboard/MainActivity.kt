package com.example.hmidashboard

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.hmidashboard.data.SimulatedVehicleDataSource
import com.example.hmidashboard.domain.VehicleViewModel
import com.example.hmidashboard.ui.DashboardScreen
import com.example.hmidashboard.ui.theme.HmiTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            HmiTheme {
                val viewModel: VehicleViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { VehicleViewModel(SimulatedVehicleDataSource()) }
                    },
                )
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                DashboardScreen(state)
            }
        }
    }
}
