package com.medisalud.appointment.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class HexagonalArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        // Importar solo clases de main para evitar que clases de test (ej. *ServiceTest en application.service)
        // interfieran con reglas de naming convention
        classes = new ClassFileImporter().importPath("build/classes/java/main");
    }

    @Test
    @DisplayName("Domain layer no debe depender de Spring ni JPA")
    void domainDoesNotDependOnFramework() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "jakarta.servlet..",
                        "com.fasterxml.jackson..", "org.hibernate..", "lombok..")
                .because("El dominio debe ser Java puro, sin dependencias de framework");

        rule.check(classes);
    }

    @Test
    @DisplayName("Application layer puede depender de domain pero no de infrastructure")
    void applicationDoesNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..infrastructure..")
                .because("La capa de aplicacion no debe depender de infraestructura");

        rule.check(classes);
    }

    @Test
    @DisplayName("Infrastructure layer puede depender de application y domain")
    void infrastructureDependsOnApplicationAndDomain() {
        ArchRule rule = classes()
                .that().resideInAnyPackage("..infrastructure..")
                .should().onlyHaveDependentClassesThat()
                .resideInAnyPackage("..infrastructure..", "..application..", "..domain..",
                        "org.springframework..", "jakarta..", "com.fasterxml.jackson..",
                        "lombok..", "org.hibernate..", "io.swagger..")
                .because("Infraestructura puede depender de cualquier capa interna");
    }

    @Test
    @DisplayName("Verificar estructura de capas hexagonal")
    void layeredArchitectureTest() {
        ArchRule rule = layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .layer("Domain").definedBy("..domain..")
                .layer("Application").definedBy("..application..")
                .layer("Infrastructure").definedBy("..infrastructure..")

                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
                .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer();

        rule.check(classes);
    }

    @Test
    @DisplayName("Naming convention: Services terminan en Service")
    void serviceNamingConvention() {
        ArchRule rule = classes()
                .that().resideInAnyPackage("..application.service..")
                .should().haveSimpleNameEndingWith("Service")
                .andShould().beAnnotatedWith(org.springframework.stereotype.Service.class)
                .because("Los servicios de aplicacion deben anotarse con @Service y terminar en 'Service'");

        rule.check(classes);
    }

    @Test
    @DisplayName("Repositories en infrastructure deben terminar en Adapter o JpaRepository")
    void repositoryNamingConvention() {
        ArchRule rule = classes()
                .that().resideInAnyPackage("..persistence.adapter..")
                .should().haveSimpleNameEndingWith("Adapter")
                .because("Los adapters de persistencia deben terminar en 'Adapter'");

        rule.check(classes);
    }
}
