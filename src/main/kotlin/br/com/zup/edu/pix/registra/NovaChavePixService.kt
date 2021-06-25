package br.com.zup.edu.pix.registra

import br.com.zup.edu.integration.itau.ContasDeClientesNoItauClient
import br.com.zup.edu.pix.ChavePix
import br.com.zup.edu.pix.ChavePixRepository
import br.com.zup.edu.shared.ChavePixExistenteException
import io.micronaut.validation.Validated
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val itauClient: ContasDeClientesNoItauClient,
    @Inject val repository: ChavePixRepository
){

    @Transactional
    fun registra(@Valid novaChave: NovaChavePix): ChavePix{

        // verifica se chave já existe no sistema
        if (repository.existsByChave(novaChave.chave)) {
            throw ChavePixExistenteException("Chave Pix '${novaChave.chave}' existente")
        }

        // buscar dados da conta no ERP-ITAU
        val response = itauClient.buscaContaPorTipo(novaChave.clienteId!!, novaChave.tipoDeConta!!.name)
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado no Itau")

        // gravar no banco de dados
        val chave = novaChave.toModel(conta)
        return repository.save(chave)
    }

}