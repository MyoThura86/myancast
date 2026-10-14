package com.example.myancast.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ★ Phase 6 (#2) — ဖုန်းရဲ့ default network ကို နားထောင်တယ်။
 * App တစ်ခုလုံးမှာ တစ်ခုတည်း (`MyanCastApp.networkMonitor`) — callback ကို app သက်တမ်းလုံး ထားတယ်။
 */
class AndroidNetworkMonitor(context: Context) : NetworkMonitor {

    private val connectivity =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _status = MutableStateFlow(currentStatus())
    override val status: StateFlow<NetworkStatus> = _status.asStateFlow()

    init {
        connectivity.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                _status.value = caps.toStatus()
            }

            override fun onLost(network: Network) {
                _status.value = NetworkStatus(online = false, metered = false)
            }

            override fun onUnavailable() {
                _status.value = NetworkStatus(online = false, metered = false)
            }
        })
    }

    private fun currentStatus(): NetworkStatus {
        val caps = connectivity.getNetworkCapabilities(connectivity.activeNetwork)
            ?: return NetworkStatus(online = false, metered = false)
        return caps.toStatus()
    }
}

private fun NetworkCapabilities.toStatus() = NetworkStatus(
    online = hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
    metered = !hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
)
