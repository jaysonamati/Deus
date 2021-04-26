package com.amati.deus.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.security.crypto.MasterKeys
import com.amati.deus.R
import com.amati.deus.databinding.FragmentSetAdminCypherBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SetAdminCypherFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SetAdminCypherFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController

    private lateinit var binding: FragmentSetAdminCypherBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_set_admin_cypher, container, false)
        auth = Firebase.auth
        navController = findNavController()
        binding.generateKeyButton.setOnClickListener {
            generatePrivateKey()
        }
        return binding.root
    }

    private fun generatePrivateKey() {
        val mainKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        Timber.e(mainKey)
        firebaseAnonAuth()
    }

    private fun firebaseAnonAuth() {
        auth.signInAnonymously()
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Timber.d("signInAnonymously:success")
                    val user = auth.currentUser
                    val actionMainFragment =
                        SetAdminCypherFragmentDirections.actionSetAdminCypherFragmentToMainPageFragment()
                    navController.navigate(actionMainFragment)
                } else {
                    Timber.e(task.exception, "signInAnonymously:failure")
                }
            }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SetAdminCypherFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SetAdminCypherFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}