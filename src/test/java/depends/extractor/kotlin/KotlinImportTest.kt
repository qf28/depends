package depends.extractor.kotlin

import depends.deptypes.DependencyType
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KotlinImportTest : KotlinParserTest() {
    @Before
    fun setUp() = init()
    override val myPackageName = "${packageName}.import.import"

    @Test
    fun shouldHandleImportAllSuccess() {
        val srcs = arrayOf(
                "./src/test/resources/kotlin-code-examples/import/import0/package0/ChildImport0.kt",
                "./src/test/resources/kotlin-code-examples/import/import0/package1/ParentImport0.kt",
                "./src/test/resources/kotlin-code-examples/import/import0/package1/Interface0Import0.kt",
                "./src/test/resources/kotlin-code-examples/import/import0/package1/Interface1Import0.kt"
        )
        val parser = createParser()
        srcs.forEach {
            parser.parse(it)
        }
        resolveAllBindings()
        val relations = entityRepo.getEntity("${myPackageName}0.package0.ChildImport0").relations
        assertEquals(3, relations.size)
        assertEquals(
                setOf(DependencyType.INHERIT, DependencyType.IMPLEMENT),
                relations.map { it.type }.toSet()
        )
        for (relation in relations) {
            if (relation.type == DependencyType.INHERIT) {
                assertEquals("ParentImport0", relation.entity.rawName.name)
            } else {
                assertTrue(
                        relation.entity.rawName.name == "Interface0Import0"
                        || relation.entity.rawName.name == "Interface1Import0"
                )
            }
        }
    }
}