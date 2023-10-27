package depends.generator

import depends.entity.Entity
import depends.entity.repo.EntityRepo


/**
 * kotlin class dependency generator
 * @constructor Create empty Class dependency generator
 */
class ClassDependencyGenerator : DependencyGenerator() {
    companion object {
        const val TYPE = "class"
    }

    override fun getType(): String = TYPE

    override fun upToOutputLevelEntityId(entityRepo: EntityRepo?, entity: Entity?): Int {
        TODO("Not yet implemented")
    }

    override fun nameOf(entity: Entity?): String {
        TODO("Not yet implemented")
    }

    override fun outputLevelMatch(entity: Entity?): Boolean {
        TODO("Not yet implemented")
    }
}