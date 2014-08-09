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

package com.grizzly.rest.Exceptions;

/**
 * Created on 04-04-14. An exception to be throw when a return type is not found.
 */
public class GrizzlyNotFoundException extends Exception{

    public GrizzlyNotFoundException() { super("There wasn't any data matching your search"); }
    public GrizzlyNotFoundException(String message) { super(message); }
    public <T> GrizzlyNotFoundException(Class<T> causeClass) {

        super("There wasn't any data matching your search in the class "+causeClass.getCanonicalName());
    }
    public GrizzlyNotFoundException(String message, Throwable cause) { super(message, cause); }
    public GrizzlyNotFoundException(Throwable cause) { super(cause); }

}
