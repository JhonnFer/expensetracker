package com.epn.expensetracker.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.epn.expensetracker.data.local.ExpenseEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    viewModel: ExpenseViewModel,
    onRecordatorioChange: (Boolean, Int, Int) -> Unit
) {
    val monto by viewModel.monto.collectAsState()
    val descripcion by viewModel.descripcion.collectAsState()
    val categoriaSeleccionada by viewModel.categoriaSeleccionada.collectAsState()
    val gastos by viewModel.gastos.collectAsState(initial = emptyList())
    val total by viewModel.total.collectAsState(initial = 0.0)

    val recordatorioActivo by viewModel.recordatorioActivo.collectAsState()
    val horaRecordatorio by viewModel.horaRecordatorio.collectAsState()
    val minutoRecordatorio by viewModel.minutoRecordatorio.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Tracker") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Formulario con lógica de Edición
            FormularioGasto(
                monto = monto,
                descripcion = descripcion,
                categoriaSeleccionada = categoriaSeleccionada,
                categorias = viewModel.categorias,
                esEdicion = viewModel.gastoAEditar != null,
                onMontoChange = { viewModel.actualizarMonto(it) },
                onDescripcionChange = { viewModel.actualizarDescripcion(it) },
                onCategoriaChange = { viewModel.seleccionarCategoria(it) },
                onGuardar = { viewModel.guardarGasto() },
                onCancelar = { viewModel.cancelarEdicion() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Configuración de Recordatorio
            ConfiguracionRecordatorio(
                activo = recordatorioActivo,
                hora = horaRecordatorio,
                minuto = minutoRecordatorio,
                onActivoChange = { nuevoEstado ->
                    viewModel.cambiarEstadoRecordatorio(nuevoEstado)
                    onRecordatorioChange(nuevoEstado, horaRecordatorio, minutoRecordatorio)
                },
                onHoraChange = { nuevaHora, nuevoMinuto ->
                    viewModel.actualizarHoraRecordatorio(nuevaHora, nuevoMinuto)
                    if (recordatorioActivo) {
                        onRecordatorioChange(true, nuevaHora, nuevoMinuto)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Resumen de Total
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Gastado:", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "$${String.format("%.2f", total ?: 0.0)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Historial con Clic para Editar
            Text("Historial de Gastos", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            if (gastos.isEmpty()) {
                Text("No hay registros", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                gastos.forEach { gasto ->
                    GastoItem(
                        gasto = gasto,
                        onEliminar = { viewModel.eliminarGasto(gasto) },
                        modifier = Modifier.clickable { viewModel.seleccionarParaEditar(gasto) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioGasto(
    monto: String,
    descripcion: String,
    categoriaSeleccionada: String,
    categorias: List<String>,
    esEdicion: Boolean,
    onMontoChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onCategoriaChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = if (esEdicion) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = if (esEdicion) "Modificar Registro" else "Nuevo Registro",
                style = MaterialTheme.typography.titleMedium,
                color = if (esEdicion) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = monto,
                onValueChange = onMontoChange,
                label = { Text("Monto ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = onDescripcionChange,
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = categoriaSeleccionada,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categorias.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = { onCategoriaChange(cat); expanded = false }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (esEdicion) {
                    OutlinedButton(onClick = onCancelar, modifier = Modifier.weight(1f)) {
                        Text("Cancelar")
                    }
                }
                Button(onClick = onGuardar, modifier = Modifier.weight(1f)) {
                    Text(if (esEdicion) "Actualizar" else "Guardar Gasto")
                }
            }
        }
    }
}

@Composable
fun GastoItem(
    gasto: ExpenseEntity,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fechaStr = remember(gasto.fecha) {
        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(gasto.fecha))
    }

    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(gasto.descripcion, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text("${gasto.categoria} • $fechaStr", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "$${String.format("%.2f", gasto.monto)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(onClick = { onEliminar() }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionRecordatorio(
    activo: Boolean,
    hora: Int,
    minuto: Int,
    onActivoChange: (Boolean) -> Unit,
    onHoraChange: (Int, Int) -> Unit
) {
    var mostrarTimePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Recordatorio diario", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Activar notificación", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = activo, onCheckedChange = onActivoChange)
            }

            if (activo) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { mostrarTimePicker = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hora del recordatorio", style = MaterialTheme.typography.bodyMedium)
                    val horaFormateada = String.format("%02d:%02d", hora, minuto)
                    Text(horaFormateada, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    if (mostrarTimePicker) {
        TimePickerDialog(
            horaInicial = hora,
            minutoInicial = minuto,
            onConfirm = { h, m -> onHoraChange(h, m); mostrarTimePicker = false },
            onDismiss = { mostrarTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    horaInicial: Int,
    minutoInicial: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(initialHour = horaInicial, initialMinute = minutoInicial, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar hora") },
        text = { TimePicker(state = timePickerState) },
        confirmButton = { TextButton(onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }) { Text("Aceptar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}