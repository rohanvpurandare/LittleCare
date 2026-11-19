package com.littlecare.babytracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.littlecare.babytracker.export.ExportResult
import com.littlecare.babytracker.export.ReportExporter
import com.littlecare.babytracker.ui.theme.AccentFeed
import com.littlecare.babytracker.ui.theme.AccentGold
import com.littlecare.babytracker.ui.theme.AccentPee
import com.littlecare.babytracker.ui.theme.OledBlack
import com.littlecare.babytracker.ui.theme.OledBorder
import com.littlecare.babytracker.ui.theme.OledBorderHighlight
import com.littlecare.babytracker.ui.theme.OledSurface
import com.littlecare.babytracker.ui.theme.OledSurfaceElevated
import com.littlecare.babytracker.ui.theme.TextPrimary
import com.littlecare.babytracker.ui.theme.TextSecondary

@Composable
fun OledCard(
    modifier: Modifier = Modifier,
    borderColor: Color = OledBorder,
    glowColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    val backgroundBrush = if (glowColor != null) {
        Brush.verticalGradient(
            colors = listOf(
                glowColor.copy(alpha = 0.08f),
                OledSurface
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                OledSurfaceElevated.copy(alpha = 0.72f),
                OledSurface
            )
        )
    }

    val borderBrush = Brush.verticalGradient(
        colors = listOf(
            if (borderColor == OledBorder) OledBorderHighlight else borderColor.copy(alpha = 0.85f),
            borderColor.copy(alpha = 0.45f)
        )
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = shape,
        border = BorderStroke(1.dp, borderBrush)
    ) {
        Column(
            modifier = Modifier
                .background(backgroundBrush)
                .padding(18.dp),
            content = content
        )
    }
}

@Composable
fun LargeQuickActionButton(
    title: String,
    subtitle: String,
    lastLoggedBadge: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val shape = RoundedCornerShape(22.dp)

    Surface(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 84.dp),
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(
            width = 1.25.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.70f),
                    accentColor.copy(alpha = 0.25f)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.16f),
                            OledSurface
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(accentColor, accentColor.copy(alpha = 0.8f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = OledBlack,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(15.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Surface(
                        color = accentColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = lastLoggedBadge,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.5.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun ExportTriggerPill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 44.dp),
        shape = RoundedCornerShape(14.dp),
        color = AccentGold.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Download,
                contentDescription = "Download Excel or PDF",
                tint = AccentGold,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Export",
                color = AccentGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CircularRingProgress(
    progress: Float,
    centerText: String,
    accentColor: Color = AccentGold,
    size: Dp = 56.dp
) {
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = 5.dp.toPx()
            drawArc(
                color = OledBorderHighlight,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke)
            )
            drawArc(
                color = accentColor,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Text(
            text = centerText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportReportsBottomSheet(
    babyName: String,
    isExporting: Boolean,
    lastExportResult: ExportResult?,
    onExportExcel: () -> Unit,
    onExportPdf: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = OledSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Description,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Download Health Report",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Export ${babyName}'s logs, WHO growth & Indian vaccines",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (isExporting) {
                OledCard(borderColor = AccentGold) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CircularProgressIndicator(
                            color = AccentGold,
                            modifier = Modifier.size(26.dp),
                            strokeWidth = 3.dp
                        )
                        Text("Preparing your report...", color = TextPrimary, fontSize = 15.sp)
                    }
                }
            }

            Button(
                onClick = onExportExcel,
                enabled = !isExporting,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 66.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPee,
                    contentColor = OledBlack
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.TableChart, contentDescription = null, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Download Excel (.xlsx)", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "4 sheets: Summary, Daily Logs, Growth & Vaccines",
                            fontSize = 12.5.sp,
                            color = OledBlack.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Button(
                onClick = onExportPdf,
                enabled = !isExporting,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 66.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentFeed,
                    contentColor = OledBlack
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Download PDF Report (.pdf)", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Formatted multi-page summary for your paediatrician",
                            fontSize = 12.5.sp,
                            color = OledBlack.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            lastExportResult?.let { result ->
                OledCard(borderColor = AccentGold, glowColor = AccentGold) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = AccentPee,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = result.savedLocationMessage,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { ReportExporter.launchShareSheet(context, result) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, AccentGold)
                    ) {
                        Icon(Icons.Rounded.IosShare, contentDescription = null, tint = AccentGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open or Share via WhatsApp / Email",
                            color = AccentGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}
