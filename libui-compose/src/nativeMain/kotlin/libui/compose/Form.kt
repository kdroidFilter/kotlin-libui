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
    val applier = composer.applier as FormApplier

    // Set the label and stretchy flag for the next item
    applier.nextLabel = label
    applier.nextStretchy = stretchy

    content()
}


/**
 * An applier for form containers that handles adding and removing children.
 *
 * @param form The form container to apply changes to.
 */
class FormApplier @OptIn(ExperimentalForeignApi::class) constructor(private val form: CPointer<uiForm>) : AppendDeleteApplier() {
    /**
     * The label for the next item to be appended.
     */
    var nextLabel: String = ""

    /**
     * Whether the next item to be appended should be stretchy.
     */
    var nextStretchy: Boolean = false

    /**
     * Appends an item to the form container.
     *
     * @param instance The control to append.
     */
    @ExperimentalForeignApi
    override fun appendItem(instance: CPointer<uiControl>?) {
        uiFormAppend(form, nextLabel, instance, if (nextStretchy) 1 else 0)
        // Reset for next item
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
