package com.example.myancast.data.network

import coil.intercept.Interceptor
import coil.request.CachePolicy
import coil.request.ImageResult

/** Admin preview လို ပုံ မပြမဖြစ်တဲ့ request တွေမှာ `setParameter(DATA_SAVER_EXEMPT, true)` */
const val DATA_SAVER_EXEMPT = "data_saver_exempt"

/**
 * ★ Phase 6 (#5) — Data ချွေတာ mode + mobile data ဆိုရင် ပုံကို network ကနေ **မဒေါင်း** —
 * cache (memory/disk) ထဲ ရှိပြီးသား ပုံကိုပဲ ပြ။
 *
 * `AsyncImage` တစ်ခုချင်း ပြင်မယ့်အစား Coil ImageLoader တစ်ခုလုံးမှာ ထည့်ထားလို့
 * PodcastCard / NewsCard / MiniPlayer / Full Player ... ဘယ်ပုံမှ မလွတ်ဘူး။
 *
 * @param saveData ပုံ request တိုင်း ခေါ် — setting/network ပြောင်းတာနဲ့ ချက်ချင်း သက်ရောက်
 */
class DataSaverInterceptor(
    private val saveData: () -> Boolean
) : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val request = chain.request
        if (!saveData() || request.parameters.value<Boolean>(DATA_SAVER_EXEMPT) == true) {
            return chain.proceed(request)
        }
        return chain.proceed(
            request.newBuilder()
                .networkCachePolicy(CachePolicy.DISABLED)
                .build()
        )
    }
}
