package com.example.gerenciacao.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gerenciacao.domain.model.Animal
import com.example.gerenciacao.domain.model.DadosAnimal
import com.example.gerenciacao.domain.model.Especie
import com.example.gerenciacao.domain.model.Genero
import com.example.gerenciacao.domain.model.Porte
import com.example.gerenciacao.domain.model.StatusAnimal
import com.example.gerenciacao.ui.*
import com.example.gerenciacao.ui.componentes.MensagemErro
import com.example.gerenciacao.ui.viewmodel.UiFicha

/**
 * Formulário de ficha: cria (inicial = null) ou edita um bichinho. Provisório até o design chegar.
 * Fotos: ainda não há envio pelo app (vem junto com o seletor de imagens).
 */
@Composable
fun TelaFichaForm(
    estado: UiFicha,
    inicial: Animal?,
    onSalvar: (dados: DadosAnimal, microchip: String, peso: String, statusMudou: Boolean) -> Unit,
    onVoltar: () -> Unit,
) {
    LaunchedEffect(estado.salvo) { if (estado.salvo) onVoltar() }

    val chave = inicial?.id ?: "novo"
    var nome by remember(chave) { mutableStateOf(inicial?.nome.orEmpty()) }
    var especie by remember(chave) { mutableStateOf<Especie?>(inicial?.especie) }
    var genero by remember(chave) { mutableStateOf<Genero?>(inicial?.genero) }
    var raca by remember(chave) { mutableStateOf(inicial?.raca ?: "SRD") }
    var idade by remember(chave) { mutableStateOf(inicial?.idadeMeses?.toString().orEmpty()) }
    var porte by remember(chave) { mutableStateOf<Porte?>(inicial?.porte ?: Porte.MEDIO) }
    var peso by remember(chave) { mutableStateOf(inicial?.pesoKg?.toString()?.replace('.', ',').orEmpty()) }
    var castrado by remember(chave) { mutableStateOf(inicial?.castrado ?: false) }
    var microchip by remember(chave) { mutableStateOf(inicial?.microchip.orEmpty()) }
    var caracteristicas by remember(chave) { mutableStateOf(inicial?.caracteristicas.orEmpty()) }
    var descricao by remember(chave) { mutableStateOf(inicial?.descricao.orEmpty()) }
    var status by remember(chave) { mutableStateOf<StatusAnimal?>(inicial?.status ?: StatusAnimal.DISPONIVEL) }
    var faltaCampo by remember { mutableStateOf<String?>(null) }

    val fundo = Cores.AzulCampo
    val borda = Cores.AmareloBorda
    // Situações que o formulário pode escolher (a adoção é controlada pelo processo de adoção)
    val opcoesStatus = buildList {
        add(StatusAnimal.DISPONIVEL); add(StatusAnimal.EM_TRATAMENTO); add(StatusAnimal.LAR_TEMPORARIO)
        inicial?.status?.takeIf { it !in this }?.let { add(it) }
    }

    Column(Modifier.fillMaxSize().background(Cores.Creme).statusBarsPadding().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Voltar", tint = Cores.Marrom,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp).size(36.dp).clickable(onClick = onVoltar),
        )
        Text(if (inicial == null) "Nova Ficha" else "Editar Ficha", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Cores.Marrom)
        Text("Fotos: em breve.", fontSize = 13.sp, color = Cores.CinzaTexto, modifier = Modifier.padding(top = 4.dp))

        RotuloCampo("Nome")
        CampoFormulario(nome, { nome = it }, "Nome do bichinho", fundo = fundo, borda = borda, sombra = true)
        RotuloCampo("Espécie")
        SeletorLista(Especie.values().toList(), especie, { it.rotulo }, { especie = it }, "Cão ou gato", fundo = fundo, borda = borda, sombra = false)
        RotuloCampo("Sexo")
        SeletorLista(Genero.values().toList(), genero, { it.rotulo }, { genero = it }, "Fêmea ou macho", fundo = fundo, borda = borda, sombra = false)
        RotuloCampo("Raça")
        CampoFormulario(raca, { raca = it }, "Ex.: Poodle (ou SRD)", fundo = fundo, borda = borda, sombra = true)
        RotuloCampo("Idade aproximada (em meses)")
        CampoFormulario(idade, { idade = it.filter(Char::isDigit) }, "Ex.: 5", fundo = fundo, borda = borda, sombra = true, teclado = KeyboardType.Number)
        RotuloCampo("Porte")
        SeletorLista(Porte.values().toList(), porte, { it.rotulo }, { porte = it }, "Porte", fundo = fundo, borda = borda, sombra = false)
        RotuloCampo("Peso (kg)")
        CampoFormulario(peso, { peso = it }, "Ex.: 3,2", fundo = fundo, borda = borda, sombra = true, teclado = KeyboardType.Decimal)
        RotuloCampo("Situação")
        SeletorLista(opcoesStatus, status, { it.rotulo }, { status = it }, "Situação", fundo = fundo, borda = borda, sombra = false)
        Spacer(Modifier.height(10.dp))
        LinhaPermissao(if (genero == Genero.MACHO) "Castrado" else "Castrada", castrado) { castrado = !castrado }
        RotuloCampo("Microchip (15 números, opcional)")
        CampoFormulario(microchip, { microchip = it.filter(Char::isDigit) }, "000000000000000", fundo = fundo, borda = borda, sombra = true, teclado = KeyboardType.Number)
        RotuloCampo("Características")
        CampoFormulario(caracteristicas, { caracteristicas = it }, "Ex.: Dócil, ativa", fundo = fundo, borda = borda, sombra = true)
        RotuloCampo("Descrição")
        CampoFormulario(descricao, { descricao = it }, "Conte a história do bichinho", fundo = fundo, borda = borda, linhas = 4)

        (faltaCampo ?: estado.erroAcao)?.let { MensagemErro(it) }
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            BotaoAcao("Cancelar", onVoltar, Modifier.weight(1f))
            BotaoAcao(
                texto = if (estado.processando) "Salvando..." else "Salvar",
                onClick = {
                    faltaCampo = when {
                        especie == null -> "Escolha a espécie."
                        genero == null -> "Escolha o sexo."
                        else -> null
                    }
                    if (faltaCampo == null) {
                        val dados = DadosAnimal(
                            nome = nome, genero = requireNotNull(genero), especie = requireNotNull(especie), raca = raca, idadeMeses = idade.toIntOrNull(),
                            castrado = castrado, porte = porte ?: Porte.MEDIO, status = status ?: StatusAnimal.DISPONIVEL,
                            pesoKg = peso.replace(',', '.').toDoubleOrNull(), microchip = microchip.ifBlank { null },
                            caracteristicas = caracteristicas, descricao = descricao,
                        )
                        onSalvar(dados, microchip, peso, inicial != null && dados.status != inicial.status)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !estado.processando,
            )
        }
        Spacer(Modifier.navigationBarsPadding().height(24.dp))
    }
}
