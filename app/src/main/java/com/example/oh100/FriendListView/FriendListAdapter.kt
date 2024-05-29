package com.example.oh100.FriendListView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.oh100.Object.User
import com.example.oh100.R
import com.example.oh100.databinding.ItemMainBinding

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
        }
    }
}
