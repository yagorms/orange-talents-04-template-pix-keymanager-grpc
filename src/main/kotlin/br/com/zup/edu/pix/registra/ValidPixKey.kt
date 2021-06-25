package br.com.zup.edu.pix.registra

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.TYPE
import kotlin.reflect.KClass

@MustBeDocumented
@Target(CLASS, TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = [ValidPixKeyValidator::class])
annotation class ValidPixKey(
    val message: String = "chave Pix inválida (\${validatedValue.tipo})",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = [],
)


@Singleton
class ValidPixKeyValidator: javax.validation.ConstraintValidator<ValidPixKey, NovaChavePix> {

    override fun isValid(value: NovaChavePix?, context: javax.validation.ConstraintValidatorContext): Boolean {

        // must be validated with @NotNull
        if (value?.tipo == null) {
            return true
        }

        val valid = value.tipo.valida(value.chave)
        if (!valid) {
            context.disableDefaultConstraintViolation()
            context
                .buildConstraintViolationWithTemplate(context.defaultConstraintMessageTemplate) // or "chave Pix inválida (${value.tipo})"
                .addPropertyNode("chave").addConstraintViolation()
        }

        return valid
    }
}


class ValidPixKeyValidatorUsingMicronautSupport: ConstraintValidator<ValidPixKey, NovaChavePix> {

    override fun isValid(
        value: NovaChavePix?,
        annotationMetadata: AnnotationValue<ValidPixKey>,
        context: ConstraintValidatorContext,
    ): Boolean {

        // must be validated with @NotNull
        if (value?.tipo == null) {
            return true
        }

        return value.tipo.valida(value.chave)
    }

}