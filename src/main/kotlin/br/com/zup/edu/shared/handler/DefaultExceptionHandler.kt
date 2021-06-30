package br.com.zup.edu.shared.handler

import br.com.zup.edu.shared.ChavePixExistenteException
import br.com.zup.edu.shared.ChavePixNaoEncontradaException
import br.com.zup.edu.shared.handler.ExceptionHandler.StatusWithDetails
import io.grpc.Status
import javax.validation.ConstraintViolationException

/**
 * By design, this class must NOT be managed by Micronaut
 */
class DefaultExceptionHandler : ExceptionHandler<Exception> {

    override fun handle(e: Exception): StatusWithDetails {
        val status = when (e) {
            is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(e.message)
            is ConstraintViolationException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            is ChavePixExistenteException -> Status.ALREADY_EXISTS.withDescription(e.message)
            is ChavePixNaoEncontradaException -> Status.NOT_FOUND.withDescription(e.message)
            else -> Status.UNKNOWN
        }
        return StatusWithDetails(status.withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return true
    }

}