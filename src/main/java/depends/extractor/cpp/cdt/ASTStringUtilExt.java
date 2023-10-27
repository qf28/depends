package depends.extractor.cpp.cdt;

import depends.entity.GenericName;
import depends.entity.TypeEntity;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplateId;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * This extends the CDT ASTStringUtil class.
 * A tricky point here is that we have to use some of the reflection mechanism to invoke 
 * some private functions in ASTStringUtils class
 * It is not good, but it seems the most easiest one to reuse existing functions
 */
public class ASTStringUtilExt extends ASTStringUtil {
	public static String getName(IASTDeclSpecifier decl) {
		StringBuilder buffer = new StringBuilder();
		String name = appendBareDeclSpecifierString(buffer, decl).toString().replace("::", ".").replace("...", "");
		return name;
	}

	public static String getName(IASTLiteralExpression expr) {
		return expr.getRawSignature().replace("::", ".").replace("...", "");
	}

	public static String getTypeIdString(IASTTypeId typeId) {
		StringBuilder sb = new StringBuilder();
		return appendBareTypeIdString(sb, typeId).toString().replace("::", ".");
	}
	

	/**
	 *  retrieve template parameters from declSpecifier 
	 */
	public static List<GenericName> getTemplateParameters(IASTDeclSpecifier declSpecifier) {
		List<GenericName> parameters = new ArrayList<>();
		declSpecifier.accept(new TemplateParameterASTVisitor(parameters));
		return parameters;
	}

	

	private static StringBuilder appendBareDeclSpecifierString(StringBuilder buffer, IASTDeclSpecifier declSpecifier) {
		if (declSpecifier instanceof IASTCompositeTypeSpecifier compositeTypeSpec) {
			appendBareNameString(buffer, compositeTypeSpec.getName());
		} else if (declSpecifier instanceof IASTElaboratedTypeSpecifier elaboratedTypeSpec) {
			appendBareNameString(buffer, elaboratedTypeSpec.getName());
		} else if (declSpecifier instanceof IASTEnumerationSpecifier enumerationSpec) {
			appendBareNameString(buffer, enumerationSpec.getName());
		} else if (declSpecifier instanceof IASTSimpleDeclSpecifier) {
			buffer.append(TypeEntity.buildInType.getRawName());
		} else if (declSpecifier instanceof IASTNamedTypeSpecifier namedTypeSpec) {
			appendBareNameString(buffer, namedTypeSpec.getName());
		}
		return buffer;
	}

	private static StringBuilder appendBareNameString(StringBuilder buffer, IASTName name) {
		if (name instanceof ICPPASTQualifiedName qualifiedName) {
			final ICPPASTNameSpecifier[] segments = qualifiedName.getAllSegments();
			for (int i = 0; i < segments.length; i++) {
				if (i > 0) {
					buffer.append(".");
				}
				appendQualifiedNameStringWithReflection(buffer, segments[i]);
			}
		} else if (name instanceof CPPASTTemplateId) {
			appendQualifiedNameStringWithReflection(buffer,(CPPASTTemplateId)name);
		} else if (name != null) {
			buffer.append(name.getSimpleID());
		}
		return buffer;
	}

	private static void appendQualifiedNameStringWithReflection(StringBuilder buffer, IASTName name) {
		try {
			Method m = ASTStringUtil.class.getDeclaredMethod("appendQualifiedNameString", StringBuilder.class,
					IASTName.class);
			m.setAccessible(true); // if security settings allow this
			m.invoke(null, buffer, name); // use null if the method is static
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			System.err.println("Error: cannot invoke ASTStringUtils method of <appendQualifiedNameString>");
		}
	}

	private static void appendQualifiedNameStringWithReflection(StringBuilder buffer,
			CPPASTTemplateId templateId) {
		appendQualifiedNameStringWithReflection(buffer,templateId.getTemplateName());
	}
	
	private static void appendQualifiedNameStringWithReflection(StringBuilder buffer,
			ICPPASTNameSpecifier nameSpecifier) {
		if (nameSpecifier instanceof CPPASTTemplateId) {
			appendQualifiedNameStringWithReflection(buffer,(CPPASTTemplateId)nameSpecifier);
			return;
		}
		try {
			Method m = ASTStringUtil.class.getDeclaredMethod("appendQualifiedNameString", StringBuilder.class,
					ICPPASTNameSpecifier.class);
			m.setAccessible(true); // if security settings allow this
			m.invoke(null, buffer, nameSpecifier); // use null if the method is static
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			System.err.println("Error: cannot invoke ASTStringUtils method of <appendQualifiedNameString>");
		}
	}

	private static StringBuilder appendBareTypeIdString(StringBuilder buffer, IASTTypeId typeId) {
		return appendBareDeclSpecifierString(buffer, typeId.getDeclSpecifier());
	}


	public static String getName(IASTDeclarator declarator) {
		return declarator.getName().toString().replace("::", ".");
	}


	public static String getName(ICPPASTUsingDeclaration declaration) {
		return declaration.getName().toString().replace("::", ".");
	}


	public static String getName(IASTName name) {
		return name.getRawSignature().toString().replace("::", ".");
	}


	private static StringBuilder appendBareNameString(StringBuilder buffer, ICPPASTNameSpecifier name) {
		if (name instanceof ICPPASTQualifiedName qualifiedName) {
			final ICPPASTNameSpecifier[] segments = qualifiedName.getAllSegments();
			for (int i = 0; i < segments.length; i++) {
				if (i > 0) {
					buffer.append(".");
				}
				appendQualifiedNameStringWithReflection(buffer, segments[i]);
			}
		} else if (name instanceof CPPASTTemplateId) {
			appendQualifiedNameStringWithReflection(buffer,(CPPASTTemplateId)name);
		} else if (name != null) {
			buffer.append(name.getRawSignature());
		}
		return buffer;
	}
	
	public static String getName(ICPPASTNameSpecifier nameSpecifier) {
		StringBuilder buffer = new StringBuilder();
		String name = appendBareNameString(buffer, nameSpecifier).toString().replace("::", ".").replace("...", "");
		return name;
	}




}