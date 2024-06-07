package com.example.oh100.MyPageView

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.oh100.Database.MyPageDBHelper
import com.example.oh100.Object.MyInfo
import com.example.oh100.R
import com.example.oh100.Service.CloudFirestoreService
import com.example.oh100.Service.MyInfoApiResponse
import com.example.oh100.Service.MyInfoApiService
import com.example.oh100.Service.update_token
import com.example.oh100.databinding.MypageViewBinding
import com.example.oh100.databinding.TimePickerViewBinding
import com.example.oh100.databinding.UserSearchingViewBinding
import com.example.oh100.solved.Problem
import com.example.oh100.solved.TierImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

        val dialog_binding = UserSearchingViewBinding.inflate(layoutInflater)
        var user_exist = false
        var user_count = 0

        dialog_binding.searchButton.setOnClickListener {
            val searching_handle = dialog_binding.userHandleEditText.text.toString()

            val retrofit = createRetrofitInstance() // Retrofit 인스턴스를 생성하는 함수 호출
            val service = retrofit.create(MyInfoApiService::class.java)

            val call = service.getMyInfo(searching_handle)
            call.enqueue(object : Callback<MyInfoApiResponse> {
                override fun onResponse(
                    call: Call<MyInfoApiResponse>, response: Response<MyInfoApiResponse>
                ) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse != null && apiResponse.items.isNotEmpty()) {
                            user_exist = true

                            val userResponse = apiResponse.items[0]
                            val profile_image_URL = userResponse.profileImageUrl
                            user_count = userResponse.solvedCount
                            val tier = userResponse.tier
                            val rank = userResponse.rank

                            dialog_binding.searchedUserHandle.text = "User ID : $searching_handle"

                            if (profile_image_URL != null) {
                                Glide.with(dialog_binding.root)
                                    .load(profile_image_URL)
                                    .placeholder(R.drawable.null_profile_image)
                                    .error(R.drawable.null_profile_image)
                                    .into(dialog_binding.searchedUserImage)
                            }

                            TierImage.load(this@MyPageViewActivity, dialog_binding.searchedUserTier, tier)

                            dialog_binding.searchedUserCount.text = "Solved Count : $user_count"

                            dialog_binding.searchedUserRank.text = "Rank : $rank"
                        }
                    } else {
                        user_exist = false

                        Toast.makeText(applicationContext, "User doesn't exist", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<MyInfoApiResponse>, t: Throwable) {
                    user_exist = false

                    Toast.makeText(applicationContext, "Server's not responding", Toast.LENGTH_SHORT).show()
                }
            })
        }

        val builder = AlertDialog.Builder(this)

        builder.setView(dialog_binding.root)

        builder.setPositiveButton("register / change") { dialog, _ ->
            if(user_exist) {
                val cur_handle = dbHelper.getMyId()
                if (cur_handle != null) {
                    dbHelper.deleteMyId(cur_handle)
                    CloudFirestoreService.drop_user(cur_handle)
                }

                val user_handle = dialog_binding.userHandleEditText.text.toString()
                dbHelper.addMyId(user_handle)

                CloudFirestoreService.add_user(user_handle, user_count)

                dialog.dismiss()

                showMyPage()
            }
        }
        builder.setNegativeButton("cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()

        dialog.setOnDismissListener {
            dialog_binding.userHandleEditText.setText("")
            dialog_binding.searchedUserImage.setImageResource(R.drawable.null_profile_image)
            dialog_binding.searchedUserHandle.text = "Handle : NULL"
            dialog_binding.searchedUserTier.setImageResource(R.drawable.level_12)
            dialog_binding.searchedUserCount.text = "Solved Count : NULL"
            dialog_binding.searchedUserRank.text = "Rank : NULL"
        }

        binding.registerOrChangeButton.setOnClickListener {
            dialog.show()
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
        dbHelper.addMyId(registered_id)

        update_token(registered_id)
    }
}