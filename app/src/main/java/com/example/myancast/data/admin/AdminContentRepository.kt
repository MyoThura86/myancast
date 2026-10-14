package com.example.myancast.data.admin

import com.example.myancast.domain.model.Episode
import com.example.myancast.domain.model.Podcast

/**
 * ★ Phase 6 — Admin က Firestore ကို **ရေး**တဲ့ တစ်ခုတည်းသော နေရာ။
 * ဖတ်တာကတော့ `PodcastRepository` ကိုပဲ ပြန်သုံး — အသုံးပြုသူ မြင်တာနဲ့ admin မြင်တာ အတူတူ ဖြစ်အောင်။
 *
 * Function အားလုံး မအောင်မြင်ရင် [AdminException] (မြန်မာ message) ပစ်တယ်။
 */
interface AdminContentRepository {
    /** ပုံ/MP3 ကို Storage မှာ document ID နဲ့ folder ခွဲသိမ်းဖို့ — save မလုပ်ခင် ID လိုတယ် */
    fun newPodcastId(): String
    fun newEpisodeId(): String

    suspend fun getEpisode(episodeId: String): Episode?

    /** အသစ်ဆို `episodeCount = 0`၊ ပြင်တာဆို episodeCount ကို မထိ · `updatedAt` = server အချိန် */
    suspend fun savePodcast(podcast: Podcast, isNew: Boolean)

    /** Podcast နဲ့ သူ့ episode အားလုံးကို ဖျက် */
    suspend fun deletePodcast(podcastId: String)

    /** Episode သိမ်းပြီး podcast ရဲ့ `episodeCount` / `updatedAt` ကိုပါ ပြင် */
    suspend fun saveEpisode(episode: Episode, isNew: Boolean)

    suspend fun deleteEpisode(episode: Episode)
}
