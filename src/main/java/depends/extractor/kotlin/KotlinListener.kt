package depends.extractor.kotlin

import depends.entity.*
import depends.entity.repo.EntityRepo
import depends.extractor.kotlin.KotlinParser.ClassParametersContext
import depends.extractor.kotlin.KotlinParser.ExplicitDelegationContext
import depends.extractor.kotlin.KotlinParser.ExpressionContext
import depends.extractor.kotlin.KotlinParser.FunctionBodyContext
import depends.extractor.kotlin.KotlinParser.FunctionDeclarationContext
import depends.extractor.kotlin.KotlinParser.FunctionValueParametersContext
import depends.extractor.kotlin.KotlinParser.ImportHeaderContext
import depends.extractor.kotlin.KotlinParser.PackageHeaderContext
import depends.extractor.kotlin.context.ExpressionUsage
import depends.extractor.kotlin.utils.hasSecondaryConstructor
import depends.extractor.kotlin.utils.parserException
import depends.extractor.kotlin.utils.typeClassName
import depends.extractor.kotlin.utils.usedTypeArguments
import depends.importtypes.ExactMatchImport
import depends.relations.IBindingResolver
import org.antlr.v4.runtime.ParserRuleContext
import org.slf4j.LoggerFactory


class KotlinListener(
        fileFullPath: String,
        private val entityRepo: EntityRepo,
        bindingResolver: IBindingResolver
) : KotlinParserBaseListener() {
    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(KotlinListener::class.java)
        private fun logReceiverTypeNotSupport() {
            logger.warn("does not support extension function now")
        }
    }

    private val context: KotlinHandlerContext
    private val expressionUsage: ExpressionUsage

    private var expressionDepth = 0

    init {
        context = KotlinHandlerContext(entityRepo, bindingResolver)
        context.startFile(fileFullPath)
        expressionUsage = ExpressionUsage(context, entityRepo, bindingResolver)
    }

    private fun exitLastEntity() {
        context.exitLastedEntity()
    }

    override fun enterEveryRule(ctx: ParserRuleContext) {
        if (ctx is ExpressionContext || expressionDepth > 0) {
            val expression = expressionUsage.foundExpression(ctx)
            if (expression != null) {
                if (ctx.parent is FunctionBodyContext
                        && ctx.parent.parent is FunctionDeclarationContext) {
                    expression.addDeducedTypeFunction(context.currentFunction())
                }
                if (ctx.parent is ExplicitDelegationContext) {
                    context.foundNewDelegation(expression)
                }
            }
        }
        super.enterEveryRule(ctx)
    }

    override fun enterExpression(ctx: ExpressionContext) {
        expressionDepth++
        super.enterExpression(ctx)
    }

    override fun exitExpression(ctx: ExpressionContext) {
        expressionDepth--
        super.enterExpression(ctx)
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
        val classNames = ctx.type().usedTypeArguments
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
        val className = ctx.simpleIdentifier().text
        context.foundNewType(GenericName.build(className), ctx.start.line)
        if (ctx.typeParameters() != null) {
            foundTypeParametersUse(ctx.typeParameters())
        }
        if (ctx.delegationSpecifiers() != null) {
            foundDelegationSpecifiersUse(ctx.delegationSpecifiers())
        }
        if (ctx.primaryConstructor() != null) {
            val method = context.foundMethodDeclarator(className, ctx.start.line)
            handleClassParameters(method, ctx.primaryConstructor().classParameters())
            method.addReturnType(context.currentType())
            // 退出主构造函数声明
            exitLastEntity()
        } else {
            // kotlin中如果不存在主构造函数，需要判断是否存在次构造函数
            // 如果二者都不存在，则需要生成默认无参数构造函数
            // 次构造函数在enterSecondaryConstructor中构造
            // 此外，接口不会生成默认的主构造函数
            if (!ctx.hasSecondaryConstructor() && ctx.INTERFACE() == null) {
                val method = context.foundMethodDeclarator(className, className, emptyList(), ctx.start.line)
                method.addReturnType(context.currentType())
                // 退出主构造函数声明
                exitLastEntity()
            }
        }
        super.enterClassDeclaration(ctx)
    }

    override fun exitClassDeclaration(ctx: KotlinParser.ClassDeclarationContext?) {
        exitLastEntity()
        super.exitClassDeclaration(ctx)
    }

    override fun enterSecondaryConstructor(ctx: KotlinParser.SecondaryConstructorContext) {
        val className = context.currentType().rawName.name
        val method = context.foundMethodDeclarator(className, ctx.start.line)
        handleFunctionParameter(method, ctx.functionValueParameters())
        method.addReturnType(context.currentType())
        super.enterSecondaryConstructor(ctx)
    }

    override fun exitSecondaryConstructor(ctx: KotlinParser.SecondaryConstructorContext?) {
        exitLastEntity()
        super.exitSecondaryConstructor(ctx)
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
        exitLastEntity()
        super.exitObjectDeclaration(ctx)
    }

    override fun enterFunctionDeclaration(ctx: FunctionDeclarationContext) {
        val type = ctx.type()
        if (ctx.receiverType() == null) {
            val funcName = ctx.simpleIdentifier().text
            val functionEntity = context.foundMethodDeclarator(funcName, ctx.start.line)
            handleFunctionParameter(functionEntity, ctx.functionValueParameters())
            if (type != null) {
                functionEntity.addReturnType(GenericName.build(type.typeClassName))
            }
        } else {
            logReceiverTypeNotSupport()
        }
        super.enterFunctionDeclaration(ctx)
    }

    override fun exitFunctionDeclaration(ctx: FunctionDeclarationContext) {
        if (ctx.receiverType() == null) {
            exitLastEntity()
        }
        super.exitFunctionDeclaration(ctx)
    }

    override fun enterPropertyDeclaration(ctx: KotlinParser.PropertyDeclarationContext) {
        val currentFunction = context.currentFunction()
        if (currentFunction != null) {
            if (ctx.receiverType() == null) {
                handleLocalVariable(ctx)
            } else {
                logReceiverTypeNotSupport()
            }
        }
        super.enterPropertyDeclaration(ctx)
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
                if (typeParam.type() != null) {
                    context.foundTypeParameters(GenericName.build(typeParam.type().typeClassName))
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
                context.foundExtends(constructorInvocation.userType().typeClassName)
            }
            val userType = delegationSpecifier.userType()
            if (userType != null) {
                context.foundImplements(userType.typeClassName)
            }
            val explicitDelegation = delegationSpecifier.explicitDelegation()
            if (explicitDelegation != null) {
                if (explicitDelegation.userType() != null) {
                    context.foundImplements(explicitDelegation.userType().typeClassName)
                }
                // 代理的目标对象的表达式在处理表达式时与本类型关联，此处不处理
            }
        }
    }

    private fun handleClassParameters(method: FunctionEntity, ctx: ClassParametersContext) {
        for (classParameter in ctx.classParameter()) {
            val varEntity = VarEntity(
                    GenericName.build(classParameter.simpleIdentifier().text),
                    GenericName.build(classParameter.type().typeClassName),
                    method, entityRepo.generateId()
            )
            method.addParameter(varEntity)
        }
    }

    private fun handleFunctionParameter(method: FunctionEntity, ctx: FunctionValueParametersContext) {
        for (functionValueParameter in ctx.functionValueParameter()) {
            val param = functionValueParameter.parameter()
            val varEntity = VarEntity(
                    GenericName.build(param.simpleIdentifier().text),
                    GenericName.build(param.type().typeClassName),
                    method, entityRepo.generateId()
            )
            method.addParameter(varEntity)
        }
    }

    private fun handleLocalVariable(ctx: KotlinParser.PropertyDeclarationContext) {
        val variableDeclaration = ctx.variableDeclaration()
        val multiVariableDeclaration = ctx.multiVariableDeclaration()
        if (variableDeclaration != null) {
            handleVariableDeclaration(variableDeclaration, ctx)
        } else if (multiVariableDeclaration != null) {
            multiVariableDeclaration.variableDeclaration().forEach {
                handleVariableDeclaration(it, ctx)
            }
        } else {
            throw parserException
        }
    }

    private fun handleVariableDeclaration(variableDeclaration: KotlinParser.VariableDeclarationContext, ctx: KotlinParser.PropertyDeclarationContext) {
        val newExpression = KotlinExpression(entityRepo.generateId())
        context.lastContainer().addExpression(ctx, newExpression)
        newExpression.setText(ctx.text)
        newExpression.setStart(ctx.start.startIndex)
        newExpression.setStop(ctx.stop.stopIndex)
        newExpression.setIdentifier(variableDeclaration.simpleIdentifier().text)
        newExpression.isSet = true
        val type = variableDeclaration.type()
        val varEntity = if (type != null) {
            context.foundVarDefinition(
                    variableDeclaration.simpleIdentifier().text,
                    GenericName.build(type.typeClassName),
                    type.usedTypeArguments.map(GenericName::build),
                    ctx.start.line
            )
        } else {
            context.foundVarDefinition(
                    context.lastContainer(),
                    variableDeclaration.simpleIdentifier().text,
                    ctx.start.line
            )
        }
        newExpression.addDeducedTypeVar(varEntity)
    }

}