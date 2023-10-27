package depends.extractor.kotlin

import depends.deptypes.DependencyType
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class KotlinAliasTest : KotlinParserTest() {
    @Before
    fun setUp() = super.init()

    override val myPackageName = "${packageName}.alias.alias"

    @Test
    fun shouldAliasSuccess0() {
        val src0 = "./src/test/resources/kotlin-code-examples/alias/alias0/package0/ChildAlias0.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/alias/alias0/package1/ParentAlias0.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        resolveAllBindings()
        val relations = entityRepo.getEntity("${myPackageName}0.package0.ChildAlias0").relations
        assertEquals(1, relations.size)
        assertEquals(DependencyType.INHERIT, relations[0].type)
        assertEquals("ParentAlias0", relations[0].entity.rawName.name)
    }
}