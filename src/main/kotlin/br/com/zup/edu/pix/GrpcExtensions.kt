package br.com.zup.edu.pix

import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.pix.registra.NovaChavePix


fun RegistraChavePixRequest.toModel() : NovaChavePix {
    return NovaChavePix(
        clienteId = clienteId,
        tipo = when (tipoDeChave) {
            br.com.zup.edu.TipoDeChave.UNKNOWN_TIPO_CHAVE -> null
            else -> TipoDeChave.valueOf(tipoDeChave.name)
        },
        chave = chave,
        tipoDeConta = when (tipoDeConta) {
            br.com.zup.edu.TipoDeConta.UNKNOWN_TIPO_CONTA -> null
            else -> TipoDeConta.valueOf(tipoDeConta.name)
        }
    )
}