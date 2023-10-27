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
                    typeReference().usedClassNames
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
            typeReference().usedClassNames
        } else parenthesizedType().usedClassNames
    }

val KotlinParser.TypeReferenceContext.usedClassNames: List<String>
    get() {
        return userType().usedClassNames
    }

val KotlinParser.UserTypeContext.usedClassNames: List<String>
    get() {
        val result = ArrayList<String>()
        simpleUserType().forEach {
            result.addAll(it.usedClassNames)
        }
        return result
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
            typeReference().usedClassNames
        } else emptyList()
    }

val KotlinParser.TypeModifiersContext.userClassNames: List<String>
    get() {
        val result = ArrayList<String>()
        TODO()
    }