package com.example.shoecollectionapp.ui.theme.ui

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import java.io.File
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
    onOpenAddEtiquette: (centerX: Float, centerY: Float) -> Unit,
    onEditEtiquette: (Etiquette) -> Unit,
    onOpenAddChaussure: (centerX: Float, centerY: Float) -> Unit,
    onOpenEditChaussure: (Chaussure) -> Unit,
    onToggleFavorite: (Chaussure) -> Unit,
    onDeleteChaussure: (Long) -> Unit,
    onUpdatePosition: (Long, Float, Float, Long?) -> Unit,
    onUpdateEtiquettePosition: (Long, Float, Float) -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onSelectBrandFilter: (String?) -> Unit
) {
    var inspectedChaussure by remember { mutableStateOf<Chaussure?>(null) }
    var isHeaderExpanded by remember { mutableStateOf(true) }
    var isEtiquetteDropdownExpanded by remember { mutableStateOf(false) }

    // Déplacement de la caméra (Pan)
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Dimensions d'un bloc de la grille : 110dp de largeur x 70dp de hauteur
    val boxWidthDp = 110.dp
    val boxHeightDp = 70.dp

    val density = LocalDensity.current
    val boxWidthPx = with(density) { boxWidthDp.toPx() }
    val boxHeightPx = with(density) { boxHeightDp.toPx() }

    // Grille totale 25 x 25
    val maxCols = 25
    val maxRows = 25
    val totalGridWidthPx = boxWidthPx * maxCols
    val totalGridHeightPx = boxHeightPx * maxRows

    // Source de vérité unique pour les positions locales des chaussures
    val shoePositions = remember(selectedPieceId) { mutableStateMapOf<Long, Offset>() }
    LaunchedEffect(chaussures, selectedPieceId) {
        chaussures.forEach { shoe ->
            if (!shoePositions.containsKey(shoe.id)) {
                shoePositions[shoe.id] = Offset(shoe.posX, shoe.posY)
            }
        }
    }

    val availableBrands = allChaussures
        .map { it.marque.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

    // Dialog d'inspection de la boîte
    inspectedChaussure?.let { shoe ->
        val currentShoe = chaussures.find { it.id == shoe.id } ?: shoe

        AlertDialog(
            onDismissRequest = { inspectedChaussure = null },
            title = { Text(text = currentShoe.nom, fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = File(currentShoe.photoPath),
                        contentDescription = currentShoe.nom,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (currentShoe.marque.isNotBlank()) {
                        Text(text = "Marque : ${currentShoe.marque}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    if (currentShoe.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Description : ${currentShoe.description}", style = MaterialTheme.typography.bodyMedium)
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
                            }
                        ) {
                            Icon(
                                imageVector = if (currentShoe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Favori", maxLines = 1)
                        }

                        Button(
                            onClick = {
                                inspectedChaussure = null
                                onOpenEditChaussure(currentShoe)
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Modifier", maxLines = 1)
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

        // Calcul des coordonnées du centre de la vue courante
        fun calculateViewportCenter(): Pair<Float, Float> {
            val viewCenterX = -panOffsetX + (screenWidthPx / 2f) - (boxWidthPx / 2f)
            val viewCenterY = -panOffsetY + (screenHeightPx / 2f) - (boxHeightPx / 2f)
            val snappedX = ((viewCenterX / boxWidthPx).roundToInt() * boxWidthPx).coerceIn(0f, totalGridWidthPx - boxWidthPx)
            val snappedY = ((viewCenterY / boxHeightPx).roundToInt() * boxHeightPx).coerceIn(0f, totalGridHeightPx - boxHeightPx)
            return Pair(snappedX, snappedY)
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
                    val cw = boxWidthPx
                    val ch = boxHeightPx

                    var x = 0f
                    while (x <= totalGridWidthPx) {
                        drawLine(gridColor, Offset(x, 0f), Offset(x, totalGridHeightPx), strokeWidth = 1.5f)
                        x += cw
                    }
                    var y = 0f
                    while (y <= totalGridHeightPx) {
                        drawLine(gridColor, Offset(0f, y), Offset(totalGridWidthPx, y), strokeWidth = 1.5f)
                        y += ch
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
                    // --- 1. BOÎTES DE CHAUSSURES (Architecture unifiée sans variables fermées obsolètes) ---
                    chaussures.forEach { shoe ->
                        val cardPos = shoePositions[shoe.id] ?: Offset(shoe.posX, shoe.posY)
                        var startPos by remember(shoe.id) { mutableStateOf(cardPos) }

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
                                .offset { IntOffset(cardPos.x.roundToInt(), cardPos.y.roundToInt()) }
                                .size(width = boxWidthDp, height = boxHeightDp)
                                .alpha(blockAlpha)
                                .border(blockBorder, RoundedCornerShape(0.dp))
                                .pointerInput(shoe.id) {
                                    detectDragGestures(
                                        onDragStart = {
                                            val current = shoePositions[shoe.id] ?: Offset(shoe.posX, shoe.posY)
                                            startPos = current
                                        },
                                        onDragEnd = {
                                            val p = shoePositions[shoe.id] ?: startPos
                                            val maxAllowedX = totalGridWidthPx - boxWidthPx
                                            val maxAllowedY = totalGridHeightPx - boxHeightPx

                                            val targetX = ((p.x / boxWidthPx).roundToInt() * boxWidthPx).coerceIn(0f, maxAllowedX)
                                            val targetY = ((p.y / boxHeightPx).roundToInt() * boxHeightPx).coerceIn(0f, maxAllowedY)

                                            val targetCol = (targetX / boxWidthPx).roundToInt()
                                            val targetRow = (targetY / boxHeightPx).roundToInt()

                                            // Recherche d'une boîte en conflit
                                            val conflictingId = shoePositions.entries.find { (id, pos) ->
                                                id != shoe.id &&
                                                        (pos.x / boxWidthPx).roundToInt() == targetCol &&
                                                        (pos.y / boxHeightPx).roundToInt() == targetRow
                                            }?.key

                                            if (conflictingId != null) {
                                                // Échange SWAP parfait
                                                shoePositions[shoe.id] = Offset(targetX, targetY)
                                                shoePositions[conflictingId] = startPos

                                                onUpdatePosition(shoe.id, targetX, targetY, shoe.idSection)
                                                onUpdatePosition(conflictingId, startPos.x, startPos.y, null)
                                            } else {
                                                // Case libre
                                                shoePositions[shoe.id] = Offset(targetX, targetY)
                                                onUpdatePosition(shoe.id, targetX, targetY, shoe.idSection)
                                            }
                                        }
                                    ) { change, dragAmount ->
                                        change.consume()
                                        val maxAllowedX = totalGridWidthPx - boxWidthPx
                                        val maxAllowedY = totalGridHeightPx - boxHeightPx
                                        val p = shoePositions[shoe.id] ?: startPos
                                        val newX = (p.x + dragAmount.x).coerceIn(0f, maxAllowedX)
                                        val newY = (p.y + dragAmount.y).coerceIn(0f, maxAllowedY)
                                        shoePositions[shoe.id] = Offset(newX, newY)
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
                        var tagX by remember(etiquette.id) { mutableStateOf(etiquette.x) }
                        var tagY by remember(etiquette.id) { mutableStateOf(etiquette.y) }

                        Surface(
                            modifier = Modifier
                                .offset { IntOffset(tagX.roundToInt(), tagY.roundToInt()) }
                                .pointerInput(etiquette.id) {
                                    detectDragGestures(
                                        onDragEnd = {
                                            onUpdateEtiquettePosition(etiquette.id, tagX, tagY)
                                        }
                                    ) { change, dragAmount ->
                                        change.consume()
                                        tagX = (tagX + dragAmount.x).coerceIn(0f, totalGridWidthPx - 100f)
                                        tagY = (tagY + dragAmount.y).coerceIn(0f, totalGridHeightPx - 50f)
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

        // --- INTERFACE FLOTTANTE LÉGÈRE ET FLUIDE ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter),
            shape = RoundedCornerShape(14.dp),
            tonalElevation = 2.dp,
            shadowElevation = 4.dp,
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
                                    trailingIcon = {
                                        if (isSelected) {
                                            IconButton(onClick = { onOpenEditPiece(piece) }, modifier = Modifier.size(16.dp)) {
                                                Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("Pièce", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { isHeaderExpanded = !isHeaderExpanded }) {
                            Icon(
                                imageVector = if (isHeaderExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Réduire / Développer"
                            )
                        }
                    }
                }

                if (isHeaderExpanded && selectedPieceId != null) {
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onUpdateSearchQuery,
                        label = { Text("Rechercher (nom, marque, description)...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (availableBrands.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedBrandFilter == null,
                                    onClick = { onSelectBrandFilter(null) },
                                    label = { Text("Toutes", style = MaterialTheme.typography.bodySmall) }
                                )
                            }
                            items(availableBrands) { brand ->
                                FilterChip(
                                    selected = selectedBrandFilter == brand,
                                    onClick = { onSelectBrandFilter(brand) },
                                    label = { Text(brand, style = MaterialTheme.typography.bodySmall) }
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
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Label,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Aller à...", style = MaterialTheme.typography.bodySmall)
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
                                                        val tagWidthPx = 140f
                                                        val tagHeightPx = 45f
                                                        val targetPanX = -(tag.x - (screenWidthPx / 2f) + (tagWidthPx / 2f))
                                                        val targetPanY = -(tag.y - (screenHeightPx / 2f) + (tagHeightPx / 2f))
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
                                val (centerX, centerY) = calculateViewportCenter()
                                onOpenAddEtiquette(centerX, centerY)
                            },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Label, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Étiquette", style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // --- CRÉATION BOÎTE (AU CENTRE DE L'ÉCRAN) ---
                        Button(
                            onClick = {
                                val (centerX, centerY) = calculateViewportCenter()
                                onOpenAddChaussure(centerX, centerY)
                            },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("Boîte", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
