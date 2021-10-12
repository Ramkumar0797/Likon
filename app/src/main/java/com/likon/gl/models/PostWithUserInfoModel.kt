package com.likon.gl.models

import android.os.Parcelable

@kotlinx.parcelize.Parcelize
data class PostWithUserInfoModel(val userInfoModel : UserInfoModel, val postModel: PostModel) : Parcelable {
}