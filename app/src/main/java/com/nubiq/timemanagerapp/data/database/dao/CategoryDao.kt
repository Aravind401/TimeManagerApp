package com.nubiq.timemanagerapp.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.nubiq.timemanagerapp.data.database.Category

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories ORDER BY name")
    fun getAllCategories(): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE name = :name")
    suspend fun getCategoryByName(name: String): Category?
}