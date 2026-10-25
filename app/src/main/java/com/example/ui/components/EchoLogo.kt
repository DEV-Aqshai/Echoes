package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BeigeYellow
import com.example.ui.theme.MintBlue
import com.example.ui.theme.SpaceGrotesk

@Composable
fun EchoLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 38.dp,
    textSize: TextUnit = 24.sp,
    showText: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Echoes Logo",
            modifier = Modifier.size(iconSize)
        )
        if (showText) {
            val annotatedTitle = buildAnnotatedString {
                // "€CHOE" in Mint Blue, "S" in Beige Yellow matching reference design
                withStyle(
                    SpanStyle(
                        color = MintBlue,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = textSize,
                        letterSpacing = 2.sp
                    )
                ) {
                    append("€CHO€")
                }
                withStyle(
                    SpanStyle(
                        color = BeigeYellow,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = textSize,
                        letterSpacing = 2.sp
                    )
                ) {
                    append("S")
                }
            }
            Text(text = annotatedTitle)
        }
    }
}
