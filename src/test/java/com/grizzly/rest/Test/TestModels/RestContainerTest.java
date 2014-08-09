package com.grizzly.rest.Test.TestModels;

import com.grizzly.rest.Model.RestContainer;
import com.grizzly.rest.Test.BaseAndroidTestClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.springframework.http.HttpMethod;

/**
 * Created by fpardo on 8/7/14.
 */
@RunWith(RobolectricTestRunner.class)
public class RestContainerTest extends BaseAndroidTestClass {

    @Test
    public void testMethod() {

        RestContainer restContainer = new RestContainer();
        restContainer.setMyHttpMethod(HttpMethod.POST);

        System.out.println("Method:"+ restContainer.getMyHttpMethod().toString());

    }

}
