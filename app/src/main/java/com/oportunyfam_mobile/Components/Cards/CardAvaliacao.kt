package com.oportunyfam_mobile.Components.Cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun CardAvalieNos(
    onSend: () -> Unit = {},
    rating: Int = 0,
    onRatingChanged: (Int) -> Unit = {},
    comment: String = "",
    onCommentChanged: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Title
            Text(
                text = "Nos Avalie",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Rating
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(5) { index ->
                    IconButton(
                        onClick = { onRatingChanged(index + 1) },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarOutline,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFA000)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Comment Section
            BasicTextField(
                value = TextFieldValue(comment),
                onValueChange = { onCommentChanged(it.text) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(10.dp)
                    .background(Color.Gray.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.padding(8.dp)) {
                        if (comment.isEmpty()) {
                            Text(
                                text = "Adicione seu comentário sobre nós:",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Send Button
            Button(
                onClick = onSend,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = "Enviar",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CardAvalieNosPreview() {
    CardAvalieNos()
}

