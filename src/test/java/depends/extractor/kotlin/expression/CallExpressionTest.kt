package depends.extractor.kotlin.expression

import depends.deptypes.DependencyType
import depends.extractor.kotlin.KotlinParserTest
import depends.extractor.kotlin.packageName
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CallExpressionTest : KotlinParserTest() {
    override val myPackageName: String = "$packageName.expression.call.call"

    @Before
    fun setUp() = super.init()

    @Test
    fun shouldHandleCallSuccess0() {
        val src0 = "./src/test/resources/kotlin-code-examples/expression/call/call0/ProviderCall0.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/expression/call/call0/TestCall0.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        resolveAllBindings()
        run {
            val relations = entityRepo.getEntity("${myPackageName}0.TestCall0.test0").relations
            assertEquals(
                    setOf(DependencyType.CALL, DependencyType.USE,
                            DependencyType.CONTAIN, DependencyType.CREATE),
                    relations.map { it.type }.toSet()
            )
            for (relation in relations) {
                when (relation.type) {
                    DependencyType.CALL -> {
                        assertTrue(relation.entity.rawName.name == "func0"
                                || relation.entity.rawName.name == "func1"
                                || relation.entity.rawName.name == "ProviderCall0")
                    }

                    DependencyType.USE -> {
                        assertTrue(relation.entity.rawName.name == "providerCall0"
                                || relation.entity.rawName.name == "ProviderCall0")
                    }

                    DependencyType.CONTAIN -> {
                        assertEquals("ProviderCall0", relation.entity.rawName.name)
                    }

                    DependencyType.CREATE -> {
                        assertEquals("ProviderCall0", relation.entity.rawName.name)
                    }
                }
            }
        }
    }

    @Test
    fun shouldHandleContinuousCallSuccess1() {
        val src0 = "./src/test/resources/kotlin-code-examples/expression/call/call1/ProviderCall1.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/expression/call/call1/TestCall1.kt"
        val src2 = "./src/test/resources/kotlin-code-examples/expression/call/call1/MiddleTypeCall1.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        parser.parse(src2)
        resolveAllBindings()
        run {
            val relations = entityRepo.getEntity("${myPackageName}1.TestCall1.test0").relations
            assertEquals(
                    setOf(DependencyType.CALL, DependencyType.USE,
                            DependencyType.CONTAIN, DependencyType.CREATE),
                    relations.map { it.type }.toSet()
            )
            for (relation in relations) {
                when (relation.type) {
                    DependencyType.CALL -> {
                        assertTrue(relation.entity.rawName.name == "func0"
                                || relation.entity.rawName.name == "funcInMiddleType"
                                || relation.entity.rawName.name == "ProviderCall1")
                    }

                    DependencyType.USE -> {
                        assertTrue(relation.entity.rawName.name == "providerCall1"
                                || relation.entity.rawName.name == "ProviderCall1"
                                || relation.entity.rawName.name == "MiddleTypeCall1")
                    }

                    DependencyType.CONTAIN -> {
                        assertEquals("ProviderCall1", relation.entity.rawName.name)
                    }

                    DependencyType.CREATE -> {
                        assertEquals("ProviderCall1", relation.entity.rawName.name)
                    }
                }
            }
        }
    }

    @Test
    fun shouldHandleCallSuccess2() {
        val src0 = "./src/test/resources/kotlin-code-examples/expression/call/call2/ProviderCall2.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/expression/call/call2/TestCall2.kt"
        val src2 = "./src/test/resources/kotlin-code-examples/expression/call/call2/InterfaceCall2.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        parser.parse(src2)
        resolveAllBindings()
        run {
            val relations = entityRepo.getEntity("${myPackageName}2.TestCall2.test0").relations
            assertEquals(
                    setOf(DependencyType.CALL, DependencyType.USE,
                            DependencyType.CONTAIN, DependencyType.CREATE),
                    relations.map { it.type }.toSet()
            )
            for (relation in relations) {
                when (relation.type) {
                    DependencyType.CALL -> {
                        assertTrue(relation.entity.rawName.name == "func0"
                                || relation.entity.rawName.name == "ProviderCall2")
                    }

                    DependencyType.USE -> {
                        assertTrue(relation.entity.rawName.name == "providerCall2"
                                || relation.entity.rawName.name == "ProviderCall2")
                    }

                    DependencyType.CONTAIN -> {
                        assertEquals("InterfaceCall2", relation.entity.rawName.name)
                    }

                    DependencyType.CREATE -> {
                        assertEquals("ProviderCall2", relation.entity.rawName.name)
                    }
                }
            }
        }
    }

    @Test
    fun shouldHandleCallInStringLiteralSuccess3() {
        val src0 = "./src/test/resources/kotlin-code-examples/expression/call/call3/ProviderCall3.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/expression/call/call3/TestCall3.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        resolveAllBindings()
        run {
            val relations = entityRepo.getEntity("${myPackageName}3.TestCall3.test0").relations
            assertEquals(
                    setOf(DependencyType.CALL, DependencyType.USE,
                            DependencyType.CONTAIN, DependencyType.CREATE),
                    relations.map { it.type }.toSet()
            )
            for (relation in relations) {
                when (relation.type) {
                    DependencyType.CALL -> {
                        assertTrue(relation.entity.rawName.name == "func0"
                                || relation.entity.rawName.name == "ProviderCall3"
                                || relation.entity.rawName.name == "func1")
                    }

                    DependencyType.USE -> {
                        assertTrue(relation.entity.rawName.name == "providerCall3"
                                || relation.entity.rawName.name == "ProviderCall3"
                                || relation.entity.rawName.name == "str" // str is a local variable
                        )
                    }

                    DependencyType.CONTAIN -> {
                        assertEquals("ProviderCall3", relation.entity.rawName.name)
                    }

                    DependencyType.CREATE -> {
                        assertEquals("ProviderCall3", relation.entity.rawName.name)
                    }
                }
            }
        }
    }
}