package com.example.oh100.MyPageView

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.oh100.Database.MyPageDBHelper
import com.example.oh100.Object.MyInfo
import com.example.oh100.R
import com.example.oh100.Service.MyInfoApiResponse
import com.example.oh100.Service.MyInfoApiService
import com.example.oh100.databinding.MypageViewBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyPageViewActivity : AppCompatActivity() {
    private lateinit var dbHelper: MyPageDBHelper
    private lateinit var binding: MypageViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MypageViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        showMyPage()
        binding.exitButton.setOnClickListener {
            finish()
        }

        binding.registerOrChangeButton.setOnClickListener {
//            val intent = Intent(this, TODO : 사용자 설정 Activity 입력::class.java)
//            startActivity(intent)
        }
    }

    private fun init() {
        dbHelper = MyPageDBHelper(this)
//        dbHelper.addMyId("binarynacho")
        dbHelper.deleteMyId("binarynacho")
    }

    private fun showMyPage() {
        val myId = dbHelper.getMyId()
        if (myId != null) {
            val retrofit = createRetrofitInstance() // Retrofit 인스턴스를 생성하는 함수 호출
            val service = retrofit.create(MyInfoApiService::class.java)
            val call = service.getMyInfo(myId)
            call.enqueue(object : Callback<MyInfoApiResponse> {
                override fun onResponse(
                    call: Call<MyInfoApiResponse>, response: Response<MyInfoApiResponse>
                ) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse != null && apiResponse.items.isNotEmpty()) {
                            val userResponse = apiResponse.items[0]
                            val myProfileImageUrl = userResponse.profileImageUrl
                            val mySolvedCount = userResponse.solvedCount
                            val myTier = userResponse.tier
                            val myRank = userResponse.rank
                            val myMaxStreak = userResponse.makStreak
                            val myInfo = MyInfo(
                                myId, myProfileImageUrl, mySolvedCount, myTier, myRank, myMaxStreak
                            )
                            updateMyPageView(myInfo)
                        }
                    } else {
                        // 서버가 오류 응답을 반환한 경우
                        // 에러 처리
                        Toast.makeText(
                            applicationContext, "Server returned error response", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<MyInfoApiResponse>, t: Throwable) {
                    // 네트워크 오류 또는 예외가 발생한 경우
                    // 실패 처리
                    Toast.makeText(applicationContext, "Call Failed", Toast.LENGTH_SHORT).show()
                }

            })
        }
    }

    private fun createRetrofitInstance(): Retrofit {
        // HttpLoggingInterceptor를 사용하여 로깅 수준을 설정
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // OkHttpClient를 생성하고 로깅 인터셉터를 추가
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        // Retrofit 인스턴스를 생성하고 OkHttpClient를 설정
        return Retrofit.Builder().baseUrl("https://solved.ac/")
            .addConverterFactory(GsonConverterFactory.create()).client(client).build()
    }

    @SuppressLint("SetTextI18n")
    private fun updateMyPageView(myInfo: MyInfo) {
        binding.userIdTextView.text = "User ID: ${myInfo.getMyId()}"
        binding.tierTextView.text = "Tier: ${myInfo.getMyTier()}"
        binding.solvedCountTextView.text = "Solved Count: ${myInfo.getMySolvedCount()}"
        binding.rankTextView.text = "Rank: ${myInfo.getMyRank()}"
        binding.maxStreakTextView.text = "maxStreak: ${myInfo.getMyMaxStreak()}"

        val profileImageUrl = myInfo.getMyProfileImageUrl()
        if (profileImageUrl != null) {
            Glide.with(binding.root)
                .load(profileImageUrl)
                .placeholder(R.drawable.null_profile_image) // 기본 이미지 설정
                .error(R.drawable.null_profile_image) // 로드 실패 시 기본 이미지 설정
                .into(binding.profileImageView)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(data == null)
            return

        val registered_id = data!!.getStringExtra("id").toString()

//        Firebase Cloud Messaging 토큰을 Cloud Firestore에 토큰을 저장
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("Firebase Cloud Messaging", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
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
}