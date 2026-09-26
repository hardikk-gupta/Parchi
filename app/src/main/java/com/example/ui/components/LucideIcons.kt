package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Custom vector definitions for Lucide Icons set to provide consistent, modern line-art icons.
 * Utilizes SVG path parser for exact 100% fidelity to standard Lucide icons.
 * Sets stroke color to Color.Black so Compose Icon tinting engine can colorize the strokes.
 */
object LucideIcons {

    private fun createLucideIcon(name: String, svgPathString: String): ImageVector {
        val nodes = PathParser().parsePathString(svgPathString).toNodes()
        return ImageVector.Builder(
            name = "Lucide.$name",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).addPath(
            pathData = nodes,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ).build()
    }

    val Mic: ImageVector by lazy {
        createLucideIcon(
            "Mic",
            "M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z M19 10v2a7 7 0 0 1-14 0v-2 M12 19v3"
        )
    }

    val Pause: ImageVector by lazy {
        createLucideIcon(
            "Pause",
            "M6 4h4v16H6z M14 4h4v16h-4z"
        )
    }

    val Receipt: ImageVector by lazy {
        createLucideIcon(
            "Receipt",
            "M4 2v20l2-1 2 1 2-1 2 1 2-1 2 1 2-1 2 1V2l-2 1-2-1-2 1-2-1-2 1-2-1-2 1Z M16 8H8 M16 12H8 M13 16H8"
        )
    }

    val TrendingUp: ImageVector by lazy {
        createLucideIcon(
            "TrendingUp",
            "M22 7l-8.5 8.5-5-5L1 18 M16 7h6v6"
        )
    }

    val Search: ImageVector by lazy {
        createLucideIcon(
            "Search",
            "M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16z M21 21l-4.35-4.35"
        )
    }

    val Store: ImageVector by lazy {
        createLucideIcon(
            "Store",
            "M2 22h20 M20 7l-2-5H6L4 7 M4 7v13 M20 7v13 M9 22v-6h6v6"
        )
    }

    val ArrowLeft: ImageVector by lazy {
        createLucideIcon(
            "ArrowLeft",
            "M19 12H5 M12 19l-7-7 7-7"
        )
    }

    val Edit: ImageVector by lazy {
        createLucideIcon(
            "Edit",
            "M12 20h9 M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"
        )
    }

    val Print: ImageVector by lazy {
        createLucideIcon(
            "Print",
            "M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2 M6 9V3h12v6 M6 14h12v8H6z"
        )
    }

    val Share: ImageVector by lazy {
        createLucideIcon(
            "Share",
            "M18 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6z M6 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6z M18 22a3 3 0 1 0 0-6 3 3 0 0 0 0 6z M8.59 13.51l6.83 3.98 M15.41 6.51l-6.82 3.98"
        )
    }

    val Close: ImageVector by lazy {
        createLucideIcon(
            "Close",
            "M18 6L6 18 M6 6l12 12"
        )
    }

    val Check: ImageVector by lazy {
        createLucideIcon(
            "Check",
            "M20 6L9 17l-5-5"
        )
    }

    val Trash: ImageVector by lazy {
        createLucideIcon(
            "Trash",
            "M3 6h18 M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"
        )
    }

    val Send: ImageVector by lazy {
        createLucideIcon(
            "Send",
            "M22 2L11 13 M22 2l-7 20-4-9-9-4 20-7z"
        )
    }
}
