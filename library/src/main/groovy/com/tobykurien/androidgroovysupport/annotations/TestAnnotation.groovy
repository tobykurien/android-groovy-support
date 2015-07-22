package com.tobykurien.androidgroovysupport.annotations

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
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
@GroovyASTTransformationClass (["com.tobykurien.androidgroovysupport.annotations.TestTransformation"])
public @interface TestAnnotation {
}

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class TestTransformation extends AbstractASTTransformation {

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        if (!astNodes) return
        if (!astNodes[0]) return
        if (!astNodes[1]) return
        if (!(astNodes[0] instanceof AnnotationNode)) return
//        if (astNodes[0].classNode?.name != TestAnnotation.class.getName()) return
        if (!(astNodes[1] instanceof ClassNode)) return

        ClassNode declaringClass = astNodes[1] as ClassNode
        System.out.println("TestAnnotation on Class ${declaringClass}")
        declaringClass.addMethod(makeMainMethod())
    }

    MethodNode makeMainMethod() {
        System.out.println("making new method")
        def ast = new AstBuilder().buildFromString("""
            return "test";
        """)
        System.out.println("got ast ${ast}")

        def meth = new MethodNode("toby", ACC_PUBLIC, ClassHelper.make(String),
            new Parameter[0], new ClassNode[0], ast[0] as BlockStatement)

        System.out.println("got method ${meth}")
        return meth
    }
}