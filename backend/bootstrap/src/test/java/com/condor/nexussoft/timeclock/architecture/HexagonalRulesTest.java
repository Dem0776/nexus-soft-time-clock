package com.condor.nexussoft.timeclock.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Verifica automáticamente las reglas de dependencia de la arquitectura hexagonal
 * (docs/iteracion-02-arquitectura/05, reglas DR-1..DR-5). Evita la erosión de la
 * arquitectura conforme se añaden bounded contexts.
 */
@AnalyzeClasses(
        packages = "com.condor.nexussoft.timeclock",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class HexagonalRulesTest {

    /** DR-1: el dominio no depende de frameworks de infraestructura. */
    @ArchTest
    static final ArchRule domain_should_not_depend_on_spring =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "org.hibernate..");

    /** DR-2: el dominio no depende de la infraestructura. */
    @ArchTest
    static final ArchRule domain_should_not_depend_on_infrastructure =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    /** DR-2: la capa de aplicación no depende de la infraestructura. */
    @ArchTest
    static final ArchRule application_should_not_depend_on_infrastructure =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure..");
}
