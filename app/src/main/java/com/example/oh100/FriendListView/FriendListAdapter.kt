package com.example.oh100.FriendListView

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.oh100.Database.FriendListDBHelper
import com.example.oh100.Database.MyPageDBHelper
import com.example.oh100.Object.MyInfo
import com.example.oh100.Object.User
import com.example.oh100.R
import com.example.oh100.Service.CloudFirestoreService
import com.example.oh100.Service.MyInfoApiResponse
import com.example.oh100.Service.MyInfoApiService
import com.example.oh100.databinding.FriendViewBinding
import com.example.oh100.databinding.ItemMainBinding
import com.example.oh100.databinding.UserSearchingViewBinding
import com.example.oh100.solved.TierImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyViewHolder(val binding: ItemMainBinding) : RecyclerView.ViewHolder(binding.root)

class FriendListAdapter(val datas: MutableList<User>) : RecyclerView.Adapter<MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemMainBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val binding = holder.binding
        val friend = datas[position]
        binding.userIdTextView.text = friend.getUserId()
        binding.solvedCount.text = "Solved: ${friend.getSolvedCount()}"

        // 프로필 이미지 로드
        val profileImageUrl = friend.getProfileImageUrl()
        if (profileImageUrl != null) {
            Glide.with(binding.root)
                .load(profileImageUrl)
                .placeholder(R.drawable.null_profile_image) // 기본 이미지 설정
                .error(R.drawable.null_profile_image) // 로드 실패 시 기본 이미지 설정
                .into(binding.profileImageView)
        } else {
            binding.profileImageView.setImageResource(R.drawable.null_profile_image)
        }

        val selected_user = friend.getUserId()
        val my_db_helper = MyPageDBHelper(holder.itemView.context)
        val friend_db_helper = FriendListDBHelper(holder.itemView.context)

        // 아이템 클릭 리스너 추가
        holder.itemView.setOnClickListener {
            val dialog_binding = FriendViewBinding.inflate(LayoutInflater.from(holder.itemView.context))

            val retrofit = createRetrofitInstance() // Retrofit 인스턴스를 생성하는 함수 호출
            val service = retrofit.create(MyInfoApiService::class.java)
            val call = service.getMyInfo(selected_user)
            call.enqueue(object : Callback<MyInfoApiResponse> {
                override fun onResponse(
                    call: Call<MyInfoApiResponse>, response: Response<MyInfoApiResponse>
                ) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse != null && apiResponse.items.isNotEmpty()) {
                            val userResponse = apiResponse.items[0]
                            val solvedCount = userResponse.solvedCount
                            val tier = userResponse.tier
                            val rank = userResponse.rank

                            dialog_binding.friendHandle.text = "Handle : $selected_user"

                            if (profileImageUrl != null) {
                                Glide.with(dialog_binding.root)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.null_profile_image) // 기본 이미지 설정
                                    .error(R.drawable.null_profile_image) // 로드 실패 시 기본 이미지 설정
                                    .into(dialog_binding.friendImage)
                            } else {
                                dialog_binding.friendImage.setImageResource(R.drawable.null_profile_image)
                            }

                            dialog_binding.friendCount.text = "Solved Count : $solvedCount"
                            dialog_binding.friendRank.text = "Rank : $rank"
                        }
                    } else {
                        // 서버가 오류 응답을 반환한 경우
                        // 에러 처리
                        Toast.makeText(holder.itemView.context, "Server returned error response", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<MyInfoApiResponse>, t: Throwable) {
                    // 네트워크 오류 또는 예외가 발생한 경우
                    // 실패 처리
                    Toast.makeText(holder.itemView.context, "Call Failed", Toast.LENGTH_SHORT).show()
                }

            })

            val builder = AlertDialog.Builder(holder.itemView.context)

            builder.setView(dialog_binding.root)

            builder.setPositiveButton("close") { dialog, _ ->
                dialog.dismiss()
            }
            builder.setNegativeButton("delete") { dialog, _ ->
                CloudFirestoreService.drop_friend(my_db_helper.getMyId()!!, selected_user)
                friend_db_helper.deleteFriend(selected_user)

                datas.removeAt(position)
                notifyDataSetChanged()

                dialog.dismiss()
            }

            val dialog = builder.create()

            dialog.setOnDismissListener {
                dialog_binding.friendImage.setImageResource(R.drawable.null_profile_image)
                dialog_binding.friendHandle.text = "Handle : NULL"
                dialog_binding.friendTier.setImageResource(R.drawable.level_12)
                dialog_binding.friendCount.text = "Solved Count : NULL"
                dialog_binding.friendRank.text = "Rank : NULL"
            }

            dialog.show()
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
}
