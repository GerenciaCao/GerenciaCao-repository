package com.example.gerenciacao.data.remote

import com.example.gerenciacao.domain.model.Abrigo
import com.example.gerenciacao.domain.model.Animal
import com.example.gerenciacao.domain.model.AnimalDetalhe
import com.example.gerenciacao.domain.model.ConsultaResumo
import com.example.gerenciacao.domain.model.Especie
import com.example.gerenciacao.domain.model.Genero
import com.example.gerenciacao.domain.model.Porte
import com.example.gerenciacao.domain.model.StatusAnimal
import com.example.gerenciacao.domain.model.VacinaResumo
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import com.example.gerenciacao.domain.model.Cargo
import com.example.gerenciacao.domain.model.Membro
import com.example.gerenciacao.domain.model.Permissao
import com.example.gerenciacao.domain.model.StatusConta

// Conversões DTO (formato da API) -> modelo de domínio (formato que as telas usam).

/** Converte o usuário da API em [Membro]. Valores desconhecidos recebem um padrão seguro (menos acesso). */
fun UsuarioDto.paraDominio() = Membro(
    id = id,
    abrigoId = abrigoId,
    nome = nome,
    sobrenome = sobrenome,
    email = email,
    telefone = telefone,
    endereco = endereco,
    cargo = runCatching { Cargo.valueOf(cargo) }.getOrDefault(Cargo.VOLUNTARIO),
    permissoes = permissoes.mapNotNull { runCatching { Permissao.valueOf(it) }.getOrNull() }.toSet(),
    status = runCatching { StatusConta.valueOf(status) }.getOrDefault(StatusConta.INATIVO),
)

/** Converte o abrigo da API em [Abrigo]. */
fun AbrigoDto.paraDominio() = Abrigo(
    id = id,
    nome = nome,
    descricao = descricao,
    telefone = telefone,
    email = email,
    endereco = endereco,
    redesSociais = redesSociais,
    capacidade = capacidade,
    animaisTotais = animaisTotais,
)

/** Converte o animal da API em [Animal]. */
fun AnimalDto.paraDominio() = Animal(
    id = id,
    nome = nome,
    genero = runCatching { Genero.valueOf(genero) }.getOrDefault(Genero.FEMEA),
    especie = runCatching { Especie.valueOf(especie) }.getOrDefault(Especie.CACHORRO),
    raca = raca,
    idadeMeses = idadeMeses,
    castrado = castrado,
    porte = runCatching { Porte.valueOf(porte) }.getOrDefault(Porte.MEDIO),
    status = runCatching { StatusAnimal.valueOf(status) }.getOrDefault(StatusAnimal.DISPONIVEL),
    pesoKg = pesoKg,
    caracteristicas = caracteristicas,
    descricao = descricao,
    microchip = microchip,
)

/** Data ISO (ex.: "2026-03-01T09:00:00Z") -> LocalDate; null se vier vazia ou inválida. */
private fun String.paraData(): LocalDate? = runCatching { Instant.parse(this).atZone(ZoneOffset.UTC).toLocalDate() }.getOrNull()

/** Converte a ficha completa da API. */
fun AnimalDetalheDto.paraDominio() = AnimalDetalhe(
    animal = animal.paraDominio(),
    vacinas = vacinas.map { VacinaResumo(it.nome, it.data.paraData()) },
    consultas = historico.map { ConsultaResumo(it.dataConsulta.paraData(), it.diagnostico, it.veterinario) },
)

/** Idade aproximada em meses -> data de nascimento ISO (para enviar à API). */
fun nascimentoPorIdade(meses: Int?): String? =
    meses?.let { LocalDate.now(ZoneOffset.UTC).minusMonths(it.toLong()).atStartOfDay(ZoneOffset.UTC).toInstant().toString() }
