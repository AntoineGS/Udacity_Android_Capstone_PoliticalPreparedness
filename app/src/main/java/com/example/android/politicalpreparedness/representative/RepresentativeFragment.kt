package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.politicalpreparedness.BuildConfig
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class DetailFragment : Fragment() {

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val LOCATION_PERMISSION_INDEX = 0
    }

    private lateinit var viewModel: RepresentativeViewModel
    private lateinit var binding: FragmentRepresentativeBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        viewModel = ViewModelProvider(this).get(RepresentativeViewModel::class.java)

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_representative, container, false
        )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        //recycler
        val representativeAdapter = RepresentativeListAdapter()
        binding.representativesRecycler.adapter = representativeAdapter
        binding.representativesRecycler.layoutManager = LinearLayoutManager(requireContext())

        viewModel.representatives.observe(viewLifecycleOwner, { reps ->
            reps.let {
                representativeAdapter.submitList(it)
            }
        })

        viewModel.showSnackBar.observe(viewLifecycleOwner, { isShown ->
            if (isShown) {
                Snackbar.make(this.requireView(), R.string.no_addresses_found, Snackbar.LENGTH_LONG)
                    .setAction("YES") {
                        run {
                            binding.addressLine1.text.clear()
                            binding.addressLine2.text.clear()
                            binding.city.text.clear()
                            binding.zip.text.clear()
                        }
                    }.show()

                viewModel.snackBarShown()
            }
        })

        // spinner :O
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.states,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.state.adapter = adapter
        }

        binding.buttonSearch.setOnClickListener {
            hideKeyboard()
            val address = Address(
                binding.addressLine1.text.toString(),
                binding.addressLine2.text.toString(),
                binding.city.text.toString(),
                binding.state.selectedItem.toString(),
                binding.zip.text.toString()
            )

            viewModel.onSearchRepresentativesByAddress(address)
            showResultsTitle()
        }

        binding.buttonLocation.setOnClickListener {
            if (checkLocationPermissions()) {
                getLocation()
                showResultsTitle()
            }
        }

        return binding.root
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED
        ) {
            Snackbar.make(
                requireView(),
                R.string.permission_denied_by_user,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.settings) {
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }.show()
        } else {
            getLocation()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            true
        } else {
            requestLocationPermission()
            false
        }
    }

    private fun isPermissionGranted() : Boolean {
        return (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                )
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(){
        val location: Task<Location> = fusedLocationProviderClient.lastLocation
        location.addOnCompleteListener {
            if (it.isSuccessful) {
                val lastKnownLocation = it.result
                if (lastKnownLocation != null) {
                    val address = geoCodeLocation(lastKnownLocation)
                    viewModel.onSearchRepresentativesByAddress(address)
                }
            } else {
                Snackbar.make(requireView(), R.string.unable_to_get_location, Snackbar.LENGTH_LONG)
            }
        }
    }

    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
                .map { address ->
                    Address(address.thoroughfare, address.subThoroughfare, address.locality, address.adminArea, address.postalCode)
                }
                .first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun showResultsTitle() {
        binding.representativeTitle.visibility = View.VISIBLE
    }

}