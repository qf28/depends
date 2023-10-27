package depends.entity

import depends.relations.IBindingResolver
import org.slf4j.LoggerFactory
import java.util.Collections

class KotlinTypeEntity(simpleName: GenericName, parent: Entity?, id: Int)
    : TypeEntity(simpleName, parent, id) {
    var delegateProvider: GenericName? = null
    var delegateProviderType: TypeEntity? = null
        private set

    override fun inferLocalLevelEntities(bindingResolver: IBindingResolver?) {
        if (delegateProvider != null) {
            val r = identifierToEntities(bindingResolver, Collections.singleton(delegateProvider))
            r.forEach {
                if (it.type != null) {
                    delegateProviderType = it.type
                } else {
                    logger.warn("${it.rawName} expected a type, but actually it is ${it.javaClass.simpleName}")
                }
            }
        }
        super.inferLocalLevelEntities(bindingResolver)
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(KotlinTypeEntity::class.java)
    }
}