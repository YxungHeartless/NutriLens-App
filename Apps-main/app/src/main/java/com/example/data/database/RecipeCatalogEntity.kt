package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_catalog")
data class RecipeCatalogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val ingredientsJson: String,
    val instructions: String = "",
    val aiGeneratedMacros: String
)
