package raid.neuroide.reproto 

actual class PlatformPrototypeLibrary {
    actual suspend fun requestPrototype(
        id: String,
        initializer: suspend (String) -> Prototype?
    ): Prototype? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
