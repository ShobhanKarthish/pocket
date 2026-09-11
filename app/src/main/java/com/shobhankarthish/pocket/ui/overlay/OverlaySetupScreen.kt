package com.shobhankarthish.pocket.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shobhankarthish.pocket.R
import com.shobhankarthish.pocket.ui.theme.Astra

@Composable
fun OverlaySetupScreen(
    onAllow: () -> Unit,
    onNotNow: () -> Unit,
) {
    val shape = RoundedCornerShape(Astra.RadiusDp.dp)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = stringResource(R.string.overlay_setup_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 24.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.overlay_setup_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = Astra.EmptyDockAboveSafeDp.dp,
                ),
            horizontalAlignment = Alignment.Start,
        ) {
            Button(
                onClick = onAllow,
                modifier = Modifier.size(
                    width = 200.dp,
                    height = Astra.EmptyAddHeightDp.dp,
                ),
                shape = shape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.overlay_setup_allow),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(Modifier.height(Astra.EmptyAddHowGapDp.dp))
            TextButton(
                onClick = onNotNow,
                modifier = Modifier.heightIn(min = Astra.HowToAddMinDp.dp),
                shape = shape,
                contentPadding = PaddingValues(horizontal = 0.dp),
            ) {
                Text(
                    text = stringResource(R.string.overlay_setup_not_now),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
