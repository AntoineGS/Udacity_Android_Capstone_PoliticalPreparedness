package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener

class ElectionsFragment: Fragment() {

    private lateinit var viewModel: ElectionsViewModel
    private lateinit var binding: FragmentElectionBinding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val application = requireActivity().application
        val database = ElectionDatabase.getInstance(application).electionDao
        val viewModelFactory = ElectionsViewModelFactory(application, database)

        viewModel =
            ViewModelProvider(this, viewModelFactory).get(ElectionsViewModel::class.java)

        binding = FragmentElectionBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Upcoming Elections
        val upcomingElectionListAdapter = ElectionListAdapter(ElectionListener {
            viewModel.showElectionDetails(it)
        })

        viewModel.upcomingElections.observe(viewLifecycleOwner, { elections ->
            elections?.let {
                upcomingElectionListAdapter.submitList(it)
            }
        })

        binding.upcomingElectionsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.upcomingElectionsRecycler.adapter = upcomingElectionListAdapter

        // Saved Elections
        val savedElectionListAdapter = ElectionListAdapter(ElectionListener {
            viewModel.showElectionDetails(it)
        })

        viewModel.savedElections.observe(viewLifecycleOwner, { elections ->
            elections?.let {
                if (it.isEmpty()) {
                    setSavedListVisibility(View.INVISIBLE)
                } else {
                    setSavedListVisibility(View.VISIBLE)
                    savedElectionListAdapter.submitList(it)
                }
            }
        })

        binding.savedElectionsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.savedElectionsRecycler.adapter = savedElectionListAdapter


        // General
        viewModel.navigateToSelectedElection.observe(viewLifecycleOwner, { election ->
            if (election != null) {
                this.findNavController().navigate(
                    ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(
                        election, election.division
                    )
                )
                viewModel.electionIsCompleted()
            }
        })

        return binding.root
    }

    private fun setSavedListVisibility(visibility: Int) {
        binding.savedElectionsRecycler.visibility = visibility
        binding.savedElectionsTitle.visibility = visibility
    }
}