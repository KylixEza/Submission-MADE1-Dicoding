package com.kylix.favorite.favorite

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kylix.submissionmade1.R
import com.kylix.core.ui.UserAdapter
import com.kylix.favorite.di.favoriteModule
import com.kylix.submissionmade1.databinding.FavoriteFragmentBinding
import com.kylix.submissionmade1.databinding.FollowFragmentBinding
import com.kylix.submissionmade1.databinding.HomeFragmentBinding
import com.kylix.submissionmade1.util.ShowState
import org.koin.android.ext.android.getKoin
import org.koin.android.viewmodel.ViewModelParameter
import org.koin.android.viewmodel.koin.getViewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

class FavoriteFragment : Fragment(), ShowState {

    private inline fun<reified VM: ViewModel> Fragment.sharedGraphViewModel (
        @IdRes navGraphId: Int,
        qualifier: Qualifier? = null,
        noinline parameters: ParametersDefinition? = null
    ) = lazy {
        val store = findNavController().getViewModelStoreOwner(navGraphId).viewModelStore
        getKoin().getViewModel(ViewModelParameter(VM::class, qualifier, parameters, store))
    }

    private lateinit var favoriteBinding: FavoriteFragmentBinding
    private lateinit var favoriteAdapter: UserAdapter
    private val favoriteViewModel: FavoriteViewModel by sharedGraphViewModel(R.id.user_navigation)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.title = getString(R.string.favorite)
        favoriteBinding = FavoriteFragmentBinding.inflate(layoutInflater, container, false)

        loadKoinModules(favoriteModule)
        return favoriteBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favoriteAdapter = UserAdapter(arrayListOf()) {username, iv ->
            findNavController().navigate(
                FavoriteFragmentDirections.actionFavoriteFragmentToDetailFragment(username),
                FragmentNavigatorExtras(iv to username)
            )
        }

        favoriteBinding.recyclerFav.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = favoriteAdapter
        }

        observeDetail()
    }

    private fun observeDetail() {
        onLoadingState(favoriteFragmentBinding = favoriteBinding)
        favoriteViewModel.favoriteUsers.observe(viewLifecycleOwner, {
            it.let {
                if (!it.isNullOrEmpty()) {
                    onSuccessState(favoriteFragmentBinding = favoriteBinding)
                    favoriteAdapter.setData(it)
                } else {
                    onErrorState(
                        favoriteFragmentBinding = favoriteBinding,
                        message = resources.getString(R.string.not_have, "", resources.getString(R.string.favorite))
                    )
                }
            }
        })
    }

    override fun onSuccessState(homeFragmentBinding: HomeFragmentBinding?,
                                followFragmentBinding: FollowFragmentBinding?,
                                favoriteFragmentBinding: FavoriteFragmentBinding?) {
        favoriteFragmentBinding?.apply {
            errlayout.mainNotFound.visibility = View.GONE
            progress.stop()
            recyclerFav.visibility = View.VISIBLE
        }
    }

    override fun onLoadingState(homeFragmentBinding: HomeFragmentBinding?,
                                followFragmentBinding: FollowFragmentBinding?,
                                favoriteFragmentBinding: FavoriteFragmentBinding?) {
        favoriteFragmentBinding?.apply {
            errlayout.mainNotFound.visibility = View.GONE
            progress.start()
            progress.loadingColor = R.color.colorAccent
            recyclerFav.visibility = View.GONE
        }
    }

    override fun onErrorState(homeFragmentBinding: HomeFragmentBinding?,
                              followFragmentBinding: FollowFragmentBinding?,
                              favoriteFragmentBinding: FavoriteFragmentBinding?,
                              message: String?) {
        favoriteFragmentBinding?.apply {
            errlayout.apply {
                mainNotFound.visibility = View.VISIBLE
                emptyText.text = message ?: resources.getString(R.string.not_found)
            }
            progress.stop()
            recyclerFav.visibility = View.GONE
        }
    }
}