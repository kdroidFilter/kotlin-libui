@file:OptIn(ExperimentalForeignApi::class)

package libui.compose

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import cnames.structs.uiTab
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import libui.*


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

class TabApplier @OptIn(ExperimentalForeignApi::class) constructor(private val tab: CPointer<uiTab>) : AppendDeleteApplier() {
    @OptIn(ExperimentalForeignApi::class)
    override fun appendItem(instance: CPointer<uiControl>?) {
        val name = ""
        uiTabAppend(tab, name, instance)
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun deleteItem(index: Int) {
        uiTabDelete(tab, index)
    }

    override fun insertItemAt(index: Int, instance: CPointer<uiControl>?) {
        val name = ""
        uiTabInsertAt(tab, name, index, instance)
    }
}
