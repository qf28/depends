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
    fun shouldCallSuccess0() {
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
}