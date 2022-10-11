package com.likon.gl.sealedClasses

import com.likon.gl.models.UserInfoModel

sealed class Data {
    data class Success(val userInfoModel: UserInfoModel?): Data()
    data class Error(val error : Exception): Data()
    object Loading : Data()
}