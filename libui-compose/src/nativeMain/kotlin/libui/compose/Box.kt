@file:Suppress("FunctionName")
@file:OptIn(ExperimentalForeignApi::class)

package libui.compose

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import cnames.structs.uiBox
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import libui.*

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

class BoxApplier @OptIn(ExperimentalForeignApi::class) constructor(
    private val box: CPointer<uiBox>,
) : AppendDeleteApplier() {
    @OptIn(ExperimentalForeignApi::class)
    override fun deleteItem(index: Int) {
        uiBoxDelete(box, index)
    }

    override fun appendItem(instance: CPointer<uiControl>?) {
        val isStretchy = false
        uiBoxAppend(box, instance, if (isStretchy) 1 else 0)
    }
}
