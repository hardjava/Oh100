package com.example.oh100.FriendListView

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.oh100.Database.FriendListDBHelper
import com.example.oh100.MyPageView.MyPageViewActivity
import com.example.oh100.Object.User
import com.example.oh100.Service.FriendListApiResponse
import com.example.oh100.Service.FriendListApiService
import com.example.oh100.databinding.FriendListViewBinding
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
}