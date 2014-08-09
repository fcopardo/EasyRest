/*
 * Copyright (c) 2014. Francisco Pardo Baeza
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.grizzly.rest.Test;

import android.app.Activity;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/**
 * Base class for android testing. Provides a generic activity.
 *
 * @author
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(RobolectricTestRunner.class)
public abstract class BaseAndroidTestClass {

    protected Activity activity;
    protected static int count = 1;
    private static String myClass = BaseAndroidTestClass.class.getSimpleName();

    /**
     * Creates a plain activity to check services and any other non visible tasks. Also, prints some messages to make
     * log reading a bit easier.
     */
    @Before
    public void setup() {
        activity = Robolectric.buildActivity(Activity.class).create().get();
        org.robolectric.shadows.ShadowLog.stream = System.out;

        int charAmount = ("Starting tests in the class:"+this.getClass().getSimpleName()).length();
        count++;

        if(!this.getClass().getSimpleName().equalsIgnoreCase(myClass)) {

            count = 1;
            myClass = this.getClass().getSimpleName();

            System.out.println("");
            for(int c = 0; c<charAmount*2;c++){

                System.out.print("-");

            }

            System.out.print("\n");

            for(int c = 0; c<charAmount/2;c++){
                System.out.print(" ");
            }

            System.out.println("Starting tests in the class:".toUpperCase()+this.getClass().getSimpleName());

            for(int c = 0; c<charAmount*2;c++){

                System.out.print("-");

            }
            System.out.println("\n");
        }
        printTitle(charAmount);

    }

    /**
     * Nullifies the activity variable, clears all the Robolectric http mock responses, and prints some
     * closing messages.
     */
    @After
    public void printClose(){

        activity = null;
        Robolectric.clearPendingHttpResponses();

        int charAmount = ("Starting tests in the class:"+this.getClass().getSimpleName()).length();

        System.out.print("\n");
        for(int c = 0; c<charAmount/2;c++){

            System.out.print("-");

        }
        System.out.print("test case:"+this.getClass().getSimpleName()+" "+getCount()+" finished");
        for(int c = 0; c<charAmount/2;c++){

            System.out.print("-");

        }
        System.out.println("\n");
    }

    /**
     * provides access to the fake activity's context.
     * @return an android context.
     */
    protected android.content.Context getContext() {
        return activity.getApplicationContext();
    }

    /**
     * Prints a message.
     * @param charAmount
     */
    private void printTitle(int charAmount) {

        for(int c = 0; c<charAmount/2;c++){

            System.out.print("-");

        }
        System.out.print(this.getClass().getSimpleName()+"-"+getCount());
        for(int c = 0; c<charAmount/2;c++){

            System.out.print("-");

        }
        System.out.println("\n");
    }

    private int getCount(){
        return count;
    }

}
