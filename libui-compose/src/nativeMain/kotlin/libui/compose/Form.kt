package libui.compose

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import cnames.structs.uiForm
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import libui.*


/**
 * A form container that arranges its children in a label-control pattern.
 * Typically used for settings or data entry forms.
 *
 * @param padded Whether the form should have padding between its children.
 * @param enabled Whether the form is enabled.
 * @param visible Whether the form is visible.
 * @param content The content of the form.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun Form(
    padded: Boolean = true,
    enabled: Boolean = true,
    visible: Boolean = true,
    content: @Composable () -> Unit
) {
    val control = rememberControl { uiNewForm()!! }

    handleChildren(content) { FormApplier(control.ptr) }

    ComposeNode<CPointer<uiForm>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
            set(padded) { uiFormSetPadded(this, if (it) 1 else 0) }
        }
    )
}

/**
 * A form item that has a label and a control.
 *
 * @param label The label for the form item.
 * @param stretchy Whether the form item should be stretchy.
 * @param content The content of the form item.
 */
@Composable
fun FormItem(
    label: String,
    stretchy: Boolean = false,
    content: @Composable () -> Unit
) {
    val composer = currentComposer
    val applier = composer.applier

    if (applier is FormApplier) {
        // Register the label and stretchy flag for the next item
        applier.registerNextLabel(label)
        applier.registerNextStretchy(stretchy)

        // Execute the content
        content()
    } else {
        // If we're not in a Form, just execute the content
        content()
    }
}


/**
 * An applier for form containers that handles adding and removing children.
 *
 * @param form The form container to apply changes to.
 */
class FormApplier @OptIn(ExperimentalForeignApi::class) constructor(private val form: CPointer<uiForm>) : AppendDeleteApplier() {
    /**
     * List of labels for items to be appended, in order of registration.
     */
    private val pendingLabels = mutableListOf<String>()

    /**
     * List of stretchy flags for items to be appended, in order of registration.
     */
    private val pendingStretchy = mutableListOf<Boolean>()

    /**
     * The label for the next item to be appended.
     * This is kept for backward compatibility.
     */
    var nextLabel: String = ""
        set(value) {
            field = value
            pendingLabels.add(value)
        }

    /**
     * Whether the next item to be appended should be stretchy.
     * This is kept for backward compatibility.
     */
    var nextStretchy: Boolean = false
        set(value) {
            field = value
            pendingStretchy.add(value)
        }

    /**
     * Registers the label for the next item to be appended.
     * 
     * @param label The label for the next item.
     */
    fun registerNextLabel(label: String) {
        pendingLabels.add(label)
    }

    /**
     * Registers the stretchy flag for the next item to be appended.
     * 
     * @param stretchy Whether the next item should be stretchy.
     */
    fun registerNextStretchy(stretchy: Boolean) {
        pendingStretchy.add(stretchy)
    }

    /**
     * Appends an item to the form container.
     *
     * @param instance The control to append.
     */
    @ExperimentalForeignApi
    override fun appendItem(instance: CPointer<uiControl>?) {
        // Get the label and stretchy flag for the current item or use defaults
        val label = if (pendingLabels.isNotEmpty()) pendingLabels.removeAt(0) else ""
        val stretchy = if (pendingStretchy.isNotEmpty()) pendingStretchy.removeAt(0) else false

        uiFormAppend(form, label, instance, if (stretchy) 1 else 0)

        // Reset for next item (for backward compatibility)
        nextLabel = ""
        nextStretchy = false
    }

    /**
     * Deletes an item from the form container at the specified index.
     *
     * @param index The index of the item to delete.
     */
    @OptIn(ExperimentalForeignApi::class)
    override fun deleteItem(index: Int) {
        uiFormDelete(form, index)
    }
}
