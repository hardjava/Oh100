package com.example.oh100.RecyclerView

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.oh100.Database.FriendListAdapter
import com.example.oh100.Database.FriendListDBHelper
import com.example.oh100.Object.Friend
import com.example.oh100.Service.ApiResponse
import com.example.oh100.Service.ApiService
import com.example.oh100.Service.UserResponse
import com.example.oh100.databinding.RecyclerViewBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.logging.HttpLoggingInterceptor

class RecyclerViewActivity : AppCompatActivity() {
    private lateinit var binding: RecyclerViewBinding
    private lateinit var friendList: ArrayList<Friend>
    private lateinit var friendInformationList: MutableList<Friend>
    private lateinit var dbHelper: FriendListDBHelper
    private lateinit var adapter: FriendListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RecyclerViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        load()
    }

    private fun init() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        dbHelper = FriendListDBHelper(this)
        friendInformationList = mutableListOf<Friend>()
//         dbHelper.addFriend("fkdlcn123",12)
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

                val service = retrofit.create(ApiService::class.java)
                val call = service.getUserData(userId)
                call.enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(
                        call: Call<ApiResponse>,
                        response: Response<ApiResponse>
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
                                val f = Friend(friend.getUserId(), solvedCount, tier, profileUrl)
                                friendInformationList.add(f)

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

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
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

    fun createRetrofitInstance(): Retrofit {
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