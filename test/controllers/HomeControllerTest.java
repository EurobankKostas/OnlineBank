package controllers;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import models.Project;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Matchers;
import play.Application;
import play.ApplicationLoader;
import play.Mode;
import play.api.Environment;
import play.api.inject.guice.GuiceableModule;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import services.FreelancerApiService;
import services.IApiService;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import javax.inject.Inject;

import static play.test.Helpers.*;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.*;
import static play.mvc.Http.Status.OK;
import static org.mockito.Mockito.*;


public class HomeControllerTest extends WithApplication {
    Application application =
            new GuiceApplicationBuilder()
                    .load(
                            new play.api.inject.BuiltinModule(),
                            new play.inject.BuiltInModule(),
                            new play.api.i18n.I18nModule(),
                    .in(new Environment(new File("6441_project_v3 (2)"), classLoader, Mode.TEST))
                    .build();
    Application fakeApp = fakeApplication();

    Application fakeAppWithMemoryDb = fakeApplication(inMemoryDatabase("test"));



    @Before
    public void setup() {
        Module testModule =
                new Module() {
                    @Override
                    public void configure() {
                        // Install custom test binding here
                    }
                };

        GuiceApplicationBuilder builder =
                new GuiceApplicationLoader()
                        .builder(new ApplicationLoader.Context(Environment.simple()))
                        .overrides(GuiceableModule.guiceable(testModule));
        Guice.createInjector(builder.applicationModule()).injectMembers(this);

        Helpers.start(application);
    }

    @After
    public void teardown() {
        Helpers.stop(application);
    }


    @Test
    public void testIndex() throws Exception {
        // Arrange
//        FreelancerApiService service = mock(FreelancerApiService.class);
//        when(service.getProjects(ArgumentMatchers.anyList(), ArgumentMatchers.anyString())).thenReturn(ArgumentMatchers.any());
//
//        // Act
//        CompletionStage<Result> completionStage = new HomeController(service).index();
//        CompletableFuture<Result> future = completionStage.toCompletableFuture();
//        assertTrue(future.isDone());

//        Result result = future.get();
//
//        // Assert
//        assertTrue(future.isDone());
//        assertEquals(OK, result.status());
//        assertEquals("text/html", result.contentType().get());
//        assertEquals("utf-8", result.charset().get());
    }
}
