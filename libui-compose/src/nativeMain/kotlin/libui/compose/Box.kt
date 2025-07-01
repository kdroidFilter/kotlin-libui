@file:Suppress("FunctionName")
@file:OptIn(ExperimentalForeignApi::class)

package libui.compose

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import cnames.structs.uiBox
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import libui.*

/**
 * A vertical box container that stacks its children vertically.
 *
 * @param padded Whether the box should have padding between its children.
 * @param enabled Whether the box is enabled.
 * @param visible Whether the box is visible.
 * @param content The content of the box.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun VBox(
    padded: Boolean = true,
    enabled: Boolean = true,
    visible: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(ctor = { uiNewVerticalBox()!! }, padded, enabled, visible, content)
}

/**
 * A horizontal box container that stacks its children horizontally.
 *
 * @param padded Whether the box should have padding between its children.
 * @param enabled Whether the box is enabled.
 * @param visible Whether the box is visible.
 * @param content The content of the box.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun HBox(
    padded: Boolean = true,
    enabled: Boolean = true,
    visible: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(ctor = { uiNewHorizontalBox()!! }, padded, enabled, visible, content)
}

/**
 * A box item that can be stretchy.
 *
 * @param stretchy Whether the box item should be stretchy.
 * @param content The content of the box item.
 */
@Composable
fun BoxItem(
    stretchy: Boolean = false,
    content: @Composable () -> Unit
) {
    val composer = currentComposer
    val applier = composer.applier

    if (applier is BoxApplier) {
        // Register the stretchy flag for the next item
        applier.registerNextStretchy(stretchy)

        // Execute the content
        content()
    } else {
        // If we're not in a Box, just execute the content
        content()
    }
}

/**
 * Internal implementation for box containers.
 *
 * @param ctor A function that creates the box container.
 * @param padded Whether the box should have padding between its children.
 * @param enabled Whether the box is enabled.
 * @param visible Whether the box is visible.
 * @param content The content of the box.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
private fun Box(
    ctor: () -> CPointer<uiBox>,
    padded: Boolean,
    enabled: Boolean,
    visible: Boolean,
    content: @Composable () -> Unit
) {
    val control = rememberControl { ctor() }

    handleChildren(content) { BoxApplier(control.ptr) }

    ComposeNode<CPointer<uiBox>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
            set(padded) { uiBoxSetPadded(this, if (it) 1 else 0) }
        }
    )
}

/**
 * An applier for box containers that handles adding and removing children.
 *
 * @param box The box container to apply changes to.
 */
class BoxApplier @OptIn(ExperimentalForeignApi::class) constructor(
    private val box: CPointer<uiBox>,
) : AppendDeleteApplier() {
    /**
     * List of stretchy flags for items to be appended, in order of registration.
     */
    private val pendingStretchy = mutableListOf<Boolean>()

    /**
     * Registers the stretchy flag for the next item to be appended.
     * 
     * @param stretchy Whether the next item should be stretchy.
     */
    fun registerNextStretchy(stretchy: Boolean) {
        pendingStretchy.add(stretchy)
    }

    /**
     * Deletes an item from the box at the specified index.
     *
     * @param index The index of the item to delete.
     */
    @OptIn(ExperimentalForeignApi::class)
    override fun deleteItem(index: Int) {
        uiBoxDelete(box, index)
    }

    /**
     * Appends an item to the box.
     *
     * @param instance The control to append.
     */
    override fun appendItem(instance: CPointer<uiControl>?) {
        // Get the stretchy flag for the current item or use default (false)
        val isStretchy = if (pendingStretchy.isNotEmpty()) pendingStretchy.removeAt(0) else false
        uiBoxAppend(box, instance, if (isStretchy) 1 else 0)
    }
}
