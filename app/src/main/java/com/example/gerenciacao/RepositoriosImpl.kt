package com.example.gerenciacao.data.repository

import com.example.gerenciacao.data.local.SessaoManager
import com.example.gerenciacao.data.remote.AtualizarAnimalRequestDto
import com.example.gerenciacao.data.remote.MudarStatusRequestDto
import com.example.gerenciacao.data.remote.NovoAnimalRequestDto
import com.example.gerenciacao.data.remote.nascimentoPorIdade
import com.example.gerenciacao.domain.model.Animal
import com.example.gerenciacao.domain.model.AnimalDetalhe
import com.example.gerenciacao.domain.model.DadosAnimal
import com.example.gerenciacao.domain.repository.AnimalRepository
import com.example.gerenciacao.data.remote.AbrigoDto
import com.example.gerenciacao.data.remote.AceitarConviteRequestDto
import com.example.gerenciacao.data.remote.ApiService
import com.example.gerenciacao.data.remote.AtualizarMembroRequestDto
import com.example.gerenciacao.data.remote.AuthResponseDto
import com.example.gerenciacao.data.remote.CadastroRequestDto
import com.example.gerenciacao.data.remote.LoginRequestDto
import com.example.gerenciacao.data.remote.NovoMembroRequestDto
import com.example.gerenciacao.data.remote.chamarApi
import com.example.gerenciacao.data.remote.paraDominio
import com.example.gerenciacao.data.remote.paraErroApp
import com.example.gerenciacao.domain.Resultado
import com.example.gerenciacao.domain.model.Abrigo
import com.example.gerenciacao.domain.model.AtualizacaoMembro
import com.example.gerenciacao.domain.model.Convite
import com.example.gerenciacao.domain.model.EstadoSessao
import com.example.gerenciacao.domain.model.Membro
import com.example.gerenciacao.domain.model.NovoMembro
import com.example.gerenciacao.domain.repository.AbrigoRepository
import com.example.gerenciacao.domain.repository.AuthRepository
import com.example.gerenciacao.domain.repository.EquipeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import retrofit2.HttpException

/** Implementação de [AuthRepository]: fala com a API e cuida do token na [SessaoManager]. */
class AuthRepositoryImpl(
    private val api: ApiService,
    private val sessao: SessaoManager,
    private val json: Json,
) : AuthRepository {

    override val estado: StateFlow<EstadoSessao> = sessao.estado

    override suspend fun login(email: String, senha: String): Resultado<Membro> =
        iniciarSessao { api.login(LoginRequestDto(email.trim(), senha)) }

    override suspend fun cadastrar(nome: String, email: String, telefone: String, senha: String): Resultado<Membro> =
        iniciarSessao { api.cadastrar(CadastroRequestDto(nome.trim(), email.trim(), telefone.trim(), senha)) }

    override suspend fun aceitarConvite(codigo: String, senha: String): Resultado<Membro> =
        iniciarSessao { api.aceitarConvite(AceitarConviteRequestDto(codigo.trim(), senha)) }

    /** Chama a API e, se der certo, guarda o token e marca a sessão como logada. */
    private suspend fun iniciarSessao(chamada: suspend () -> AuthResponseDto): Resultado<Membro> =
        when (val r = chamarApi(json, chamada)) {
            is Resultado.Sucesso -> {
                val usuario = r.dados.usuario.paraDominio()
                sessao.iniciar(r.dados.token, usuario)
                Resultado.Sucesso(usuario)
            }
            is Resultado.Erro -> r
        }

    override suspend fun restaurarSessao() {
        if (sessao.token() == null) {
            sessao.semSessao()
            return
        }
        try {
            sessao.definirUsuario(api.eu().paraDominio())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 401: o interceptor já encerrou a sessão. Qualquer outro erro (ex.: sem internet): pede login de novo,
            // mas mantém o token guardado se o erro não foi de autenticação.
            if (e is HttpException && e.code() == 401) sessao.encerrar() else sessao.semSessao()
        }
    }

    override fun sair() = sessao.encerrar()
}

/** Implementação de [EquipeRepository]. */
class EquipeRepositoryImpl(private val api: ApiService, private val json: Json) : EquipeRepository {

    override suspend fun listar(): Resultado<List<Membro>> =
        chamarApi(json) { api.listarEquipe().map { it.paraDominio() } }

    override suspend fun buscar(id: String): Resultado<Membro> =
        chamarApi(json) { api.membro(id).paraDominio() }

    override suspend fun convidar(novo: NovoMembro): Resultado<Convite> = chamarApi(json) {
        val r = api.convidar(
            NovoMembroRequestDto(novo.nome.trim(), novo.sobrenome.trim(), novo.email.trim(), novo.telefone.trim(), novo.endereco.trim(), novo.cargo.name),
        )
        Convite(r.membro.paraDominio(), r.conviteToken)
    }

    override suspend fun atualizar(id: String, alteracao: AtualizacaoMembro): Resultado<Membro> = chamarApi(json) {
        api.atualizarMembro(
            id,
            AtualizarMembroRequestDto(
                email = alteracao.email?.trim(),
                telefone = alteracao.telefone?.trim(),
                endereco = alteracao.endereco?.trim(),
                cargo = alteracao.cargo?.name,
                permissoes = alteracao.permissoes?.map { it.name },
            ),
        ).paraDominio()
    }

    override suspend fun remover(id: String): Resultado<Unit> = chamarApi(json) { api.removerMembro(id) }
}

/** Implementação de [AbrigoRepository]. */
class AbrigoRepositoryImpl(private val api: ApiService, private val json: Json) : AbrigoRepository {
    override suspend fun buscar(): Resultado<Abrigo> = chamarApi(json) { api.abrigo().paraDominio() }
}

/** Implementação de [AnimalRepository]. */
class AnimalRepositoryImpl(private val api: ApiService, private val json: Json) : AnimalRepository {

    override suspend fun listar(): Resultado<List<Animal>> =
        chamarApi(json) { api.animais(pagina = 0, tamanho = 100).itens.map { it.paraDominio() } }

    override suspend fun detalhe(id: String): Resultado<AnimalDetalhe> = chamarApi(json) { api.animal(id).paraDominio() }

    override suspend fun criar(dados: DadosAnimal): Resultado<Animal> = chamarApi(json) {
        api.criarAnimal(
            NovoAnimalRequestDto(
                nome = dados.nome.trim(), genero = dados.genero.name, especie = dados.especie.name, raca = dados.raca.trim().ifEmpty { "SRD" },
                nascimento = nascimentoPorIdade(dados.idadeMeses), castrado = dados.castrado, porte = dados.porte.name, status = dados.status.name,
                microchip = dados.microchip?.trim()?.ifEmpty { null }, pesoKg = dados.pesoKg,
                caracteristicas = dados.caracteristicas.trim(), descricao = dados.descricao.trim(),
            ),
        ).paraDominio()
    }

    override suspend fun atualizar(id: String, dados: DadosAnimal, statusMudou: Boolean): Resultado<Animal> = chamarApi(json) {
        val atualizado = api.atualizarAnimal(
            id,
            AtualizarAnimalRequestDto(
                nome = dados.nome.trim(), genero = dados.genero.name, especie = dados.especie.name, raca = dados.raca.trim().ifEmpty { "SRD" },
                nascimento = nascimentoPorIdade(dados.idadeMeses), castrado = dados.castrado, porte = dados.porte.name,
                microchip = dados.microchip?.trim()?.ifEmpty { null }, pesoKg = dados.pesoKg,
                caracteristicas = dados.caracteristicas.trim(), descricao = dados.descricao.trim(),
            ),
        )
        (if (statusMudou) api.statusAnimal(id, MudarStatusRequestDto(dados.status.name)) else atualizado).paraDominio()
    }

    override suspend fun remover(id: String): Resultado<Unit> = chamarApi(json) { api.removerAnimal(id) }
}
