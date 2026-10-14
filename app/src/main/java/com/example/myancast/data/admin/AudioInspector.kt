package com.example.myancast.data.admin

import com.example.myancast.domain.model.AudioInfo
import com.example.myancast.domain.model.MediaInput

/**
 * ★ Phase 6 — MP3 ကို ဖွင့်ကြည့်ပြီး ကြာချိန် / အရွယ် / format စစ်သူ။
 * Admin က ကြာချိန်ကို လက်နဲ့ မှန်းရိုက်စရာ မလိုတော့ဘူး (Phase 5 ရဲ့ `duration` data မကိုက်တဲ့ ပြဿနာ)။
 */
interface AudioInspector {
    /** ဖတ်လို့ မရရင် (လင့်ခ် ပျက်၊ အသံဖိုင် မဟုတ်) `null` */
    suspend fun inspect(input: MediaInput): AudioInfo?
}
