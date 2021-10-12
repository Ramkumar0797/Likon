package com.likon.gl.interfaces

import com.likon.gl.models.PostModel
import com.likon.gl.models.UserInfoModel


interface OnPostItemsClickListener {

    fun onItemClick(navigateTO : Int, postModel: PostModel?, userInfoModel: UserInfoModel? )

}