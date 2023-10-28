package depends.extractor.kotlin.expression

import depends.deptypes.DependencyType
import depends.extractor.kotlin.KotlinParserTest
import depends.extractor.kotlin.packageName
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class AsExpressionTest : KotlinParserTest() {
    override val myPackageName: String = "$packageName.expression.asExpression.as"

    @Before
    fun setUp() = super.init()

    @Test
    fun shouldAliasSuccess0() {
        val src0 = "./src/test/resources/kotlin-code-examples/expression/asExpression/as0/ChildAs0.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/expression/asExpression/as0/ProviderAs0.kt"
        val src2 = "./src/test/resources/kotlin-code-examples/expression/asExpression/as0/UserAs0.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        parser.parse(src2)
        resolveAllBindings()

        run {
            val relationsChildAs0 = entityRepo.getEntity("${myPackageName}0.ChildAs0").relations
            assertEquals(1, relationsChildAs0.size)
            assertEquals(DependencyType.INHERIT, relationsChildAs0[0].type)
            assertEquals("ProviderAs0", relationsChildAs0[0].entity.rawName.name)
        }
        run {
            val relationsUserAs0 = entityRepo.getEntity("${myPackageName}0.UserAs0").relations
            assertEquals(1, relationsUserAs0.size)
            assertEquals(DependencyType.CAST, relationsUserAs0[0].type)
            assertEquals("ProviderAs0", relationsUserAs0[0].entity.rawName.name)
        }
    }
}