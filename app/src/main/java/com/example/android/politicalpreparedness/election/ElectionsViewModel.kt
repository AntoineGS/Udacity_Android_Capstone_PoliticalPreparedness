package com.example.android.politicalpreparedness.election

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch

class ElectionsViewModel(application: Application,
                         electionDao: ElectionDao
): AndroidViewModel(application) {

    val upcomingElections = MutableLiveData<List<Election>>()
    val savedElections = electionDao.getAllElections()

    private val _navigateToSelectedElection = MutableLiveData<Election>()
    val navigateToSelectedElection: LiveData<Election>
        get() = _navigateToSelectedElection

    init {
        viewModelScope.launch {
            try {
                val result = CivicsApi.retrofitService.getElections().elections

                if (result.isNotEmpty()) {
                    upcomingElections.value = result
                }else {
                    upcomingElections.value = ArrayList()
                }
            } catch (e: Exception) {
                // on error init empty
                upcomingElections.value = ArrayList()
            }
        }
    }

    fun showElectionDetails(election: Election) {
        _navigateToSelectedElection.value = election
    }

    fun electionIsCompleted() {
        _navigateToSelectedElection.value = null
    }
}