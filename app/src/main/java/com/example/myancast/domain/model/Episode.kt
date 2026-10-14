package com.example.myancast.domain.model

import com.google.firebase.Timestamp

data class Episode(
    val id: String = "",
    val podcastId: String = "",
    val title: String = "",
    val description: String = "",
    val audioUrl: String = "",
    val duration: Int = 0,
    val publishedAt: Timestamp? = null,
    val coverUrl: String = "",
    /** ★ Phase 6 — MP3 ဖိုင် အရွယ် (byte) · 0 = မသိ (Console က ထည့်ထားတဲ့ data အဟောင်း) */
    val fileSizeBytes: Long = 0L
) {
    val formattedDuration: String
        get() {
            val minutes = duration / 60
            val seconds = duration % 60
            return "%02d:%02d".format(minutes, seconds)
        }
}