package raid.neuroide.reproto

class MockStorage : PrototypeStorageGateway {
    private val storage: MutableMap<String, String> = mutableMapOf()

    override fun load(id: String): String? {
        return storage[id]
    }

    override fun store(id: String, prototype: String) {
        storage[id] = prototype
    }
}