package com.example.shoecollectionapp.ui.theme.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.shoecollectionapp.data.Chaussure
import com.example.shoecollectionapp.data.Etiquette
import com.example.shoecollectionapp.data.Piece
import com.example.shoecollectionapp.utils.GridUtils
import java.io.File
import kotlin.math.atan2
import kotlin.math.roundToInt

@Composable
fun CollectionScreen(
    pieces: List<Piece>,
    etiquettes: List<Etiquette>,
    chaussures: List<Chaussure>,
    allChaussures: List<Chaussure>,
    selectedPieceId: Long?,
    searchQuery: String,
    selectedBrandFilter: String?,
    onSelectPiece: (Long) -> Unit,
    onOpenAddPiece: () -> Unit,
    onOpenEditPiece: (Piece) -> Unit,
    onOpenAddEtiquette: (xDp: Float, yDp: Float) -> Unit,
    onEditEtiquette: (Etiquette) -> Unit,
    onOpenAddChaussure: (col: Int, row: Int) -> Unit,
    onOpenEditChaussure: (Chaussure) -> Unit,
    onToggleFavorite: (Chaussure) -> Unit,
    onDeleteChaussure: (Long) -> Unit,
    onUpdatePosition: (Long, Int, Int, Long?) -> Unit,
    onUpdateEtiquettePosition: (Long, Float, Float) -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onSelectBrandFilter: (String?) -> Unit
) {
    // ID de la boîte actuellement en cours de glissement
    var draggingShoeId by remember { mutableStateOf<Long?>(null) }

    var inspectedChaussure by remember { mutableStateOf<Chaussure?>(null) }
    var isHeaderExpanded by remember { mutableStateOf(true) }
    var isEtiquetteDropdownExpanded by remember { mutableStateOf(false) }

    // Déplacement de la caméra (Pan)
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Dimensions d'un bloc de la grille : 110dp de largeur x 70dp de hauteur
    val boxWidthDp = GridUtils.CELL_W_DP.dp
    val boxHeightDp = GridUtils.CELL_H_DP.dp

    val density = LocalDensity.current
    val dpToPx = density.density
    val boxWidthPx = with(density) { boxWidthDp.toPx() }
    val boxHeightPx = with(density) { boxHeightDp.toPx() }

    // Grille totale 25 x 25
    val maxCols = GridUtils.COLS
    val maxRows = GridUtils.ROWS
    val totalGridWidthPx = boxWidthPx * maxCols
    val totalGridHeightPx = boxHeightPx * maxRows

    // Source de vérité synchrone en mémoire pour la grille (col, row) de chaque chaussure
    val gridPositions = remember(selectedPieceId) { mutableStateMapOf<Long, Pair<Int, Int>>() }
    LaunchedEffect(chaussures, selectedPieceId) {
        chaussures.forEach { shoe ->
            if (!gridPositions.containsKey(shoe.id)) {
                gridPositions[shoe.id] = Pair(shoe.gridCol, shoe.gridRow)
            }
        }
    }

    // Marges de clamp des étiquettes
    val tagMarginXPx = with(density) { 100.dp.toPx() }
    val tagMarginYPx = with(density) { 50.dp.toPx() }

    // Filtrer les marques uniquement parmi les chaussures présentes dans la pièce active
    val availableBrands = chaussures
        .map { it.marque.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

    // Dialog d'inspection de la boîte (Consultation haut de gamme & adaptative)
    inspectedChaussure?.let { shoe ->
        val currentShoe = chaussures.find { it.id == shoe.id } ?: shoe

        AlertDialog(
            onDismissRequest = { inspectedChaussure = null },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Text(
                    text = currentShoe.nom,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Carte héro adaptative pour la photo
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        tonalElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 180.dp, max = 250.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            AsyncImage(
                                model = File(currentShoe.photoPath),
                                contentDescription = currentShoe.nom,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (currentShoe.marque.isNotBlank()) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentShoe.marque,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (currentShoe.description.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = currentShoe.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                onToggleFavorite(currentShoe)
                                inspectedChaussure = currentShoe.copy(isFavorite = !currentShoe.isFavorite)
                            },
                            shape = CircleShape,
                            colors = if (currentShoe.isFavorite) {
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            } else {
                                ButtonDefaults.outlinedButtonColors()
                            },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Icon(
                                imageVector = if (currentShoe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Favori", maxLines = 1, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                inspectedChaussure = null
                                onOpenEditChaussure(currentShoe)
                            },
                            shape = CircleShape
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Modifier", maxLines = 1, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = { inspectedChaussure = null }) {
                        Text("Fermer", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }

        // Case située au centre de la vue courante
        fun calculateViewportCenterCell(): Pair<Int, Int> {
            val viewCenterX = -panOffsetX + (screenWidthPx / 2f) - (boxWidthPx / 2f)
            val viewCenterY = -panOffsetY + (screenHeightPx / 2f) - (boxHeightPx / 2f)
            val col = (viewCenterX / boxWidthPx).roundToInt().coerceIn(0, maxCols - 1)
            val row = (viewCenterY / boxHeightPx).roundToInt().coerceIn(0, maxRows - 1)
            return Pair(col, row)
        }

        // --- ESPACE 2D BORNÉ (25x25) ---
        if (selectedPieceId != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            panOffsetX = (panOffsetX + dragAmount.x).coerceIn(-totalGridWidthPx + 300f, 300f)
                            panOffsetY = (panOffsetY + dragAmount.y).coerceIn(-totalGridHeightPx + 300f, 300f)
                        }
                    }
            ) {
                // Grille exacte bornée 25 x 25 avec lignes blanches bien visibles
                val gridColor = Color.White.copy(alpha = 0.35f)
                val canvasModifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        translationX = panOffsetX,
                        translationY = panOffsetY
                    )

                Canvas(modifier = canvasModifier) {

                    var x = 0f
                    while (x <= totalGridWidthPx) {
                        drawLine(gridColor, Offset(x, 0f), Offset(x, totalGridHeightPx), strokeWidth = 1.5f)
                        x += boxWidthPx
                    }
                    var y = 0f
                    while (y <= totalGridHeightPx) {
                        drawLine(gridColor, Offset(0f, y), Offset(totalGridWidthPx, y), strokeWidth = 1.5f)
                        y += boxHeightPx
                    }
                }

                // Conteneur des éléments
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            translationX = panOffsetX,
                            translationY = panOffsetY
                        )
                ) {
                    // --- 1. BOÎTES DE CHAUSSURES (SWAP parfait et synchrone) ---
                    chaussures.forEach { shoe ->
                        val currentCell = gridPositions[shoe.id] ?: Pair(shoe.gridCol, shoe.gridRow)
                        var offsetX by remember(shoe.id) { mutableFloatStateOf(currentCell.first * boxWidthPx) }
                        var offsetY by remember(shoe.id) { mutableFloatStateOf(currentCell.second * boxHeightPx) }

                        var startCell by remember(shoe.id) { mutableStateOf(currentCell) }

                        // Synchronisation avec la grille en mémoire
                        LaunchedEffect(currentCell, boxWidthPx, boxHeightPx) {
                            if (draggingShoeId != shoe.id) {
                                offsetX = currentCell.first * boxWidthPx
                                offsetY = currentCell.second * boxHeightPx
                            }
                        }

                        val matchesFilter = (searchQuery.isBlank() ||
                                shoe.nom.contains(searchQuery, ignoreCase = true) ||
                                shoe.marque.contains(searchQuery, ignoreCase = true) ||
                                shoe.description.contains(searchQuery, ignoreCase = true)) &&
                                (selectedBrandFilter == null || shoe.marque.equals(selectedBrandFilter, ignoreCase = true))

                        val blockAlpha = if (matchesFilter) 1f else 0.25f
                        val isFiltering = searchQuery.isNotBlank() || selectedBrandFilter != null
                        val blockBorder = if (isFiltering && matchesFilter) {
                            BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                        } else if (!matchesFilter) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        } else {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        }

                        Card(
                            modifier = Modifier
                                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                                .size(width = boxWidthDp, height = boxHeightDp)
                                .alpha(blockAlpha)
                                .border(blockBorder, RoundedCornerShape(0.dp))
                                .pointerInput(shoe.id) {
                                    detectDragGestures(
                                        onDragStart = {
                                            draggingShoeId = shoe.id
                                            startCell = gridPositions[shoe.id] ?: Pair(shoe.gridCol, shoe.gridRow)
                                        },
                                        onDragEnd = {
                                            val targetCol = (offsetX / boxWidthPx).roundToInt().coerceIn(0, maxCols - 1)
                                            val targetRow = (offsetY / boxHeightPx).roundToInt().coerceIn(0, maxRows - 1)

                                            // Recherche de l'occupant de la case cible directement dans la carte mémoire synchrone
                                            val occupantId = gridPositions.entries.find { (id, cell) ->
                                                id != shoe.id && cell.first == targetCol && cell.second == targetRow
                                            }?.key

                                            if (occupantId != null) {
                                                // SWAP SYNCHRONE PARFAIT
                                                gridPositions[shoe.id] = Pair(targetCol, targetRow)
                                                gridPositions[occupantId] = startCell

                                                offsetX = targetCol * boxWidthPx
                                                offsetY = targetRow * boxHeightPx

                                                onUpdatePosition(shoe.id, targetCol, targetRow, shoe.idSection)
                                                onUpdatePosition(occupantId, startCell.first, startCell.second, null)
                                            } else {
                                                // CASE LIBRE
                                                gridPositions[shoe.id] = Pair(targetCol, targetRow)

                                                offsetX = targetCol * boxWidthPx
                                                offsetY = targetRow * boxHeightPx

                                                onUpdatePosition(shoe.id, targetCol, targetRow, shoe.idSection)
                                            }
                                            draggingShoeId = null
                                        },
                                        onDragCancel = {
                                            draggingShoeId = null
                                        }
                                    ) { change, dragAmount ->
                                        change.consume()
                                        val maxAllowedX = totalGridWidthPx - boxWidthPx
                                        val maxAllowedY = totalGridHeightPx - boxHeightPx
                                        offsetX = (offsetX + dragAmount.x).coerceIn(0f, maxAllowedX)
                                        offsetY = (offsetY + dragAmount.y).coerceIn(0f, maxAllowedY)
                                    }
                                }
                                .clickable { inspectedChaussure = shoe },
                            elevation = CardDefaults.cardElevation(defaultElevation = if (matchesFilter) 3.dp else 0.dp),
                            shape = RoundedCornerShape(0.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (matchesFilter) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp)
                                ) {
                                    Text(
                                        text = shoe.nom,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    if (shoe.marque.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = shoe.marque,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1
                                        )
                                    }
                                }

                                AsyncImage(
                                    model = File(shoe.photoPath),
                                    contentDescription = shoe.nom,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    // --- 2. ÉTIQUETTES FLOTTANTES (AU PREMIER PLAN) ---
                    etiquettes.forEach { etiquette ->
                        var tagX by remember(etiquette.id, dpToPx) { mutableFloatStateOf(etiquette.x * dpToPx) }
                        var tagY by remember(etiquette.id, dpToPx) { mutableFloatStateOf(etiquette.y * dpToPx) }

                        Surface(
                            modifier = Modifier
                                .offset { IntOffset(tagX.roundToInt(), tagY.roundToInt()) }
                                .pointerInput(etiquette.id) {
                                    detectDragGestures(
                                        onDragEnd = {
                                            onUpdateEtiquettePosition(etiquette.id, tagX / dpToPx, tagY / dpToPx)
                                        }
                                    ) { change, dragAmount ->
                                        change.consume()
                                        tagX = (tagX + dragAmount.x).coerceIn(0f, totalGridWidthPx - tagMarginXPx)
                                        tagY = (tagY + dragAmount.y).coerceIn(0f, totalGridHeightPx - tagMarginYPx)
                                    }
                                }
                                .clickable { onEditEtiquette(etiquette) },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            tonalElevation = 8.dp,
                            shadowElevation = 6.dp,
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Label,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = etiquette.texte,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // --- 3. CURSEURS/INDICATEURS DE BORD POUR ÉTIQUETTES HORS ÉCRAN ---
                val viewMinX = -panOffsetX
                val viewMaxX = -panOffsetX + screenWidthPx
                val viewMinY = -panOffsetY + with(density) { 120.dp.toPx() } // sous le header
                val viewMaxY = -panOffsetY + screenHeightPx - with(density) { 60.dp.toPx() } // au-dessus du bottom navigation

                val viewCenterX = -panOffsetX + (screenWidthPx / 2f)
                val viewCenterY = -panOffsetY + (screenHeightPx / 2f)

                val minEdgeX = with(density) { 16.dp.toPx() }
                val maxEdgeX = screenWidthPx - with(density) { 100.dp.toPx() }
                val minEdgeY = with(density) { 130.dp.toPx() }
                val maxEdgeY = screenHeightPx - with(density) { 70.dp.toPx() }

                etiquettes.forEach { tag ->
                    val tagWidthPx = (tag.texte.length * 11f + 60f).coerceIn(120f, 400f)
                    val tagHeightPx = 36f

                    val tagPxX = tag.x * dpToPx
                    val tagPxY = tag.y * dpToPx
                    val tagCenterX = tagPxX + (tagWidthPx / 2f)
                    val tagCenterY = tagPxY + (tagHeightPx / 2f)

                    val isOffScreen = tagCenterX !in viewMinX..viewMaxX ||
                            tagCenterY < viewMinY || tagCenterY > viewMaxY

                    if (isOffScreen) {
                        val dx = tagCenterX - viewCenterX
                        val dy = tagCenterY - viewCenterY

                        val scaleX = if (dx > 0) (maxEdgeX - (screenWidthPx / 2f)) / dx else ((screenWidthPx / 2f) - minEdgeX) / -dx
                        val scaleY = if (dy > 0) (maxEdgeY - (screenHeightPx / 2f)) / dy else ((screenHeightPx / 2f) - minEdgeY) / -dy
                        val scale = minOf(scaleX, scaleY).coerceAtLeast(0f)

                        val edgeX = (screenWidthPx / 2f) + dx * scale
                        val edgeY = (screenHeightPx / 2f) + dy * scale

                        val angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

                        Surface(
                            modifier = Modifier
                                .offset { IntOffset(edgeX.roundToInt(), edgeY.roundToInt()) }
                                .clickable {
                                    val targetPanX = -(tag.x * dpToPx - (screenWidthPx / 2f) + (tagWidthPx / 2f))
                                    val targetPanY = -(tag.y * dpToPx - (screenHeightPx / 2f) + (tagHeightPx / 2f))
                                    panOffsetX = targetPanX.coerceIn(-totalGridWidthPx + screenWidthPx, 0f)
                                    panOffsetY = targetPanY.coerceIn(-totalGridHeightPx + screenHeightPx, 0f)
                                },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shadowElevation = 6.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .rotate(angleDeg + 90f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tag.texte.take(8) + if (tag.texte.length > 8) "…" else "",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sélectionnez ou créez une pièce pour afficher l'espace.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- INTERFACE FLOTTANTE LÉGÈRE, FLUIDE ET ANIMÉE ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter)
                .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 3.dp,
            shadowElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pieces.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(pieces) { piece ->
                                val isSelected = piece.id == selectedPieceId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectPiece(piece.id) },
                                    label = { Text(piece.nom) },
                                    shape = CircleShape,
                                    trailingIcon = {
                                        if (isSelected) {
                                            IconButton(onClick = { onOpenEditPiece(piece) }, modifier = Modifier.size(16.dp)) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(16.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        Text(text = "Aucune pièce", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = onOpenAddPiece,
                            modifier = Modifier.height(36.dp),
                            shape = CircleShape
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pièce", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { isHeaderExpanded = !isHeaderExpanded }) {
                            Icon(
                                imageVector = if (isHeaderExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Réduire / Développer"
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = isHeaderExpanded && selectedPieceId != null,
                    enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(250)) + fadeOut(animationSpec = tween(250))
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onUpdateSearchQuery,
                            label = { Text("Rechercher (nom, marque, description)...") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (availableBrands.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                item {
                                    FilterChip(
                                        selected = selectedBrandFilter == null,
                                        onClick = { onSelectBrandFilter(null) },
                                        label = { Text("Toutes", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold) },
                                        shape = CircleShape
                                    )
                                }
                                items(availableBrands) { brand ->
                                    FilterChip(
                                        selected = selectedBrandFilter == brand,
                                        onClick = { onSelectBrandFilter(brand) },
                                        label = { Text(brand, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold) },
                                        shape = CircleShape
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // --- LISTE DÉROULANTE ALLER À L'ÉTIQUETTE ---
                            if (etiquettes.isNotEmpty()) {
                                Box {
                                    OutlinedButton(
                                        onClick = { isEtiquetteDropdownExpanded = true },
                                        modifier = Modifier.height(36.dp),
                                        shape = CircleShape,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Label,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Aller à...", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    }

                                    DropdownMenu(
                                        expanded = isEtiquetteDropdownExpanded,
                                        onDismissRequest = { isEtiquetteDropdownExpanded = false }
                                    ) {
                                        val calcMaxHeight = minOf(200.dp, (etiquettes.size * 48).dp)
                                        Box(
                                            modifier = Modifier
                                                .width(180.dp)
                                                .height(calcMaxHeight)
                                        ) {
                                            Column(
                                                modifier = Modifier.verticalScroll(rememberScrollState())
                                            ) {
                                                etiquettes.forEach { tag ->
                                                    DropdownMenuItem(
                                                        text = { Text(tag.texte, fontWeight = FontWeight.SemiBold) },
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = Icons.AutoMirrored.Filled.Label,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        },
                                                        onClick = {
                                                            isEtiquetteDropdownExpanded = false
                                                            val tagWidthPx = (tag.texte.length * 11f + 60f).coerceIn(120f, 600f)
                                                            val tagHeightPx = 45f
                                                            val targetPanX = -(tag.x * dpToPx - (screenWidthPx / 2f) + (tagWidthPx / 2f))
                                                            val targetPanY = -(tag.y * dpToPx - (screenHeightPx / 2f) + (tagHeightPx / 2f))
                                                            panOffsetX = targetPanX.coerceIn(-totalGridWidthPx + screenWidthPx, 0f)
                                                            panOffsetY = targetPanY.coerceIn(-totalGridHeightPx + screenHeightPx, 0f)
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            // --- CRÉATION ÉTIQUETTE (AU CENTRE DE L'ÉCRAN) ---
                            OutlinedButton(
                                onClick = {
                                    val (col, row) = calculateViewportCenterCell()
                                    onOpenAddEtiquette(col * GridUtils.CELL_W_DP, row * GridUtils.CELL_H_DP)
                                },
                                modifier = Modifier.height(36.dp),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Label, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Étiquette", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // --- CRÉATION BOÎTE (AU CENTRE DE L'ÉCRAN) ---
                            Button(
                                onClick = {
                                    val (col, row) = calculateViewportCenterCell()
                                    onOpenAddChaussure(col, row)
                                },
                                modifier = Modifier.height(36.dp),
                                shape = CircleShape
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text("Boîte", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
