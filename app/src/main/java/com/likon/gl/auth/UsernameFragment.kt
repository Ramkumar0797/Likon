package com.likon.gl.auth


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.likon.gl.R
import com.likon.gl.adapters.PeopleListAdapter
import com.likon.gl.databinding.FragmentUsernameBinding
import com.likon.gl.interfaces.OnUserDataSetListener
import com.likon.gl.interfaces.TitleChangeListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.tasks.asDeferred
import java.lang.Exception
import java.util.*

private const val TAG = "UsernameFragment"

class UsernameFragment : Fragment(R.layout.fragment_username) {

    private var _binding: FragmentUsernameBinding? = null
    private val binding get() = _binding!!
    private  lateinit var onUserDataSetListener : OnUserDataSetListener
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var progressDialog : AlertDialog
    private lateinit var titleChangeListener: TitleChangeListener
    private var searchJob: Job? = null
    private var query : String? = null
    private lateinit var mActivity: Activity
    private val mContext get() = mActivity
    private lateinit var sl : OnCompleteListener<QuerySnapshot>


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is OnUserDataSetListener && context is TitleChangeListener){
            context.also {
                onUserDataSetListener = it
                titleChangeListener = it
            }
        }

    }
    override fun onResume() {
        super.onResume()
        titleChangeListener.onChange("Username")

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUsernameBinding.bind(view)

        binding.apply {
            val filter = InputFilter { source: CharSequence, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int ->
                for (i in start until end) {
                    if (!Character.isLetterOrDigit(source[i]) && source != "." && source != "_"  && source != " "){
                        usernameInfo.error = ("Sorry, only letters (a-z),numbers (0-9), underscore (_) and periods(.) are allowed.")
                    }else{
                        usernameInfo.isErrorEnabled = false
                    }
                }
                source.toString().lowercase(Locale.ROOT).replace(" ","_")
            }

            username.apply {
                filters = arrayOf(filter, LengthFilter(30))
                addTextChangedListener(object : TextWatcher{
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun afterTextChanged(p0: Editable?) {


                        usernameInfo.isErrorEnabled = false
//                        usernameNext.isEnabled = false
//                        searchJob?.cancel()
//                        if(p0.toString().trim() != ""){
//
//                            if(p0.toString().trim().replace("_", "").replace(".", "") == ""){
//                                usernameNext.isEnabled = true
//                                Log.d(TAG, "afterTextChanged: rrrrrrrrrrrrrrrrrrrrrr")
//                                return
//                            }else{
//                                progress.visibility = View.VISIBLE
//                                searchJob = lifecycleScope.launchWhenResumed {
//                                    getUsername(p0.toString().trim())
//
//                                }
//                            }
//
//                        }

                    }
                })
            }

            usernameNext.setOnClickListener {
                username.onEditorAction(EditorInfo.IME_ACTION_DONE)
                val tempUsername = username.text.toString().trim().lowercase(Locale.ROOT)
                if(tempUsername == "" ){
                    usernameInfo.error = ("Should not leave as empty this field!")
                }else{
                    progressDialog.show()
                    usernameInfo.isErrorEnabled = false
                    usernameNext.isEnabled = false
                    if(tempUsername.replace("_", "").replace(".", "") == ""){
                        showErrorDialog("Invalid username", "Username must also contain with letters (a-z) or numbers (0-9)")
                    }else{

                        db.collection("users").whereEqualTo("username", tempUsername)

                                .get(Source.SERVER).addOnSuccessListener {


                                    if(it.size() == 0){
                                            doubleCheck(tempUsername)
                                    }else  if(it.size() == 1){

                                        if(it.documents[0].id == auth.uid ){
                                                doubleCheck(tempUsername)
                                        }else{
                                            showError()
                                        }

                                    }else{
                                        showError()
                                    }

                                }.addOnFailureListener{
                                    showErrorDialog("Network error", "Please check your internet connection")
                                }
                    }
                }
            }

        }

        progressDialog = AlertDialog.Builder(requireContext()).apply {
            setView(R.layout.dialog_progress_layout)
            setCancelable(false)
        }.create()

    }

//    private fun getUsername(username :String){
//        db.collection("users").whereEqualTo("username", username)
//
//            .get(Source.SERVER).addOnSuccessListener {
//
//                Log.d(TAG, "getUsername: jjjjjjjjjjjjjjjjjjjjjjjjjjjj ${searchJob?.isActive} -- ${searchJob?.isCompleted} --${searchJob?.isCancelled}")
//                if(it.size() == 0){
//                   binding.usernameNext.isEnabled = true
//                    binding.progress.visibility = View.INVISIBLE
//
//                }else  if(it.size() == 1){
//
//                    if(it.documents[0].id == auth.uid ){
//                        binding.usernameNext.isEnabled = true
//                        binding.progress.visibility = View.INVISIBLE
//
//                    }else{
//                        binding.usernameNext.isEnabled = false
//                        binding.progress.visibility = View.INVISIBLE
//
//                        showError()
//                    }
//
//                }else{
//                    binding.usernameNext.isEnabled = false
//                    binding.progress.visibility = View.INVISIBLE
//
//                    showError()
//                }
//
//            }.addOnFailureListener{
//                showErrorDialog("Network error", "Please check your internet connection")
//            }
//    }


    private fun doubleCheck(tempUsername : String){

        val setDocRef = db.collection("users").document(auth.uid!!)

        db.runTransaction { transaction  ->

                transaction .set(setDocRef,hashMapOf("username" to tempUsername), SetOptions.merge())

        }.addOnSuccessListener {

            db.collection("users").whereEqualTo("username", tempUsername)
                    .get(Source.SERVER).addOnSuccessListener {

                        if(it.size() == 0){
                            doubleCheck(tempUsername)
                        }else  if(it.size() == 1){

                            if(it.documents[0].id == auth.uid ){

                                    onUserDataSetListener.onDataSet(this@UsernameFragment,
                                        7,tempUsername,null)
                            }else{
                                showError()
                            }

                        }else{
                            showError()
                        }

                    }.addOnFailureListener{
                        showErrorDialog("Network error", "Please check your internet connection")
                    }

        }.addOnFailureListener{

            showErrorDialog("Network error", "Please check your internet connection")
        }
    }

    private fun showErrorDialog(title : String, message : String){
        progressDialog.dismiss()
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok") { dialog, which ->
                    dialog.dismiss()
                }.show()
        binding.usernameNext.isEnabled = true
    }

    private fun showError(){
        progressDialog.dismiss()
        binding.usernameInfo.error = ("Username already exist!")
        binding.usernameNext.isEnabled = true

        // Remove the 'username' field from the document
        val docRef = db.collection("users").document(auth.uid!!)
        val updates = hashMapOf<String, Any>("username" to FieldValue.delete())
        docRef.update(updates)
    }

    fun toSetError(){
        showErrorDialog("Network error", "Please check your internet connection")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}