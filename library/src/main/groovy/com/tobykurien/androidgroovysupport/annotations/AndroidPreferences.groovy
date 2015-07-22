package com.tobykurien.androidgroovysupport.annotations

import android.content.Context
import com.tobykurien.androidgroovysupport.utils.BasePreferences
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention (RetentionPolicy.SOURCE)
@Target ([ElementType.TYPE])
@GroovyASTTransformationClass (["com.tobykurien.androidgroovysupport.annotations.PreferenceTransformation"])
public @interface AndroidPreferences {
}

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class PreferenceTransformation extends AbstractASTTransformation {

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        if (!astNodes) return
        if (!astNodes[0]) return
        if (!astNodes[1]) return
        if (!(astNodes[0] instanceof AnnotationNode)) return
//        if (astNodes[0].classNode?.name != TestAnnotation.class.getName()) return
        if (!(astNodes[1] instanceof ClassNode)) return

        ClassNode clazz = astNodes[1] as ClassNode
        if (!clazz.isDerivedFrom(ClassHelper.make(BasePreferences))) {
            //clazz.setSuperClass(ClassHelper.make(BasePreferences))
        }

        //clazz.addMethod(getSettings(clazz))

        clazz.getFields().each { field ->
            if (field.isPublic()) {
                clazz.addMethod(prefGetter(field))
                clazz.addMethod(prefSetter(field))
            }
        }
    }

    String toFirstUpper(String name) {
        name[0].toUpperCase() + name[1..-1]
    }

    MethodNode prefGetter(FieldNode field) {
        def type = field.type.nameWithoutPackage

        def ast = new AstBuilder().buildFromString("""
            return pref.get${type}("${field.name}", ${field.name})
        """)

        def getterName = "get" + toFirstUpper(field.name)
        def meth = new MethodNode(getterName, ACC_PUBLIC, field.type,
                new Parameter[0], new ClassNode[0], ast[0] as BlockStatement)

        return meth
    }

    MethodNode prefSetter(FieldNode field) {
        def type = field.type.nameWithoutPackage

        def ast = new AstBuilder().buildFromString("""
            pref.edit().put${type}("${field.name}", ${field.name}).commit()
            return
        """)

        def setterName = "set" + toFirstUpper(field.name)
        def meth = new MethodNode(setterName, ACC_PUBLIC, ClassHelper.VOID_TYPE,
                [ new Parameter(field.type, field.name)] as Parameter[],
                new ClassNode[0], ast[0] as BlockStatement)

        return meth
    }

    MethodNode getSettings(ClassNode clazz) {
        def ast = new AstBuilder().buildFromString("""
            getPreferences(context, ${clazz.name})
        """)

        def meth = new MethodNode("getSettings", ACC_PUBLIC | ACC_STATIC, clazz,
                [ new Parameter(ClassHelper.make(Context), "context")] as Parameter[],
                new ClassNode[0], ast[0] as BlockStatement)

        return meth
    }
}