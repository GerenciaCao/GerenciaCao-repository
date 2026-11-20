package com.example.gerenciacao.ui.telas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gerenciacao.R
import com.example.gerenciacao.domain.model.Animal
import com.example.gerenciacao.domain.model.Especie
import com.example.gerenciacao.domain.model.StatusAnimal
import com.example.gerenciacao.ui.*
import com.example.gerenciacao.ui.componentes.EstadoCarregando
import com.example.gerenciacao.ui.componentes.EstadoErro
import com.example.gerenciacao.ui.viewmodel.UiAnimais

/** Cor da etiqueta de status do bichinho. */
internal fun StatusAnimal.cor(): Color = when (this) {
    StatusAnimal.DISPONIVEL -> Cores.Verde
    StatusAnimal.EM_PROCESSO_ADOCAO -> Cores.AmareloStatus
    StatusAnimal.EM_TRATAMENTO -> Cores.Rosa
    StatusAnimal.LAR_TEMPORARIO -> Cores.Azul
    StatusAnimal.ADOTADO -> Cores.CinzaCampo
}

/**
 * Lista de animais do abrigo (dados da API). Busca, chips de espécie e painel de filtro agem sobre a lista carregada.
 * O botão flutuante "Nova ficha" só aparece para quem pode editar prontuários.
 */
@Composable
fun TelaAnimais(
    estado: UiAnimais,
    podeCriar: Boolean,
    onConfig: () -> Unit,
    onAvatar: () -> Unit,
    onAbrirFicha: (String) -> Unit,
    onNovaFicha: () -> Unit,
    onTentarNovamente: () -> Unit,
) {
    val filtros = listOf<Especie?>(null, Especie.CACHORRO, Especie.GATO)
    var filtro by remember { mutableStateOf<Especie?>(null) }
    var busca by remember { mutableStateOf("") }
    var mostrarFiltro by remember { mutableStateOf(false) }
    var criterios by remember { mutableStateOf(CriteriosFiltro()) }

    val lista = estado.animais.filter {
        (filtro == null || it.especie == filtro) && it.nome.contains(busca, ignoreCase = true) && criterios.aceita(it)
    }

    Box(Modifier.fillMaxSize()) {
        BaseTela(onConfig = onConfig, onAvatar = onAvatar) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { Text("Animais", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Cores.Marrom, modifier = Modifier.padding(horizontal = 20.dp)) }
                item {
                    BarraBusca(
                        valor = busca,
                        aoMudar = { busca = it },
                        dica = "Buscar por Ficha",
                        onFiltro = { mostrarFiltro = true },
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filtros) { f ->
                            val sel = f == filtro
                            val miniatura = when (f) {
                                Especie.CACHORRO -> R.drawable.filtro_caes
                                Especie.GATO -> R.drawable.filtro_gatos
                                null -> R.drawable.filtro_todos
                            }
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (sel) Cores.AmareloChip else Cores.Marrom)
                                    .clickable { filtro = f }
                                    .padding(start = 6.dp, end = 18.dp, top = 6.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(painterResource(miniatura), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(40.dp).clip(CircleShape))
                                Spacer(Modifier.width(8.dp))
                                Text(f?.plural ?: "Todos", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (sel) Cores.Marrom else Cores.Creme)
                            }
                        }
                    }
                }
                item { Text("Fichas", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Cores.Marrom, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) }
                if (estado.carregando && estado.animais.isEmpty()) item { EstadoCarregando() }
                if (estado.erro != null && estado.animais.isEmpty()) item { EstadoErro(estado.erro, onTentarNovamente) }
                if (!estado.carregando && estado.erro == null && estado.animais.isEmpty()) {
                    item { Text("Nenhum bichinho cadastrado ainda. Toque em \"Nova ficha\" para começar.", fontSize = 16.sp, color = Cores.CinzaTexto, modifier = Modifier.padding(horizontal = 20.dp)) }
                }
                items(lista, key = { it.id }) { animal ->
                    CardAnimal(animal, Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp), onAbrir = { onAbrirFicha(animal.id) })
                }
            }
        }

        if (podeCriar) {
            ExtendedFloatingActionButton(
                onClick = onNovaFicha,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nova ficha", fontWeight = FontWeight.Bold) },
                containerColor = Cores.Laranja,
                contentColor = Cores.Marrom,
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            )
        }

        if (mostrarFiltro) {
            FiltroAnimais(
                inicial = criterios,
                onAplicar = {
                    criterios = it
                    mostrarFiltro = false
                },
                onFechar = { mostrarFiltro = false },
            )
        }
    }
}

/** Card de um bichinho. A foto virá do banco (endpoint de fotos); por enquanto, um ícone de patinha no círculo. */
@Composable
private fun CardAnimal(animal: Animal, modifier: Modifier = Modifier, onAbrir: () -> Unit = {}) {
    val corCard = if (animal.femea) Cores.Azul else Cores.AzulClaro
    val corCirculo = if (animal.femea) Cores.AmareloChip else Cores.Laranja

    Box(modifier.fillMaxWidth().height(116.dp)) {
        Box(Modifier.matchParentSize().clip(RoundedCornerShape(24.dp)).background(corCard).clickable(onClick = onAbrir))
        Box(Modifier.align(Alignment.CenterStart).padding(start = 22.dp).size(96.dp).clip(CircleShape).background(corCirculo), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Pets, contentDescription = null, tint = Cores.Marrom.copy(alpha = 0.55f), modifier = Modifier.size(44.dp))
        }
        Column(Modifier.padding(start = 140.dp, top = 12.dp, end = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(animal.nome, fontSize = 24.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, color = Cores.AzulTitulo, maxLines = 1)
                Text(if (animal.femea) " ♀" else " ♂", fontSize = 20.sp, color = if (animal.femea) Color(0xFFE0568F) else Cores.AzulTitulo)
                Spacer(Modifier.width(6.dp))
                Text(
                    animal.status.rotulo,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cores.Marrom,
                    maxLines = 1,
                    modifier = Modifier.background(animal.status.cor(), RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
            Text(animal.raca, fontSize = 14.sp, lineHeight = 17.sp, color = Cores.Marrom)
            Text(animal.idadeTexto, fontSize = 14.sp, lineHeight = 17.sp, color = Cores.Marrom)
            Text("Porte ${animal.porte.rotulo}", fontSize = 14.sp, lineHeight = 17.sp, color = Cores.Marrom)
        }
    }
}
