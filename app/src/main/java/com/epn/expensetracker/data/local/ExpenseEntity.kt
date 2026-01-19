package com.epn.expensetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity(tableName = "gastos")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val monto: Double,
    val descripcion: String,
    val categoria: String,
    val fecha: Long = System.currentTimeMillis()
)
