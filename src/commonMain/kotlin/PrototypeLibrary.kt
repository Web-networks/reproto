package raid.neuroide.reproto

expect class PlatformPrototypeLibrary() {
    suspend fun requestPrototype(id: String, initializer: suspend (String) -> Prototype?): Prototype?
}

class PrototypeLibrary(private val initializer: suspend (String) -> Prototype?) {
    private val platformLibrary = PlatformPrototypeLibrary()

    suspend fun requestPrototype(id: String) = platformLibrary.requestPrototype(id, initializer)
}
