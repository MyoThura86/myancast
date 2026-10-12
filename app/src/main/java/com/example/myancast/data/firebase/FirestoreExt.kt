// data/firebase/FirestoreExt.kt
package com.example.myancast.data.firebase

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// Console မှာ field type မှားထည့်ထားတဲ့ document (ဥပမာ episodeCount ကို string) တစ်ခုကြောင့်
// app တစ်ခုလုံး crash မဖြစ်အောင် အဲဒီ document ကိုပဲ ကျော်ပြီး log ထုတ်မယ်
fun <T : Any> QuerySnapshot.toListWithIds(
    clazz: Class<T>,
    withId: (T, String) -> T
): List<T> = documents.mapNotNull { doc ->
    try {
        doc.toObject(clazz)?.let { withId(it, doc.id) }
    } catch (e: RuntimeException) {
        Log.w(TAG, "Skipping ${doc.reference.path}: ${e.message}")
        null
    }
}

private const val TAG = "FirestoreExt"

fun <T : Any> DocumentSnapshot.toObjectWithId(
    clazz: Class<T>,
    withId: (T, String) -> T
): T? = toObject(clazz)?.let { withId(it, id) }

fun <T : Any> Query.snapshotFlow(
    clazz: Class<T>,
    withId: (T, String) -> T
): Flow<List<T>> = callbackFlow {
    val listener = addSnapshotListener { snapshot, error ->
        if (error != null) {
            close(error)
            return@addSnapshotListener
        }
        trySend(snapshot?.toListWithIds(clazz, withId) ?: emptyList())
    }
    awaitClose { listener.remove() }
}