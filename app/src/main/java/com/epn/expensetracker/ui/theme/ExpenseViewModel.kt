package com.epn.expensetracker.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.epn.expensetracker.data.local.ExpenseEntity
import com.epn.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val repository: ExpenseRepository,
    recordatorioActivoInicial: Boolean = true,
    horaRecordatorioInicial: Int = 21,
    minutoRecordatorioInicial: Int = 0
) : ViewModel() {

    // --- ESTADO PARA EL RETO 1 (ACTUALIZAR) ---
    var gastoAEditar by mutableStateOf<ExpenseEntity?>(null)
        private set

    // Estado del formulario
    private val _monto = MutableStateFlow("")
    val monto: StateFlow<String> = _monto.asStateFlow()

    private val _descripcion = MutableStateFlow("")
    val descripcion: StateFlow<String> = _descripcion.asStateFlow()

    private val _categoriaSeleccionada = MutableStateFlow("Comida")
    val categoriaSeleccionada: StateFlow<String> = _categoriaSeleccionada.asStateFlow()

    // Estado del recordatorio
    private val _recordatorioActivo = MutableStateFlow(recordatorioActivoInicial)
    val recordatorioActivo = _recordatorioActivo.asStateFlow()

    private val _horaRecordatorio = MutableStateFlow(horaRecordatorioInicial)
    val horaRecordatorio = _horaRecordatorio.asStateFlow()

    private val _minutoRecordatorio = MutableStateFlow(minutoRecordatorioInicial)
    val minutoRecordatorio = _minutoRecordatorio.asStateFlow()

    // Datos del repositorio
    val gastos = repository.todosLosGastos
    val total = repository.totalGeneral
    val categorias = listOf("Comida", "Transporte", "Entretenimiento", "Servicios", "Otros")

    // --- LÓGICA DE ACTUALIZACIÓN (RETO 1) ---

    fun seleccionarParaEditar(gasto: ExpenseEntity) {
        gastoAEditar = gasto
        _monto.value = gasto.monto.toString()
        _descripcion.value = gasto.descripcion
        _categoriaSeleccionada.value = gasto.categoria
    }

    fun cancelarEdicion() {
        gastoAEditar = null
        _monto.value = ""
        _descripcion.value = ""
        _categoriaSeleccionada.value = "Comida"
    }

    fun guardarGasto() {
        val montoDouble = _monto.value.toDoubleOrNull()
        if (montoDouble == null || montoDouble <= 0) return
        if (_descripcion.value.isBlank()) return

        viewModelScope.launch {
            if (gastoAEditar == null) {
                val nuevoGasto = ExpenseEntity(
                    monto = montoDouble,
                    descripcion = _descripcion.value.trim(),
                    categoria = _categoriaSeleccionada.value
                )
                repository.agregar(nuevoGasto)
            } else {
                val gastoActualizado = gastoAEditar!!.copy(
                    monto = montoDouble,
                    descripcion = _descripcion.value.trim(),
                    categoria = _categoriaSeleccionada.value
                )
                repository.actualizar(gastoActualizado)
            }
            cancelarEdicion()
        }
    }

    // Funciones de actualización de UI
    fun actualizarMonto(valor: String) {
        if (valor.isEmpty() || valor.matches(Regex("^\\d*\\.?\\d*$"))) {
            _monto.value = valor
        }
    }

    fun actualizarDescripcion(valor: String) { _descripcion.value = valor }
    fun seleccionarCategoria(categoria: String) { _categoriaSeleccionada.value = categoria }
    fun eliminarGasto(gasto: ExpenseEntity) { viewModelScope.launch { repository.eliminar(gasto) } }
    fun cambiarEstadoRecordatorio(activo: Boolean) { _recordatorioActivo.value = activo }
    fun actualizarHoraRecordatorio(hora: Int, minuto: Int) {
        _horaRecordatorio.value = hora
        _minutoRecordatorio.value = minuto
    }
}

/**
 * FACTORY: Necesaria para inyectar dependencias en el ViewModel
 */
class ExpenseViewModelFactory(
    private val repository: ExpenseRepository,
    private val recordatorioActivo: Boolean,
    private val horaRecordatorio: Int,
    private val minutoRecordatorio: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            return ExpenseViewModel(
                repository,
                recordatorioActivo,
                horaRecordatorio,
                minutoRecordatorio
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}