package br.com.zup.edu.pix.remove

import br.com.zup.edu.pix.ChavePixRepository
import br.com.zup.edu.shared.ChavePixNaoEncontradaException
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemoveChaveService(@Inject val repository: ChavePixRepository) {

    @Transactional
    fun remove(
        @NotBlank
        clienteId: String,
        @NotBlank
        pixId: String
    ){
        val uuidPixId = UUID.fromString(pixId)
        val uuidClienteId = UUID.fromString(clienteId)

        val chave = repository.findByIdAndClienteId(uuidPixId, uuidClienteId)
            .orElseThrow { ChavePixNaoEncontradaException("Chave Pix não encontrada ou não pertence ao cliente") }

        repository.delete(chave)

    }
}