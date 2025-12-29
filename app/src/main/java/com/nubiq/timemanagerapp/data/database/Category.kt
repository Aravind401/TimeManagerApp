package com.nubiq.timemanagerapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    val name: String,
    val color: Int, // Color in integer format
    val icon: String = "", // Icon resource name
    val isDefault: Boolean = false
)