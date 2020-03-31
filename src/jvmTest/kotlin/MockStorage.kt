package raid.neuroide.reproto

class MockStorage : LoadGateway, StoreGateway {
    private val storage: MutableMap<String, String> = mutableMapOf()

    override suspend fun load(id: String): String? {
        return storage[id]
    }

    override suspend fun store(id: String, prototype: String) {
        storage[id] = prototype
    }
}