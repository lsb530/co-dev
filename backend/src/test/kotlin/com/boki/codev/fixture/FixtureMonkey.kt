package com.boki.codev.fixture

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector
import com.navercorp.fixturemonkey.api.jqwik.JavaTypeArbitraryGenerator
import com.navercorp.fixturemonkey.api.jqwik.JqwikPlugin
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin
import com.navercorp.fixturemonkey.api.plugin.SimpleValueJqwikPlugin
import com.navercorp.fixturemonkey.jackson.plugin.JacksonPlugin
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin
import com.navercorp.fixturemonkey.kotest.KotestPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.introspector.KotlinAndJavaCompositeArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.introspector.PrimaryConstructorArbitraryIntrospector
import net.jqwik.api.Arbitraries
import net.jqwik.api.arbitraries.StringArbitrary

val sut: FixtureMonkey = FixtureMonkey.builder()
    .plugin(SimpleValueJqwikPlugin())
    .plugin(
        JqwikPlugin().javaTypeArbitraryGenerator(
            object : JavaTypeArbitraryGenerator {
                override fun strings(): StringArbitrary = Arbitraries.strings().alpha()
            },
        ),
    )
    .plugin(InterfacePlugin().useAnonymousArbitraryIntrospector(false))
    .plugin(KotestPlugin())
    .plugin(JakartaValidationPlugin())
    .plugin(JacksonPlugin())
    .plugin(KotlinPlugin())
    .objectIntrospector(
        KotlinAndJavaCompositeArbitraryIntrospector(
            kotlinArbitraryIntrospector = PrimaryConstructorArbitraryIntrospector.INSTANCE,
            javaArbitraryIntrospector = ConstructorPropertiesArbitraryIntrospector.INSTANCE
        )
    )
    .build()