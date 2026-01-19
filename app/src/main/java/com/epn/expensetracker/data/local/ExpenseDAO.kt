package com.epn.expensetracker.data.local
import com.epn.expensetracker.data.local.ExpenseEntity
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao{
    @Query("SELECT * FROM gastos ORDER BY fecha DESC")
    fun obtenerTodos(): Flow<List<ExpenseEntity>>


    @Query( "SELECT SUM(monto) FROM gastos WHERE categoria = :categoria " )
            fun totalPorCategoria (categoria: String): Flow<Double?>

    @Query("SELECT SUM (monto)FROM gastos")
    fun totalGeneral(): Flow<Double?>

    @Insert
    suspend fun insertar(gasto: ExpenseEntity)

    @Update
    suspend fun actualizar(gasto: ExpenseEntity)

    @Delete
    suspend fun eliminar(gasto: ExpenseEntity)
}