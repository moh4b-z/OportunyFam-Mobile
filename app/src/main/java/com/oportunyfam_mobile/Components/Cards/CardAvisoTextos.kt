package com.oportunyfam_mobile_ong.Components.Cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oportunyfam_mobile_ong.R

@Composable
fun CardBoasVindas() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.security),
                    contentDescription = "Ícone de segurança",
                    modifier = Modifier
                        .size(28.dp)
                        .padding(end = 8.dp)
                )

                Text(
                    text = "Bem-vindo à OportunityFam",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            Text(
                text = "Nossa plataforma conecta mães, famílias e ONGs para criar uma rede de apoio segura e organizada.",
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "Ao utilizar nossa plataforma, você concorda com os termos estabelecidos neste documento. Nossa missão é promover a inclusão social e fortalecer vínculos comunitários através da tecnologia, sempre priorizando a segurança e bem-estar das crianças e famílias.",
                fontSize = 14.sp,
                color = Color(0xFF333333),
                lineHeight = 20.sp,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CardBoasVindasPreview() {
    CardBoasVindas()
}
