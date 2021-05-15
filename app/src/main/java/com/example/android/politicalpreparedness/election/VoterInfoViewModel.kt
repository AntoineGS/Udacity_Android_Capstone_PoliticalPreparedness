package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch

class VoterInfoViewModel(
    election: Election, division: Division, private val electionDao: ElectionDao
) : ViewModel() {

    private val _voterInfoBallotInfo = MutableLiveData<String>()
    val voterInfoBallotInfo: LiveData<String>
        get() = _voterInfoBallotInfo

    private val _voterInfoAddress = MutableLiveData<String>()
    val voterInfoAddress: LiveData<String>
        get() = _voterInfoAddress

    private val _voterInfoIsSaved = MutableLiveData<Boolean>()
    val voterInfoIsSaved: LiveData<Boolean>
        get() = _voterInfoIsSaved

    private val _selectedElection = MutableLiveData<Election>()
    val selectedElection: LiveData<Election>
        get() = _selectedElection

    private val _votingLocations = MutableLiveData<String>()
    val votingLocations: LiveData<String>
        get() = _votingLocations

    init {
        showListOfVoterInfo(election, division)

        viewModelScope.launch {
            val election = electionDao.getElectionById(election.id)
            _voterInfoIsSaved.value = election != null
        }
    }

    private fun showListOfVoterInfo(election: Election, division: Division) {
        viewModelScope.launch {
            _selectedElection.value = election
            val voterInfoAddress = division.state + ", " + division.country

            try {
                val voterInfoResponse =
                    CivicsApi.retrofitService.getVoterInfo(voterInfoAddress, election.id)

                voterInfoResponse.state?.let { state ->
                    if (state.isNotEmpty()) {
                        val electionAdministrationBody = state[0].electionAdministrationBody

                        electionAdministrationBody.let {
                            _votingLocations.value = it.votingLocationFinderUrl
                            _voterInfoBallotInfo.value = it.ballotInfoUrl
                            _voterInfoAddress.value = it.correspondenceAddress?.toFormattedString()
                        }
                    }
                }
            } catch (e: Exception) {
                // ignore the error, we assume there's nothing good to show
            }
        }
    }

    fun saveElection() {
        viewModelScope.launch {
            _selectedElection.value?.let { electionDao.insertElection(it) }
        }
        _voterInfoIsSaved.value = true
    }

    fun deleteElectionById() {
        viewModelScope.launch {
            _selectedElection.value?.let { electionDao.deleteElectionById(it.id) }
        }
        _voterInfoIsSaved.value = false
    }
}