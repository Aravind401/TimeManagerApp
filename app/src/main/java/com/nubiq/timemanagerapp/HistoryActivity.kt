package com.nubiq.timemanagerapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
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
            val resultIntent = Intent().apply {
                putExtra("SELECTED_DATE", date)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
        binding.rvDates.layoutManager = LinearLayoutManager(this)
        binding.rvDates.adapter = adapter
    }

    private fun loadDates() {
        viewModel.allDates.observe(this) { dates ->
            dates?.let {
                adapter.submitList(it.sortedDescending())
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        setResult(RESULT_CANCELED)
        finish()
        return true
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }
}