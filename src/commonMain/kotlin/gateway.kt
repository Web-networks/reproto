package raid.neuroide.reproto

interface ChangesGateway {
    fun subscribe(processor: UpdateProcessor)
}

interface InitialGateway {
    suspend fun load(id: String): String?
}

interface UpdateProcessor {
    fun process(update: String)
}
