package com.likon.gl.interfaces

import com.likon.gl.models.UserInfoModel
import java.util.*

interface OnChatClickedListener {

    fun onItemClick(userInfo : UserInfoModel)
}