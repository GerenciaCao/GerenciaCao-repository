package com.example.gerenciacao.domain.model

import java.time.LocalDate

enum class Genero(val rotulo: String) { FEMEA("Fêmea"), MACHO("Macho") }

enum class Especie(val rotulo: String, val plural: String) { CACHORRO("Cão", "Cães"), GATO("Gato", "Gatos") }

enum class Porte(val rotulo: String) { PEQUENO("Pequeno"), MEDIO("Médio"), GRANDE("Grande") }

/** Situação do bichinho. EM_PROCESSO_ADOCAO e ADOTADO são controlados pelo processo de adoção. */
enum class StatusAnimal(val rotulo: String) {
    DISPONIVEL("Disponível"),
    EM_PROCESSO_ADOCAO("Em Processo de Adoção"),
    EM_TRATAMENTO("Em Tratamento"),
    LAR_TEMPORARIO("Lar Temporário"),
    ADOTADO("Adotado"),
}

/** Ficha resumida de um bichinho (lista e ficha individual). */
data class Animal(
    val id: String,
    val nome: String,
    val genero: Genero,
    val especie: Especie,
    val raca: String,
    val idadeMeses: Int?,
    val castrado: Boolean,
    val porte: Porte,
    val status: StatusAnimal,
    val pesoKg: Double?,
    val caracteristicas: String,
    val descricao: String,
    val microchip: String?,
) {
    /** Verdadeiro se for fêmea. */
    val femea: Boolean get() = genero == Genero.FEMEA

    /** Idade em texto amigável: "5 Meses" ou "2 Anos". */
    val idadeTexto: String get() = idadeMeses?.let(::textoIdade) ?: "Idade não informada"
}

/** Converte meses em "N Meses" (menos de 1 ano) ou "N Anos". */
fun textoIdade(meses: Int): String =
    if (meses < 12) "$meses ${if (meses == 1) "Mês" else "Meses"}" else (meses / 12).let { "$it ${if (it == 1) "Ano" else "Anos"}" }

data class VacinaResumo(val nome: String, val data: LocalDate?)
data class ConsultaResumo(val data: LocalDate?, val diagnostico: String, val veterinario: String)

/** Ficha completa: dados + vacinas + histórico médico. */
data class AnimalDetalhe(val animal: Animal, val vacinas: List<VacinaResumo>, val consultas: List<ConsultaResumo>)

/** Dados do formulário de ficha (criar e editar). */
data class DadosAnimal(
    val nome: String,
    val genero: Genero,
    val especie: Especie,
    val raca: String,
    val idadeMeses: Int?,
    val castrado: Boolean,
    val porte: Porte,
    val status: StatusAnimal,
    val pesoKg: Double?,
    val microchip: String?,
    val caracteristicas: String,
    val descricao: String,
)
