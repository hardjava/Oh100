package com.example.oh100.Service

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object CloudFirestoreService
{
    fun add_user(user_handle : String, user_count : Int)
    {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            val token = task.result
            val db = Firebase.firestore

            val token_data = hashMapOf("token" to token, "solved_count" to user_count)
            db.collection("users").document(user_handle)
                .set(token_data, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("Firebase Cloud Firestore", "Token saved successfully")
                }
                .addOnFailureListener { e ->
                    Log.w("Firebase Cloud Firestore", "Error saving token", e)
                }
        })
    }

    fun drop_user(user_handle : String)
    {
        val db = Firebase.firestore
        val documentRef = db.collection("users").document(user_handle)

        documentRef.delete()
    }

    fun add_friend(user_handle: String, friend_handle : String, friend_count : Int)
    {
        val db = Firebase.firestore

        val token_data = hashMapOf(friend_handle to friend_count)
        db.collection("friends_count").document(user_handle)
            .set(token_data, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Firebase Cloud Firestore", "Token saved successfully")
            }
            .addOnFailureListener { e ->
                Log.w("Firebase Cloud Firestore", "Error saving token", e)
            }
    }

    fun drop_friend(user_handle: String, friend_handle : String)
    {
        val db = Firebase.firestore

        val documentRef = db.collection("friends_count").document(user_handle)

        val updates = hashMapOf<String, Any>(
            friend_handle to FieldValue.delete()
        )

        documentRef.update(updates)
    }

    suspend fun rename_document(collectionPath: String, oldDocumentId: String, newDocumentId: String) {
        val db = Firebase.firestore

        val docRef = db.collection(collectionPath).document(oldDocumentId)
        val newDocRef = db.collection(collectionPath).document(newDocumentId)

        try {
            // 기존 문서 데이터 가져오기
            val snapshot = docRef.get().await()
            val data = snapshot.data

            if (data != null) {
                // 새로운 문서 생성
                newDocRef.set(data).await()

                // 기존 문서 삭제
                docRef.delete().await()
            }
        } catch (e: Exception) {
            Log.e("Oh100 Firestore", "Error renaming document: ${e.message}")
        }
    }
}