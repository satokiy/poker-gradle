plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"

    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // Use the JUnit 5 integration.
    testImplementation(libs.junit.jupiter.engine)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api(libs.commons.math3)

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation(libs.guava)
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

tasks.register<Sample>("kotlin") {}

abstract class Sample : DefaultTask() {
    @TaskAction
    fun kotlin() {
        println("KotlinVersion >> " + KotlinVersion.CURRENT)
    }
}

tasks.register<Draw>("draw") {}

abstract class Draw : DefaultTask() {
    @TaskAction
    fun draw() {
        println(Deck().draw().toString())
    }
}

tasks.register<Judge>("judge") {
    args.set((project.findProperty("args") as String?)?.split(" ") ?: listOf())
}

abstract class Judge : DefaultTask() {
    @get:Input
    abstract val args: ListProperty<String>

    @TaskAction
    fun execute() {
        val input = args.get()
        val rank =
            validate(input).run {
                toHand(this)
                    .judge()
            }
        println(rank.name)
    }

    private fun validate(input: List<String>): List<String> {
        TODO("not implemented")
        return input
    }

    private fun toHand(input: List<String>): Hand {
        return Hand(input.map { Card.of(it) }.toTypedArray())
    }
}

class Hand(private val cards: Array<Card>) {
    fun judge(): Role {
        return when {
            isStraightFlush() -> Role.STRAIGHT_FLUSH
            isFourOfAKind() -> Role.FOUR_OF_A_KIND
            isFullHouse() -> Role.FULL_HOUSE
            isFlush() -> Role.FLUSH
            isStraight() -> Role.STRAIGHT
            isThreeOfAKind() -> Role.THREE_OF_A_KIND
            isTwoPair() -> Role.TWO_PAIR
            isOnePair() -> Role.ONE_PAIR
            else -> Role.HIGH_CARDS
        }
    }

    private fun isStraightFlush(): Boolean = isFlush() && isStraight()

    private fun isFlush() = cards.map { it.suit }.toSet().size == 1

    private fun isStraight(): Boolean {
        return isRoyalStraight() ||
            cards.map { it.rank.value }.toSet().let {
                it.size == 5 && (it.max() - it.min() == 4)
            }
    }

    private fun isRoyalStraight(): Boolean {
        return cards.map { it.rank }.toSet() ==
            setOf(
                Rank.ACE,
                Rank.KING,
                Rank.QUEEN,
                Rank.JACK,
                Rank.TEN,
            )
    }

    private fun isFourOfAKind(): Boolean {
        return cards.groupingBy { it.rank }.eachCount().let {
            it.size == 2 && it.containsValue(4)
        }
    }

    private fun isFullHouse(): Boolean {
        return cards.groupingBy { it.rank }.eachCount().let {
            it.size == 2 && it.containsValue(3) && it.containsValue(2)
        }
    }

    private fun isThreeOfAKind(): Boolean {
        return cards.groupingBy { it.rank }.eachCount().let {
            it.size == 3 && it.containsValue(3)
        }
    }

    private fun isTwoPair(): Boolean {
        return cards.groupingBy { it.rank }.eachCount().let {
            it.size == 3 && it.containsValue(2)
        }
    }

    private fun isOnePair(): Boolean {
        return cards.groupingBy { it.rank }.eachCount().let {
            it.size == 4 && it.containsValue(2)
        }
    }
}

enum class Suit(val symbol: String) {
    SPADE("♠"),
    HEART("♥"),
    DIAMOND("♦"),
    CLUB("♣"),
    ;

    companion object {
        fun of(symbol: String): Suit {
            return Suit.values().find {
                it.symbol == symbol
            } ?: throw Exception("can't convert to Suit. symbol: $symbol")
        }
    }
}

enum class Rank(val symbol: String, val value: Int) {
    TWO("2", 2),
    THREE("3", 3),
    FOUR("4", 4),
    FIVE("5", 5),
    SIX("6", 6),
    SEVEN("7", 7),
    EIGHT("8", 8),
    NINE("9", 9),
    TEN("10", 10),
    JACK("11", 11),
    QUEEN("12", 12),
    KING("13", 13),
    ACE("A", 1),
    ;

    companion object {
        fun of(symbol: String): Rank {
            return Rank.values().find {
                it.symbol == symbol
            } ?: throw Exception("can't convert to Rank. symbol: $symbol")
        }
    }
}

enum class Role(rank: Int) {
    STRAIGHT_FLUSH(1),
    FOUR_OF_A_KIND(2),
    FULL_HOUSE(3),
    FLUSH(4),
    STRAIGHT(5),
    THREE_OF_A_KIND(6),
    TWO_PAIR(7),
    ONE_PAIR(9),
    HIGH_CARDS(10),
}

class Card(val suit: Suit, val rank: Rank) {
    override fun toString(): String {
        return "${suit.symbol}${rank.symbol}"
    }

    companion object {
        fun of(card: String): Card {
            val suit = Suit.of(card.take(1))
            val rank = Rank.of(card.drop(1))
            return Card(suit, rank)
        }
    }
}

class Deck {
    private val cards: MutableList<Card> = mutableListOf()

    init {
        for (s in Suit.values()) {
            for (r in Rank.values()) {
                cards.add(Card(suit = s, rank = r))
            }
        }
        cards.shuffle()
    }

    fun draw(): Card {
        return cards.removeAt(0)
    }
}
