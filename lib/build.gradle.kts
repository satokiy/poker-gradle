plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)

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
        println("KotlinVersion >> " + KotlinVersion.CURRENT)  // =>  KotlinVersion >> 1.8.10
    }
}

tasks.register<Draw>("draw") {}
abstract class Draw : DefaultTask() {
    @TaskAction
    fun draw() {
        val deck = Deck()
        val card = deck.draw()
        println(card.toString())
    }
}

enum class Suit(val symbol: String) {
    SPADE("♠"),
    HEART("♥"),
    DIAMOND("♦"),
    CLUB("♣"),
    ;
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
}

class Card(val suit: Suit, val rank: Rank) {
    override fun toString(): String {
        return "${suit.symbol}${rank.symbol}"
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
