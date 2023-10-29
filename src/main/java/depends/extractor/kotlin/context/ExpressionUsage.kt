package depends.extractor.kotlin.context

import depends.entity.Expression
import depends.entity.GenericName
import depends.entity.TypeEntity
import depends.entity.repo.EntityRepo
import depends.entity.repo.IdGenerator
import depends.extractor.kotlin.KotlinHandlerContext
import depends.extractor.kotlin.KotlinParser.*
import depends.extractor.kotlin.utils.typeClassName
import depends.relations.IBindingResolver
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.RuleContext

class ExpressionUsage(
        private val context: KotlinHandlerContext,
        private val entityRepo: EntityRepo,
        private val bindingResolver: IBindingResolver
) {
    val idGenerator: IdGenerator = entityRepo

    private fun findExpressionInStack(ctx: RuleContext?): Expression? {
        if (ctx == null) return null
        if (ctx.parent == null) return null
        if (context.lastContainer() == null) {
            return null
        }
        return if (context.lastContainer().expressions().containsKey(ctx.parent))
            context.lastContainer().expressions()[ctx.parent]
        else findExpressionInStack(ctx.parent)
    }

    private fun isExpressionContext(ctx: ParserRuleContext): Boolean {
        return ctx is ExpressionContext
                || ctx is DisjunctionContext
                || ctx is ConjunctionContext
                || ctx is EqualityContext
                || ctx is ComparisonContext
                || ctx is GenericCallLikeComparisonContext
                || ctx is InfixOperationContext
                || ctx is ElvisExpressionContext
                || ctx is InfixFunctionCallContext
                || ctx is RangeExpressionContext
                || ctx is AdditiveExpressionContext
                || ctx is MultiplicativeExpressionContext
                || ctx is AsExpressionContext
                || ctx is PrefixUnaryExpressionContext
                || ctx is PostfixUnaryExpressionContext
                || ctx is PrimaryExpressionContext
                || ctx is ParenthesizedExpressionContext
                || ctx is LiteralConstantContext
                || ctx is StringLiteralContext
                || ctx is CallableReferenceContext
                || ctx is FunctionLiteralContext
                || ctx is ObjectLiteralContext
                || ctx is CollectionLiteralContext
                || ctx is ThisExpressionContext
                || ctx is SuperExpressionContext
                || ctx is IfExpressionContext
                || ctx is WhenExpressionContext
                || ctx is TryExpressionContext
                || ctx is JumpExpressionContext
    }

    fun foundExpression(ctx: ParserRuleContext): Expression? {
        if (!isExpressionContext(ctx)) {
            return null
        }
        if (context.lastContainer().containsExpression(ctx)) {
            return null
        }
        /* create expression and link it with parent*/
        val parent = findExpressionInStack(ctx)
        val expression = if (ctx.parent?.childCount == 1 && parent != null
                && parent.location.startIndex == ctx.start.startIndex
                && parent.location.stopIndex == ctx.stop.stopIndex) {
            parent
        } else {
            val newExpression = Expression(idGenerator.generateId())
            context.lastContainer().addExpression(ctx, newExpression)
            newExpression.parent = parent
            newExpression.setText(ctx.text)
            newExpression.setStart(ctx.start.startIndex)
            newExpression.setStop(ctx.stop.stopIndex)
            newExpression
        }
        tryDeduceExpression(expression, ctx)
        return expression
    }

    private fun tryDeduceExpression(expression: Expression, ctx: ParserRuleContext) {
        //如果就是自己，则无需创建新的Expression
        val booleanName = GenericName.build("boolean")
        // 注意kotlin的运算符重载，因此不能推导算术运算的类型
        when (ctx) {
            is DisjunctionContext -> {
                val conjunctions = ctx.conjunction()
                if (conjunctions.size > 1) {
                    expression.rawType = booleanName
                    expression.disableDriveTypeFromChild()
                }
            }

            is ConjunctionContext -> {
                if (ctx.equality().size > 1) {
                    expression.rawType = booleanName
                    expression.disableDriveTypeFromChild()
                }
            }

            is EqualityContext -> {
                if (ctx.comparison().size > 1) {
                    expression.rawType = booleanName
                    expression.disableDriveTypeFromChild()
                }
            }

            is ComparisonContext -> {
                if (ctx.genericCallLikeComparison().size > 1) {
                    expression.rawType = booleanName
                    expression.disableDriveTypeFromChild()
                }
            }

            is InfixOperationContext -> {
                if (ctx.isOperator.size >= 1 || ctx.inOperator().size >= 1) {
                    expression.rawType = booleanName
                    expression.disableDriveTypeFromChild()
                }
            }

            is AsExpressionContext -> {
                val types = ctx.type()
                if (types.size >= 1) {
                    val typeClassName = types.last().typeClassName
                    expression.isCast = true
                    expression.setRawType(typeClassName)
                    expression.disableDriveTypeFromChild()
                }
            }

            is GenericCallLikeComparisonContext -> {
                if (ctx.callSuffix().size >= 1)
                    expression.isCall = true
            }

            is PostfixUnaryExpressionContext -> {
                handlePostfixUnary(ctx, expression)
            }

            is PrimaryExpressionContext -> {
                if (ctx.simpleIdentifier() != null) {
                    val name = ctx.simpleIdentifier().text
                    val typeEntity = context.foundEntityWithName(GenericName.build(name))
                    if (typeEntity is TypeEntity && typeEntity.id > 0) {
                        expression.isCreate = true
                        expression.setType(typeEntity.type, typeEntity, bindingResolver)
                        expression.rawType = typeEntity.rawName
                    } else {
                        expression.setIdentifier(name)
                    }
                }
            }

            is ThisExpressionContext -> {
                expression.setIdentifier("this")
            }

            is SuperExpressionContext -> {
                expression.setIdentifier("super")
            }
        }
    }

    private fun handlePostfixUnary(ctx: PostfixUnaryExpressionContext, expression: Expression) {
        val suffix = ctx.postfixUnarySuffix()
        if (suffix?.callSuffix() != null) {
            expression.isCall = true
        } else if (suffix?.navigationSuffix() != null) {
            expression.isDot = true
        }
        val navigationSuffix = suffix?.navigationSuffix()
        if (navigationSuffix?.simpleIdentifier() != null) {
            if (expression.parent?.isCall == true) {
                // 识别到父表达式是函数调用，则将自己设置为函数调用
                // TODO 对函数对象的调用；调用运算符重载
                expression.isCall = true
            }
            expression.setIdentifier(navigationSuffix.simpleIdentifier().text)
        }
    }
}