package com.likon.gl.interfaces

interface OnUpVoteClickedListener {

    fun onUpVoteClick(upState : Boolean , downState : Boolean, postId : String?, userId : String?)

}