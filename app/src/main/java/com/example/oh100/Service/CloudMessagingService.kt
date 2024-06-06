package com.example.oh100.Service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import android.util.Log
import com.example.oh100.Database.MyPageDBHelper
import com.example.oh100.FriendListView.FriendListViewActivity
import com.example.oh100.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

val TAG = "Firebase Cloud Messaging"

class CloudMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        val db_helper = MyPageDBHelper(this)

        val registered_id = db_helper.getMyId();
        if(registered_id != null) {
            update_token(registered_id)
        }
    }

//    Firebase Cloud Messaging 알림을 실제 표시하는 부분입니다.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.data.isNotEmpty().let {
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]
            sendNotification(title, body)
        }
    }

    private fun sendNotification(title: String?, messageBody: String?) {
        val intent = Intent(this, FriendListViewActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = "default_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "오백완", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}

fun update_token(registered_id : String)
{
//        Firebase Cloud Messaging 토큰을 Cloud Firestore에 토큰을 저장
    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
        if (!task.isSuccessful) {
            Log.w(TAG, "Fetching FCM registration token failed", task.exception)
            return@OnCompleteListener
        }

        val token = task.result
        val db = Firebase.firestore

        val token_data = hashMapOf("token" to token)
        db.collection("users").document(registered_id)
            .set(token_data, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Firebase Cloud Firestore", "Token saved successfully")
            }
            .addOnFailureListener { e ->
                Log.w("Firebase Cloud Firestore", "Error saving token", e)
            }
    })
}
