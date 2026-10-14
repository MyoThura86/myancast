package com.example.myancast.data.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NetworkStatus(
    val online: Boolean = true,
    val metered: Boolean = false     // mobile data = true · Wi-Fi = false
)

/** ★ Phase 6 Contract — ViewModel က ConnectivityManager (android.*) ကို မသိရအောင် */
interface NetworkMonitor {
    val status: StateFlow<NetworkStatus>
}

/** Test / Preview အတွက် — အမြဲ online */
class AlwaysOnlineNetworkMonitor : NetworkMonitor {
    override val status: StateFlow<NetworkStatus> = MutableStateFlow(NetworkStatus()).asStateFlow()
}
