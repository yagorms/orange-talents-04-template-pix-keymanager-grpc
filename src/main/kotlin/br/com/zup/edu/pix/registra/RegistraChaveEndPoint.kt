package br.com.zup.edu.pix.registra

import br.com.zup.edu.KeymanagerGrpcServiceGrpc
import br.com.zup.edu.KeymanagerRegistraGrpcServiceGrpc
import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.RegistraChavePixResponse
import br.com.zup.edu.pix.toModel
import br.com.zup.edu.shared.handler.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RegistraChaveEndPoint(@Inject val service: NovaChavePixService): KeymanagerRegistraGrpcServiceGrpc.KeymanagerRegistraGrpcServiceImplBase() {

    override fun registra(
        request: RegistraChavePixRequest,
        responseObserver: StreamObserver<RegistraChavePixResponse>
    ) {

        val novaChave = request.toModel()
        val chaveCriada = service.registra(novaChave)

        responseObserver.onNext(RegistraChavePixResponse.newBuilder()
            .setClienteId(chaveCriada.clienteId.toString())
            .setPixId(chaveCriada.id.toString())
            .build())
        responseObserver.onCompleted()
    }
}