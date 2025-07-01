package libui.compose

import androidx.compose.runtime.*
import kotlinx.cinterop.*
import libui.*
import cnames.structs.uiWindow

/**
 * Utility functions for displaying dialogs in libui-compose applications.
 */

// Global variable to store the main window pointer
// This is set by the Window composable
@OptIn(ExperimentalForeignApi::class)
private var mainWindowPtr: CPointer<uiWindow>? = null

/**
 * Sets the main window pointer for dialogs.
 * This should be called by the Window composable.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun setMainWindowPtr(ptr: CPointer<uiWindow>) {
    mainWindowPtr = ptr
}

/**
 * Displays a modal Open File Dialog.
 *
 * @return The selected file path, or null if the dialog was cancelled.
 */
@OptIn(ExperimentalForeignApi::class)
fun openFileDialog(): String? {
    val windowPtr = mainWindowPtr ?: return null
    val rawName = uiOpenFile(windowPtr)
    if (rawName == null) return null
    val strName = rawName.toKString()
    uiFreeText(rawName)
    return strName
}

/**
 * Displays a modal Open Folder Dialog.
 *
 * @return The selected folder path, or null if the dialog was cancelled.
 */
@OptIn(ExperimentalForeignApi::class)
fun openFolderDialog(): String? {
    val windowPtr = mainWindowPtr ?: return null
    val rawName = uiOpenFolder(windowPtr)
    if (rawName == null) return null
    val strName = rawName.toKString()
    uiFreeText(rawName)
    return strName
}

/**
 * Displays a modal Save File Dialog.
 *
 * @return The selected file path, or null if the dialog was cancelled.
 */
@OptIn(ExperimentalForeignApi::class)
fun saveFileDialog(): String? {
    val windowPtr = mainWindowPtr ?: return null
    val rawName = uiSaveFile(windowPtr)
    if (rawName == null) return null
    val strName = rawName.toKString()
    uiFreeText(rawName)
    return strName
}

/**
 * Displays a modal Message Box.
 *
 * @param text The text to display in the message box.
 * @param details Additional details to display in the message box.
 */
@OptIn(ExperimentalForeignApi::class)
fun msgBox(text: String, details: String = "") {
    val windowPtr = mainWindowPtr ?: return
    uiMsgBox(windowPtr, text, details)
}

/**
 * Displays a modal Error Message Box.
 *
 * @param text The text to display in the error message box.
 * @param details Additional details to display in the error message box.
 */
@OptIn(ExperimentalForeignApi::class)
fun msgBoxError(text: String, details: String = "") {
    val windowPtr = mainWindowPtr ?: return
    uiMsgBoxError(windowPtr, text, details)
}

/**
 * A composable function that provides a file dialog with a button and a text field.
 *
 * @param label The label for the form item.
 * @param buttonText The text to display on the button.
 * @param enabled Whether the components are enabled.
 * @param visible Whether the components are visible.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun FileDialogField(
    label: String = "File",
    buttonText: String = "Open File",
    enabled: Boolean = true,
    visible: Boolean = true
) {
    val text = remember { mutableStateOf("") }

    FormItem(label = label) {
        HBox {
            Button(
                text = buttonText,
                onClick = { 
                    val result = openFileDialog()
                    if (result != null) {
                        text.value = result
                    }
                },
                enabled = enabled,
                visible = visible
            )

            TextField(
                text = text,
                readOnly = true,
                enabled = enabled,
                visible = visible
            )
        }
    }
}

/**
 * A composable function that provides a folder dialog with a button and a text field.
 *
 * @param label The label for the form item.
 * @param buttonText The text to display on the button.
 * @param enabled Whether the components are enabled.
 * @param visible Whether the components are visible.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun FolderDialogField(
    label: String = "Folder",
    buttonText: String = "Open Folder",
    enabled: Boolean = true,
    visible: Boolean = true
) {
    val text = remember { mutableStateOf("") }

    FormItem(label = label) {
        HBox {
            Button(
                text = buttonText,
                onClick = { 
                    val result = openFolderDialog()
                    if (result != null) {
                        text.value = result
                    }
                },
                enabled = enabled,
                visible = visible
            )

            TextField(
                text = text,
                readOnly = true,
                enabled = enabled,
                visible = visible
            )
        }
    }
}

/**
 * A composable function that provides a save file dialog with a button and a text field.
 *
 * @param label The label for the form item.
 * @param buttonText The text to display on the button.
 * @param enabled Whether the components are enabled.
 * @param visible Whether the components are visible.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun SaveFileDialogField(
    label: String = "Save File",
    buttonText: String = "Save File",
    enabled: Boolean = true,
    visible: Boolean = true
) {
    val text = remember { mutableStateOf("") }

    FormItem(label = label) {
        HBox {
            Button(
                text = buttonText,
                onClick = { 
                    val result = saveFileDialog()
                    if (result != null) {
                        text.value = result
                    }
                },
                enabled = enabled,
                visible = visible
            )

            TextField(
                text = text,
                readOnly = true,
                enabled = enabled,
                visible = visible
            )
        }
    }
}

/**
 * A composable function that provides a button to display a message box.
 *
 * @param text The text to display on the button.
 * @param messageText The text to display in the message box.
 * @param messageDetails Additional details to display in the message box.
 * @param enabled Whether the button is enabled.
 * @param visible Whether the button is visible.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun MessageBoxButton(
    text: String = "Message Box",
    messageText: String = "This is a normal message box.",
    messageDetails: String = "More detailed information can be shown here.",
    enabled: Boolean = true,
    visible: Boolean = true
) {
    Button(
        text = text,
        onClick = { msgBox(messageText, messageDetails) },
        enabled = enabled,
        visible = visible
    )
}

/**
 * A composable function that provides a button to display an error message box.
 *
 * @param text The text to display on the button.
 * @param messageText The text to display in the error message box.
 * @param messageDetails Additional details to display in the error message box.
 * @param enabled Whether the button is enabled.
 * @param visible Whether the button is visible.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun ErrorMessageBoxButton(
    text: String = "Error Box",
    messageText: String = "This message box describes an error.",
    messageDetails: String = "More detailed information can be shown here.",
    enabled: Boolean = true,
    visible: Boolean = true
) {
    Button(
        text = text,
        onClick = { msgBoxError(messageText, messageDetails) },
        enabled = enabled,
        visible = visible
    )
}
