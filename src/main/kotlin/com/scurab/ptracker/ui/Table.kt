@file:OptIn(ExperimentalFoundationApi::class)

package com.scurab.ptracker.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.scurab.ptracker.model.Transaction
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

val dateFormat = DateTimeFormatter.ofPattern("dd/MM")



object Colors {
    val ScrollBarBackground = Color(0xFFEFEFEF)
}

@Composable
fun Table(items: List<Transaction>) {
    val vState = rememberLazyListState()
    val hState = rememberScrollState()
    val widths = remember { mutableStateListOf(100f, 100f, 100f, 100f, 100f) }
    Box() {
        Box(
            modifier = Modifier
                .horizontalScroll(hState)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = vState, modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxSize()
            ) {
                item {
                    Row {
                        Text("Symbol", modifier = Modifier.width(Dp(widths[0]) - 8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color.Red)
                                .width(16.dp)
                                .fillMaxHeight()
                                .draggable(rememberDraggableState {
                                    widths[0] += (it * 10)
                                }, orientation = Orientation.Vertical)
                        ) {
                            Text("xxx")
                        }
                        Text("Date")
                        Text("Amount")
                        Text("Fee")
                    }
                    Divider(Modifier.background(Color.Black).height(4.dp))
                }
                items(items.size) {
                    TableRow(items[it], widths)
                    Divider(color = Color.LightGray)
                }
            }
        }
        VerticalScrollbar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .background(Colors.ScrollBarBackground)
                .fillMaxHeight()
                .width(16.dp),
            adapter = ScrollbarAdapter(vState)
        )
        HorizontalScrollbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Colors.ScrollBarBackground)
                .height(16.dp)
                .fillMaxWidth(1f),
            adapter = ScrollbarAdapter(hState)
        )
    }
}

@Composable
fun TableRow(item: Transaction, widths: SnapshotStateList<Float>) {
    println("Bind:${item}")
    var background by remember { mutableStateOf(Color.White) }
    val scope = rememberCoroutineScope()
    val hoverSource = remember {
        MutableInteractionSource().also {
            scope.launch {
                it.interactions.collect {
                    println("${it::class.java.name} - $item")
                    when (it) {
                        is HoverInteraction.Enter -> background = Color.LightGray
                        is HoverInteraction.Exit -> background = Color.White
                    }
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .hoverable(hoverSource)
    ) {
        Cell(item.symbol, widths[0])
        VDivider()
        Cell(item.time.toString(), widths[1])
        VDivider()
        Cell(item.amount.toString(), widths[2])
        VDivider()
        Cell(item.fee.toString(), widths[3])
    }
}

@Composable
private fun VDivider() = Spacer(modifier = Modifier.width(4.dp))

@Composable
private fun Cell(text: String, width: Float) {
    SelectionContainer {
        Text(
            text, modifier = Modifier.width(Dp(width)), fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
    }
}