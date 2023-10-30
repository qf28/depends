package depends.extractor.kotlin

import depends.entity.GenericName
import depends.entity.KotlinExpression
import depends.entity.KotlinTypeEntity
import depends.entity.TypeEntity
import depends.entity.repo.EntityRepo
import depends.extractor.java.JavaHandlerContext
import depends.relations.IBindingResolver
import org.slf4j.LoggerFactory

class KotlinHandlerContext(entityRepo: EntityRepo, bindingResolver: IBindingResolver)
    : JavaHandlerContext(entityRepo, bindingResolver) {

    override fun foundNewType(name: GenericName, startLine: Int): TypeEntity {
        val currentTypeEntity = KotlinTypeEntity(name, latestValidContainer(),
                idGenerator.generateId())
        currentTypeEntity.line = startLine
        pushToStack(currentTypeEntity)
        addToRepo(currentTypeEntity)
        currentFileEntity.addType(currentTypeEntity)
        return currentTypeEntity
    }

    open fun foundNewDelegation(delegationExpression: KotlinExpression) {
        val currentType = currentType()
        if (currentType is KotlinTypeEntity) {
            delegationExpression.deducedTypeDelegates.add(currentType)
        } else {
            logger.warn("currentType should be ${KotlinTypeEntity::class.simpleName}")
        }
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(KotlinHandlerContext::class.java)
    }
}