package br.com.paymentslab.split;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Regras de dependencia da arquitetura.
 *
 * <p>A Constitution, artigo 5, diz que o nucleo nao conhece framework. Isso era
 * verificado por revisao manual, o que depende de disciplina. Aqui virou teste:
 * se alguem importar Spring ou Kafka dentro de dominio ou aplicacao, o build
 * quebra.
 */
@AnalyzeClasses(packages = "br.com.paymentslab.split")
class ArquiteturaTest {

    @ArchTest
    static final ArchRule o_nucleo_nao_conhece_framework = noClasses()
            .that().resideInAnyPackage("..dominio..", "..aplicacao..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "org.springframework..",
                    "org.apache.kafka..",
                    "io.micrometer..",
                    "jakarta..",
                    "com.fasterxml.jackson..")
            .because("dominio e aplicacao precisam ser testaveis sem contexto");

    @ArchTest
    static final ArchRule o_nucleo_nao_depende_dos_adaptadores = noClasses()
            .that().resideInAnyPackage("..dominio..", "..aplicacao..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..adaptadores..")
            .because("a dependencia aponta para dentro, nunca para a borda");
}
