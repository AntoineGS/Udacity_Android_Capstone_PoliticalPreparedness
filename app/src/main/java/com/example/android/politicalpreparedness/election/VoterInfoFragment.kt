package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding

class VoterInfoFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val binding = FragmentVoterInfoBinding.inflate(inflater)
        val application = requireActivity().application
        val database = ElectionDatabase.getInstance(application).electionDao

        val args = VoterInfoFragmentArgs.fromBundle(requireArguments())
        val election = args.argElection
        val division = args.argDivision
        val viewModelFactory = VoterInfoViewModelFactory(election, division, database)
        val viewModel =
            ViewModelProvider(this, viewModelFactory).get(VoterInfoViewModel::class.java)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.voterInfoBallotInfo.observe(viewLifecycleOwner, { ballotInfo ->
            binding.stateBallot.isInvisible = ballotInfo == null
        })

        viewModel.voterInfoAddress.observe(viewLifecycleOwner, { address ->
            binding.addressGroup.isInvisible = address == null
        })

        viewModel.votingLocations.observe(viewLifecycleOwner, { votingLocations ->
            binding.stateLocations.isInvisible = votingLocations == null
        })

        binding.stateBallot.setOnClickListener {
            val url = viewModel.voterInfoBallotInfo.value
            if (url != null) {
                loadUrlIntent(url)
            }
        }

        binding.stateLocations.setOnClickListener {
            val url = viewModel.votingLocations.value
            if (url != null) {
                loadUrlIntent(url)
            }
        }

        viewModel.voterInfoIsSaved.observe(viewLifecycleOwner, { isStateSaved ->
            when (isStateSaved) {
                true -> {
                    binding.saveElectionButton.apply {
                        this.text = getString(R.string.delete_election)

                        setOnClickListener {
                            viewModel.deleteElectionById()
                            Toast.makeText(context, R.string.election_deleted, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
                false -> {
                    binding.saveElectionButton.apply {
                        this.text = getString(R.string.save_election)

                        setOnClickListener {
                            viewModel.saveElection()
                            Toast.makeText(context, R.string.election_saved, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        })
        return binding.root
    }

    private fun loadUrlIntent(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}