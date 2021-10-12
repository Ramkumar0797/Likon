package com.likon.gl.interfaces

import android.os.Bundle
import androidx.fragment.app.Fragment
import java.util.*

interface OnUserDataSetListener {

    fun onDataSet(currentFragment : Fragment, addFragment : Int, value : String?, date: Date?)
}