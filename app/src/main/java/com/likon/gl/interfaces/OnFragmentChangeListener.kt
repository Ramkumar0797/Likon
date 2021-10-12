package com.likon.gl.interfaces

import android.os.Bundle
import androidx.fragment.app.Fragment

interface OnFragmentChangeListener {

   fun onChange(currentFragment : Fragment, addFragment : Int, bundle: Bundle?)
}