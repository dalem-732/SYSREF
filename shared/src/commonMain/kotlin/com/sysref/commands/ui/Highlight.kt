package com.sysref.commands.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.sysref.commands.foldRu

@Composable
internal fun rememberHighlight(text: String, query: String, base: Color, hit: Color): AnnotatedString =
    remember(text, query, base, hit) { highlight(text, query, base, hit) }

internal fun highlight(text: String, query: String, base: Color, hit: Color): AnnotatedString = buildAnnotatedString {
    val needle = foldRu(query.trim())
    if (needle.isEmpty()) {
        withStyle(SpanStyle(color = base)) { append(text) }
        return@buildAnnotatedString
    }
    val hay = foldRu(text)
    var cursor = 0
    while (cursor < text.length) {
        val found = hay.indexOf(needle, cursor)
        if (found < 0) {
            withStyle(SpanStyle(color = base)) { append(text.substring(cursor)) }
            break
        }
        if (found > cursor) {
            withStyle(SpanStyle(color = base)) { append(text.substring(cursor, found)) }
        }
        val end = found + needle.length
        withStyle(SpanStyle(color = Color.Black, background = hit, fontWeight = FontWeight.Bold)) {
            append(text.substring(found, end))
        }
        cursor = end
    }
}
