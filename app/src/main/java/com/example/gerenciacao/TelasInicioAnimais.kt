package com.example.gerenciacao.ui.telas

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gerenciacao.R
import com.example.gerenciacao.domain.model.Abrigo
import com.example.gerenciacao.ui.*
import kotlin.math.roundToInt

// ================================================================ INÍCIO

private val AzulBorda = Color(0xFF4D9EFF)

@Composable
fun TelaInicio(
    nomeUsuario: String,
    abrigo: Abrigo?,
    onVerAgenda: () -> Unit,
    onVerDados: () -> Unit,
    onConfig: () -> Unit = {},
    onAvatar: () -> Unit = {},
    onParcerias: () -> Unit = {},
    onConexoes: () -> Unit = {},
) {
    val diasSemana = listOf("D", "S", "T", "Q", "Q", "S", "S")
    val diasMes = listOf(25, 26, 27, 28, 29, 30, 31) // TODO: calcular a semana atual com java.time
    val diaSelecionado = 28
    val diasComCompromisso = setOf(31) // TODO: vir da agenda (dias com eventos)

    val animaisTotais = abrigo?.animaisTotais ?: 0
    val vagas = abrigo?.vagas ?: 0
    val percentual = abrigo?.ocupacaoPercentual ?: 0

    BaseTela(
        onConfig = onConfig,
        onAvatar = onAvatar,
        configSemFundo = true,
        saudacao = {
            Column {
                Text("Olá, $nomeUsuario!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Cores.Marrom)
                Text("No que podemos te ajudar hoje?", fontSize = 14.sp, color = Cores.Marrom)
            }
        },
        extra = { CardCuidadosUrgentes(onVerAgenda) },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item { BannerParcerias(onParcerias) }
            item {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("Agenda", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Cores.Marrom)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Ver agenda",
                            fontSize = 14.sp,
                            color = Cores.Marrom,
                            modifier = Modifier.clickable(onClick = onVerAgenda).padding(bottom = 2.dp),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        diasMes.forEachIndexed { i, dia ->
                            val sel = dia == diaSelecionado
                            val fundo = when {
                                sel -> Cores.Laranja
                                dia in diasComCompromisso -> Color(0xFFE8EDF3)
                                else -> Cores.Creme
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(fundo)
                                    .border(1.dp, if (sel) Cores.Laranja else AzulBorda, RoundedCornerShape(50))
                                    .clickable(onClick = onVerAgenda)
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("$dia", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Cores.Marrom)
                                Text(diasSemana[i], fontSize = 16.sp, color = Cores.Marrom)
                            }
                        }
                    }
                }
            }
            item {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("Nível de Ocupação", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Cores.Marrom)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Ver dados",
                            fontSize = 14.sp,
                            color = Cores.Marrom,
                            modifier = Modifier.clickable(onClick = onVerDados).padding(bottom = 2.dp),
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AnelOcupacao(percentual, Modifier.size(156.dp))
                        Spacer(Modifier.width(18.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            CartaoNumeroInicio(
                                "Vagas disponíveis", vagas,
                                Brush.horizontalGradient(listOf(Color(0xFFFFE6CC), Cores.Creme)), Cores.Laranja,
                            )
                            CartaoNumeroInicio(
                                "Total de animais", animaisTotais,
                                Brush.horizontalGradient(listOf(Color(0xFFDCEBFA), Color(0xFFB9D9F7))), Cores.AzulTitulo,
                            )
                        }
                    }
                }
            }
            item { CardCuidadoresProximos(onConexoes) }
        }
    }
}

/** Cartão creme do topo ("Cuidados Urgentes"), com uma segunda camada por baixo. */
@Composable
private fun CardCuidadosUrgentes(onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.93f)
                .height(40.dp)
                .offset(y = 6.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Cores.Creme.copy(alpha = 0.55f)),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Cores.Creme)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = Color(0xFF3F6FA8), modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Cuidados Urgentes", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Cores.Marrom)
                Text(
                    "Acesse sua agenda e dê uma olhada nos compromissos do dia",
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    color = Cores.Marrom,
                )
            }
        }
    }
}

@Composable
private fun BannerParcerias(onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(146.dp)
            .clip(RoundedCornerShape(24.dp))
            .paint(painterResource(R.drawable.banner_osso), contentScale = ContentScale.FillBounds),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.62f)
                .padding(start = 16.dp, top = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    "Cuidar do seu Pet\ncusta menos!",
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Cores.AzulTitulo,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Acesse nossa rede de parceiros e garanta descontos que você só encontra aqui",
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cores.Marrom,
                )
            }
            Text(
                "Ir para parcerias",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF123A6B),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(AzulBorda)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            )
        }
    }
}

@Composable
private fun AnelOcupacao(percentual: Int, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val espessura = 18.dp.toPx()
            val canto = Offset(espessura / 2, espessura / 2)
            val tamanho = Size(size.width - espessura, size.height - espessura)
            drawArc(
                color = Color(0xFFE0D8CC),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = canto,
                size = tamanho,
                style = Stroke(espessura),
            )
            drawArc(
                color = Cores.Laranja,
                startAngle = -90f,
                sweepAngle = 360f * percentual / 100f,
                useCenter = false,
                topLeft = canto,
                size = tamanho,
                style = Stroke(espessura, cap = StrokeCap.Round),
            )
        }
        Text("$percentual%", fontSize = 36.sp, color = Cores.Marrom)
    }
}

@Composable
private fun CartaoNumeroInicio(titulo: String, valor: Int, fundo: Brush, cor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(fundo, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(titulo, fontSize = 16.sp, color = cor)
        Text("$valor", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = cor)
    }
}

/** Cartão azul-borda do rodapé: leva à tela Conexão. Mostra a primeira rede de exemplo. */
@Composable
private fun CardCuidadoresProximos(onClick: () -> Unit) {
    val proxima = ConexoesMock.redes.first()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, AzulBorda, RoundedCornerShape(16.dp))
            .background(Cores.Creme)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Encontre outros cuidadores perto de você", fontSize = 12.sp, color = Cores.Marrom)
            Text(proxima.nome, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Cores.Laranja)
        }
        if (proxima.foto != null) {
            Image(
                painter = painterResource(proxima.foto),
                contentDescription = proxima.nome,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(14.dp)),
            )
        }
    }
}
