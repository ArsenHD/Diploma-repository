/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.builder;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("compiler/fir/raw-fir/psi2fir/testData/rawBuilder")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class RawFirBuilderTestCaseGenerated extends AbstractRawFirBuilderTestCase {
    private void runTest(String testDataFilePath) throws Exception {
        KotlinTestUtils.runTest(this::doRawFirTest, this, testDataFilePath);
    }

    public void testAllFilesPresentInRawBuilder() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/fir/raw-fir/psi2fir/testData/rawBuilder"), Pattern.compile("^(.+)\\.kt$"), null, true);
    }

    @TestMetadata("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class Declarations extends AbstractRawFirBuilderTestCase {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doRawFirTest, this, testDataFilePath);
        }

        public void testAllFilesPresentInDeclarations() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @TestMetadata("annotation.kt")
        public void testAnnotation() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/annotation.kt");
        }

        @TestMetadata("annotationsOnNullableParenthesizedTypes.kt")
        public void testAnnotationsOnNullableParenthesizedTypes() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/annotationsOnNullableParenthesizedTypes.kt");
        }

        @TestMetadata("annotationsOnParenthesizedTypes.kt")
        public void testAnnotationsOnParenthesizedTypes() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/annotationsOnParenthesizedTypes.kt");
        }

        @TestMetadata("complexTypes.kt")
        public void testComplexTypes() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/complexTypes.kt");
        }

        @TestMetadata("constructorInObject.kt")
        public void testConstructorInObject() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/constructorInObject.kt");
        }

        @TestMetadata("constructorOfAnonymousObject.kt")
        public void testConstructorOfAnonymousObject() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/constructorOfAnonymousObject.kt");
        }

        @TestMetadata("delegates.kt")
        public void testDelegates() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/delegates.kt");
        }

        @TestMetadata("derivedClass.kt")
        public void testDerivedClass() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/derivedClass.kt");
        }

        @TestMetadata("emptyAnonymousObject.kt")
        public void testEmptyAnonymousObject() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/emptyAnonymousObject.kt");
        }

        @TestMetadata("enums.kt")
        public void testEnums() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/enums.kt");
        }

        @TestMetadata("enums2.kt")
        public void testEnums2() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/enums2.kt");
        }

        @TestMetadata("expectActual.kt")
        public void testExpectActual() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/expectActual.kt");
        }

        @TestMetadata("external.kt")
        public void testExternal() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/external.kt");
        }

        @TestMetadata("F.kt")
        public void testF() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/F.kt");
        }

        @TestMetadata("functionTypes.kt")
        public void testFunctionTypes() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/functionTypes.kt");
        }

        @TestMetadata("genericFunctions.kt")
        public void testGenericFunctions() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/genericFunctions.kt");
        }

        @TestMetadata("genericProperty.kt")
        public void testGenericProperty() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/genericProperty.kt");
        }

        @TestMetadata("initBlockWithDeclarations.kt")
        public void testInitBlockWithDeclarations() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/initBlockWithDeclarations.kt");
        }

        @TestMetadata("nestedClass.kt")
        public void testNestedClass() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/nestedClass.kt");
        }

        @TestMetadata("NestedOfAliasedType.kt")
        public void testNestedOfAliasedType() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/NestedOfAliasedType.kt");
        }

        @TestMetadata("NestedSuperType.kt")
        public void testNestedSuperType() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/NestedSuperType.kt");
        }

        @TestMetadata("noPrimaryConstructor.kt")
        public void testNoPrimaryConstructor() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/noPrimaryConstructor.kt");
        }

        @TestMetadata("simpleClass.kt")
        public void testSimpleClass() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/simpleClass.kt");
        }

        @TestMetadata("simpleFun.kt")
        public void testSimpleFun() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/simpleFun.kt");
        }

        @TestMetadata("simpleTypeAlias.kt")
        public void testSimpleTypeAlias() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/simpleTypeAlias.kt");
        }

        @TestMetadata("splitModifierList.kt")
        public void testSplitModifierList() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/splitModifierList.kt");
        }

        @TestMetadata("suspendFunctionTypes.kt")
        public void testSuspendFunctionTypes() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/suspendFunctionTypes.kt");
        }

        @TestMetadata("typeAliasWithGeneric.kt")
        public void testTypeAliasWithGeneric() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/typeAliasWithGeneric.kt");
        }

        @TestMetadata("typeParameterVsNested.kt")
        public void testTypeParameterVsNested() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/typeParameterVsNested.kt");
        }

        @TestMetadata("typeParameters.kt")
        public void testTypeParameters() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/typeParameters.kt");
        }

        @TestMetadata("where.kt")
        public void testWhere() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/where.kt");
        }

        @TestMetadata("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/contracts")
        @TestDataPath("$PROJECT_ROOT")
        @RunWith(JUnit3RunnerWithInners.class)
        public static class Contracts extends AbstractRawFirBuilderTestCase {
            private void runTest(String testDataFilePath) throws Exception {
                KotlinTestUtils.runTest(this::doRawFirTest, this, testDataFilePath);
            }

            public void testAllFilesPresentInContracts() throws Exception {
                KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/contracts"), Pattern.compile("^(.+)\\.kt$"), null, true);
            }

            @TestMetadata("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/contracts/newSyntax")
            @TestDataPath("$PROJECT_ROOT")
            @RunWith(JUnit3RunnerWithInners.class)
            public static class NewSyntax extends AbstractRawFirBuilderTestCase {
                private void runTest(String testDataFilePath) throws Exception {
                    KotlinTestUtils.runTest(this::doRawFirTest, this, testDataFilePath);
                }

                public void testAllFilesPresentInNewSyntax() throws Exception {
                    KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/contracts/newSyntax"), Pattern.compile("^(.+)\\.kt$"), null, true);
                }

                @TestMetadata("contractFunctionsDeclaration.kt")
                public void testContractFunctionsDeclaration() throws Exception {
                    runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/contracts/newSyntax/contractFunctionsDeclaration.kt");
                }

                @TestMetadata("functionWithBothOldAndNewSyntaxContractDescription.kt")
                public void testFunctionWithBothOldAndNewSyntaxContractDescription() throws Exception {
                    runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/contracts/newSyntax/functionWithBothOldAndNewSyntaxContractDescription.kt");
                }

                @TestMetadata("propertyAccessorsContractDescription.kt")
                public void testPropertyAccessorsContractDescription() throws Exception {
                    runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/contracts/newSyntax/propertyAccessorsContractDescription.kt");
                }

                @TestMetadata("simpleFunctionsContractDescription.kt")
                public void testSimpleFunctionsContractDescription() throws Exception {
                    runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/contracts/newSyntax/simpleFunctionsContractDescription.kt");
                }
            }

            @TestMetadata("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/contracts/oldSyntax")
            @TestDataPath("$PROJECT_ROOT")
            @RunWith(JUnit3RunnerWithInners.class)
            public static class OldSyntax extends AbstractRawFirBuilderTestCase {
                private void runTest(String testDataFilePath) throws Exception {
                    KotlinTestUtils.runTest(this::doRawFirTest, this, testDataFilePath);
                }

                public void testAllFilesPresentInOldSyntax() throws Exception {
                    KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/contracts/oldSyntax"), Pattern.compile("^(.+)\\.kt$"), null, true);
                }

                @TestMetadata("contractDescription.kt")
                public void testContractDescription() throws Exception {
                    runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/declarations/contracts/oldSyntax/contractDescription.kt");
                }
            }
        }
    }

    @TestMetadata("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class Expressions extends AbstractRawFirBuilderTestCase {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doRawFirTest, this, testDataFilePath);
        }

        public void testAllFilesPresentInExpressions() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @TestMetadata("annotated.kt")
        public void testAnnotated() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/annotated.kt");
        }

        @TestMetadata("arrayAccess.kt")
        public void testArrayAccess() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/arrayAccess.kt");
        }

        @TestMetadata("arrayAssignment.kt")
        public void testArrayAssignment() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/arrayAssignment.kt");
        }

        @TestMetadata("branches.kt")
        public void testBranches() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/branches.kt");
        }

        @TestMetadata("callableReferences.kt")
        public void testCallableReferences() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/callableReferences.kt");
        }

        @TestMetadata("calls.kt")
        public void testCalls() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/calls.kt");
        }

        @TestMetadata("classReference.kt")
        public void testClassReference() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/classReference.kt");
        }

        @TestMetadata("collectionLiterals.kt")
        public void testCollectionLiterals() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/collectionLiterals.kt");
        }

        @TestMetadata("destructuring.kt")
        public void testDestructuring() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/destructuring.kt");
        }

        @TestMetadata("for.kt")
        public void testFor() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/for.kt");
        }

        @TestMetadata("genericCalls.kt")
        public void testGenericCalls() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/genericCalls.kt");
        }

        @TestMetadata("in.kt")
        public void testIn() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/in.kt");
        }

        @TestMetadata("inBrackets.kt")
        public void testInBrackets() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/inBrackets.kt");
        }

        @TestMetadata("init.kt")
        public void testInit() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/init.kt");
        }

        @TestMetadata("labelForInfix.kt")
        public void testLabelForInfix() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/labelForInfix.kt");
        }

        @TestMetadata("lambda.kt")
        public void testLambda() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/lambda.kt");
        }

        @TestMetadata("lambdaAndAnonymousFunction.kt")
        public void testLambdaAndAnonymousFunction() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/lambdaAndAnonymousFunction.kt");
        }

        @TestMetadata("locals.kt")
        public void testLocals() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/locals.kt");
        }

        @TestMetadata("modifications.kt")
        public void testModifications() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/modifications.kt");
        }

        @TestMetadata("namedArgument.kt")
        public void testNamedArgument() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/namedArgument.kt");
        }

        @TestMetadata("nullability.kt")
        public void testNullability() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/nullability.kt");
        }

        @TestMetadata("qualifierWithTypeArguments.kt")
        public void testQualifierWithTypeArguments() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/qualifierWithTypeArguments.kt");
        }

        @TestMetadata("simpleReturns.kt")
        public void testSimpleReturns() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/simpleReturns.kt");
        }

        @TestMetadata("super.kt")
        public void testSuper() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/super.kt");
        }

        @TestMetadata("these.kt")
        public void testThese() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/these.kt");
        }

        @TestMetadata("try.kt")
        public void testTry() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/try.kt");
        }

        @TestMetadata("typeOperators.kt")
        public void testTypeOperators() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/typeOperators.kt");
        }

        @TestMetadata("unary.kt")
        public void testUnary() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/unary.kt");
        }

        @TestMetadata("variables.kt")
        public void testVariables() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/variables.kt");
        }

        @TestMetadata("while.kt")
        public void testWhile() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/rawBuilder/expressions/while.kt");
        }
    }
}
