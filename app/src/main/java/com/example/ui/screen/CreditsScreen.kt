package com.example.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BeigeYellow
import com.example.ui.theme.MintBlue
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PremiumBlack
import com.example.ui.theme.SpaceGrotesk

@Composable
fun CreditsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val githubUrl = "https://github.com/DEV-Aqshai/Echoes"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumBlack)
    ) {
        // Top Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(start = 16.dp, top = 20.dp)
                .size(48.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = OffWhite
            )
        }

        // Center Content matching design_refs/credits_ref.png
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Echoes Logo",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // €CHO€S Title
            val annotatedTitle = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = MintBlue,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 38.sp,
                        letterSpacing = 4.sp
                    )
                ) {
                    append("€CHO€")
                }
                withStyle(
                    SpanStyle(
                        color = BeigeYellow,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 38.sp,
                        letterSpacing = 4.sp
                    )
                ) {
                    append("S")
                }
            }
            Text(text = annotatedTitle)

            Spacer(modifier = Modifier.height(44.dp))

            // "Built By Prime™" in Beige Yellow
            Text(
                text = "Built By Prime™",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = BeigeYellow
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Authors
            Text(
                text = "Code + Management → AqshaiDev",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp,
                color = OffWhite.copy(alpha = 0.95f),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Design + Marketing → Santhosh",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp,
                color = OffWhite.copy(alpha = 0.95f),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Source Code button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                        context.startActivity(browserIntent)
                    }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .testTag("source_code_link"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, OffWhite, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Source Code",
                        tint = OffWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Source Code",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = OffWhite
                )
            }
        }
    }
}
