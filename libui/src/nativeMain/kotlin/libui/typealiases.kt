package libui

import kotlinx.cinterop.*
import cnames.structs.*
import platform.posix.tm

// Typealias definitions for opaque types
typealias uiDrawTextLayout = cnames.structs.uiDrawTextLayout
typealias uiAttribute = cnames.structs.uiAttribute
typealias uiAttributedString = cnames.structs.uiAttributedString
typealias uiOpenTypeFeatures = cnames.structs.uiOpenTypeFeatures
typealias uiMenuItem = cnames.structs.uiMenuItem
typealias uiMenu = cnames.structs.uiMenu
typealias uiDrawContext = cnames.structs.uiDrawContext
typealias uiButton = cnames.structs.uiButton
typealias uiWindow = cnames.structs.uiWindow

// Map platform.posix.tm to cnames.structs.tm
@OptIn(ExperimentalForeignApi::class)
fun CPointer<platform.posix.tm>.toCnamesStructsTm(): CValuesRef<cnames.structs.tm> = this.reinterpret()
