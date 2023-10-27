package depends.extractor.kotlin

import depends.entity.GenericName
import depends.entity.repo.EntityRepo
import depends.extractor.kotlin.KotlinParser.ImportHeaderContext
import depends.extractor.kotlin.KotlinParser.PackageHeaderContext
import depends.extractor.kotlin.utils.usedClassName
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
        val alias = ctx.importAlias()
        if (alias != null) {
            context.foundNewAlias(alias.simpleIdentifier().text, ctx.identifier().text)
        }
        super.enterImportHeader(ctx)
    }

    override fun enterTypeAlias(ctx: KotlinParser.TypeAliasContext) {
        if (ctx.typeParameters() != null) {
            foundTypeParametersUse(ctx.typeParameters())
        }
        val classNames = ctx.type().usedClassNames
        if (classNames.size == 1) {
            context.foundNewAlias(ctx.simpleIdentifier().text, classNames[0])
        }
        super.enterTypeAlias(ctx)
    }

    /**
     * Enter class declaration
     * ```text
     * classDeclaration
     * : modifiers? (CLASS | (FUN NL*)? INTERFACE) NL* simpleIdentifier
     * (NL* typeParameters)? // done
     * (NL* primaryConstructor)?
     * (NL* COLON NL* delegationSpecifiers)?
     * (NL* typeConstraints)?
     * (NL* classBody | NL* enumClassBody)?
     * ;
     * @param ctx
     */
    override fun enterClassDeclaration(ctx: KotlinParser.ClassDeclarationContext) {
        context.foundNewType(GenericName.build(ctx.simpleIdentifier().text), ctx.start.line)
        if (ctx.typeParameters() != null) {
            foundTypeParametersUse(ctx.typeParameters())
        }
        if (ctx.delegationSpecifiers() != null) {
            foundDelegationSpecifiersUse(ctx.delegationSpecifiers())
        }
        super.enterClassDeclaration(ctx)
    }

    override fun exitClassDeclaration(ctx: KotlinParser.ClassDeclarationContext?) {
        exitLastEntity()
        super.exitClassDeclaration(ctx)
    }

    /**
     * Enter object declaration
     * ```text
     * objectDeclaration
     * : modifiers? OBJECT
     * NL* simpleIdentifier // done
     * (NL* COLON NL* delegationSpecifiers)? // done
     * (NL* classBody)?
     * ;
     * ```
     * @param ctx
     */
    override fun enterObjectDeclaration(ctx: KotlinParser.ObjectDeclarationContext) {
        context.foundNewType(GenericName.build(ctx.simpleIdentifier().text), ctx.start.line)
        if (ctx.delegationSpecifiers() != null) {
            foundDelegationSpecifiersUse(ctx.delegationSpecifiers())
        }
        super.enterObjectDeclaration(ctx)
    }

    override fun exitObjectDeclaration(ctx: KotlinParser.ObjectDeclarationContext?) {
        context.exitLastedEntity()
        super.exitObjectDeclaration(ctx)
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

    /**
     * Found delegation specifiers use
     * ```text
     * annotatedDelegationSpecifier
     * :
     * annotation*
     * NL*
     * delegationSpecifier
     * ;
     * ```
     * @param ctx
     */
    private fun foundDelegationSpecifiersUse(ctx: KotlinParser.DelegationSpecifiersContext) {
        for (i in ctx.annotatedDelegationSpecifier().indices) {
            val annotatedDelegationSpecifier = ctx.annotatedDelegationSpecifier(i)
            val delegationSpecifier = annotatedDelegationSpecifier.delegationSpecifier()

            /**
             * delegationSpecifier
             * : constructorInvocation // 构造函数调用
             * | explicitDelegation // 确实是委托
             * | userType // 实现接口
             * | functionType // 实现kotlin标准库中的函数式接口
             * | SUSPEND NL* functionType
             * ;
             */
            val constructorInvocation = delegationSpecifier.constructorInvocation()
            if (constructorInvocation != null) {
                context.foundExtends(constructorInvocation.userType().usedClassName)
            }
            val userType = delegationSpecifier.userType()
            if (userType != null) {
                context.foundImplements(userType.usedClassName)
            }
            val explicitDelegation = delegationSpecifier.explicitDelegation()
            if (explicitDelegation != null) {
                if (explicitDelegation.userType() != null) {
                    context.foundImplements(explicitDelegation.userType().usedClassName)
                }
                val expression = explicitDelegation.expression()
                // TODO 动态推导表达式的类型
            }
        }
    }
}