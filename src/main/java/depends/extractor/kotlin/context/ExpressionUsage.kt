package depends.extractor.kotlin.context

import depends.entity.Expression
import depends.entity.GenericName
import depends.entity.repo.EntityRepo
import depends.entity.repo.IdGenerator
import depends.extractor.kotlin.KotlinHandlerContext
import depends.extractor.kotlin.KotlinParser
import depends.extractor.kotlin.utils.usedClassNames
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.RuleContext

class ExpressionUsage(
        private val context: KotlinHandlerContext,
        private val entityRepo: EntityRepo
) {
    val idGenerator: IdGenerator = entityRepo

    var exprStart = false

    private fun findParentInStack(ctx: RuleContext?): Expression? {
        if (ctx == null) return null
        if (ctx.parent == null) return null
        if (context.lastContainer() == null) {
            return null
        }
        return if (context.lastContainer().expressions().containsKey(ctx.parent))
            context.lastContainer().expressions()[ctx.parent]
        else findParentInStack(ctx.parent)
    }

    fun foundExpression(ctx: ParserRuleContext) {
        if (!exprStart) return
        if (context.lastContainer().containsExpression(ctx)) return
        /* create expression and link it with parent*/
        val parent = findParentInStack(ctx)
        val expression = Expression(idGenerator.generateId())
        context.lastContainer().addExpression(ctx, expression)

        expression.parent = parent
        expression.setText(ctx.text)
        //如果就是自己，则无需创建新的Expression
        val booleanName = GenericName.build("boolean")
        // 注意kotlin的运算符重载，因此不能推导算术运算的类型
        when (ctx) {
            is KotlinParser.DisjunctionContext -> {
                val conjunctions = ctx.conjunction()
                if (conjunctions.size > 1) {
                    expression.rawType = booleanName
                    expression.disableDriveTypeFromChild()
                }
            }

            is KotlinParser.ConjunctionContext -> {
                if (ctx.equality().size > 1) {
                    expression.rawType = booleanName
                    expression.disableDriveTypeFromChild()
                }
            }

            is KotlinParser.EqualityContext -> {
                if (ctx.comparison().size > 1) {
                    expression.rawType = booleanName
                    expression.disableDriveTypeFromChild()
                }
            }

            is KotlinParser.ComparisonContext -> {
                if (ctx.genericCallLikeComparison().size > 1) {
                    expression.rawType = booleanName
                    expression.disableDriveTypeFromChild()
                }
            }

            is KotlinParser.InfixOperationContext -> {
                if (ctx.isOperator.size >= 1 || ctx.inOperator().size >= 1) {
                    expression.rawType = booleanName
                    expression.disableDriveTypeFromChild()
                }
            }

            is KotlinParser.AsExpressionContext -> {
                val types = ctx.type()
                if (types.size >= 1) {
                    // TODO 从复杂类型中推导表达式类型
                    val classNames = types.last().usedClassNames
                    if (classNames.size == 1) {
                        expression.isCast = true
                        expression.setRawType(classNames[0])
                        expression.disableDriveTypeFromChild()
                    }
                }
            }

            is KotlinParser.GenericCallLikeComparisonContext -> {
                if (ctx.callSuffix().size >= 1)
                    expression.isCall = true
            }
        }
    }
}