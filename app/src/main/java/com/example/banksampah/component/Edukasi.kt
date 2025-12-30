package com.example.banksampah.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.banksampah.R
import com.example.banksampah.data.ListTittle
import com.example.banksampah.ui.theme.BankSampahTheme

@Composable
fun Edukasi(modifier: Modifier = Modifier, listTittle: ListTittle) {
    Button(
        onClick = {},
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.greenlight),
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(12.dp),

    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = listTittle.tittleImg),
                contentDescription = "image1",
                modifier = Modifier
                    .size(61.dp)
                    .border(1.dp, Color.Black, RoundedCornerShape(8.dp))

            )
            Text(
                text = listTittle.tittleText,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow),
                contentDescription = "arrow",
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EdukasiPreview() {
    BankSampahTheme {
        Edukasi(listTittle = ListTittle(1, R.drawable.image1, "Apa Dampak Sampah terhadap Kesehatan?"))
    }
}