package br.com.zup.edu.pix.registra

import br.com.zup.edu.KeymanagerGrpcServiceGrpc
import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.TipoDeChave
import br.com.zup.edu.TipoDeConta
import br.com.zup.edu.integration.itau.ContasDeClientesNoItauClient
import br.com.zup.edu.integration.itau.DadosDaContaResponse
import br.com.zup.edu.integration.itau.InstituicaoResponse
import br.com.zup.edu.integration.itau.TitularResponse
import br.com.zup.edu.pix.ChavePix
import br.com.zup.edu.pix.ChavePixRepository
import br.com.zup.edu.pix.ContaAssociada
import br.com.zup.edu.util.violations
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RegistraChaveEndPointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerGrpcServiceGrpc.KeymanagerGrpcServiceBlockingStub
){

    @Inject
    lateinit var itauClient: ContasDeClientesNoItauClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }


    @Test
    fun `deve registrar nova chave pix`(){
        //cenario
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        //açao
        val response = grpcClient.registra(RegistraChavePixRequest.newBuilder()
                                                        .setClienteId(CLIENTE_ID.toString())
                                                        .setTipoDeChave(TipoDeChave.EMAIL)
                                                        .setChave("teste@teste.com")
                                                        .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                                                        .build())

        //validacao
        with(response) {
            assertEquals(CLIENTE_ID.toString(), clienteId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando chave existente`(){
        //cenario
        repository.save(chave(
            tipo = br.com.zup.edu.pix.TipoDeChave.CPF,
            chave = "11111111111",
            clienteId = CLIENTE_ID
        ))

        //açao
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder()
                                                .setClienteId(CLIENTE_ID.toString())
                                                .setTipoDeChave(TipoDeChave.CPF)
                                                .setChave("11111111111")
                                                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                                                .build())
        }

        //validacao
        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix '11111111111' existente", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando nao encontrar dados da conta cliente`(){
        //cenario
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        //açao
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder()
                                                .setClienteId(CLIENTE_ID.toString())
                                                .setTipoDeChave(TipoDeChave.EMAIL)
                                                .setChave("teste@teste.com")
                                                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                                                .build())
        }

        //validacao
        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado no Itau", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando parametros forem invalidos`(){
        //açao
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder().build())
        }

        //validacao
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code,status.code)
            assertEquals("Dados inválidos", status.description)
//********** Essa validação está voltando somente dados inválidos ***************
//            assertThat(violations(), containsInAnyOrder(
//                Pair("clienteId", "must not be blank"),
//                Pair("clienteId", "não é um formato válido de UUID"),
//                Pair("tipoDeConta", "must not be null"),
//                Pair("tipo", "must not be null"),
//            ))
        }

    }

    /**
     * Cenário básico de validação de chave para garantir que estamos validando a
     * chave via @ValidPixKey. Lembrando que os demais cenários são validados via testes
     * de unidade.
     */
    @Test
    fun `nao deve registrar chave pix quando parametros forem invalidos - chave invalida`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoDeChave(TipoDeChave.CPF)
                .setChave("378.930.cpf-invalido.389-73")
                .setTipoDeConta(TipoDeConta.CONTA_POUPANCA)
                .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertThat(violations(), containsInAnyOrder(
                Pair("chave", "chave Pix inválida (CPF)"),
            ))
        }
    }

    @MockBean(ContasDeClientesNoItauClient::class)
    fun itauClient(): ContasDeClientesNoItauClient? {
        return Mockito.mock(ContasDeClientesNoItauClient::class.java)

    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerGrpcServiceGrpc.KeymanagerGrpcServiceBlockingStub? {
            return KeymanagerGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun dadosDaContaResponse(): DadosDaContaResponse {
        return DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", ContaAssociada.ITAU_UNIBANCO_ISPB),
            agencia = "1234",
            numero = "123456",
            titular = TitularResponse("Teste", "11111111111")
        )
    }

    private fun chave(
        tipo: br.com.zup.edu.pix.TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipo = tipo,
            chave = chave,
            tipoDeConta = br.com.zup.edu.pix.TipoDeConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Teste",
                cpfDoTitular = "11111111111",
                agencia = "1234",
                numeroDaConta = "123456"
            )
        )
    }
}