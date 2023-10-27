package depends.extractor.kotlin.utils

import depends.extractor.kotlin.KotlinParser

val KotlinParser.TypeContext.usedClassNames: List<String>
    get() {
        val result = ArrayList<String>()
        if (typeModifiers() != null) {
            result.addAll(typeModifiers().userClassNames)
        }
        result.addAll(
                if (functionType() != null) {
                    functionType().usedClassNames
                } else if (parenthesizedType() != null) {
                    parenthesizedType().usedClassNames
                } else if (nullableType() != null) {
                    nullableType().usedClassNames
                } else if (typeReference() != null) {
                    listOf(typeReference().usedClassName)
                } else emptyList()
        )
        return result
    }

val KotlinParser.ParenthesizedTypeContext.usedClassNames: List<String>
    get() {
        return this.type().usedClassNames
    }

val KotlinParser.NullableTypeContext.usedClassNames: List<String>
    get() {
        return if (typeReference() != null) {
            listOf(typeReference().usedClassName)
        } else parenthesizedType().usedClassNames
    }

val KotlinParser.TypeReferenceContext.usedClassName: String
    get() {
        return userType().usedClassName
    }

val KotlinParser.UserTypeContext.usedClassName: String
    get() {
        var r = StringBuilder()
        for (i in simpleUserType().indices) {
            val dot = if (r.isEmpty()) "" else "."
            r = r.append(dot).append(simpleUserType(i).text)
        }
        return r.toString()
    }

val KotlinParser.SimpleUserTypeContext.usedClassNames: List<String>
    get() {
        val result = ArrayList<String>()
        result.add(simpleIdentifier().text)
        typeArguments()?.typeProjection()?.forEach {
            result.addAll(it.usedClassNames)
        }
        return result
    }

val KotlinParser.TypeProjectionContext.usedClassNames: List<String>
    get() {
        return this.type().usedClassNames
    }

val KotlinParser.FunctionTypeContext.usedClassNames: List<String>
    get() {
        val result = ArrayList<String>()
        functionTypeParameters().type().forEach {
            result.addAll(it.usedClassNames)
        }
        result.addAll(receiverType().usedClassNames)
        return result
    }

val KotlinParser.ReceiverTypeContext.usedClassNames: List<String>
    get() {
        return if (parenthesizedType() != null) {
            parenthesizedType().usedClassNames
        } else if (nullableType() != null) {
            nullableType().usedClassNames
        } else if (typeReference() != null) {
            listOf(typeReference().usedClassName)
        } else emptyList()
    }

val KotlinParser.TypeModifiersContext.userClassNames: List<String>
    get() {
        val result = ArrayList<String>()
        TODO()
    }