package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.FleetDataSource
import com.example.data.SupabaseConfig
import com.example.ui.theme.AvailableEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupModal(
    currentConfig: SupabaseConfig,
    dataSource: FleetDataSource,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSaveAndConnect: (url: String, anonKey: String, whatsappNumber: String) -> Unit,
    onUseSampleData: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var urlInput by remember(currentConfig.url) { mutableStateOf(currentConfig.url) }
    var keyInput by remember(currentConfig.anonKey) { mutableStateOf(currentConfig.anonKey) }
    var whatsappInput by remember(currentConfig.whatsappNumber) {
        mutableStateOf(currentConfig.whatsappNumber)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.setup_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.setup_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("setup_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.setup_close)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Connection Status Banner
            val isLive = dataSource == FleetDataSource.LIVE_SUPABASE
            val statusColor = when {
                errorMessage != null -> MaterialTheme.colorScheme.error
                isLive -> AvailableEmerald
                else -> MaterialTheme.colorScheme.secondary
            }
            val statusText = when {
                errorMessage != null -> errorMessage
                isLive -> "Connected to live table \"VA CARS\""
                else -> "Currently showing built-in sample fleet (Baleno, Innova & more)"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (isLive) Icons.Outlined.CloudDone else Icons.Outlined.CloudOff,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text(stringResource(R.string.setup_url_label)) },
                placeholder = { Text(stringResource(R.string.setup_url_hint)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Link, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_url_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = keyInput,
                onValueChange = { keyInput = it },
                label = { Text(stringResource(R.string.setup_key_label)) },
                placeholder = { Text(stringResource(R.string.setup_key_hint)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Key, contentDescription = null)
                },
                minLines = 2,
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_key_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = whatsappInput,
                onValueChange = { whatsappInput = it },
                label = { Text(stringResource(R.string.setup_whatsapp_label)) },
                placeholder = { Text(stringResource(R.string.setup_whatsapp_hint)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Phone, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_whatsapp_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "Expected Supabase table: \"VA CARS\" with columns id, car_name, price_per_day, seats, transmission, fuel_type, is_available, image_url.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onUseSampleData,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("setup_sample_button")
                ) {
                    Text(stringResource(R.string.setup_use_sample))
                }

                Button(
                    onClick = { onSaveAndConnect(urlInput, keyInput, whatsappInput) },
                    enabled = urlInput.isNotBlank() && keyInput.isNotBlank(),
                    modifier = Modifier
                        .weight(1.3f)
                        .heightIn(min = 48.dp)
                        .testTag("setup_save_button")
                ) {
                    Text(stringResource(R.string.setup_save_connect))
                }
            }
        }
    }
}
