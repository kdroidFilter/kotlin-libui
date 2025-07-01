import androidx.compose.runtime.*
import libui.compose.*
import libui.uiQuit
import kotlinx.cinterop.*
import libui.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
fun main() = runLibUI {
    val state = remember { WindowState(SizeInt(320, 240)) }

    Window(
        onCloseRequest = { uiQuit() },
        state = state,
        title = "Date / Time",
        hasMenubar = false,
        margined = true
    ) {
        // We'll use VBox and HBox for layout
        VBox {
            // Create mutable states for the labels
            val labelBothText = remember { mutableStateOf("") }
            val labelDateText = remember { mutableStateOf("") }
            val labelTimeText = remember { mutableStateOf("") }

            // Create mutable states for the pickers' values
            val dateTimeValue = remember { mutableStateOf(0L) }
            val dateValue = remember { mutableStateOf(0L) }
            val timeValue = remember { mutableStateOf(0L) }

            // First row: DateTime picker
            BoxItem(stretchy = true) {
                // Extended DateTimePicker with value and onChange
                DateTimePickerWithValue(
                    value = dateTimeValue,
                    onChange = { 
                        // Update the label when the value changes
                        labelBothText.value = formatDateTime(dateTimeValue.value)
                    }
                )
            }

            // Second row: Date and Time pickers
            HBox {
                BoxItem(stretchy = true) {
                    // Extended DatePicker with value and onChange
                    DatePickerWithValue(
                        value = dateValue,
                        onChange = {
                            // Update the label when the value changes
                            labelDateText.value = formatDate(dateValue.value)
                        }
                    )
                }

                BoxItem(stretchy = true) {
                    // Extended TimePicker with value and onChange
                    TimePickerWithValue(
                        value = timeValue,
                        onChange = {
                            // Update the label when the value changes
                            labelTimeText.value = formatTime(timeValue.value)
                        }
                    )
                }
            }

            // Third row: Label for DateTime
            BoxItem {
                Label(labelBothText.value)
            }

            // Fourth row: Labels for Date and Time
            HBox {
                BoxItem(stretchy = true) {
                    Label(labelDateText.value)
                }

                BoxItem(stretchy = true) {
                    Label(labelTimeText.value)
                }
            }

            // Fifth row: Buttons
            HBox {
                BoxItem(stretchy = true) {
                    Button(
                        text = "Now",
                        onClick = {
                            // Get current time
                            val now = time(null)
                            // Update all pickers
                            dateTimeValue.value = now
                            dateValue.value = now
                            timeValue.value = now
                            // Trigger onChange callbacks
                            labelBothText.value = formatDateTime(now)
                            labelDateText.value = formatDate(now)
                            labelTimeText.value = formatTime(now)
                        }
                    )
                }

                BoxItem(stretchy = true) {
                    Button(
                        text = "Unix epoch",
                        onClick = {
                            // Set to Unix epoch (January 1, 1970)
                            val epoch = 0L
                            dateTimeValue.value = epoch
                            dateValue.value = epoch
                            timeValue.value = epoch
                            // Trigger onChange callbacks
                            labelBothText.value = formatDateTime(epoch)
                            labelDateText.value = formatDate(epoch)
                            labelTimeText.value = formatTime(epoch)
                        }
                    )
                }
            }
        }
    }
}

// Helper functions to format date and time
@OptIn(ExperimentalForeignApi::class)
fun formatDate(timestamp: Long): String = memScoped {
    val time = alloc<time_tVar>()
    time.value = timestamp
    val tm = localtime(time.ptr)!!
    val buf = allocArray<ByteVar>(64)
    strftime(buf, 64u, "%x", tm) // %x is the locale's date representation
    return buf.toKString()
}

@OptIn(ExperimentalForeignApi::class)
fun formatTime(timestamp: Long): String = memScoped {
    val time = alloc<time_tVar>()
    time.value = timestamp
    val tm = localtime(time.ptr)!!
    val buf = allocArray<ByteVar>(64)
    strftime(buf, 64u, "%X", tm) // %X is the locale's time representation
    return buf.toKString()
}

@OptIn(ExperimentalForeignApi::class)
fun formatDateTime(timestamp: Long): String = memScoped {
    val time = alloc<time_tVar>()
    time.value = timestamp
    val tm = localtime(time.ptr)!!
    val buf = allocArray<ByteVar>(64)
    strftime(buf, 64u, "%c", tm) // %c is the locale's date and time representation
    return buf.toKString()
}

// Extended DateTimePicker with value and onChange
@OptIn(ExperimentalForeignApi::class)
@Composable
fun DateTimePickerWithValue(
    value: MutableState<Long>,
    onChange: () -> Unit = {},
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    // Use the existing DateTimePicker
    DateTimePicker(enabled = enabled, visible = visible)

    // Update the UI when the value changes
    DisposableEffect(value.value) {
        onChange()
        onDispose { }
    }
}

// Extended DatePicker with value and onChange
@OptIn(ExperimentalForeignApi::class)
@Composable
fun DatePickerWithValue(
    value: MutableState<Long>,
    onChange: () -> Unit = {},
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    // Use the existing DatePicker
    DatePicker(enabled = enabled, visible = visible)

    // Update the UI when the value changes
    DisposableEffect(value.value) {
        onChange()
        onDispose { }
    }
}

// Extended TimePicker with value and onChange
@OptIn(ExperimentalForeignApi::class)
@Composable
fun TimePickerWithValue(
    value: MutableState<Long>,
    onChange: () -> Unit = {},
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    // Use the existing TimePicker
    TimePicker(enabled = enabled, visible = visible)

    // Update the UI when the value changes
    DisposableEffect(value.value) {
        onChange()
        onDispose { }
    }
}
