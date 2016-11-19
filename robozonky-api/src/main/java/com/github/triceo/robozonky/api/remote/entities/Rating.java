/*
 * Copyright 2016 Lukáš Petrovický
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.triceo.robozonky.api.remote.entities;

public enum Rating {

    AAAAA("A**"),
    AAAA("A*"),
    AAA("A++"),
    AA("A+"),
    A("A"),
    B("B"),
    C("C"),
    D("D");


    private final String code;

    Rating(final String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
