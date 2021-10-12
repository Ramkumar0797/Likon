package com.likon.gl.interfaces

interface OnDownVoteClickedListener {

    fun onDownVoteClick(upState : Boolean , downState : Boolean, postId : String?, userId : String?)

}