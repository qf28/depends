package depends.entity

import org.slf4j.LoggerFactory
import java.io.Serializable

class KotlinTypeEntity(simpleName: GenericName, parent: Entity?, id: Int)
    : TypeEntity(simpleName, parent, id), Serializable {
    var delegateProviderType: TypeEntity? = null
        internal set

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(KotlinTypeEntity::class.java)
    }
}