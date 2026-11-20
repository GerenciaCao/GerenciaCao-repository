package com.example.gerenciacao.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gerenciacao.domain.model.AnimalDetalhe
import com.example.gerenciacao.ui.*
import com.example.gerenciacao.ui.componentes.EstadoCarregando
import com.example.gerenciacao.ui.componentes.EstadoErro
import com.example.gerenciacao.ui.componentes.MensagemErro
import com.example.gerenciacao.ui.viewmodel.UiFicha
import java.time.format.DateTimeFormatter

private val AzulTexto = Color(0xFF1B3F6B)
private val FormatoData: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** Ficha Individual do bichinho (dados da API). Quem pode editar prontuários também vê "Editar ficha" e "Excluir ficha". */
@Composable
fun TelaFicha(
    estado: UiFicha,
    podeEditar: Boolean,
    onVoltar: () -> Unit,
    onEditar: () -> Unit,
    onExcluir: () -> Unit,
    onTentarNovamente: () -> Unit,
) {
    LaunchedEffect(estado.removido) { if (estado.removido) onVoltar() }
    var confirmarExclusao by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Cores.Creme).verticalScroll(rememberScrollState())) {
        // Faixa laranja com a seta e o título
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                .background(Cores.Laranja)
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 22.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White, modifier = Modifier.size(36.dp).clickable(onClick = onVoltar))
            Spacer(Modifier.height(28.dp))
            Text("Ficha Individual", fontSize = 34.sp, fontWeight = FontWeight.Black, color = Cores.Marrom)
        }

        val detalhe = estado.detalhe
        when {
            detalhe != null -> ConteudoFicha(detalhe, estado, podeEditar, onEditar) { confirmarExclusao = true }
            estado.carregando -> EstadoCarregando()
            else -> EstadoErro(estado.erro ?: "Não foi possível carregar.", onTentarNovamente)
        }
        Spacer(Modifier.height(20.dp))
    }

    if (confirmarExclusao) {
        Dialog(onDismissRequest = { confirmarExclusao = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp).fillMaxWidth().shadow(8.dp, RoundedCornerShape(20.dp)).background(Cores.AzulDialogo, RoundedCornerShape(20.dp)).padding(20.dp),
            ) {
                Text("Excluir ficha", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(Modifier.height(12.dp))
                Text("Vacinas, histórico médico e fotos desta ficha também serão apagados. Essa ação não poderá ser desfeita.", fontSize = 16.sp, color = Color.Black)
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    BotaoAcao("Cancelar", { confirmarExclusao = false }, Modifier.weight(1f))
                    BotaoAcao("Excluir", { confirmarExclusao = false; onExcluir() }, Modifier.weight(1f), corFundo = Cores.VermelhoBotao)
                }
            }
        }
    }
}

/** Foto (provisória), identificação, dados clínicos, descrição, vacinas e histórico. */
@Composable
private fun ConteudoFicha(detalhe: AnimalDetalhe, estado: UiFicha, podeEditar: Boolean, onEditar: () -> Unit, onExcluir: () -> Unit) {
    val animal = detalhe.animal
    val castrado = when {
        animal.castrado && animal.femea -> "Castrada"
        animal.castrado -> "Castrado"
        animal.femea -> "Não castrada"
        else -> "Não castrado"
    }
    val peso = animal.pesoKg?.let { " • " + "%.1f".format(it).replace('.', ',') + "kg" }.orEmpty()

    Box(Modifier.fillMaxWidth()) {
        Box(Modifier.align(Alignment.TopCenter).padding(top = 6.dp).size(292.dp).clip(CircleShape).background(Cores.AzulClaro))
        // TODO: foto do bichinho vinda do banco (endpoint de fotos)
        Icon(Icons.Filled.Pets, contentDescription = null, tint = Cores.Azul, modifier = Modifier.align(Alignment.TopCenter).padding(top = 50.dp).size(90.dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 150.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Cores.Azul)
                .padding(horizontal = 18.dp, vertical = 18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(animal.nome, fontSize = 44.sp, fontWeight = FontWeight.Bold, color = AzulTexto, maxLines = 1, modifier = Modifier.weight(1f, fill = false))
                Text(if (animal.femea) "♀" else "♂", fontSize = 38.sp, color = if (animal.femea) Color(0xFFE0568F) else AzulTexto, modifier = Modifier.padding(start = 4.dp))
                Spacer(Modifier.weight(1f))
                Text(
                    animal.status.rotulo, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Cores.Marrom, maxLines = 1,
                    modifier = Modifier.background(animal.status.cor(), RoundedCornerShape(50)).padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().border(1.5.dp, AzulTexto, RoundedCornerShape(16.dp)).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                DadoFicha(animal.especie.rotulo, "Espécie", Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(44.dp).background(AzulTexto))
                DadoFicha(animal.raca, "Raça", Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(44.dp).background(AzulTexto))
                DadoFicha(abreviarIdade(animal.idadeMeses), "Idade", Modifier.weight(1f), prefixo = "Aprox.")
            }
            Spacer(Modifier.height(16.dp))
            LinhaFicha("${animal.porte.rotulo}$peso", "Porte/Peso atual")
            LinhaFicha(castrado, "Status reprodutivo")
            if (animal.caracteristicas.isNotBlank()) LinhaFicha(animal.caracteristicas, "Características")
            if (!animal.microchip.isNullOrBlank()) LinhaFicha(animal.microchip, "Microchip")

            Text("Descrição do animal", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AzulTexto, modifier = Modifier.padding(top = 14.dp, bottom = 10.dp))
            Text(
                animal.descricao.ifBlank { "Sem descrição." },
                fontSize = 16.sp, lineHeight = 22.sp, textAlign = TextAlign.Justify, color = AzulTexto,
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp).background(Color(0xFFDCEEFF), RoundedCornerShape(24.dp)).padding(18.dp),
            )

            if (detalhe.vacinas.isNotEmpty()) {
                Text("Vacinas", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AzulTexto, modifier = Modifier.padding(top = 18.dp, bottom = 6.dp))
                detalhe.vacinas.forEach { v -> LinhaFicha(v.nome, v.data?.format(FormatoData) ?: "—") }
            }
            if (detalhe.consultas.isNotEmpty()) {
                Text("Histórico médico", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AzulTexto, modifier = Modifier.padding(top = 18.dp, bottom = 6.dp))
                detalhe.consultas.forEach { c -> LinhaFicha(c.diagnostico, (c.data?.format(FormatoData) ?: "—") + "\n" + c.veterinario) }
            }
            estado.erroAcao?.let { MensagemErro(it) }
            if (podeEditar) {
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    BotaoAcao("Editar ficha", onEditar, Modifier.weight(1f), icone = Icons.Filled.Edit, enabled = !estado.processando)
                    BotaoAcao("Excluir", onExcluir, Modifier.weight(1f), corFundo = Cores.VermelhoBotao, enabled = !estado.processando)
                }
            }
        }
    }
}

/** "5 Meses" -> "5 m"; "2 Anos" -> "2 a". */
private fun abreviarIdade(meses: Int?): String = when {
    meses == null -> "—"
    meses < 12 -> "$meses m"
    else -> "${meses / 12} a"
}

@Composable
private fun DadoFicha(valor: String, rotulo: String, modifier: Modifier = Modifier, prefixo: String? = null) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (prefixo != null) Text(prefixo, fontSize = 12.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold, color = AzulTexto)
        Text(valor, fontSize = 26.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, color = AzulTexto, maxLines = 1)
        Text(rotulo, fontSize = 12.sp, color = AzulTexto)
    }
}

@Composable
private fun LinhaFicha(valor: String, rotulo: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(valor, fontSize = 21.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold, color = AzulTexto, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Text(rotulo, fontSize = 15.sp, color = AzulTexto.copy(alpha = 0.7f), textAlign = TextAlign.End, modifier = Modifier.width(120.dp))
    }
}
