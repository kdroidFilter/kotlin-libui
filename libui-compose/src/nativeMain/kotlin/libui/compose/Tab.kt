@file:OptIn(ExperimentalForeignApi::class)

package libui.compose

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import cnames.structs.uiTab
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import libui.*


/**
 * A tab container that allows switching between different pages of content.
 * Each child component added to the TabPane will be placed in a separate tab.
 *
 * @param enabled Whether the tab pane is enabled.
 * @param visible Whether the tab pane is visible.
 * @param content The content of the tab pane.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun TabPane(
    enabled: Boolean = true,
    visible: Boolean = true,
    content: @Composable () -> Unit
) {
    val control = rememberControl { uiNewTab()!! }

    handleChildren(content) { TabApplier(control.ptr) }

    ComposeNode<CPointer<uiTab>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
        }
    )
}

/**
 * An applier for tab containers that handles adding and removing children.
 *
 * @param tab The tab container to apply changes to.
 */
class TabApplier @OptIn(ExperimentalForeignApi::class) constructor(private val tab: CPointer<uiTab>) : AppendDeleteApplier() {
    /**
     * Appends an item to the tab container.
     *
     * @param instance The control to append.
     */
    @OptIn(ExperimentalForeignApi::class)
    override fun appendItem(instance: CPointer<uiControl>?) {
        val name = ""
        uiTabAppend(tab, name, instance)
    }

    /**
     * Deletes an item from the tab container at the specified index.
     *
     * @param index The index of the item to delete.
     */
    @OptIn(ExperimentalForeignApi::class)
    override fun deleteItem(index: Int) {
        uiTabDelete(tab, index)
    }

    /**
     * Inserts an item into the tab container at the specified index.
     *
     * @param index The index at which to insert the item.
     * @param instance The control to insert.
     */
    override fun insertItemAt(index: Int, instance: CPointer<uiControl>?) {
        val name = ""
        uiTabInsertAt(tab, name, index, instance)
    }
}
