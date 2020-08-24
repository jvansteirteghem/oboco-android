package com.gitlab.jeeto.oboco.api;

import android.content.Context;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;

@RunWith(MockitoJUnitRunner.class)
public class ApiTest {
    private String baseUrl = "http://127.0.0.1:8080";
    private String name = "administrator";
    private String password = "administrator";

    @Mock
    private Context mockContext;
    private MockSharedPreferences mockSharedPreferences;
    private MockSharedPreferences.Editor mockSharedPreferencesEditor;

    @Before
    public void before() throws Exception {
        mockSharedPreferences = new MockSharedPreferences();
        mockSharedPreferencesEditor = mockSharedPreferences.edit();

        mockSharedPreferencesEditor.putString("baseUrl", baseUrl);
        mockSharedPreferencesEditor.putString("name", name);
        mockSharedPreferencesEditor.putString("idToken", null);
        mockSharedPreferencesEditor.putString("refreshToken", null);

        Mockito.when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences);
    }

    @Ignore
    @Test
    public void testGetAuthenticatedUser() {
        System.out.println("testGetAuthenticatedUser");

        try {
            AuthenticationManager authenticationManager = new AuthenticationManager(mockContext);

            authenticationManager.login(name, password).blockingAwait();

            ApplicationService applicationService = new ApplicationService(null, baseUrl, authenticationManager);

            UserDto user = applicationService.getAuthenticatedUser().blockingGet();

            System.out.println("user.id=" + user.getId());
            System.out.println("user.name=" + user.getName());
            System.out.println("user.password=" + user.getPassword());
            System.out.println("user.roles=" + user.getRoles());
            System.out.println("user.updateDate=" + user.getUpdateDate());
        } catch(Exception e) {
            if(e instanceof ProblemException) {
                ProblemException pe = (ProblemException) e;
                ProblemDto problem = pe.getProblem();

                System.out.println("problem.statusCode=" + problem.getStatusCode());
                System.out.println("problem.code=" + problem.getCode());
                System.out.println("problem.description=" + problem.getDescription());
            } else {
                e.printStackTrace();
            }
        }
    }

    @Ignore
    @Test
    public void testGetRootBookCollection() {
        System.out.println("testGetRootBookCollection");

        try {
            AuthenticationManager authenticationManager = new AuthenticationManager(mockContext);

            authenticationManager.login(name, password).blockingAwait();

            ApplicationService applicationService = new ApplicationService(null, baseUrl, authenticationManager);

            BookCollectionDto rootBookCollection = applicationService.getRootBookCollection("(bookCollections,books)").blockingGet();

            System.out.println("rootBookCollection.id=" + rootBookCollection.getId());
            System.out.println("rootBookCollection.updateDate=" + rootBookCollection.getUpdateDate());
            System.out.println("rootBookCollection.name=" + rootBookCollection.getName());

            List<BookCollectionDto> bookCollections = rootBookCollection.getBookCollections();

            for(BookCollectionDto bookCollection: bookCollections) {
                System.out.println("bookCollection.id=" + bookCollection.getId());
                System.out.println("bookCollection.updateDate=" + bookCollection.getUpdateDate());
                System.out.println("bookCollection.name=" + bookCollection.getName());
            }
        } catch(Exception e) {
            if(e instanceof ProblemException) {
                ProblemException pe = (ProblemException) e;
                ProblemDto problem = pe.getProblem();

                System.out.println("problem.statusCode=" + problem.getStatusCode());
                System.out.println("problem.code=" + problem.getCode());
                System.out.println("problem.description=" + problem.getDescription());
            } else {
                e.printStackTrace();
            }
        }
    }
}