package br.com.zup.edu.shared

import java.lang.RuntimeException

class ChavePixExistenteException(message: String?): RuntimeException(message)
class ChavePixNaoEncontradaException(message: String) : RuntimeException(message)