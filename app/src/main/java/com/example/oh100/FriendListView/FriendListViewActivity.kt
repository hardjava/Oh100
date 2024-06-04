package com.example.oh100.FriendListView

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.oh100.Database.FriendListDBHelper
import com.example.oh100.MyPageView.MyPageViewActivity
import com.example.oh100.Object.User
import com.example.oh100.Service.FriendListApiResponse
import com.example.oh100.Service.FriendListApiService
import com.example.oh100.databinding.FriendListViewBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.logging.HttpLoggingInterceptor

class FriendListViewActivity : AppCompatActivity() {
    private lateinit var binding: FriendListViewBinding
    private lateinit var friendList: ArrayList<User>
    private lateinit var friendInformationList: MutableList<User>
    private lateinit var dbHelper: FriendListDBHelper
    private lateinit var adapter: FriendListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FriendListViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        load()
        binding.myPageButton.setOnClickListener {
            val intent = Intent(this, MyPageViewActivity::class.java)
            startActivity(intent)
        }

//        Firebase Cloud Messaging 서비스를 위해서 알림 권한을 요청합니다. (이미 허가되어 있으면 자동으로 생략됩니다.)
        askNotificationPermission()
    }

    private fun init() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        dbHelper = FriendListDBHelper(this)
        friendInformationList = mutableListOf<User>()
//         dbHelper.addFriend("songpy123",12)
//          dbHelper.addFriend("binarynacho",12)
//       dbHelper.deleteFriend("fkdlcn123")
//        dbHelper.deleteFriend("binarynacho")

    }

    private fun load() {
        friendList = dbHelper.getAllFriends() // friendListDB에 있는 friendList
        if (friendList.isNotEmpty()) {
            for (friend in friendList) {
                val userId = friend.getUserId()
                val retrofit = createRetrofitInstance() // Retrofit 인스턴스를 생성하는 함수 호출

                val service = retrofit.create(FriendListApiService::class.java)
                val call = service.getUserData(userId)
                call.enqueue(object : Callback<FriendListApiResponse> {
                    override fun onResponse(
                        call: Call<FriendListApiResponse>,
                        response: Response<FriendListApiResponse>
                    ) {
                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            // 전체 JSON 응답 로그 출력
                            if (apiResponse != null && apiResponse.items.isNotEmpty()) {
                                val userResponse =
                                    apiResponse.items[0]  // 첫 번째 UserResponse 객체 가져오기
                                val tier = userResponse.tier
                                val solvedCount = userResponse.solvedCount
                                val profileUrl = userResponse.profileImageUrl
                                val myFriend = User(friend.getUserId(), solvedCount, tier, profileUrl)
                                friendInformationList.add(myFriend)

                                // 데이터가 변경되었음을 어댑터에 알림
                                adapter.notifyDataSetChanged()
                            }
                        } else {
                            // 서버가 오류 응답을 반환한 경우
                            // 에러 처리
                            Toast.makeText(
                                applicationContext,
                                "Server returned error response",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<FriendListApiResponse>, t: Throwable) {
                        // 네트워크 오류 또는 예외가 발생한 경우
                        // 실패 처리
                        Toast.makeText(applicationContext, "Call Failed", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            // 여기서 어댑터를 설정하는 대신, 데이터가 변경될 때마다 어댑터를 갱신합니다.
            adapter = FriendListAdapter(friendInformationList)
            binding.recyclerView.adapter = adapter
        }
    }

    private fun createRetrofitInstance(): Retrofit {
        // HttpLoggingInterceptor를 사용하여 로깅 수준을 설정
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // OkHttpClient를 생성하고 로깅 인터셉터를 추가
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        // Retrofit 인스턴스를 생성하고 OkHttpClient를 설정
        return Retrofit.Builder()
            .baseUrl("https://solved.ac/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

//    Firebase Cloud Messaging 권한 요청을 위한 부분입니다.
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (및 앱)가 알림을 게시할 수 있습니다.
        } else {
            Toast.makeText(this, "알림 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askNotificationPermission() {
        // API 레벨 33 (TIRAMISU) 이상에서만 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED -> {
                    // FCM SDK (및 앱)가 알림을 게시할 수 있습니다.
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showPermissionRationale()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("알림 권한 필요")
            .setMessage("문제 풀이 알림을 보내기 위해 알림 권한이 필요합니다.")
            .setPositiveButton("허가") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("거부") { _, _ ->
                Toast.makeText(this, "알림 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}