import androidx.compose.runtime.*
import libui.compose.*
import libui.uiQuit
import kotlinx.cinterop.ExperimentalForeignApi
import cnames.structs.uiWindow

@OptIn(ExperimentalForeignApi::class)
fun main() = runLibUI {
    val state = remember { WindowState(SizeInt(640, 480)) }

    Window(
        onCloseRequest = { uiQuit() },
        state = state,
        title = "LibUI Compose Control Gallery",
        hasMenubar = false,
        margined = true
    ) {
        // We'll use a TabPane to organize the controls
        TabPane {
            // Basic Controls Tab
            TabItem("Basic Controls") {
                VBox {
                    HBox {
                        Button(
                            text = "Button",
                            onClick = {}
                        )

                        Checkbox(
                            label = "Checkbox",
                            checked = remember { mutableStateOf(false) }
                        )
                    }

                    Label("This is a label. Right now, labels can only span one line.")

                    HorizontalSeparator()

                    Group(title = "Entries") {
                        Form {
                            FormItem(label = "Text Field") {
                                TextField(
                                    text = remember { mutableStateOf("") }
                                )
                            }

                            FormItem(label = "Password Field") {
                                PasswordField(
                                    text = remember { mutableStateOf("") }
                                )
                            }

                            FormItem(label = "Search Field") {
                                SearchField(
                                    text = remember { mutableStateOf("") }
                                )
                            }

                            FormItem(label = "Multiline Field") {
                                MultilineEntry(
                                    text = remember { mutableStateOf("") },
                                    readOnly = false
                                )
                            }

                            FormItem(label = "Multiline Field No Wrap") {
                                NonWrappingMultilineEntry(
                                    text = remember { mutableStateOf("") },
                                    readOnly = false
                                )
                            }
                        }
                    }
                }
            }

            // Numbers and Lists Tab
            TabItem("Numbers and Lists") {
                HBox {
                    BoxItem(stretchy = true) {
                        Group(title = "Numbers") {
                            VBox {
                                val value = remember { mutableStateOf(0) }

                                Spinbox(
                                    value = value,
                                    min = 0,
                                    max = 100
                                )

                                Slider(
                                    value = value,
                                    min = 0,
                                    max = 100
                                )

                                ProgressBar(value = value.value)

                                ProgressBar() // Indeterminate
                            }
                        }
                    }

                    BoxItem(stretchy = true) {
                        Group(title = "Lists") {
                            VBox {
                                val selectedCombobox = remember { mutableStateOf(0) }
                                val comboboxItems = remember { listOf("Combobox Item 1", "Combobox Item 2", "Combobox Item 3") }

                                Combobox(
                                    selected = selectedCombobox,
                                    items = comboboxItems
                                )

                                val editableText = remember { mutableStateOf("") }
                                val editableItems = remember { listOf("Editable Item 1", "Editable Item 2", "Editable Item 3") }

                                EditableCombobox(
                                    text = editableText,
                                    items = editableItems
                                )

                                val selectedRadio = remember { mutableStateOf(0) }
                                val radioOptions = remember { listOf("Radio Button 1", "Radio Button 2", "Radio Button 3") }

                                RadioButtons(
                                    selected = selectedRadio,
                                    options = radioOptions
                                )
                            }
                        }
                    }
                }
            }

            // Data Choosers Tab
            TabItem("Data Choosers") {
                HBox {
                    // Left column - Date/Time pickers
                    BoxItem(stretchy = true) {
                        VBox {
                            DatePicker()
                            TimePicker()
                            DateTimePicker()
                            FontButton()
                            ColorButton(color = remember { mutableStateOf(Color(0.0, 0.0, 0.0)) })
                        }
                    }

                    // Separator
                    HorizontalSeparator()

                    // Right column - File dialogs and message boxes
                    BoxItem(stretchy = true) {
                        VBox {
                            // File dialogs
                            Form {
                                FileDialogField(
                                    label = "Open File",
                                    buttonText = "Open File"
                                )

                                FolderDialogField(
                                    label = "Open Folder",
                                    buttonText = "Open Folder"
                                )

                                SaveFileDialogField(
                                    label = "Save File",
                                    buttonText = "Save File"
                                )
                            }

                            // Message boxes
                            HBox {
                                MessageBoxButton(
                                    text = "Message Box",
                                    messageText = "This is a normal message box.",
                                    messageDetails = "More detailed information can be shown here."
                                )

                                ErrorMessageBoxButton(
                                    text = "Error Box",
                                    messageText = "This message box describes an error.",
                                    messageDetails = "More detailed information can be shown here."
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
