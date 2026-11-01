package jpark.bro.days.ui.state

sealed class FilterOption {
    object All : FilterOption() {
        override fun toString() = "전체"
    }

    data class Yearly(
        val year: Int,
    ) : FilterOption() {
        override fun toString() = "${year}년"

        override fun equals(other: Any?): Boolean = other is Yearly && other.year == this.year

        override fun hashCode(): Int = year.hashCode()
    }

    data class Monthly(
        val year: Int,
        val month: Int,
    ) : FilterOption() {
        override fun toString() = "${year}년 ${month}월"

        override fun equals(other: Any?): Boolean =
            other is Monthly && other.year == this.year && other.month == this.month

        override fun hashCode(): Int = 31 * year.hashCode() + month.hashCode()
    }
}
