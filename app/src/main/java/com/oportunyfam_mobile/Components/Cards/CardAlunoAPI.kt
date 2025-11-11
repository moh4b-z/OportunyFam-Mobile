package com.oportunyfam_mobile_ong.Components.Cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.oportunyfam_mobile_ong.R
import com.oportunyfam_mobile_ong.model.Aluno

@Composable
fun CardAlunoAPI(aluno: Aluno) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto da criança
            if (!aluno.crianca_foto.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(aluno.crianca_foto)
                        .crossfade(true)
                        .build(),
                    contentDescription = aluno.crianca_nome,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.instituicao),
                    error = painterResource(id = R.drawable.instituicao)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.instituicao),
                    contentDescription = aluno.crianca_nome,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(aluno.crianca_nome, fontWeight = FontWeight.Bold)
                Text(aluno.atividade_titulo, fontSize = 14.sp, color = Color.Gray)
                Text(
                    "Inscrição: ${aluno.data_inscricao.take(10)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Badge de status
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (aluno.status_inscricao) {
                    "Aprovada" -> Color(0xFF4CAF50)
                    "Pendente" -> Color(0xFFFFA000)
                    "Recusada" -> Color(0xFFF44336)
                    else -> Color.Gray
                }
            ) {
                Text(
                    aluno.status_inscricao,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}