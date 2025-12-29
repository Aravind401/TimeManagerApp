package com.nubiq.timemanagerapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nubiq.timemanagerapp.data.viewmodel.TimeViewModel
import com.nubiq.timemanagerapp.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: TimeViewModel
    private lateinit var adapter: HistoryDateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TimeViewModel::class.java]

        setupToolbar()
        setupRecyclerView()
        loadDates()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "History"
    }

    private fun setupRecyclerView() {
        adapter = HistoryDateAdapter { date ->
            // Return to MainActivity with selected date
            setResult(RESULT_OK)
            finish()
        }
        binding.rvDates.layoutManager = LinearLayoutManager(this)
        binding.rvDates.adapter = adapter
    }

    private fun loadDates() {
        // Use the property, not the method
        viewModel.allDates.observe(this) { dates ->
            adapter.submitList(dates.sortedDescending())
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}