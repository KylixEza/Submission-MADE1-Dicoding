package com.kylix.submissionmade1.follow

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.kylix.submissionmade1.R
import com.kylix.core.data.Resource
import com.kylix.core.ui.UserAdapter
import com.kylix.submissionmade1.databinding.FavoriteFragmentBinding
import com.kylix.submissionmade1.databinding.FollowFragmentBinding
import com.kylix.submissionmade1.databinding.HomeFragmentBinding
import com.kylix.submissionmade1.util.ShowState
import com.kylix.submissionmade1.util.TypeView
import com.shashank.sony.fancytoastlib.FancyToast
import org.koin.android.viewmodel.ext.android.viewModel

class FollowFragment : Fragment(), ShowState {

    companion object {
        fun newInstance(username: String, type: String) =
            FollowFragment().apply {
                arguments = Bundle().apply {
                    putString(USERNAME, username)
                    putString(TYPE, type)
                }
            }

        private const val USERNAME ="username"
        private const val TYPE = "type"
    }

    private lateinit var followBinding: FollowFragmentBinding
    private lateinit var followAdapter: UserAdapter
    private lateinit var username: String
    private var type: String? = null
    private val followViewModel: FollowViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            username = it.getString(USERNAME).toString()
            type = it.getString(TYPE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        followBinding = FollowFragmentBinding.inflate(layoutInflater, container, false)
        return followBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        followAdapter = UserAdapter(arrayListOf()) { user, _ ->
            FancyToast.makeText(context, user, FancyToast.LENGTH_SHORT, FancyToast.INFO, false).show()
        }

        followBinding.recylerFollow.apply { 
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = followAdapter
        }

        when(type) {
            resources.getString(R.string.following) -> followViewModel.setFollow(username, TypeView.FOLLOWING)
            resources.getString(R.string.followers) -> followViewModel.setFollow(username, TypeView.FOLLOWER)
            else -> onErrorState(followFragmentBinding = followBinding, message = null)
        }

        observeFollow()
    }

    private fun observeFollow() {
        followViewModel.favoriteUsers.observe(viewLifecycleOwner, {
            it.let {
                when (it) {
                    is Resource.Success ->
                        if (!it.data.isNullOrEmpty()) {
                            onSuccessState(followFragmentBinding = followBinding)
                            followAdapter.run { setData(it.data) }
                        } else {
                            onErrorState(followFragmentBinding = followBinding, message =  resources.getString(R.string.not_have, username, type))
                        }
                    is Resource.Loading -> onLoadingState(followFragmentBinding = followBinding)
                    is Resource.Error -> onErrorState(followFragmentBinding = followBinding, message = it.message)
                }
            }
        })
    }

    override fun onSuccessState(homeFragmentBinding: HomeFragmentBinding?,
                                followFragmentBinding: FollowFragmentBinding?
                                , favoriteFragmentBinding: FavoriteFragmentBinding?) {
        followFragmentBinding?.apply {
            errLayout.mainNotFound.visibility = View.GONE
            progress.stop()
            recylerFollow.visibility = View.VISIBLE
        }
    }

    override fun onLoadingState(homeFragmentBinding: HomeFragmentBinding?,
                                followFragmentBinding: FollowFragmentBinding?,
                                favoriteFragmentBinding: FavoriteFragmentBinding?) {
        followFragmentBinding?.apply {
            errLayout.mainNotFound.visibility = View.GONE
            progress.start()
            progress.loadingColor = R.color.colorAccent
            recylerFollow.visibility = View.GONE
        }
    }

    override fun onErrorState(homeFragmentBinding: HomeFragmentBinding?,
                              followFragmentBinding: FollowFragmentBinding?,
                              favoriteFragmentBinding: FavoriteFragmentBinding?,
                              message: String?) {
        followFragmentBinding?.apply {
            errLayout.apply {
                mainNotFound.visibility = View.VISIBLE
                emptyText.text = message ?: resources.getString(R.string.not_found)
            }
            progress.stop()
            recylerFollow.visibility = View.GONE
        }
    }
}