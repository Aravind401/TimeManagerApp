package com.nubiq.timemanagerapp

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nubiq.timemanagerapp.data.database.Category
import com.nubiq.timemanagerapp.data.viewmodel.TimeViewModel
import com.nubiq.timemanagerapp.databinding.ActivitySettingsBinding
import kotlin.random.Random

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: TimeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TimeViewModel::class.java]

        setupToolbar()
        setupCategoryList()
        setupAddCategoryButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
    }

    private fun setupCategoryList() {
        viewModel.allCategories.observe(this) { categories ->
            val categoryNames = categories.map { it.name }
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                categoryNames
            )
            binding.lvCategories.adapter = adapter

            binding.lvCategories.setOnItemClickListener { _, _, position, _ ->
                val category = categories[position]
                showCategoryOptions(category)
            }
        }
    }

    private fun showCategoryOptions(category: Category) {
        if (category.isDefault) {
            // Default categories cannot be deleted
            MaterialAlertDialogBuilder(this)
                .setTitle(category.name)
                .setMessage("This is a default category and cannot be deleted.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(category.name)
            .setItems(arrayOf("Delete")) { _, which ->
                when (which) {
                    0 -> deleteCategory(category)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteCategory(category: Category) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete '${category.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteCategory(category)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupAddCategoryButton() {
        binding.btnAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun showAddCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val etCategoryName = dialogView.findViewById<android.widget.EditText>(R.id.etCategoryName)

        MaterialAlertDialogBuilder(this)
            .setTitle("Add New Category")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val categoryName = etCategoryName.text.toString().trim()
                if (categoryName.isNotEmpty()) {
                    val randomColor = Random.nextInt(0x1000000) or 0xFF000000.toInt()
                    val newCategory = Category(
                        name = categoryName,
                        color = randomColor,
                        isDefault = false
                    )
                    viewModel.addCategory(newCategory)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}