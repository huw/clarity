package nu.huw.clarity.model

data class ID(val value: String = IDHelper.generate()) {
    init {
        if (!IDHelper.validate(value)) throw IllegalArgumentException("Given ID $value does not validate")
    }

    /**
     * Let's be nice about comparing IDs with other things like Strings
     */
    override fun equals(other: Any?): Boolean {
        when (other) {
            is ID -> return value == other.value
            is String -> return value == other
            else -> return this == other
        }
    }

    override fun toString(): String {
        return value
    }

    /**
     * Little helper method for converting strings to IDs
     */
    fun String.toID(): ID {
        return ID(this)
    }
}