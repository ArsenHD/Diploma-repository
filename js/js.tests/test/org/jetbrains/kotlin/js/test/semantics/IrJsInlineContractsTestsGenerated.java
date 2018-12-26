/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.test.semantics;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TargetBackend;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("compiler/testData/codegen/boxInline/contracts")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class IrJsInlineContractsTestsGenerated extends AbstractIrJsInlineContractsTests {
    private void runTest(String testDataFilePath) throws Exception {
        KotlinTestUtils.runTest0(this::doTest, TargetBackend.JS_IR, testDataFilePath);
    }

    public void testAllFilesPresentInContracts() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("compiler/testData/codegen/boxInline/contracts"), Pattern.compile("^(.+)\\.kt$"), TargetBackend.JS_IR, true);
    }

    @TestMetadata("cfgDependendValInitialization.kt")
    public void testCfgDependendValInitialization() throws Exception {
        runTest("compiler/testData/codegen/boxInline/contracts/cfgDependendValInitialization.kt");
    }

    @TestMetadata("complexInitializer.kt")
    public void testComplexInitializer() throws Exception {
        runTest("compiler/testData/codegen/boxInline/contracts/complexInitializer.kt");
    }

    @TestMetadata("complexInitializerWithStackTransformation.kt")
    public void testComplexInitializerWithStackTransformation() throws Exception {
        runTest("compiler/testData/codegen/boxInline/contracts/complexInitializerWithStackTransformation.kt");
    }

    @TestMetadata("definiteLongValInitialization.kt")
    public void testDefiniteLongValInitialization() throws Exception {
        runTest("compiler/testData/codegen/boxInline/contracts/definiteLongValInitialization.kt");
    }

    @TestMetadata("definiteNestedValInitialization.kt")
    public void testDefiniteNestedValInitialization() throws Exception {
        runTest("compiler/testData/codegen/boxInline/contracts/definiteNestedValInitialization.kt");
    }

    @TestMetadata("definiteValInitInInitializer.kt")
    public void testDefiniteValInitInInitializer() throws Exception {
        runTest("compiler/testData/codegen/boxInline/contracts/definiteValInitInInitializer.kt");
    }

    @TestMetadata("definiteValInitialization.kt")
    public void testDefiniteValInitialization() throws Exception {
        runTest("compiler/testData/codegen/boxInline/contracts/definiteValInitialization.kt");
    }

    @TestMetadata("exactlyOnceCrossinline.kt")
    public void testExactlyOnceCrossinline() throws Exception {
        runTest("compiler/testData/codegen/boxInline/contracts/exactlyOnceCrossinline.kt");
    }

    @TestMetadata("exactlyOnceNoinline.kt")
    public void testExactlyOnceNoinline() throws Exception {
        runTest("compiler/testData/codegen/boxInline/contracts/exactlyOnceNoinline.kt");
    }

    @TestMetadata("nonLocalReturn.kt")
    public void testNonLocalReturn() throws Exception {
        runTest("compiler/testData/codegen/boxInline/contracts/nonLocalReturn.kt");
    }

    @TestMetadata("nonLocalReturnWithCycle.kt")
    public void testNonLocalReturnWithCycle() throws Exception {
        runTest("compiler/testData/codegen/boxInline/contracts/nonLocalReturnWithCycle.kt");
    }

    @TestMetadata("propertyInitialization.kt")
    public void testPropertyInitialization() throws Exception {
        runTest("compiler/testData/codegen/boxInline/contracts/propertyInitialization.kt");
    }

    @TestMetadata("valInitializationAndUsageInNestedLambda.kt")
    public void testValInitializationAndUsageInNestedLambda() throws Exception {
        runTest("compiler/testData/codegen/boxInline/contracts/valInitializationAndUsageInNestedLambda.kt");
    }
}
