// DO NOT EDIT MANUALLY!
// Generated by org/jetbrains/kotlin/generators/arguments/GenerateGradleOptions.kt
package org.jetbrains.kotlin.gradle.dsl

@Suppress("DEPRECATION")
interface KotlinJsOptions  : org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions {

    /**
     * Disable internal declaration export
     * Default value: false
     */
     var friendModulesDisabled: kotlin.Boolean

    /**
     * Define whether the `main` function should be called upon execution
     * Possible values: "call", "noCall"
     * Default value: "call"
     */
     var main: kotlin.String

    /**
     * Generate .meta.js and .kjsm files with metadata. Use to create a library
     * Default value: true
     */
     var metaInfo: kotlin.Boolean

    /**
     * Kind of the JS module generated by the compiler
     * Possible values: "plain", "amd", "commonjs", "umd"
     * Default value: "plain"
     */
     var moduleKind: kotlin.String

    /**
     * Don't automatically include the default Kotlin/JS stdlib into compilation dependencies
     * Default value: true
     */
     var noStdlib: kotlin.Boolean

    /**
     * Destination *.js file for the compilation result
     * Default value: null
     */
     var outputFile: kotlin.String?

    /**
     * Generate source map
     * Default value: false
     */
     var sourceMap: kotlin.Boolean

    /**
     * Embed source files into source map
     * Possible values: "never", "always", "inlining"
     * Default value: null
     */
     var sourceMapEmbedSources: kotlin.String?

    /**
     * Add the specified prefix to paths in the source map
     * Default value: null
     */
     var sourceMapPrefix: kotlin.String?

    /**
     * Generate JS files for specific ECMA version
     * Possible values: "v5"
     * Default value: "v5"
     */
     var target: kotlin.String

    /**
     * Translate primitive arrays to JS typed arrays
     * Default value: true
     */
     var typedArrays: kotlin.Boolean
}
