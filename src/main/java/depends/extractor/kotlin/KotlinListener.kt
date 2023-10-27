package depends.extractor.kotlin

import depends.entity.GenericName
import depends.entity.repo.EntityRepo
import depends.extractor.kotlin.KotlinParser.ImportHeaderContext
import depends.extractor.kotlin.KotlinParser.PackageHeaderContext
import depends.extractor.kotlin.utils.usedClassNames
import depends.importtypes.ExactMatchImport
import depends.relations.IBindingResolver
import org.slf4j.LoggerFactory


class KotlinListener(
        fileFullPath: String,
        entityRepo: EntityRepo,
        bindingResolver: IBindingResolver
) : KotlinParserBaseListener() {
    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(KotlinListener::class.java)
    }

    private val context: KotlinHandlerContext

    init {
        context = KotlinHandlerContext(entityRepo, bindingResolver)
        context.startFile(fileFullPath)
    }

    private fun exitLastEntity() {
        context.exitLastedEntity()
    }

    override fun enterPackageHeader(ctx: PackageHeaderContext) {
        if (ctx.identifier() != null) {
            context.foundNewPackage(ContextHelper.getName(ctx.identifier()))
        }
        super.enterPackageHeader(ctx)
    }

    override fun enterImportHeader(ctx: ImportHeaderContext) {
        context.foundNewImport(ExactMatchImport(ContextHelper.getName(ctx.identifier())))
        //TODO: alias of import
        if (ctx.importAlias() != null) {

        }
        super.enterImportHeader(ctx)
    }

    /**
     * Enter class declaration
     * ```text
     * classDeclaration
     * : modifierList? (CLASS | INTERFACE) NL* simpleIdentifier
     * (NL* typeParameters)? // done
     * (NL* primaryConstructor)?
     * (NL* COLON NL* delegationSpecifiers)?
     * (NL* typeConstraints)?
     * (NL* classBody | NL* enumClassBody)?
     * ;
     * ```
     * @param ctx
     */
    override fun enterClassDeclaration(ctx: KotlinParser.ClassDeclarationContext) {
        context.foundNewType(GenericName.build(ctx.simpleIdentifier().text), ctx.start.line)
        if (ctx.typeParameters() != null) {
            foundTypeParametersUse(ctx.typeParameters())
        }

        super.enterClassDeclaration(ctx)
    }

    override fun exitClassDeclaration(ctx: KotlinParser.ClassDeclarationContext?) {
        exitLastEntity()
        super.exitClassDeclaration(ctx)
    }

    /**
     * Found type parameters use
     * 将泛型的模板类型和约束类型注册到上下文
     * ```kotlin
     * class A<T1, T2: Base, in T3>
     * ```
     * 类A中，T1、T2、T3为模板类型，Base为约束类型
     * @param typeParameters
     */
    private fun foundTypeParametersUse(typeParameters: KotlinParser.TypeParametersContext) {
        for (i in typeParameters.typeParameter().indices) {
            val typeParam = typeParameters.typeParameter(i)
            val simpleId = typeParam.simpleIdentifier()
            if (simpleId != null) {
                typeParam.type().usedClassNames.forEach {
                    context.foundTypeParameters(GenericName.build(it))
                }
            }
            if (typeParam.simpleIdentifier() != null) {
                context.currentType().addTypeParameter(
                        GenericName.build(typeParam.simpleIdentifier().text))
            }
        }
    }
}