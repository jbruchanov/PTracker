package com.scurab.ptracker.ui.common

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.scurab.ptracker.ui.AppSizes

@Composable
fun WSpacer(width: Dp = AppSizes.current.Space, modifier: Modifier = Modifier) = Spacer(modifier = modifier.width(width))

@Composable
fun WSpacer2(modifier: Modifier = Modifier) = WSpacer(AppSizes.current.Space2, modifier = modifier)

@Composable
fun WSpacer4(modifier: Modifier = Modifier) = WSpacer(AppSizes.current.Space4, modifier = modifier)

@Composable
fun HSpacer(height: Dp = AppSizes.current.Space, modifier: Modifier = Modifier) = Spacer(modifier = modifier.height(height))

@Composable
fun HSpacer05(modifier: Modifier = Modifier) = HSpacer(AppSizes.current.Space05, modifier)

@Composable
fun HSpacer2(modifier: Modifier = Modifier) = HSpacer(AppSizes.current.Space2, modifier)

@Composable
fun HSpacer4(modifier: Modifier = Modifier) = HSpacer(AppSizes.current.Space4, modifier)

@Composable
fun RowScope.FSpacer(weight: Float = 1f, modifier: Modifier = Modifier) = Spacer(modifier = modifier.weight(weight))

@Composable
fun ColumnScope.FSpacer(weight: Float = 1f, modifier: Modifier = Modifier) = Spacer(modifier = modifier.weight(weight))
