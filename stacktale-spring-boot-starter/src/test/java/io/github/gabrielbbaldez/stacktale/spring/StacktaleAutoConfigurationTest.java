package io.github.gabrielbbaldez.stacktale.spring;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.github.gabrielbbaldez.stacktale.StacktaleAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class StacktaleAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(StacktaleAutoConfiguration.class));

    @AfterEach
    void detachGlobalAppender() {
        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
        ctx.getLogger(Logger.ROOT_LOGGER_NAME).detachAppender(StacktaleAutoConfiguration.APPENDER_NAME);
    }

    @Test
    void registersTheAppenderOnTheRootLoggerByDefault() {
        runner.withPropertyValues("stacktale.file=target/starter-test-errors.log").run(context -> {
            assertThat(context).hasSingleBean(StacktaleAppender.class);
            LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
            assertThat(ctx.getLogger(Logger.ROOT_LOGGER_NAME)
                    .getAppender(StacktaleAutoConfiguration.APPENDER_NAME)).isNotNull();
            // request logger feeds stacktale only — never the console
            assertThat(ctx.getLogger(StacktaleRequestFilter.REQUEST_LOGGER).isAdditive()).isFalse();
        });
    }

    @Test
    void masterSwitchDisablesEverything() {
        runner.withPropertyValues("stacktale.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(StacktaleAppender.class);
            LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
            assertThat(ctx.getLogger(Logger.ROOT_LOGGER_NAME)
                    .getAppender(StacktaleAutoConfiguration.APPENDER_NAME)).isNull();
        });
    }

    @Test
    void doesNotDoubleRegisterWhenUserAlreadyConfiguredOne() {
        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
        StacktaleAppender manual = new StacktaleAppender();
        manual.setContext(ctx);
        manual.setName(StacktaleAutoConfiguration.APPENDER_NAME);
        manual.setFile("target/manual-errors.log");
        manual.start();
        ctx.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(manual);

        runner.run(context ->
                assertThat(context.getBean(StacktaleAppender.class)).isSameAs(manual));
    }
}
