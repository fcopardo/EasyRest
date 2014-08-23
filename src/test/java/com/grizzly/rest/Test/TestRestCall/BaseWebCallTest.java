package com.grizzly.rest.Test.TestRestCall;

import com.grizzly.rest.Definitions.DefinitionsHttpMethods;
import com.grizzly.rest.EasyRestCall;
import com.grizzly.rest.GenericRestCall;
import com.grizzly.rest.Model.BaseWebCall;
import com.grizzly.rest.Test.BaseAndroidTestClass;
import com.grizzly.rest.Test.TestEntities.TestString;
import com.grizzly.rest.WebServiceFactory;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.springframework.http.HttpStatus;


import java.util.concurrent.ExecutionException;

/**
 * Created by fpardo on 8/7/14.
 */
@RunWith(RobolectricTestRunner.class)
public class BaseWebCallTest extends BaseAndroidTestClass {


    @Test
    public void restTest(){

        WebServiceFactory webFactory = new WebServiceFactory();

        EasyRestCall<BaseWebCall, TestString> restCall = webFactory.getRestCallInstance(BaseWebCall.class, TestString.class, true);

        BaseWebCall webCall = new BaseWebCall();

        webCall.getRestContainer().setRequestUrl("www.google.cl");
        webCall.getRestContainer().setMyHttpMethod(DefinitionsHttpMethods.METHOD_POST);

        Header headers = new BasicHeader("Content-type", "application/json");

        Robolectric.addPendingHttpResponse(HttpStatus.OK.value(), "{\n" +
                "    \"my_value\": \"asdf\"\n" +
                "}", headers);

        restCall.setEntity(webCall);

        try {

            System.out.println("Rest test: BaseWebCall");
            org.junit.Assert.assertTrue(restCall.execute().get());

            System.out.println("\nMy test string is:"+restCall.getJsonResponseEntity().getMyValue());

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void genericRestTest(){

        WebServiceFactory webFactory = new WebServiceFactory();

        GenericRestCall<String, TestString> restCall = webFactory.getGenericRestCallInstance(String.class, TestString.class, true);

        String webCall = "a web call with no configuration";
        Header headers = new BasicHeader("Content-type", "application/json");

        Robolectric.addPendingHttpResponse(HttpStatus.OK.value(), "{\n" +
                "    \"my_value\": \"asdf\"\n" +
                "}", headers);

        restCall.setEntity(webCall);

        restCall.setUrl("www.google.cl");
        restCall.setMethodToCall(DefinitionsHttpMethods.METHOD_POST);

        try {

            System.out.println("Rest test: BaseWebCall");
            org.junit.Assert.assertTrue(restCall.execute().get());

            System.out.println("\nMy test string is:"+restCall.getJsonResponseEntity().getMyValue());

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void UrlParamsTest(){

        WebServiceFactory webFactory = new WebServiceFactory();

        EasyRestCall<BaseWebCall, TestString> restCall = webFactory.getRestCallInstance(BaseWebCall.class, TestString.class, true);

        BaseWebCall webCall = new BaseWebCall();

        webCall.getRestContainer().setRequestUrl("www.google.cl");
        webCall.getRestContainer().setMyHttpMethod(DefinitionsHttpMethods.METHOD_POST);
        webCall.getRestContainer().addParameterToUrl("q", "cheese is bad");
        webCall.getRestContainer().addParameterToUrl("ie", "UTF-8");

        Header headers = new BasicHeader("Content-type", "application/json");

        Robolectric.addPendingHttpResponse(HttpStatus.OK.value(), "{\n" +
                "    \"my_value\": \"asdf\"\n" +
                "}", headers);

        restCall.setEntity(webCall);

        try {

            System.out.println("Rest test: BaseWebCall");
            org.junit.Assert.assertTrue(restCall.execute().get());

            System.out.println("\nMy test string is:"+restCall.getJsonResponseEntity().getMyValue());
            System.out.println("\nMy url params are:"+webCall.getRestContainer().getRequestUrl());

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void InheritClassTest(){

        WebServiceFactory webFactory = new WebServiceFactory();

        EasyRestCall<BaseWebCall, TestString> restCall = new EasyRestCall<>(BaseWebCall.class, TestString.class, 1);

        BaseWebCall webCall = new BaseWebCall();

        webCall.getRestContainer().setRequestUrl("www.google.cl");
        webCall.getRestContainer().setMyHttpMethod(DefinitionsHttpMethods.METHOD_POST);
        webCall.getRestContainer().addParameterToUrl("q", "cheese is bad");
        webCall.getRestContainer().addParameterToUrl("ie", "UTF-8");

        Header headers = new BasicHeader("Content-type", "application/json");

        Robolectric.addPendingHttpResponse(HttpStatus.OK.value(), "{\n" +
                "    \"my_value\": \"asdf\"\n" +
                "}", headers);

        restCall.setEntity(webCall);

        try {

            System.out.println("Rest test: EasyRestCall2");
            org.junit.Assert.assertTrue(restCall.execute().get());

            System.out.println("\nMy test string is:"+restCall.getJsonResponseEntity().getMyValue());
            System.out.println("\nMy url params are:"+webCall.getRestContainer().getRequestUrl());

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

}
