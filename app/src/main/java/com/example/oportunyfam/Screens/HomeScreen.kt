package com.example.tcc

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.Components.BarraTarefas
import com.example.oportunyfam.R

@Composable
fun HomeScreen(navController: NavHostController?) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.mapa),
            contentDescription = "Mapa",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFFFFA000))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Pesquise aqui",
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notificação",
                modifier = Modifier.align(Alignment.CenterEnd),
                tint = Color.Red
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ChipMock("Jiu Jitsu")
            ChipMock("T.I")
            ChipMock("Centro Cultural")
            ChipMock("Biblioteca")
        }

        FloatingActionButton(
            onClick = { },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(70.dp, 80.dp, 16.dp, 90.dp),
            containerColor = Color.DarkGray
        ) {
            Icon(Icons.Filled.Face, contentDescription = "Usuários")
        }

        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BarraTarefas(navController = navController)
        }
    }
}

@Composable
fun ChipMock(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.DarkGray)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, color = Color.White, fontSize = 14.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = null)
}
