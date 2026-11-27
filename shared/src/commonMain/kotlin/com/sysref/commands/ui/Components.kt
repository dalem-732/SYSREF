package com.sysref.commands.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sysref.commands.CommandEntry
import com.sysref.commands.categoryById
import com.sysref.commands.platformById
import com.sysref.commands.resources.Res
import com.sysref.commands.resources.app_tagline
import com.sysref.commands.resources.danger_description
import com.sysref.commands.resources.danger_mark
import com.sysref.commands.resources.db_count
import com.sysref.commands.resources.db_loading
import com.sysref.commands.resources.meta
import com.sysref.commands.resources.mode_browse
import com.sysref.commands.resources.mode_search
import com.sysref.commands.resources.star_add
import com.sysref.commands.resources.star_remove
import com.sysref.commands.resources.star_state_off
import com.sysref.commands.resources.star_state_on
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun Header(count: Int?) {
    val tagline = stringResource(Res.string.app_tagline)
    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Amber, fontWeight = FontWeight.Bold)) { append("SYSREF") }
                withStyle(SpanStyle(color = PhosphorDim)) { append("  "); append(tagline) }
            },
            fontFamily = Mono,
            fontSize = 15.sp,
        )
        Text(
            text = if (count == null) stringResource(Res.string.db_loading) else pluralStringResource(Res.plurals.db_count, count, count),
            color = PhosphorMuted,
            fontFamily = Mono,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
internal fun ModeSwitch(screen: Screen, onScreen: (Screen) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ModeKey(stringResource(Res.string.mode_browse), screen == Screen.BROWSE) { onScreen(Screen.BROWSE) }
        ModeKey(stringResource(Res.string.mode_search), screen == Screen.SEARCH) { onScreen(Screen.SEARCH) }
    }
}

@Composable
private fun ModeKey(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Amber else Color.Transparent
    val fg = if (selected) Color.Black else Phosphor
    Text(
        text = "[ $label ]",
        color = fg,
        fontFamily = Mono,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = Modifier
            .background(bg)
            .border(1.dp, if (selected) Amber else PhosphorDim)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    )
}

private val RULE = "-".repeat(80)

@Composable
internal fun Rule() {
    Text(
        text = RULE,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        color = Line,
        fontFamily = Mono,
        fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = 14.dp),
    )
}

@Composable
internal fun CenterNote(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = PhosphorMuted, fontFamily = Mono, fontSize = 14.sp)
    }
}

@Composable
internal fun CommandBlock(
    entry: CommandEntry,
    query: String,
    onCopy: (CommandEntry) -> Unit,
    favorite: Boolean,
    onToggleFavorite: () -> Unit,
) {
    Row(Modifier.fillMaxWidth()) {
        CommandText(entry, query, onCopy, Modifier.weight(1f))
        StarToggle(favorite = favorite, onToggle = onToggleFavorite)
    }
}

@Composable
private fun StarToggle(favorite: Boolean, onToggle: () -> Unit) {
    val description = stringResource(if (favorite) Res.string.star_remove else Res.string.star_add)
    val state = stringResource(if (favorite) Res.string.star_state_on else Res.string.star_state_off)
    Box(
        modifier = Modifier
            .size(48.dp)
            .toggleable(value = favorite, role = Role.Checkbox, onValueChange = { onToggle() })
            .semantics {
                contentDescription = description
                stateDescription = state
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (favorite) "★" else "☆",
            color = if (favorite) Amber else PhosphorDim,
            fontSize = 20.sp,
        )
    }
}

@Composable
private fun CommandText(
    entry: CommandEntry,
    query: String,
    onCopy: (CommandEntry) -> Unit,
    modifier: Modifier,
) {
    val meta = stringResource(
        Res.string.meta,
        stringResource(platformById(entry.platform).titleRes),
        stringResource(categoryById(entry.category).titleRes),
    )
    Column(
        modifier
            .clickable { onCopy(entry) }
            .padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
    ) {
        Text(
            text = rememberHighlight(entry.command, query, Phosphor, Amber),
            fontFamily = Mono,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            lineHeight = 20.sp,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = rememberHighlight(entry.description, query, PhosphorSoft, Amber),
            fontFamily = Mono,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = PhosphorSoft,
        )
        if (entry.danger) {
            DangerMark()
        }
        Text(
            text = rememberHighlight(meta, query, PhosphorMuted, Amber),
            fontFamily = Mono,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun DangerMark() {
    val description = stringResource(Res.string.danger_description)
    Text(
        text = stringResource(Res.string.danger_mark),
        color = Danger,
        fontFamily = Mono,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        modifier = Modifier
            .padding(top = 4.dp)
            .border(1.dp, Danger)
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .semantics { contentDescription = description },
    )
}

@Composable
internal fun BackLine(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = Amber,
        fontFamily = Mono,
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    )
}

@Composable
internal fun PathLine(path: String) {
    Text(
        text = path,
        color = PhosphorDim,
        fontFamily = Mono,
        fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
    )
}

@Composable
internal fun Hint(text: String) {
    Text(
        text = text,
        color = PhosphorDim,
        fontFamily = Mono,
        fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
    )
}
