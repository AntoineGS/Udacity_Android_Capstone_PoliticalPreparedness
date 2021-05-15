package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch

class RepresentativeViewModel: ViewModel() {

    private val _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>>
        get() = _representatives

    private val _address = MutableLiveData<Address>()
    val address: LiveData<Address>
        get() = _address

    private val _showSnackBar = MutableLiveData<Boolean>()
    val showSnackBar: LiveData<Boolean>
        get() = _showSnackBar

    init {
        _showSnackBar.value = false
    }

    fun onSearchRepresentativesByAddress(address: Address) {
        _showSnackBar.value = false
        _address.value = address
        findMatchingRepresentatives()
    }

    private fun findMatchingRepresentatives() {
        if (_address.value != null) {
            viewModelScope.launch {
                try {
                    val addressString = _address.value!!.toFormattedString()
                    val representativeResponse =
                        CivicsApi.retrofitService.getRepresentatives(addressString)

                    _representatives.value = representativeResponse.offices.flatMap { office ->
                        office.getRepresentatives(representativeResponse.officials)
                    }
                } catch (ex: Exception) {
                    _showSnackBar.value = true
                    _representatives.value = ArrayList()
                }
            }
        } else {
            _representatives.value = ArrayList()
        }
    }

    fun snackBarShown(){
        _showSnackBar.value = false
    }
}
